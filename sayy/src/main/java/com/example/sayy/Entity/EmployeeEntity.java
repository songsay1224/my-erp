package com.example.sayy.Entity;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeEntity {

    /** 입사일 기준으로 오늘 날짜 기준 근속년수를 실시간 계산해서 반환. */
    public Integer getYearsOfServiceCalculated() {
        if (hireDate == null) return yearsOfService;
        return Period.between(hireDate, LocalDate.now()).getYears();
    }

    /** 생년월일 기준으로 오늘 날짜 기준 나이를 실시간 계산해서 반환. */
    public Integer getAgeCalculated() {
        if (birthDate == null) return age;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * 주민등록번호 7번째 자리로 성별을 추론.
     * gender 필드가 직접 입력되어 있으면 그 값을 우선 반환.
     * 없으면 주민등록번호에서 자동 추출: 1,5 → 남성, 2,6 → 여성, 3,7 → 남성(2000년대), 4,8 → 여성(2000년대)
     */
    public String getGenderResolved() {
        if (gender != null && !gender.isBlank()) return gender;
        if (identificationNumber == null || identificationNumber.isBlank()) return null;
        String compact = identificationNumber.replaceAll("[^0-9]", "");
        if (compact.length() < 7) return null;
        char code = compact.charAt(6);
        return switch (code) {
            case '1', '3', '5', '7' -> "남성";
            case '2', '4', '6', '8' -> "여성";
            default -> null;
        };
    }

    /** 주민등록번호를 앞 6자리-뒷 7자리 형식(960101-1******)으로 반환. 저장값이 이미 하이픈 포함이면 그대로. */
    public String getIdentificationNumberFormatted() {
        if (identificationNumber == null || identificationNumber.isBlank()) return null;
        String v = identificationNumber.trim();
        if (v.contains("-")) return v;
        if (v.length() >= 7) {
            return v.substring(0, 6) + "-" + v.substring(6);
        }
        return v;
    }

    @Pattern(regexp = "^[0-9]+$", message = "사번은 숫자만 입력 가능합니다.")
    private String empNo;

    // 로그인 아이디(엑셀 업로드로 등록). 계정 발급 시 users.username으로 사용(없으면 empNo fallback)
    private String loginUsername;

    private String name;

    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;

    // 4대 보험 가입 여부(정규직/계약직은 선택, 프리랜서는 보통 미가입)
    private Boolean fourInsured;

    // 세금 계산 방식(고용 형태에 따라 달라짐)
    private TaxScheme taxScheme;

    // 계약 종료일(계약직/프리랜서 필수, 정규직은 보통 null)
    private LocalDate contractEndDate;

    private LocalDate hireDate;
    private LocalDate groupHireDate;

    // 직위/직무(직무는 콤마(,) 구분 문자열로 저장)
    private String positionName;
    private String departmentName;
    private String jobNames;

    private String identificationNumber;
    private Integer age;
    private Integer yearsOfService;
    private String email;
    private LocalDate birthDate;
    private LocalDate terminationDate;

    // 인사정보 변경 메모(관리자용)
    private String hrMemo;

    // 계약정보 - 근로계약
    private LocalDate laborContractStart;
    private LocalDate laborContractEnd;
    private LocalDate probationStart;
    private LocalDate probationEnd;
    private Integer probationPayRate;   // 수습 급여 지급률 (%)

    // 계약정보 - 임금계약
    private String incomeType;          // 근로소득 / 사업소득 등
    private LocalDate wageContractStart;
    private LocalDate wageContractEnd;
    private Long monthlySalary;         // 월 기본급 (원)

    // 개인정보
    private String nameEn;
    private String gender;
    private String nationality;
    private String phone;
    private String bankName;
    private String bankAccount;
    private String bankHolder;

    private LocalDateTime createdAt;

    private UserEntity user;
}

