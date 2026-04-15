package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class ContractPerformanceEntity {
    private Long id;
    private Long projectId;
    private String customerAgency;   // 수요기관
    private String contractName;     // 계약번명
    private Long businessAmount;     // 사업금액
    private String supplyType;       // 수급형태
    private BigDecimal paymentRate;  // 지불률
    private Long contractAmount;     // 계약금액
    private LocalDate completionDate; // 준공일
    private String businessPeriod;   // 사업기간
    private String categoryL1;       // L1
    private String categoryL2;       // L2
    private String categoryL3;       // L3
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
