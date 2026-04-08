package com.example.sayy.Entity;

public enum EmploymentStatus {
    ACTIVE("재직"),
    LEAVE("휴직"),
    TERMINATED("퇴사");

    private final String label;

    EmploymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

