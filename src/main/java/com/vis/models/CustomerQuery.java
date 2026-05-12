package com.vis.models;

import java.time.LocalDateTime;

public class CustomerQuery {
    private int queryId;
    private int customerId;
    private int vehicleId;
    private LocalDateTime queryDate;
    private String queryText;
    private String responseText;

    public CustomerQuery() {}

    // Getters and Setters
    public int getQueryId() {
        return queryId;
    }
    public void setQueryId(int queryId) {
        this.queryId = queryId;
    }
    public int getCustomerId() {
        return customerId;
    }
    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }
    public int getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    public LocalDateTime getQueryDate() {
        return queryDate;
    }
    public void setQueryDate(LocalDateTime queryDate) {
        this.queryDate = queryDate;
    }
    public String getQueryText() {
        return queryText;
    }
    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }
    public String getResponseText() {
        return responseText;
    }
    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }
}