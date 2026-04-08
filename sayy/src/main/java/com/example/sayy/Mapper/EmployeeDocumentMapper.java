package com.example.sayy.Mapper;

import com.example.sayy.Entity.EmployeeDocumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeDocumentMapper {

    List<EmployeeDocumentEntity> selectByEmpNo(@Param("empNo") String empNo);
    EmployeeDocumentEntity selectById(@Param("id") Long id);
    int insert(EmployeeDocumentEntity doc);
    int deleteById(@Param("id") Long id, @Param("empNo") String empNo);
}
