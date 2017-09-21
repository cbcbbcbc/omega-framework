package com.omega.demo.service.domain;

import com.omega.demo.api.bean.ISendEmailTask;
import com.omega.demo.api.bean.OrderDetail;
import com.omega.demo.api.bean.OrderForm;
import com.omega.demo.api.bean.OrderListModel;
import com.omega.demo.service.dao.OrderDao;
import com.omega.framework.index.IndexCommandService;
import com.omega.framework.index.IndexWorker;
import com.omega.framework.index.bean.IndexCommand;
import com.omega.framework.task.TaskQueue;
import com.omega.framework.task.bean.Task;
import com.omega.framework.cache.ICacheClient;
import javafx.util.Pair;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.filter.Filter;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.sum.Sum;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jackychenb on 25/12/2016.
 */

@Service
public class OrderEntity {

    private static final String INDEX_COMMAND_TYPE = "orderform";
    private static final String INDEX_DATA_ID = "id";

    // 如果按照业务规则不同的订单要存储到不同的索引库，则不应使用此常量定义
    private static final String DEFAULT_INDEX_NAME = "orderform";

    @Autowired
    private OrderDao dao;

    @Autowired
    private TaskQueue taskQueue;

    @Autowired
    private IndexCommandService indexCommandService;

    @Autowired
    private TransportClient elasticsearchClient;

    @Autowired
    private ICacheClient cacheClient;

    /**
     * @return 索引任务ID，用于后续实时搜索
     */
    @Transactional
    public String createOrder(OrderForm o) {
        dao.createOrderForm(o);

        for (OrderDetail d : o.getDetailList()) {
            dao.createOrderDetail(d);
        }

        Task task = new Task(ISendEmailTask.TASK_TYPE);
        task.data(ISendEmailTask.DATA_USER_ID, o.getUserId());
        task.data(ISendEmailTask.DATA_EVENT, "Order Placed");
        taskQueue.addTask(task);

        return updateIndex(o.getId());
    }

    private String updateIndex(String orderId) {
        IndexCommand cmd = new IndexCommand(INDEX_COMMAND_TYPE, DEFAULT_INDEX_NAME, IndexCommand.OP_ADD);
        cmd.data(INDEX_DATA_ID, orderId);
        return indexCommandService.exec(cmd);
    }

    private String removeIndex(String orderId) {
        IndexCommand cmd = new IndexCommand(INDEX_COMMAND_TYPE, DEFAULT_INDEX_NAME, IndexCommand.OP_DELETE);
        cmd.data(INDEX_DATA_ID, orderId);
        return indexCommandService.exec(cmd);
    }

    @IndexWorker(INDEX_COMMAND_TYPE)
    public void index(IndexCommand cmd) {
        String id = cmd.data(INDEX_DATA_ID);
        if (IndexCommand.OP_DELETE == cmd.getOp()) {
            // 注意用cmd.getIndexName()而不是写死DEFAULT_INDEX_NAME。
            // 当重建索引数据的时候，管理脚本会新建一个索引库，然后向消息队列批量灌入IndexCommand，
            // IndexCommand中的indexName指向新索引库。待完成索引过程后，再将索引别名指向新的索引库。
            elasticsearchClient.prepareDelete(cmd.getIndexName(), cmd.getIndexName(), id).get();
        } else {
            OrderForm o = getOrderById(id);

            Map<String, Object> m = new HashMap<String, Object>();
            m.put("userId", o.getUserId());
            m.put("number", o.getNumber());
            m.put("amount", o.getAmount().multiply(new BigDecimal(100)).intValue());
            m.put("gmtCreated", o.getGmtCreated());

            List<String> itemNoList = new ArrayList<String>();
            List<String> itemNameList = new ArrayList<String>();
            for (OrderDetail detail : o.getDetailList()) {
                itemNoList.add(detail.getItemNo());
                itemNameList.add(detail.getItemName());
            }

            m.put("itemNo", itemNoList);
            m.put("itemName", itemNameList);

            // 注意用cmd.getIndexName()而不是写死DEFAULT_INDEX_NAME。
            // 当重建索引数据的时候，管理脚本会新建一个索引库，然后向消息队列批量灌入IndexCommand，
            // IndexCommand中的indexName指向新索引库。待完成索引过程后，再将索引别名指向新的索引库。
            elasticsearchClient.prepareIndex(cmd.getIndexName(), cmd.getIndexName(), id)
                    .setSource(m).get();
        }
    }

    private String getCacheKey(String id) {
        return "order" + id;
    }

    public OrderForm getOrderById(String id) {
        String key = getCacheKey(id);
        OrderForm o = (OrderForm) cacheClient.get(key);
        if (o != null) {
            return o;
        }

        o = dao.getOrderFormById(id);
        if (o == null) {
            return null;
        }

        o.setDetailList(dao.getOrderDetailListByOrderFormId(id));

        cacheClient.set(key, o);
        return o;
    }

    public List<OrderForm> getOrderFormListByUserId(String userId) {
        return dao.getOrderFormListByUserId(userId);
    }

    public OrderListModel search(OrderListModel listModel) {
        if (StringUtils.isNotBlank(listModel.getLastIndexCommandId())) {
            indexCommandService.ensureRefresh(DEFAULT_INDEX_NAME, listModel.getLastIndexCommandId());
        }

        // 不影响评分的精确匹配条件用Filter
        BoolQueryBuilder filter = QueryBuilders.boolQuery();

        if (StringUtils.isNotBlank(listModel.getUserId())) {
            filter.must(QueryBuilders.termQuery("userId", listModel.getUserId()));
        }

        if (StringUtils.isNotBlank(listModel.getNumber())) {
            filter.must(QueryBuilders.termQuery("number", listModel.getNumber()));
        }

        if (StringUtils.isNotBlank(listModel.getItemNo())) {
            filter.must(QueryBuilders.termQuery("itemNo", listModel.getItemNo()));
        }

        // 典型场景：左边显示一个日期列表，右边显示某一个日期的订单列表，同时显示总金额。

        // 先按日期聚合统计，注意此时不要加入日期过滤条件，也不需要排序、计算评分（因此用constant_score）
        SearchResponse aggsRsp = elasticsearchClient.prepareSearch(DEFAULT_INDEX_NAME).setTypes(DEFAULT_INDEX_NAME)
                .addAggregation(AggregationBuilders.filter("filtered_orders", filter).subAggregation(
                        AggregationBuilders.dateHistogram("gmtCreated").field("gmtCreated")
                                .dateHistogramInterval(DateHistogramInterval.DAY)
                                .format("yyyy-MM-dd")
                )).get();

        Histogram his = (Histogram) ((Filter) aggsRsp.getAggregations().get("filtered_orders"))
                .getAggregations().get("gmtCreated");

        List<Pair<String, Long>> gmtCreatedList = new ArrayList<Pair<String, Long>>();
        for (Histogram.Bucket bucket : his.getBuckets()) {
            String date = bucket.getKeyAsString();
            long count = bucket.getDocCount();
            gmtCreatedList.add(new Pair<>(date, count));
        }

        listModel.setGmtCreatedList(gmtCreatedList);

        // 查询当前日期范围的订单，并聚合统计总金额
        SearchRequestBuilder b = elasticsearchClient.prepareSearch(DEFAULT_INDEX_NAME).setTypes(DEFAULT_INDEX_NAME);

        if (listModel.getGmtCreatedStart() != null || listModel.getGmtCreatedEnd() != null) {
            RangeQueryBuilder rb = QueryBuilders.rangeQuery("gmtCreated");
            if (listModel.getGmtCreatedStart() != null) {
                rb.gte(listModel.getGmtCreatedStart());
            }
            if (listModel.getGmtCreatedEnd() != null) {
                rb.lt(listModel.getGmtCreatedEnd());
            }

            filter.must(rb);
        }

        // 如果用户指定了排序字段，则无需评分，用constant_score提高查询性能
        QueryBuilder query;
        if (StringUtils.isNotBlank(listModel.getOrderBy()) || StringUtils.isBlank(listModel.getItemName())) {
            if (StringUtils.isNotBlank(listModel.getItemName())) {
                filter.must(QueryBuilders.matchQuery("itemName", listModel.getItemName()));
            }

            query = QueryBuilders.constantScoreQuery(filter);

            if (StringUtils.isNotBlank(listModel.getOrderBy())) {
                b.addSort(listModel.getOrderBy(), listModel.isDesc() ? SortOrder.DESC : SortOrder.ASC);
            }
        } else {
            // 实际上这种业务场景较少用关键词匹配度排序，这里只是为了演示
            BoolQueryBuilder bq = QueryBuilders.boolQuery();
            bq.must(QueryBuilders.matchQuery("itemName", listModel.getItemName()));
            bq.filter(filter);
            query = bq;
        }

        SearchResponse sr = b.setQuery(query)
                .addAggregation(AggregationBuilders.sum("amount").field("amount"))
                .setFrom(listModel.getStart()).setSize(listModel.getPageSize()).get();

        SearchHits hits = sr.getHits();
        List<OrderForm> orderList = new ArrayList<OrderForm>();
        for (SearchHit hit : hits) {
            OrderForm order = getOrderById(hit.getId());
            if (order != null) {
                orderList.add(order);
            }
        }

        Sum sum = (Sum) sr.getAggregations().get("amount");

        listModel.setListData(orderList);
        listModel.setTotal((int) hits.getTotalHits());
        listModel.setSum(new BigDecimal(sum.getValue() / 100).setScale(2, BigDecimal.ROUND_HALF_UP));
        return listModel;
    }

}
