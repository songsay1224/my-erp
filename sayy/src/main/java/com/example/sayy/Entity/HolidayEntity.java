package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class HolidayEntity {
    private LocalDate locDate;
    private String name;
    private boolean isHoliday;
    private LocalDateTime createdAt;
}

