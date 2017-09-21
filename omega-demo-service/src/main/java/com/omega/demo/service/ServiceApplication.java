package com.omega.demo.service;

import com.omega.framework.EnableOmegaServiceFramework;
import com.omega.framework.cache.EnableCacheClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Hello world!
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableCacheClient
@EnableOmegaServiceFramework
@ComponentScan({"com.omega.demo.service"})
public class ServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }
}
