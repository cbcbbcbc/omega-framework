package com.omega.demo.service.facade;

import com.omega.demo.api.GUID;
import com.omega.demo.api.bean.OrderDetail;
import com.omega.demo.api.bean.OrderForm;
import com.omega.demo.api.bean.OrderListModel;
import com.omega.demo.api.bean.User;
import com.omega.demo.api.error.CommonError;
import com.omega.demo.api.exception.BizException;
import com.omega.demo.service.domain.OrderEntity;
import com.omega.demo.service.domain.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by wuzhengtao on 16/12/6.
 */

@RestController
public class OrderFacade {

    @Autowired
    OrderEntity orderEntity;

    @Autowired
    UserEntity userEntity;

    @RequestMapping("/order/{id}")
    public OrderForm getById(@PathVariable("id") String id) {
        return orderEntity.getOrderById(id);
    }

    @RequestMapping("/orders/{userId}")
    public List<OrderForm> list(@PathVariable("userId") String userId) {
        return orderEntity.getOrderFormListByUserId(userId);
    }

    @RequestMapping("/orders")
    public OrderListModel search(@ModelAttribute OrderListModel listModel) {
        return orderEntity.search(listModel);
    }

    @RequestMapping(value="/testOrder/{userId}")
    public String testCreate(@PathVariable("userId") String userId) {
        User user = userEntity.getById(userId);
        if (user == null) {
            throw new BizException(CommonError.RECORD_NOT_EXIST);
        }

        String[] NAMES = {
                "测试", "手机", "中国", "护眼灯", "茶杯", "电脑桌",
                "塑料", "不锈钢", "电脑", "椅子", "窗帘", "三体",
                "日本", "小狗", "电视"
        };

        StringBuffer sb = new StringBuffer();
        Random rand = new Random();
        int length = rand.nextInt(3) + 1;
        for (int i = 0; i < length; i++) {
            sb.append(NAMES[rand.nextInt(NAMES.length)]);
        }

        int price0 = rand.nextInt(10000) + 1;
        BigDecimal price = new BigDecimal(price0).divide(new BigDecimal(100), 2, RoundingMode.CEILING);

        int qty = rand.nextInt(100) + 1;
        BigDecimal amount = price.multiply(new BigDecimal(qty));

        int days = rand.nextInt(3);
        Date gmtCreated = new Date(new Date().getTime() - days * 24 * 3600 * 1000L);

        OrderForm o = new OrderForm();
        o.setId(generateId(user.getZoneCode()));
        o.setUserId(userId);
        o.setNumber("xxx");
        o.setAmount(amount);
        o.setGmtCreated(gmtCreated);

        OrderDetail d = new OrderDetail();
        d.setId(generateId(user.getZoneCode()));
        d.setOrderFormId(o.getId());
        d.setItemNo("HK001");
        d.setItemName(sb.toString());
        d.setPrice(price);
        d.setQty(qty);
        d.setAmount(amount);

        List<OrderDetail> detailList = new ArrayList<>();
        detailList.add(d);
        o.setDetailList(detailList);

        return orderEntity.createOrder(o);
    }

    private String generateId(String zoneCode) {
        return GUID.get() + zoneCode;
    }

}
