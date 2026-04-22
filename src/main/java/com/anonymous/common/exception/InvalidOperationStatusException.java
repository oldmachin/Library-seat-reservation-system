package com.anonymous.common.exception;

public class InvalidOperationStatusException extends RuntimeException {
    public InvalidOperationStatusException(String message) {
        super("Invalid Operation status: " + message);
    }
}
