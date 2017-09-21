package com.omega.demo.api;

import com.omega.demo.api.bean.User;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@FeignClient(value = "omega-demo-service")
public interface UserService {

    @RequestMapping("/user/{id}")
    User getById(@PathVariable("id") String id);

    @RequestMapping(value="/user/create", method = RequestMethod.POST)
    void createUser(@RequestBody User user);

}
