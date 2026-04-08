package com.example.sayy.Controller;

import com.example.sayy.Entity.HolidayEntity;
import com.example.sayy.Service.HolidayService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.util.List;

@RestController
public class DayController {
    private final HolidayService holidayService;

    public DayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping("/admin/holidays")
    public List<HolidayEntity> getHolidays(@RequestParam(required = false) Integer year) {
        int y = (year == null) ? Year.now().getValue() : year;
        return holidayService.fetchHolidays(y);
    }

    @GetMapping("/admin/holidays/sync")
    public HolidayService.SyncResult syncHolidays(@RequestParam(required = false) Integer year) {
        int y = (year == null) ? Year.now().getValue() : year;
        return holidayService.syncHolidays(y);
    }
}