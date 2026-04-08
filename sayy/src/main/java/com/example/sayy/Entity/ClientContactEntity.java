package com.example.sayy.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ClientContactEntity {
    private Long id;
    private Long clientId;
    private String name;
    private String department;
    private String position;
    private String phone;
    private String email;
    private Boolean isPrimary;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
