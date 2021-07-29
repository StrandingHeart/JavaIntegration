package com.zy.integrate.exception;

import com.zy.integrate.common.Result;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhangyong
 * Created on 2021-07-03
 */

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleAppException(Exception ex, HttpServletRequest request) {
        Result<String> s = new Result<>(123,null,ex.getMessage());
        return s;
    }
}
