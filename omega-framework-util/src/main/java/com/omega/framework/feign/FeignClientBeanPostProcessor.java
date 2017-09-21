package com.omega.framework.feign;

import com.netflix.loadbalancer.ILoadBalancer;
import com.omega.framework.OmegaServiceContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryPolicyFactory;
import org.springframework.cloud.netflix.feign.ribbon.CachingSpringLoadBalancerFactory;
import org.springframework.cloud.netflix.feign.ribbon.FeignLoadBalancer;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class FeignClientBeanPostProcessor implements BeanPostProcessor {

    private final SpringClientFactory clientFactory;
    private final LoadBalancedRetryPolicyFactory retryPolicyFactory;

    public FeignClientBeanPostProcessor(SpringClientFactory clientFactory,
                                        LoadBalancedRetryPolicyFactory retryPolicyFactory) {

        this.clientFactory = clientFactory;
        this.retryPolicyFactory = retryPolicyFactory;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof CachingSpringLoadBalancerFactory) {
            // IMPORTANT: 针对不同的Spring Cloud，CachingSpringLoadBalancerFactory的构造函数有不同版本，如果升级Spring Cloud，此处代码需要做相应修改
            bean = new CachingSpringLoadBalancerFactory(clientFactory) {
                public FeignLoadBalancer create(String clientName) {
                    ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                    HttpServletRequest leadRequest = attrs.getRequest();

                    String grayTag = null;
                    Cookie[] cookies = leadRequest.getCookies();
                    if (cookies != null) {
                        for (Cookie cookie : cookies) {
                            if (OmegaServiceContext.GRAY_TAG_COOKIE_NAME.equals(cookie.getName())) {
                                grayTag = cookie.getValue();
                                break;
                            }
                        }
                    }

                    if (grayTag == null) {
                        return super.create(clientName);
                    }

                    String grayClientName = clientName + "--" + grayTag;
                    ILoadBalancer lb = clientFactory.getLoadBalancer(grayClientName);
                    if (lb.getAllServers().size() > 0) {
                        return super.create(grayClientName);
                    } else {
                        return super.create(clientName);
                    }
                }
            };
        }

        return bean;
    }
}
