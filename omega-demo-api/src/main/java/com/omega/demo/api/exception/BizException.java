package com.omega.demo.api.exception;

/**
 * Created by jackychenb on 25/12/2016.
 */
/**
 * 业务逻辑异常
 */

public class BizException extends RuntimeException {

    private static final long serialVersionUID = 4524632967825015383L;

    private final Enum errorCode;
    private final Object[] parameters;

    /**
     * @param errorCode 错误代码
     * @param parameters 错误信息相关参数
     */
    public BizException(Enum errorCode, Object... parameters) {
        this.errorCode = errorCode;
        this.parameters = parameters;
    }

    public BizException(Enum errorCode) {
        this(errorCode, (Object[]) null);
    }

    public Enum getErrorCode() {
        return errorCode;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String getMessage() {
        return errorCode.toString();
    }

    public String toString() {
        return getMessage();
    }

    /**
     * 避免构建异常堆栈付出的开销
     */
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

}