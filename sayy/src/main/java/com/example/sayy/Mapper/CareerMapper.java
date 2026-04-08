package com.example.sayy.Mapper;

import com.example.sayy.Entity.CareerEntity;
import com.example.sayy.Entity.CertificateEntity;
import com.example.sayy.Entity.EducationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CareerMapper {

    List<CareerEntity> selectCareersByEmpNo(@Param("empNo") String empNo);
    int insertCareer(CareerEntity career);
    int deleteCareer(@Param("id") Long id, @Param("empNo") String empNo);

    List<EducationEntity> selectEducationsByEmpNo(@Param("empNo") String empNo);
    int insertEducation(EducationEntity education);
    int deleteEducation(@Param("id") Long id, @Param("empNo") String empNo);

    List<CertificateEntity> selectCertificatesByEmpNo(@Param("empNo") String empNo);
    int insertCertificate(CertificateEntity certificate);
    int deleteCertificate(@Param("id") Long id, @Param("empNo") String empNo);
}
