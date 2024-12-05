package com.example.product_comparison.controller;

import com.example.product_comparison.entity.User;
import com.example.product_comparison.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/user")
    public List query(){
        List<User> list=userMapper.selectList(null);
        System.out.println(list);
        return list;
    }

    @PostMapping("/user")
    public String save(User user){
        int i = userMapper.insert(user);
        System.out.println(user);
        if(i>0){
            return "success";
        }else {
            return "fail";
        }
    }
}
