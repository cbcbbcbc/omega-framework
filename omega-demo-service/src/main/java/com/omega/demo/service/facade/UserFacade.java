package com.omega.demo.service.facade;

import com.omega.demo.api.GUID;
import com.omega.demo.api.bean.User;
import com.omega.demo.service.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@RestController
public class UserFacade {

    @Autowired
    UserEntity userEntity;

    @RequestMapping("/user/{id}")
    public User getById(@PathVariable("id") String id) {
        return userEntity.getById(id);
    }

    @RequestMapping(value="/user", method = RequestMethod.POST)
    public void create(@RequestBody User user) {
        String zoneCode = allocateZoneCode(user);
        user.setId(generateId(zoneCode));
        user.setZoneCode(zoneCode);
        userEntity.create(user);
    }

    private String allocateZoneCode(User user) {
        return "0" + new Random().nextInt(10);
    }

    private String generateId(String zoneCode) {
        return GUID.get() + zoneCode;
    }

    @RequestMapping(value="/testUser")
    public void test() {
        User user = new User();
        String zoneCode = allocateZoneCode(user);
        user.setId(generateId(zoneCode));
        user.setName(user.getId());
        user.setZoneCode(zoneCode);
        userEntity.create(user);
    }

}
