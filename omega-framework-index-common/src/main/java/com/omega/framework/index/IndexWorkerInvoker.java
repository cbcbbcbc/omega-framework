package com.omega.framework.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omega.framework.index.bean.IndexCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Created by jackychenb on 12/12/2016.
 */

@Component
public class IndexWorkerInvoker {

    private static final Logger logger = LoggerFactory.getLogger(IndexWorkerInvoker.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${elasticsearch.index.commandTableName:IndexCommand}")
    private String commandTableName;

    public void invoke(Message message, IndexWorkerRegistry.InvocationTarget invocationTarget) {
        IndexCommand cmd;
        try {
            String cmdString = new String(message.getBody(), "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            cmd = mapper.readValue(cmdString, IndexCommand.class);
        } catch (Exception e) {
            logger.error("failed to read message: " + message.toString(), e);
            return;
        }

        try {
            invoke(cmd, invocationTarget);
        } catch (Exception e) {
            logger.error("failed to execute command: " + cmd.getId(), e);
            return;
        }
    }

    public Long invoke(IndexCommand cmd, IndexWorkerRegistry.InvocationTarget invocationTarget) throws Exception {
        invocationTarget.getMethod().invoke(invocationTarget.getBean(), cmd);
        Long indexTime = System.currentTimeMillis();
        jdbcTemplate.update("delete from " + commandTableName + " where id=?", new Object[]{ cmd.getId() });
        return indexTime;
    }

}
