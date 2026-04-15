package com.example.sayy.Mapper;

import com.example.sayy.Entity.ContractSalesEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractSalesMapper {
    List<ContractSalesEntity> selectByProjectId(@Param("projectId") Long projectId);
    ContractSalesEntity selectById(@Param("id") Long id);
    int insert(ContractSalesEntity entity);
    int update(ContractSalesEntity entity);
    int deleteById(@Param("id") Long id, @Param("projectId") Long projectId);
}
