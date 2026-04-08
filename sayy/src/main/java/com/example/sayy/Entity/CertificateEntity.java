package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CertificateEntity {
    private Long id;
    private String empNo;
    private String certName;
    private String issuer;
    private LocalDate acquiredDate;
    private LocalDate expiryDate;   // null = 영구
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
