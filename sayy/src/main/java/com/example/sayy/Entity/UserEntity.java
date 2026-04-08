package com.example.sayy.Entity;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

public class UserEntity {

    private String username;

    private String pwd;

    // Spring Security hasRole("ADMIN") 과 매칭되도록 "ADMIN"/"USER" 형태로 저장
    private String role;

    private boolean enabled = true;

    private LocalDateTime createdAt;

    private String employeeNo;

    @Builder
    public UserEntity(String username, String pwd, String role, boolean enabled, String employeeNo) {
        this.username = username;
        this.pwd = pwd;
        this.role = role;
        this.enabled = enabled;
        this.employeeNo = employeeNo;
    }
}