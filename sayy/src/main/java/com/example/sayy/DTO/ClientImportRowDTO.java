package com.example.sayy.DTO;

import java.io.Serializable;

public class ClientImportRowDTO implements Serializable {
    private int rowNum;
    private String clientType;
    private String businessRegNo;
    private String name;
    private String ceoName;
    private String corporateRegNo;
    private String region;
    private String address;
    private String addressDetail;
    private String phone;
    private String homepage;
    private String errorMessage;

    public int getRowNum() { return rowNum; }
    public void setRowNum(int rowNum) { this.rowNum = rowNum; }
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    public String getBusinessRegNo() { return businessRegNo; }
    public void setBusinessRegNo(String businessRegNo) { this.businessRegNo = businessRegNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCeoName() { return ceoName; }
    public void setCeoName(String ceoName) { this.ceoName = ceoName; }
    public String getCorporateRegNo() { return corporateRegNo; }
    public void setCorporateRegNo(String corporateRegNo) { this.corporateRegNo = corporateRegNo; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAddressDetail() { return addressDetail; }
    public void setAddressDetail(String addressDetail) { this.addressDetail = addressDetail; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getHomepage() { return homepage; }
    public void setHomepage(String homepage) { this.homepage = homepage; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public boolean hasError() { return errorMessage != null && !errorMessage.isBlank(); }
}
