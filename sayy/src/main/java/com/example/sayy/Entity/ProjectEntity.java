package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@NoArgsConstructor
public class ProjectEntity {
    private Long id;
    private String name;
    private Long clientId;
    private String status;   // ESTIMATE / CONTRACT / IN_PROGRESS / DONE / CANCELLED
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    private Long contractAmount;
    private String description;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 기본 정보 추가 필드
    private String projectCode;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate approvedDate;
    private String pmEmpNo;
    private String executiveEmpNo;
    private String businessPlace;
    private String categoryMajor;   // L1
    private String categoryMinor;   // L2
    private String categoryL3;      // L3
    private Integer managementYear; // 관리년도
    private String division;
    private String department;
    private Long orgUnitId;
    private String participationType;
    private String supplyType;
    private String salesEmpNo;

    // 계약 일반 필드
    private String customerAgency;
    private String customerOrdering;
    private String contractName;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate contractDate;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate contractStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate contractEnd;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate asStart;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate asEnd;
    private Integer ourShareRate;
    private Long ourAmount;
    private Long ourAmountVatEx;
    private Long contractAmountVatEx;
    private String paymentCondition;
    private Integer defectPeriod;
    private java.math.BigDecimal penaltyRate;

    // 조회 시 함께 로딩
    private String clientName;
    private String pmName;
    private String executiveName;
    private String salesName;
    private String orgUnitName;
    private List<ContractEntity> contracts;

    public String formatAmount(Long amount) {
        if (amount == null) return "-";
        return NumberFormat.getNumberInstance(Locale.KOREA).format(amount) + " 원";
    }

    public String getStatusLabel() {
        if (status == null) return "-";
        return switch (status) {
            case "ESTIMATE"    -> "견적";
            case "CONTRACT"    -> "계약";
            case "IN_PROGRESS" -> "진행중";
            case "DONE"        -> "완료";
            case "CANCELLED"   -> "취소";
            default            -> status;
        };
    }

    public String getStatusBadgeClass() {
        if (status == null) return "text-bg-light";
        return switch (status) {
            case "ESTIMATE"    -> "text-bg-secondary";
            case "CONTRACT"    -> "text-bg-primary";
            case "IN_PROGRESS" -> "text-bg-warning";
            case "DONE"        -> "text-bg-success";
            case "CANCELLED"   -> "text-bg-danger";
            default            -> "text-bg-light";
        };
    }
}
