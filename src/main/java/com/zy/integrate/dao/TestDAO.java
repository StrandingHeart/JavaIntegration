package com.zy.integrate.dao;


import com.zy.integrate.domain.TestPO;
import com.zy.integrate.util.TkMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 只需加这两个注解并继承util中的TkMapper就可以orm
 */
@Mapper
@Repository
public interface TestDAO extends TkMapper<TestPO> {
    TestPO getByName(@Param("name") String name);

    List<TestPO> getByAge(@Param("age") Byte age);
}