package com.vis.models;

public class Vehicle {
    private int vehicleId;
    private String regNumber;
    private String make;
    private String model;
    private int year;
    private int ownerId;
    private String ownerName;  // from view

    public Vehicle() {}

    public Vehicle(int vehicleId, String regNumber, String make, String model, int year, int ownerId) {
        this.vehicleId = vehicleId;
        this.regNumber = regNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.ownerId = ownerId;
    }

    // Getters and Setters
    public int getVehicleId() {
        return vehicleId;
    }
    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }
    public String getRegNumber() {
        return regNumber;
    }
    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }
    public String getMake() {
        return make;
    }
    public void setMake(String make) {
        this.make = make;
    }
    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }
    public int getYear() {
        return year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
    public String getOwnerName() {
        return ownerName;
    }
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}