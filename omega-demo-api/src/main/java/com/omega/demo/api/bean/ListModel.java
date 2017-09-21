package com.omega.demo.api.bean;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by jackychenb on 17/03/2017.
 */
public class ListModel implements Serializable {

    private static final long serialVersionUID = 8582733790767029728L;

    private static final Pattern PATTERN_ORDER_BY = Pattern.compile("[a-zA-z_]+[\\w\\.]*");

    /**
     * 每页显示记录数
     */
    private int pageSize;

    //--------- 输入参数 ----------

    /**
     * 第几页，从1开始
     */
    private int page = 1;

    /**
     * 排序字段名称
     */
    private String orderBy;

    /**
     * 是否降序
     */
    private boolean desc;

    /**
     * 实时搜索，确保该索引任务已经完成并且可搜索
     */
    private String lastIndexCommandId;

    //--------- 输出参数 ----------

    /**
     * 记录总数
     */
    private int total;

    /**
     * 当前页结果集
     */
    private List listData = Collections.EMPTY_LIST;


    public ListModel(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isDesc() {
        return desc;
    }

    public void setDesc(boolean desc) {
        this.desc = desc;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(String orderBy) {
        if (StringUtils.isNotBlank(orderBy) && !PATTERN_ORDER_BY.matcher(orderBy).matches()) {
            throw new IllegalArgumentException();
        }

        this.orderBy = orderBy;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        if (page < 1) {
            page = 1;
        }

        if (page > 100) {
            page = 100;
        }

        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("pageSize should be larger than ZERO");
        }

        if (pageSize > 100) {
            throw new IllegalArgumentException("pageSize should be less than 100");
        }

        this.pageSize = pageSize;
    }

    public String getLastIndexCommandId() {
        return lastIndexCommandId;
    }

    public void setLastIndexCommandId(String lastIndexCommandId) {
        this.lastIndexCommandId = lastIndexCommandId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;

        // 调整page的实际位置
        int pageCount = getPageCount();
        if (page > pageCount) {
            page = pageCount;
        }

        if (page < 1) {
            page = 1;
        }
    }

    public List getListData() {
        return listData;
    }

    public void setListData(List listData) {
        this.listData = listData;
    }

    //--------- 计算属性 ----------

    /**
     * 分页查询的起始记录
     */
    public int getStart() {
        int pg = page < 1 ? 1 : page;
        return pageSize * (pg - 1);
    }

    /**
     * 排序类型
     */
    public String getOrderType() {
        return desc ? "desc" : "asc";
    }

    /**
     * 总页数
     */
    public int getPageCount() {
        return (total + pageSize - 1) / pageSize;
    }

}
