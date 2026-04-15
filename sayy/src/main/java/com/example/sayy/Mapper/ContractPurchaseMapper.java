package com.example.sayy.Mapper;

import com.example.sayy.Entity.ContractPurchaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ContractPurchaseMapper {
    List<ContractPurchaseEntity> selectByProjectId(@Param("projectId") Long projectId);
    ContractPurchaseEntity selectById(@Param("id") Long id);
    int insert(ContractPurchaseEntity entity);
    int update(ContractPurchaseEntity entity);
    int deleteById(@Param("id") Long id, @Param("projectId") Long projectId);
}
