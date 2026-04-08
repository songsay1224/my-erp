package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EmployeeOrgAssignmentEntity {
    private Long id;
    private String empNo;
    private Long orgUnitId;
    private String titleName;
    private Boolean orgLeader;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}

