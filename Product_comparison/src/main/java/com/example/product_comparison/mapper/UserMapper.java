package com.example.product_comparison.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.product_comparison.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("select * from user where id = #{id}")
    User selectById(int id);

    @Select("select * from user")
    @Results(
            {
                    @Result(column = "id",property = "id"),
                    @Result(column = "username",property = "username"),
                    @Result(column = "password",property = "password"),
                    @Result(column = "birthday",property = "birthday"),
                    @Result(column = "id",property = "orders",javaType = List.class,
                            many = @Many(select = "com.example.product_comparison.mapper.OrderMapper.selectByUid")
                    )
            }
    )
    List<User> selectAllUserAndOrders();
}
