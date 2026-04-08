package com.example.sayy.Mapper;

import com.example.sayy.Entity.EmployeeOrgAssignmentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeOrgAssignmentMapper {
    List<EmployeeOrgAssignmentEntity> selectByEmpNo(@Param("empNo") String empNo);

    int deleteByEmpNo(@Param("empNo") String empNo);

    int insert(EmployeeOrgAssignmentEntity e);
}

