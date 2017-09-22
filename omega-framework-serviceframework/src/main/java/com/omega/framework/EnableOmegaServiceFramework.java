package com.omega.framework;

import com.omega.framework.eureka.EurekaClientConfiguration;
import com.omega.framework.feign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by jackychenb on 17/03/2017.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ EurekaClientConfiguration.class, FeignClientsConfiguration.class })
public @interface EnableOmegaServiceFramework {
}
