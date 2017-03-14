package com.omega.framework.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omega.framework.task.bean.Task;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

/**
 * Created by jackychenb on 12/12/2016.
 */

@Component
public class TaskConsumerInvoker {

    private static final Logger logger = LoggerFactory.getLogger(TaskConsumerInvoker.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CuratorFramework curatorFramework;

    @Value("${task.tableName:Task}")
    private String taskTableName;

    @Value("${task.lockPath:/task}")
    private String lockPath = "/task";

    private String getLockName(String taskId) {
        return lockPath + "/" + taskId;
    }

    private boolean lock(String taskId) {
        String lockName = getLockName(taskId);

        try {
            curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath(lockName);
        } catch (KeeperException e) {
            if (KeeperException.Code.NODEEXISTS.equals(e.code())) {
                logger.warn("There is already a task with id " + taskId + " being executing");
            } else {
                logger.error("Failed to create zookeeper node " + lockName, e);
            }

            return false;
        } catch (Exception e) {
            logger.error("Failed to create zookeeper node " + lockName, e);
            return false;
        }

        return true;
    }

    private void unlock(String taskId) {
        String lockName = getLockName(taskId);

        try {
            curatorFramework.delete().forPath(lockName);
        } catch (Exception e) {
            logger.error("Failed to delete zookeeper node: " + lockName, e);
        }
    }

    public void invoke(Message message, Object bean, Method method) {
        Task task;
        try {
            String taskString = new String(message.getBody(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            task = mapper.readValue(taskString, Task.class);
        } catch (Exception e) {
            logger.error("failed to read message: " + message.toString(), e);
            return;
        }

        String taskId = task.getId();
        if (!lock(taskId)) {
            return;
        }

        try {
            try {
                method.invoke(bean, task);
            } catch (Exception e) {
                logger.error("failed to execute task: " + taskId, e);
                return;
            }

            try {
                jdbcTemplate.update("delete from " + taskTableName + " where id=?",
                        new Object[]{ taskId });
            } catch (Exception e) {
                logger.error("failed to delete task: " + taskId, e);
                return;
            }
        } finally {
            unlock(taskId);
        }
    }

}
