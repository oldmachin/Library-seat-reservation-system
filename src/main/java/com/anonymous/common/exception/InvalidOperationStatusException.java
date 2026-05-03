package com.anonymous.common.exception;

public class InvalidOperationStatusException extends BusinessException {
    public InvalidOperationStatusException(String message) {
        super(409, message);
    }
}
