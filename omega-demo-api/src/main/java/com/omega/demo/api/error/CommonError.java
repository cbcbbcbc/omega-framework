package com.omega.demo.api.error;

/**
 * Created by jackychenb on 25/12/2016.
 */
public enum CommonError implements IError {

    MSG(-1),
    NO_PERMISSION(-2),
    NOT_LOGINED(-3),
    RECORD_NOT_EXIST(-4),
    INVALID_VERIFY_CODE(-5),
    EXPIRED_VERIFY_CODE(-6);

    private final int code;
    private CommonError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
