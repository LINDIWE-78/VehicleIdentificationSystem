package com.vis.models;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Claim {
    private int claimId;
    private int policyId;
    private LocalDate claimDate;
    private BigDecimal claimAmount;
    private String status; // Pending, Approved, Rejected

    // Getters and Setters
    public int getClaimId() {
        return claimId;
    }
    public void setClaimId(int claimId) {
        this.claimId = claimId;
    }
    public int getPolicyId() {
        return policyId;
    }
    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }
    public LocalDate getClaimDate() {
        return claimDate;
    }
    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }
    public BigDecimal getClaimAmount() {
        return claimAmount;
    }
    public void setClaimAmount(BigDecimal claimAmount) {
        this.claimAmount = claimAmount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status; }
}