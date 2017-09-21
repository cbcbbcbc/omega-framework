package com.omega.demo.service.dao;

import com.omega.demo.api.bean.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by jackychenb on 08/12/2016.
 */

@Mapper
public interface UserDao {

    User getById(String id);
    void create(User user);

}
