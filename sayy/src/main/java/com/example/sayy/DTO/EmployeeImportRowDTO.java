package com.example.sayy.DTO;

import java.util.ArrayList;
import java.util.List;

public class EmployeeImportRowDTO {
    private int rowNum; // Excel row number (1-based)
    private String empNo;
    private String name;
    private String employmentType; // REGULAR/CONTRACT/FREELANCER
    private String employmentStatus; // ACTIVE/LEAVE/TERMINATED
    private String fourInsured; // true/false/blank
    private String taxScheme; // SALARY_WITHHOLDING/BUSINESS_INCOME_3_3/blank
    private String contractEndDate; // yyyy-MM-dd/blank
    private String hireDate; // yyyy-MM-dd/blank
    private String groupHireDate; // yyyy-MM-dd/blank
    private String positionName;
    private String identificationNumber;
    private String age;
    private String yearsOfService;
    private String email;
    private String birthDate; // yyyy-MM-dd/blank
    private String terminationDate; // yyyy-MM-dd/blank
    private String username; // required
    private String gender;

    private final List<String> errors = new ArrayList<>();

    public boolean isValid() {
        return errors.isEmpty();
    }

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getEmpNo() {
        return empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getFourInsured() {
        return fourInsured;
    }

    public void setFourInsured(String fourInsured) {
        this.fourInsured = fourInsured;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getTaxScheme() {
        return taxScheme;
    }

    public void setTaxScheme(String taxScheme) {
        this.taxScheme = taxScheme;
    }

    public String getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(String contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public String getHireDate() {
        return hireDate;
    }

    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
    }

    public String getGroupHireDate() {
        return groupHireDate;
    }

    public void setGroupHireDate(String groupHireDate) {
        this.groupHireDate = groupHireDate;
    }

    public String getPositionName() {
        return positionName;
    }

    public void setPositionName(String positionName) {
        this.positionName = positionName;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getYearsOfService() {
        return yearsOfService;
    }

    public void setYearsOfService(String yearsOfService) {
        this.yearsOfService = yearsOfService;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getErrors() {
        return errors;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void addError(String message) {
        if (message != null && !message.isBlank()) {
            errors.add(message);
        }
    }
}

