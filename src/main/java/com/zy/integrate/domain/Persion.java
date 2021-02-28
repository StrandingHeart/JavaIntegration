package com.zy.integrate.domain;

import java.io.Serializable;

/**
 * @author zhangyong05
 * Created on 2021-02-24
 */
public class Persion implements Serializable{
    private static final long serialVersionUID = -2651502494420055099L;
    private String name;

    private Integer age;

    private Long time;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "Persion{" +
                "name='" + name + '\'' +
                ", age=" + age +
                ", time=" + time +
                '}';
    }
}
