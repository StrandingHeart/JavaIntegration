package com.zy.integrate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author baihong
 * @Date 2020/7/23 15:51
 */
@Controller
public class ViewController {
    @RequestMapping("/frist")
    public  String first(){

        return "frist";
    }
}
