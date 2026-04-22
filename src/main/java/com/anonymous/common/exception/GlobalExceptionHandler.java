package com.anonymous.common.exception;

import com.anonymous.common.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 拦截业务逻辑异常（如：座位已被占用、余额不足等）
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        // 逻辑：将 Service 层抛出的错误信息直接透传给前端
        return Result.fail(null, e.getMessage());
    }

    // 拦截参数校验异常（需引入 validation 依赖）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(null, "参数校验失败: " + message);
    }

    // 拦截兜底异常（未捕获的系统级错误）
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        // 在职业实践中，此处应记录完整堆栈日志，并给用户返回模糊的错误提示以对冲安全风险
        return Result.fail(null, "系统繁忙，请稍后再试");
    }
}
