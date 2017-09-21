package com.omega.framework.feign;

import feign.RequestInterceptor;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientsConfiguration {

    @Bean
    public RequestInterceptor headerInterceptor() {
        return new FeignClientRequestInterceptor();
    }

    @Bean
    public FeignClientBeanPostProcessor feignClientBeanPostProcessor(
            SpringClientFactory clientFactory,
            LoadBalancedRetryPolicyFactory retryPolicyFactory) {

        return new FeignClientBeanPostProcessor(clientFactory, retryPolicyFactory);
    }

}
