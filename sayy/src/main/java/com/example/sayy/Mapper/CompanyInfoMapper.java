package com.example.sayy.Mapper;

import com.example.sayy.Entity.CompanyInfoEntity;
import org.apache.ibatis.annotations.Param;

public interface CompanyInfoMapper {
    CompanyInfoEntity selectOne();

    int upsert(CompanyInfoEntity entity);

    int updateLogoPath(@Param("logoPath") String logoPath);

    int updateSealPath(@Param("sealPath") String sealPath);
}

