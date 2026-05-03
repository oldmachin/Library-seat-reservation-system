package com.anonymous.common;

public class Result<T> {
    private Integer code; // 400 - 参数错误， 401 - 未登录， 403 - 禁用， 409 - 状态冲突， 500 - 系统异常
    private String message;
    private T data;

    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.code = 200;
        result.data = data;
        result.message = message;
        return result;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> Result<T> fail(Integer code, T data, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.data = data;
        result.message = message;
        return result;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return fail(code, null, message);
    }
}
