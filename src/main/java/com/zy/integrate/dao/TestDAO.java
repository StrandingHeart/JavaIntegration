package com.zy.integrate.dao;


import com.zy.integrate.domain.TestPO;
import com.zy.integrate.util.TkMapper;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;

/**
 * 只需加这两个主键并继承util中的TkMapper就可以orm
 */
@Mapper
@Repository
public interface TestDAO extends TkMapper<TestPO> {

}