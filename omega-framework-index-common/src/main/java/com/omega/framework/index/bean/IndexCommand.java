package com.omega.framework.index.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jackychenb on 11/12/2016.
 */
public class IndexCommand {

    public static final int OP_ADD = 1;
    public static final int OP_DELETE = 2;

    private String id;
    private String type;
    private String indexName;
    private int op;
    private Map<String ,String> dataMap = new HashMap<>();

    public IndexCommand() {

    }

    public IndexCommand(String type, String indexName, int op) {
        this.type = type;
        this.indexName = indexName;
        this.op = op;
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

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }

    public IndexCommand data(String key, String value) {
        dataMap.put(key, value);
        return this;
    }

    public String data(String key) {
        return dataMap.get(key);
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

        if (!(obj instanceof IndexCommand)) {
            return false;
        }

        return ((IndexCommand) obj).id.equals(id);
    }

}
