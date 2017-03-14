package com.omega.framework.index;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by jackychenb on 08/12/2016.
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class IndexSchedulerApplication {
    public static void main(String[] args) {
        SpringApplication.run(IndexSchedulerApplication.class, args);
    }
}
