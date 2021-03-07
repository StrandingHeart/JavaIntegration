package com.zy.integrate.domain;

import java.util.List;

/**
 * @author zhangyong
 * Created on 2021-03-01
 */
public class EsDemoPO {
    private String id;
    private String businessId;
    private AttrDTO attribute;
    private Integer age;
    private String name;
    private List<String> signalList;

    public EsDemoPO() {
    }

    public EsDemoPO(String id, String businessId, AttrDTO attribute, Integer age, String name, List<String> signalList) {
        this.id = id;
        this.businessId = businessId;
        this.attribute = attribute;
        this.age = age;
        this.name = name;
        this.signalList = signalList;
    }

    public List<String> getSignalList() {
        return signalList;
    }

    public void setSignalList(List<String> signalList) {
        this.signalList = signalList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public AttrDTO getAttribute() {
        return attribute;
    }

    public void setAttribute(AttrDTO attribute) {
        this.attribute = attribute;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "EsDemoPO{" +
                "id=" + id +
                ", businessId=" + businessId +
                ", attribute=" + attribute +
                ", age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
