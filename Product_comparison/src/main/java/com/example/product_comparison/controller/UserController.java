package com.example.product_comparison.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.product_comparison.entity.User;
import com.example.product_comparison.mapper.UserMapper;
import com.example.product_comparison.utils.JwtUtils;
import com.example.product_comparison.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RequestMapping("/user")
@RestController
public class UserController {

//    @PostMapping("/login")
//    // querystring:username=zhangsan&password=123   User user,String username,String password
//    // json:{username:zhangsan,password:123}
//    // 如果前端传递的数据是json格式，必须使用对象接收，同时需要添加@RequestBody
//    public Result login(@RequestBody User user){
//
//        String token = JwtUtils.generateToken(user.getUsername());
//        return Result.ok().data("token",token);
//    }
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        // 检查用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User existingUser = userMapper.selectOne(queryWrapper);
        if (existingUser == null) {
            return Result.error().message("Username does not exist");
        }
        // 验证密码是否正确
        if (!existingUser.getPassword().equals(user.getPassword())) {
            return Result.error().message("Incorrect password");
        }
        // 生成 Token
        String token = JwtUtils.generateToken(existingUser.getUsername());
        // 返回登录成功的结果
        System.out.println(existingUser.getId());
        return Result.ok().data("userId", existingUser.getId()).data("email", existingUser.getEmail()).data("token", token).message("Login successful");
    }

    @GetMapping("/info")    //"token:xxx"
    public Result info(String token){
        String username = JwtUtils.getClaimsByToken(token).getSubject();
        String url = "https://cdn.jsdelivr.net/gh/B1ANKC-MOV/HttpImg@master/20240111/fox.19xwzzz8qc3k.webp";
        return Result.ok().data("name",username).data("avatar",url);
    }

    @PostMapping("/logout") //"token:xxx"
    public Result logout(){ return Result.ok();}

    @PostMapping("/register") //"token:xxx"
    public Result register(@RequestBody User user){
        System.out.println(user);
        userMapper.insert(user);
        return Result.ok();
    }

    @GetMapping("/check-username")
    public Result checkUsername(@RequestParam String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        int count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            System.out.println("Username already exists");
            return Result.ok().data("exists", true).message("Username already exists");
        } else {
            return Result.ok().data("exists", false).message("Username is available");
        }
    }

    @GetMapping("/check-email")
    public Result checkEmail(@RequestParam String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        int count = userMapper.selectCount(queryWrapper);

        if (count > 0) {
            return Result.ok().data("exists", true).message("Email already registered");
        } else {
            return Result.ok().data("exists", false).message("Email is available");
        }
    }

    @Autowired
    private UserMapper userMapper;

}
