package com.example.sayy.Mapper;

import com.example.sayy.Entity.HolidayEntity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface HolidayMapper {
    int upsert(HolidayEntity holiday);

    HolidayEntity selectByLocDate(@Param("locDate") LocalDate locDate);

    List<HolidayEntity> selectByYear(@Param("year") int year);
}

