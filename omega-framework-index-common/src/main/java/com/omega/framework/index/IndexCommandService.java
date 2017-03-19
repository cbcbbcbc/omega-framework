package com.omega.framework.index;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omega.framework.index.bean.GUID;
import com.omega.framework.index.bean.IndexCommand;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by jackychenb on 11/12/2016.
 */

@Component
public class IndexCommandService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private IndexWorkerRegistry indexWorkerRegistry;

    @Autowired
    private IndexWorkerInvoker indexWorkerInvoker;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${elasticsearch.index.commandTableName:IndexCommand}")
    private String commandTableName = "IndexCommand";

    @Value("${elasticsearch.refresh.duration:500}")
    private long refreshDuration = 500L; // 预估的最长的refresh调用耗时

    @Autowired
    private CuratorFramework curatorFramework;

    @Autowired
    private TransportClient elasticsearchClient;

    @Value("${elasticsearch.refresh.lockPath:/index/refresh}")
    private String lockPath = "/index/refresh";

    @Autowired
    private IndexWorkerHelper indexWorkerHelper;

    public String exec(final IndexCommand cmd) {
        checkCommand(cmd);

        String dataMapString = null;
        Map<String, String> dataMap = cmd.getDataMap();
        if (dataMap != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                dataMapString = mapper.writeValueAsString(dataMap);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        jdbcTemplate.update("insert into " + commandTableName +
                "(id, type, indexName, op, dataMap) values" +
                "(?, ?, ?, ?, ?)", new Object[] {
                cmd.getId(), cmd.getType(), cmd.getIndexName(), cmd.getOp(), dataMapString
        });

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronizationAdapter() {
                        public void afterCommit() {
                            invokeCommand(cmd);
                        }
                    });
        } else {
            invokeCommand(cmd);
        }

        return cmd.getId();
    }

    private void invokeCommand(IndexCommand cmd) {
        IndexWorkerRegistry.InvocationTarget invocationTarget = indexWorkerRegistry.getInvocationTarget(cmd.getType());
        indexWorkerInvoker.invoke(cmd, invocationTarget, true);
    }

    protected void checkCommand(IndexCommand cmd) {
        if (StringUtils.isNotBlank(cmd.getId())) {
            throw new IllegalArgumentException("The command id is to be generated by system");
        }

        String zoneCode = jdbcTemplate.queryForObject("/** mycat:tag=current_data_node*/ select getTaskZoneCode() from dual", String.class);
        cmd.setId(GUID.get() + zoneCode);

        String type = cmd.getType();
        if (type == null || !type.matches("[a-zA-Z_]+\\w*")) {
            throw new IllegalArgumentException("Illegal command type: " + type);
        }

        String indexName = cmd.getIndexName();
        if (indexName == null || !indexName.matches("[\\w\\-]+")) {
            throw new IllegalArgumentException("Illegal index name: " + indexName);
        }

        int op = cmd.getOp();
        if (IndexCommand.OP_ADD != op && IndexCommand.OP_DELETE != op) {
            throw new IllegalArgumentException("Illegal index operation: " + op);
        }
    }

    private String getRefreshCacheKey(String indexName) {
        return indexName + "/refresh";
    }

    private String getLockName(String indexName) {
        return lockPath + "/" + indexName;
    }

    public void ensureRefresh(String indexName) {
        Long indexTime = indexWorkerHelper.getLastCommandFinishTime(indexName);
        if (indexTime == null) {
            return;
        }

        ensureRefresh(indexName, indexTime);
    }

    public void ensureRefresh(String indexName, String indexCommandId) {
        Long indexTime = indexWorkerHelper.getCommandFinishTime(indexCommandId);
        if (indexTime == null) {
            return;
        }

        ensureRefresh(indexName, indexTime);
    }

    private void ensureRefresh(String indexName, Long indexTime) {
        String key = getRefreshCacheKey(indexName);
        Long refreshTime = (Long) redisTemplate.opsForValue().get(key);
        if (refreshTime != null && indexTime.compareTo(refreshTime) < 0) {
            return;
        }

        String lockName = getLockName(indexName);
        InterProcessMutex lock = new InterProcessMutex(curatorFramework, lockName);

        try {
            if (lock.acquire(refreshDuration, TimeUnit.MILLISECONDS)) {
                // 获得锁后再次检查索引库更新时间
                Long refreshTime2 = (Long) redisTemplate.opsForValue().get(key);
                if (refreshTime2 != null && indexTime.compareTo(refreshTime2) < 0) {
                    return;
                }

                // 时间戳一定要在调用ES刷新接口前获得
                long now = System.currentTimeMillis();
                elasticsearchClient.admin().indices().prepareRefresh(indexName).get();
                redisTemplate.opsForValue().set(key, now);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                lock.release();
            } catch (Exception e) {
                // ignored
            }
        }
    }

}
