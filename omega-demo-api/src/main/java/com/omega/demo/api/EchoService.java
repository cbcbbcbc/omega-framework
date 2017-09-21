package com.omega.demo.api;

import com.omega.demo.api.bean.Greeting;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@FeignClient(value = "omega-demo-service", fallback = EchoService.HystrixEchoService.class)
public interface EchoService {

    @RequestMapping(value = "/echo/{foo}", method = RequestMethod.POST)
    String echo(@PathVariable("foo") String foo, @RequestBody Greeting greeting);

    @Component
    public static class HystrixEchoService implements EchoService {
        @Override
        public String echo(String foo, Greeting greeting) {
            return foo+ ":" + greeting.getNick() + ":" + greeting.getMessage();
        }
    }

}
