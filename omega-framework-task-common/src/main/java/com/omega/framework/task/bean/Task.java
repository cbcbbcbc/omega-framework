package com.omega.framework.task.bean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackychenb on 11/12/2016.
 */
public class Task {

    private String id;
    private String type;
    private Date triggerTime;
    private Map<String ,String> dataMap = new HashMap<>();

    public Task() {

    }

    public Task(String type, Date triggerTime) {
        this.type = type;
        this.triggerTime = triggerTime;
    }

    public Task(String type) {
        this(type, null);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public Task data(String key, String value) {
        dataMap.put(key, value);
        return this;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Task)) {
            return false;
        }

        return ((Task) obj).id.equals(id);
    }

}
