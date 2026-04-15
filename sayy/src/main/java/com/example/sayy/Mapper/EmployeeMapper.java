package com.example.sayy.Mapper;

import com.example.sayy.Entity.EmployeeEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EmployeeMapper {
    EmployeeEntity selectByEmpNo(@Param("empNo") String empNo);

    EmployeeEntity selectWithUserByEmpNo(@Param("empNo") String empNo);

    List<EmployeeEntity> selectAllWithUser();

    List<EmployeeEntity> selectByEmploymentTypeWithUser(@Param("employmentType") String employmentType);

    List<EmployeeEntity> selectByFiltersWithUser(@Param("employmentType") String employmentType,
                                                 @Param("employmentStatus") String employmentStatus,
                                                 @Param("keyword") String keyword,
                                                 @Param("sort") String sort,
                                                 @Param("limit") Integer limit,
                                                 @Param("offset") Integer offset);

    long countByFilters(@Param("employmentType") String employmentType,
                        @Param("employmentStatus") String employmentStatus,
                        @Param("keyword") String keyword);

    long countAll();

    long countByEmploymentType(@Param("employmentType") String employmentType);

    long countByEmploymentStatus(@Param("employmentStatus") String employmentStatus);

    int insert(EmployeeEntity employee);

    int deleteByEmpNo(@Param("empNo") String empNo);

    int existsByEmpNo(@Param("empNo") String empNo);

    int existsByLoginUsername(@Param("loginUsername") String loginUsername);

    int updateHrInfo(EmployeeEntity employee);

    int updateContractInfo(EmployeeEntity employee);

    int updatePersonalInfo(EmployeeEntity employee);
}

