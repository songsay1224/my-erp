package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CareerEntity {
    private Long id;
    private String empNo;
    private String companyName;
    private String department;
    private String position;
    private LocalDate startDate;
    private LocalDate endDate;     // null = 현재 재직
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
