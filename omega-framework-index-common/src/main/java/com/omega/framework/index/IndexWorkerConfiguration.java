package com.omega.framework.index;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * Created by jackychenb on 12/12/2016.
 */

@Configuration
public class IndexWorkerConfiguration {

    @Value("${zookeeper.servers}")
    private String zooKeeperServers;

    @Value("${elasticsearch.clusterName}")
    private String elasticsearchClusterName;

    @Value("${elasticsearch.endPoints}")
    private String elasticsearchEndPoints;

    @Bean
    public IndexWorkerAnnotationBeanPostProcessor indexWorkerAnnotationBeanPostProcessor(
            IndexWorkerRegistry indexWorkerRegistry) {

        return new IndexWorkerAnnotationBeanPostProcessor(indexWorkerRegistry);
    }

    @Bean
    public IndexWorkerRegistry indexWorkerRegistry(ConnectionFactory connectionFactory,
                                                    IndexWorkerInvoker indexWorkerInvoker) {

        return new IndexWorkerRegistry(connectionFactory, indexWorkerInvoker);
    }

    @Bean(initMethod="start", destroyMethod="close")
    public CuratorFramework curatorFramework() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        return CuratorFrameworkFactory.newClient(zooKeeperServers, retryPolicy);
    }

    @Bean(destroyMethod="close")
    public TransportClient elasticsearchClient() throws Exception {
        Settings settings = Settings.builder()
                .put("cluster.name", elasticsearchClusterName)
                .put("client.transport.sniff", true)
                .build();

        TransportClient client = new PreBuiltTransportClient(settings);

        String[] pairs = elasticsearchEndPoints.split("\\s*,\\s*");
        for (String p : pairs) {
            String[] kv = p.split("\\s*:\\s*");
            client.addTransportAddress(new InetSocketTransportAddress(
                    InetAddress.getByName(kv[0]), Integer.parseInt(kv[1])));
        }

        return client;
    }

}
