package com.omega.framework.task;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by jackychenb on 12/12/2016.
 */

@Configuration
public class TaskConsumerConfiguration {

    @Value("${zookeeper.servers}")
    private String zooKeeperServers;

    @Bean
    public TaskConsumerAnnotationBeanPostProcessor taskConsumerAnnotationBeanPostProcessor(
            TaskConsumerRegistry taskConsumerRegistry) {

        return new TaskConsumerAnnotationBeanPostProcessor(taskConsumerRegistry);
    }

    @Bean
    public TaskConsumerRegistry taskConsumerRegistry(ConnectionFactory connectionFactory,
                                                     TaskConsumerInvoker taskConsumerInvoker) {

        return new TaskConsumerRegistry(connectionFactory, taskConsumerInvoker);
    }

    @Bean(initMethod="start", destroyMethod="close")
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        return CuratorFrameworkFactory.newClient(zooKeeperServers, retryPolicy);
    }

}
