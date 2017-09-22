package com.omega.framework.feign;

import com.omega.framework.OmegaServiceContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class FeignClientRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attrs.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    String value = values.nextElement();
                    requestTemplate.header(name, value);
                }
            }
        }

        String grayTag = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (OmegaServiceContext.GRAY_TAG_COOKIE_NAME.equals(cookie.getName())) {
                    grayTag = cookie.getValue();
                    break;
                }
            }
        }

        if (grayTag == null) {
            grayTag = OmegaServiceContext.getGrayTag();
            if (grayTag != null) {
                requestTemplate.header("Cookie", grayTag);
            }
        }
    }

}
