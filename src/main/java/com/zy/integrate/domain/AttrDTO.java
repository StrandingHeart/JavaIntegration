package com.zy.integrate.domain;

import java.math.BigDecimal;

/**
 * @author zhangyong
 * Created on 2021-03-01
 */
public class AttrDTO {
    private String language;

    private BigDecimal money;

    public AttrDTO() {
    }

    public AttrDTO(String language, BigDecimal money) {
        this.language = language;
        this.money = money;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "AttrDTO{" +
                "language='" + language + '\'' +
                ", money=" + money +
                '}';
    }
}
