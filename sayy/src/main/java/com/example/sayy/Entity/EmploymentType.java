package com.example.sayy.Entity;

public enum EmploymentType {
    REGULAR("정규직"),
    CONTRACT("계약직"),
    FREELANCER("프리랜서");

    private final String label;

    EmploymentType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

