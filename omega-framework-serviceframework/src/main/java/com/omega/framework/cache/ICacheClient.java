package com.omega.framework.cache;

/**
 * Created by jackychenb on 17/03/2017.
 */
public interface ICacheClient {

    void set(String key, Object value);
    void set(String key, Object value, long lifecycle);
    Object get(String key);
    void remove(String key);

}
