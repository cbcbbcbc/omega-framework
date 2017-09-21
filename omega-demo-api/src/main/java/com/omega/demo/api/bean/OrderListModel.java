package com.omega.demo.api.bean;

import javafx.util.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Created by jackychenb on 17/03/2017.
 */
public class OrderListModel extends ListModel implements Serializable {

    private String userId;
    private String number;
    private BigDecimal gmtCreatedStart;
    private BigDecimal gmtCreatedEnd; // 约定范围都是前闭后开
    private String itemNo;
    private String itemName;

    private List<Pair<String, Long>> gmtCreatedList; // 有订单的日期列表，Pair暂时借用javafx的
    private BigDecimal sum;

    public OrderListModel() {
        this(40);
    }

    public OrderListModel(int pageSize) {
        super(pageSize);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getGmtCreatedStart() {
        return gmtCreatedStart;
    }

    public void setGmtCreatedStart(BigDecimal gmtCreatedStart) {
        this.gmtCreatedStart = gmtCreatedStart;
    }

    public BigDecimal getGmtCreatedEnd() {
        return gmtCreatedEnd;
    }

    public void setGmtCreatedEnd(BigDecimal gmtCreatedEnd) {
        this.gmtCreatedEnd = gmtCreatedEnd;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<Pair<String, Long>> getGmtCreatedList() {
        return gmtCreatedList;
    }

    public void setGmtCreatedList(List<Pair<String, Long>> gmtCreatedList) {
        this.gmtCreatedList = gmtCreatedList;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

}
