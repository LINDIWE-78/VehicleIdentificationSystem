package com.vis.models;

import java.time.LocalDate;
import java.math.BigDecimal;

public class ServiceRecord {
    private int serviceId;
    private int vehicleId;
    private LocalDate serviceDate;
    private String serviceType;
    private String description;
    private BigDecimal cost;

    // Getters and Setters
    public int getServiceId() {
        return serviceId;
    }
    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }
    public int getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    public LocalDate getServiceDate() {
        return serviceDate;
    }
    public void setServiceDate(LocalDate serviceDate) {
        this.serviceDate = serviceDate;
    }
    public String getServiceType() {
        return serviceType;
    }
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getCost() {
        return cost;
    }
    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }
}