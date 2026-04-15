package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class ContractPurchaseEntity {
    private Long id;
    private Long projectId;
    private String purchaseType;    // 구분
    private String projectName;     // 프로젝트명
    private String subcontractName; // 하도급명
    private String vendorName;      // 거래서명
    private LocalDate contractDate; // 계약체결일
    private LocalDate contractStart; // 계약시작일
    private LocalDate contractEnd;   // 계약종료일
    private Long supplyAmount;      // 공급금액
    private Long vatAmount;         // 부가세액
    private Long totalAmount;       // 합계금액
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
