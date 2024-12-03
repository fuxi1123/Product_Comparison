package com.example.product_comparison.controller;

import com.example.product_comparison.entity.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @GetMapping("/user/{id}")
    public String getUserById(@PathVariable int id){
        /*由于RESTful风格的接口提倡使用/user/10类似的URL传输参数，而非/user?id=10,此种传参为动态显示的，故需要加上注解@PathVariable，否则只能拿到以?形式传输的参数*/
        System.out.println(id);
        return "根据ID获取用户信息";
    }
    @PostMapping("/user")
    public String save(User user){
        return "添加用户";
    }
    @PutMapping("/user")
    public String update(User user){
        return "更新用户";
    }
    @DeleteMapping("/user/{id}")
    public String deleteById(@PathVariable int id){
        System.out.println(id);
        return "根据ID删除用户";
    }
}
