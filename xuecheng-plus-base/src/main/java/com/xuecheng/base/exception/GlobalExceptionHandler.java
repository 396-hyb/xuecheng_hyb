package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author hyb
 * @version 1.0
 * @description 全局异常处理器
 * @date 2024/11/12
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseBody //@ResponseBody: 表示方法的返回值将直接作为 HTTP 响应体返回。
    @ExceptionHandler(XueChengPlusException.class) //指定这个方法用于处理所有 XueChengPlusException 类型的异常。
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置 HTTP 响应的状态码为 500，表示服务器内部错误。
    public RestErrorResponse customException(XueChengPlusException e){
        log.error("【系统异常】{}",e.getErrMessage(),e);
        return new RestErrorResponse(e.getErrMessage());
    }

    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception e){
        log.error("【系统异常】{}",e.getMessage(),e);
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
    }
}
