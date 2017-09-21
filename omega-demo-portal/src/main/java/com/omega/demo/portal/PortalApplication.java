package com.omega.demo.portal;

import com.omega.framework.EnableOmegaServiceFramework;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


/**
 * Hello world!
 */


@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableOmegaServiceFramework
@EnableFeignClients({"com.omega.demo.api"})
@ComponentScan({"com.omega.demo.api"})
@ComponentScan({"com.omega.demo.portal"})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
