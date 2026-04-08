package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ProjectMemberEntity {
    private Long id;
    private Long projectId;
    private String empNo;
    private String role;
    private Boolean isLeader;
    private LocalDateTime createdAt;

    // 조회 시 함께 로딩
    private String employeeName;
    private String positionName;
}
