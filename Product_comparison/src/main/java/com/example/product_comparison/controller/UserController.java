package com.example.product_comparison.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.product_comparison.entity.User;
import com.example.product_comparison.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/user/findAll")
    public List<User> find(){
        return userMapper.selectAllUserAndOrders();
    }

    @GetMapping("/user/find")
    public List<User> findByCond(){
        QueryWrapper<User> queryWrapper = new QueryWrapper();
        queryWrapper.eq("username","zhangsan");
        return userMapper.selectList(queryWrapper);
    }

    @GetMapping("/user/findByPage")
    public IPage findByPage(){
        Page<User> page = new Page<>(0,2);
        IPage iPage = userMapper.selectPage(page,null);
        return iPage;
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
