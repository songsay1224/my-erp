package com.example.sayy.Service;

import com.example.sayy.Entity.DayOffCustomEntity;
import com.example.sayy.Entity.HolidayEntity;
import com.example.sayy.Mapper.DayOffCustomMapper;
import com.example.sayy.Mapper.HolidayMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class DaysOffService {
    private final HolidayMapper holidayMapper;
    private final DayOffCustomMapper dayOffCustomMapper;

    public DaysOffService(HolidayMapper holidayMapper, DayOffCustomMapper dayOffCustomMapper) {
        this.holidayMapper = holidayMapper;
        this.dayOffCustomMapper = dayOffCustomMapper;
    }

    public List<HolidayEntity> getHolidays(int year) {
        return holidayMapper.selectByYear(year);
    }

    public List<DayOffCustomEntity> getCustomDaysOff(int year) {
        return dayOffCustomMapper.selectByYear(year);
    }

    public List<DayOffCustomEntity> getRepeatCustomDaysOff() {
        return dayOffCustomMapper.selectRepeatAnnually();
    }

    public DayOffCustomEntity findCustomByLocDate(LocalDate locDate) {
        if (locDate == null) return null;
        return dayOffCustomMapper.selectByLocDate(locDate);
    }

    @Transactional
    public void upsertCustom(LocalDate date, String name, boolean repeatAnnually) {
        if (date == null) throw new IllegalArgumentException("날짜는 필수입니다.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("이름은 필수입니다.");
        DayOffCustomEntity e = new DayOffCustomEntity();
        e.setLocDate(date);
        e.setName(name.trim());
        e.setRepeatAnnually(repeatAnnually);
        dayOffCustomMapper.upsert(e);
    }

    @Transactional
    public void deleteCustom(long id) {
        dayOffCustomMapper.deleteById(id);
    }
}

