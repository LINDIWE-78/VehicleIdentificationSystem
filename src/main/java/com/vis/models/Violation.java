package com.vis.models;

import java.time.LocalDate;
import java.math.BigDecimal;

public class Violation {
    private int violationId;
    private int vehicleId;
    private LocalDate violationDate;
    private String violationType;
    private BigDecimal fineAmount;
    private String status; // Paid, Unpaid

    // Getters and Setters
    public int getViolationId() {
        return violationId;
    }
    public void setViolationId(int violationId) {
        this.violationId = violationId;
    }
    public int getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    public LocalDate getViolationDate() {
        return violationDate;
    }
    public void setViolationDate(LocalDate violationDate) {
        this.violationDate = violationDate;
    }
    public String getViolationType() {
        return violationType;
    }
    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }
    public BigDecimal getFineAmount() {
        return fineAmount;
    }
    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}