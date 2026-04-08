package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClientEntity {
    private Long id;
    private String name;
    private String businessRegNo;
    private String corporateRegNo;
    private String ceoName;
    private String industry;
    private String phone;
    private String email;
    private String zipCode;
    private String address;
    private String addressDetail;
    private String status;          // ACTIVE / DORMANT / CLOSED
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조회 시 함께 로딩
    private List<ClientContactEntity> contacts;

    /** 사업자등록번호를 XXX-XX-XXXXX 형식으로 반환 */
    public String getBusinessRegNoFormatted() {
        return formatBusinessRegNo(businessRegNo);
    }

    /** 법인등록번호를 XXXXXX-XXXXXXX 형식으로 반환 */
    public String getCorporateRegNoFormatted() {
        return formatCorporateRegNo(corporateRegNo);
    }

    private static String formatBusinessRegNo(String v) {
        if (v == null) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 10) return d.substring(0, 3) + "-" + d.substring(3, 5) + "-" + d.substring(5);
        return v;
    }

    private static String formatCorporateRegNo(String v) {
        if (v == null) return null;
        String d = v.replaceAll("[^0-9]", "");
        if (d.length() == 13) return d.substring(0, 6) + "-" + d.substring(6);
        return v;
    }

    public String getStatusLabel() {
        if (status == null) return "-";
        return switch (status) {
            case "ACTIVE"  -> "거래중";
            case "DORMANT" -> "휴면";
            case "CLOSED"  -> "거래종료";
            default        -> status;
        };
    }
}
