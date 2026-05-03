package com.anonymous.common.exception;

public class InvalidParameterException extends BusinessException {
    public InvalidParameterException(String parameterName) {
        super(400, "参数错误：" + parameterName);
    }
}
