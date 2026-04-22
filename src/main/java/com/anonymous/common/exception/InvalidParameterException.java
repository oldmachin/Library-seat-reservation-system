package com.anonymous.common.exception;

public class InvalidParameterException extends RuntimeException {
    public InvalidParameterException(String parameterName) {
        super("Invalid Parameter: " + parameterName);
    }
}
