package com.example.product_comparison.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @RequestMapping(value="/hello",method = RequestMethod.GET)
    public String Hello(String nickname,String phone) {
        System.out.println(phone);
        return "Hello World"+nickname;
    }
}
