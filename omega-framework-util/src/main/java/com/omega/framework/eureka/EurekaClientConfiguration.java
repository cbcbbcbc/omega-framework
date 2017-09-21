package com.omega.framework.eureka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EurekaClientConfiguration {

    @Bean
    public EurekaClientBeanPostProcessor eurekaClientBeanPostProcessor() {
        return new EurekaClientBeanPostProcessor();
    }

}
