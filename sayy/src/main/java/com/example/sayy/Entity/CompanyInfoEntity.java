package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CompanyInfoEntity {
    private Integer id; // 항상 1을 사용

    private String companyName;
    private String ceoName;
    private String phone;
    private LocalDate foundedDate;
    private String zipCode;
    private String address;
    private String addressDetail;
    private String businessRegNo;
    private String corporateRegNo;

    // /uploads/... 형태로 노출 가능한 상대 경로 저장
    private String logoPath;
    private String sealPath;

    private LocalDateTime updatedAt;
}

