package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EducationEntity {
    private Long id;
    private String empNo;
    private String schoolName;
    private String major;
    private String degree;            // 고등학교졸업 / 전문학사 / 학사 / 석사 / 박사
    private LocalDate startDate;
    private LocalDate endDate;
    private String graduationStatus;  // 졸업 / 재학 / 중퇴 / 편입
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
