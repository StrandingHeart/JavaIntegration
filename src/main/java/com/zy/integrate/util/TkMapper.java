package com.zy.integrate.util;


import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * @Author zhangyong
 * @Date 2020/7/4 16:05
 */
public interface TkMapper<T> extends Mapper<T>, MySqlMapper<T> {

}