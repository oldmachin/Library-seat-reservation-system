package com.anonymous.common.exception;

import com.anonymous.common.Result;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<Object> handleBusinessException(BusinessException e) {
        return Result.fail(e.getCode(), null, e.getMessage());
    }

    // 拦截业务逻辑异常（如：座位已被占用、余额不足等）
    @ExceptionHandler(RuntimeException.class)
    public Result<Object> handleRuntimeException(RuntimeException e) {
        return Result.fail(500, e.getMessage() == null ? "业务处理失败" : e.getMessage());

    }

    // 拦截参数校验异常（需引入 validation 依赖）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "请求参数校验错误";
        return Result.fail(400, "参数校验失败: " + message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Object> handleMissingParameter(MissingServletRequestParameterException e) {
        return Result.fail(400, null, "缺少请求参数: " + e.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Object> handleUnreadableBody(HttpMessageNotReadableException e) {
        return Result.fail(400, null, "请求体格式错误");
    }

    // 拦截兜底异常（未捕获的系统级错误）
    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        // 在职业实践中，此处应记录完整堆栈日志，并给用户返回模糊的错误提示以对冲安全风险
        return Result.fail(500, e.getMessage() == null ? "业务处理失败" : e.getMessage());
    }
}
