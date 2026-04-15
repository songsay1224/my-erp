package com.example.sayy.Mapper;

import com.example.sayy.Entity.ContractPerformanceEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractPerformanceMapper {
    List<ContractPerformanceEntity> selectByProjectId(@Param("projectId") Long projectId);
    ContractPerformanceEntity selectById(@Param("id") Long id);
    int insert(ContractPerformanceEntity entity);
    int update(ContractPerformanceEntity entity);
    int deleteById(@Param("id") Long id, @Param("projectId") Long projectId);
}
