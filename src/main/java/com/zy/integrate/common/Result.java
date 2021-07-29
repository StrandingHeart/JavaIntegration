package com.zy.integrate.common;

import lombok.Data;

/**
 * @author zhangyong
 * Created on 2021-07-03
 */
@Data
public class Result<T> {
    int code;
    T data;
    String message;

    public Result(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

}
