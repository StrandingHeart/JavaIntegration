package com.zy.integrate.service;

import com.zy.integrate.dao.TestDAO;
import com.zy.integrate.domain.TestPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class TestService {

    @Resource
    private TestDAO testDAO;

    public void insertTest(){
        //id是自增的。
        TestPO t = new TestPO(null,"zhangyong2","pass","hobby",(byte)23);
        this.testDAO.insertSelective(t);
    }

    public TestPO getTest(Integer id){
        TestPO t = new TestPO();
        t.setId(id);
        return this.testDAO.select(t).get(0);
    }

    public List<TestPO> findAll(){
        return this.testDAO.selectAll();
    }

    public TestPO getByName(String name){
        return this.testDAO.getByName(name);
    }

    public List<TestPO> getByAge(Byte age){
        return this.testDAO.getByAge(age);
    }
}
