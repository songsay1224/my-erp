package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class DayOffCustomEntity {
    private Long id;
    private LocalDate locDate;
    private String name;
    private boolean repeatAnnually;
    private LocalDateTime createdAt;
}

