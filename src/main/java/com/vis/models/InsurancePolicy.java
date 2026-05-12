package com.vis.models;

import java.time.LocalDate;

public class InsurancePolicy {
    private int policyId;
    private int vehicleId;
    private String insuranceCompany;
    private String policyNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private String coverageDetails;

    public InsurancePolicy() {}

    // Getters and Setters
    public int getPolicyId() {
        return policyId;
    }
    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }
    public int getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    public String getInsuranceCompany() {
        return insuranceCompany;
    }
    public void setInsuranceCompany(String insuranceCompany) {
        this.insuranceCompany = insuranceCompany;
    }
    public String getPolicyNumber() {
        return policyNumber; }
    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    public String getCoverageDetails() {
        return coverageDetails;
    }
    public void setCoverageDetails(String coverageDetails) {
        this.coverageDetails = coverageDetails;
    }
}