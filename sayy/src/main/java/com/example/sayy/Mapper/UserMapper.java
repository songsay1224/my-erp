package com.example.sayy.Mapper;

import com.example.sayy.Entity.UserEntity;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    UserEntity selectByUsername(@Param("username") String username);

    int insert(UserEntity user);

    int deleteByUsername(@Param("username") String username);

    int existsByUsername(@Param("username") String username);
}

