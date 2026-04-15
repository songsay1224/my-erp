package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor
public class ContractSalesEntity {
    private Long id;
    private Long projectId;
    private String contractName;       // 계약명
    private String customerAgency;     // 수요기관
    private Long contractAmount;       // 계약금액
    private Long accumulatedProgress;  // 누적기성
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
