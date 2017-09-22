package com.omega.framework.eureka;

import com.omega.framework.OmegaServiceContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class EurekaClientBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        String grayTag = environment.getProperty(OmegaServiceContext.GRAY_TAG_ENV_NAME);
        if (grayTag != null && bean instanceof EurekaInstanceConfigBean) {
            OmegaServiceContext.setGrayTag(grayTag);

            EurekaInstanceConfigBean instanceConfig = (EurekaInstanceConfigBean) bean;
            String appName = instanceConfig.getAppname();
            String grayAppName = appName + "--" + grayTag;
            instanceConfig.setAppname(grayAppName);
            instanceConfig.setVirtualHostName(grayAppName);
            instanceConfig.setSecureVirtualHostName(grayAppName);
        }

        return bean;
    }

}
