package com.omega.demo.service.dao;

import com.omega.demo.api.bean.OrderDetail;
import com.omega.demo.api.bean.OrderForm;
import com.omega.demo.api.bean.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Created by jackychenb on 08/12/2016.
 */

@Mapper
public interface OrderDao {

    OrderForm getOrderFormById(String id);
    List<OrderForm> getOrderFormListByUserId(String userId);
    List<OrderDetail> getOrderDetailListByOrderFormId(String orderFormId);
    void createOrderForm(OrderForm orderForm);
    void createOrderDetail(OrderDetail orderDetail);

}
