package com.omega.demo.api.bean;

import java.io.Serializable;

/**
 * Created by wuzhengtao on 16/12/6.
 */
public class User implements Serializable {

    private String id;
    private String name;
    private String zoneCode;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

}
