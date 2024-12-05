package com.example.product_comparison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product_comparison.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Select("select * from user")
//    public List<User> find();
//
//    @Insert("insert into user values (#{id},#{username},#{password},#{birthday})")
//    public int insert(User user);
}
