package com.omega.framework.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omega.framework.index.bean.IndexCommand;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.*;

/**
 * Created by jackychenb on 11/12/2016.
 */

@Configuration
public class IndexScheduler {

    private static final Logger logger = LoggerFactory.getLogger(IndexScheduler.class);

    @Value("${elasticsearch.index.scheduler.loadBatchSize:100}")
    private int loadBatchSize = 100; // 每次查任务表取多少条任务

    @Value("${elasticsearch.index.scheduler.loadTimes:10}")
    private int loadTimes = 10; // 预估的每轮调度查多少次任务表，运行过程会根据负载情况自动调整

    @Value("${elasticsearch.index.duration:5000}")
    private long indexDuration = 5000L; // 预估的最长的索引任务执行时间，单位毫秒

    @Autowired
    private JdbcTemplate jdbcTemplate; // TODO: 分库情况下需要遍历所有的数据库中的IndexCommand表

    @Value("${elasticsearch.index.commandTableName:IndexCommand}")
    private String commandTableName = "IndexCommand";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${elasticsearch.index.scheduler.exchangeName:index}")
    private String exchangeName = "index";

    @Autowired
    private RabbitAdmin rabbitAdmin;

    private final Set<String> queueNameSet = new HashSet<String>();

    private Map<Integer, Long> lastRunSNMap = Collections.EMPTY_MAP; // 上一轮调度的任务SN范围
    private int busyCount = 0;
    private int idleCount = 0;

    @Scheduled(fixedDelayString="${elasticsearch.index.scheduler.loadInterval:10000")
    public void schedule() throws Exception {
        Date endTime = new Date(System.currentTimeMillis() - indexDuration);
        Map<Integer, Long> snMap = new HashMap<Integer, Long>();
        boolean overlapped = false;
        boolean full = false;
        for (int i = 0; i < loadTimes; i++) {
            List<Map<String, Object>> cmdList = jdbcTemplate.queryForList(
                    "select * from " + commandTableName +
                    " where createTime<? " +
                    " order by priority, sn " +
                    " limit ?, ?", new Object[]{ endTime, loadBatchSize * i, loadBatchSize });

            ObjectMapper mapper = new ObjectMapper();
            for (Map<String, Object> m : cmdList) {
                IndexCommand c = new IndexCommand();
                c.setId((String) m.get("id"));
                c.setType((String) m.get("type"));
                c.setIndexName((String) m.get("indexName"));
                c.setOp(Integer.parseInt(m.get("op").toString()));

                String dataMapString = (String) m.get("dataMap");
                if (StringUtils.isNotBlank(dataMapString)) {
                    try {
                        c.setDataMap(mapper.readValue(dataMapString, HashMap.class));
                    } catch (IOException e) {
                        logger.error("failed to read data map of task " + c.getId(), e);
                        continue;
                    }
                }

                dispatchCommand(c);

                Integer priority = Integer.valueOf(m.get("priority").toString());
                Long sn = Long.valueOf(m.get("sn").toString());

                Long lastSN = lastRunSNMap.get(priority);
                if (lastSN != null && lastSN.compareTo(sn) >= 0) { // 说明上一轮调度过的任务还没执行完
                    overlapped = true;
                }

                snMap.put(priority, sn);
            }

            // 查到最后一页了
            if (cmdList.size() < loadBatchSize) {
                break;
            }

            // 最后一次还是查到整页记录
            if (i == (loadTimes - 1)) {
                full = true;
            }
        }

        if (overlapped) {
            busyCount++;
            idleCount = 0;
            if (busyCount > 1) {
                loadTimes /= 2;
            } else {
                loadTimes--;
            }
        } else if (full) {
            idleCount++;
            busyCount = 0;
            if (idleCount > 1) {
                loadTimes *= 2;
            } else {
                loadTimes++;
            }
        } else if (loadTimes == 0) {
            busyCount--;
            if (busyCount == 0) {
                loadTimes = 1;
            }
        }

        lastRunSNMap = snMap;
    }

    private void dispatchCommand(IndexCommand cmd) throws Exception {
        String type = cmd.getType();
        if (!queueNameSet.contains(type)) {
            synchronized (rabbitAdmin) {
                Queue queue = new Queue(type);
                DirectExchange exchange = new DirectExchange(exchangeName);
                Binding binding = BindingBuilder.bind(queue).to(exchange).with(type);
                rabbitAdmin.declareQueue(queue);
                rabbitAdmin.declareExchange(exchange);
                rabbitAdmin.declareBinding(binding);

                queueNameSet.add(type);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        String taskString = mapper.writeValueAsString(cmd);
        rabbitTemplate.convertAndSend(exchangeName, type, taskString);
    }

}
