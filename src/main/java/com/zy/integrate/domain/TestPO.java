package com.zy.integrate.domain;

import lombok.*;

import javax.persistence.Table;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name="test")
public class TestPO {

    private Integer id;

    private String name;

    private String password;

    private String hobby;

    private Byte age;

}