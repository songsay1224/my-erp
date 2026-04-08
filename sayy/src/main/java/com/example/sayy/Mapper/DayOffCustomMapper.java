package com.example.sayy.Mapper;

import com.example.sayy.Entity.DayOffCustomEntity;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

public interface DayOffCustomMapper {
    List<DayOffCustomEntity> selectByYear(@Param("year") int year);

    List<DayOffCustomEntity> selectRepeatAnnually();

    DayOffCustomEntity selectByLocDate(@Param("locDate") LocalDate locDate);

    int upsert(DayOffCustomEntity item);

    int deleteById(@Param("id") long id);
}

