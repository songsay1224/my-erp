package com.example.sayy.Mapper;

import com.example.sayy.Entity.ContractEntity;
import com.example.sayy.Entity.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProjectMapper {
    List<ProjectEntity> selectAll(@Param("status") String status, @Param("keyword") String keyword);
    ProjectEntity selectById(@Param("id") Long id);
    int insert(ProjectEntity project);
    int update(ProjectEntity project);
    int deleteById(@Param("id") Long id);

    List<ContractEntity> selectContractsByProjectId(@Param("projectId") Long projectId);
    ContractEntity selectContractById(@Param("id") Long id);
    int insertContract(ContractEntity contract);
    int updateContract(ContractEntity contract);
    int deleteContract(@Param("id") Long id, @Param("projectId") Long projectId);
}
