package com.sjsu.obdreader.model;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class Vehicle {

    private String rpm;
    private String speed;
    private String latitude;
    private String longitude;
    private String timestamp;
    private String vehicleId;
    private String fuelLevel;
    private String oilLevel;
    private String temperature;
    private String miles;
    private String mileage;
    private String tirePressure;
    private String engineCoolantTemp;

    public Vehicle() {
    }

    public Vehicle(String rpm, String speed, String latitude, String longitude, String timestamp, String vehicleId, String fuelLevel, String oilLevel, String temperature, String miles, String mileage, String tirePressure) {
        this.rpm = rpm;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.vehicleId = vehicleId;
        this.fuelLevel = fuelLevel;
        this.oilLevel = oilLevel;
        this.temperature = temperature;
        this.miles = miles;
        this.mileage = mileage;
        this.tirePressure = tirePressure;
    }

    public String getRpm() {
        return rpm;
    }

    public void setRpm(String rpm) {
        this.rpm = rpm;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(String fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public String getOilLevel() {
        return oilLevel;
    }

    public void setOilLevel(String oilLevel) {
        this.oilLevel = oilLevel;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getMiles() {
        return miles;
    }

    public void setMiles(String miles) {
        this.miles = miles;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(String tirePressure) {
        this.tirePressure = tirePressure;
    }

    public String getEngineCoolantTemp() {
        return engineCoolantTemp;
    }

    public void setEngineCoolantTemp(String engineCoolantTemp) {
        this.engineCoolantTemp = engineCoolantTemp;
    }
}
