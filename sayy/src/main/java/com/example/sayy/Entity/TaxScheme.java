package com.example.sayy.Entity;

public enum TaxScheme {
    SALARY_WITHHOLDING("근로소득(원천징수)"),
    BUSINESS_INCOME_3_3("사업소득(3.3%)");

    private final String label;

    TaxScheme(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

