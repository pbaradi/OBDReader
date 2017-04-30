package com.sjsu.obdreader.model;

/**
 * Created by pavanibaradi on 4/5/17.
 */
public class VehicleLog {

    private String id;
    private String rpm;
    private String speed;
    private double latitude;
    private double longitude;
    private String engineCoolantTemp;
    private String fuelLevel;
    private String mileage;
    private String miles;
    private String oilLevel;
    private String temperature;
    private String timestamp;
    private String tirePressure;
    private String vehicleId;

    public VehicleLog(){
       super();
    }

    public VehicleLog(String rpm, String speed, double latitude, double longitude, String engineCoolantTemp, String fuelLevel, String mileage, String miles, String oilLevel, String temperature, String timestamp, String tirePressure, String vehicleId) {
        this.rpm = rpm;
        this.speed = speed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.engineCoolantTemp = engineCoolantTemp;
        this.fuelLevel = fuelLevel;
        this.mileage = mileage;
        this.miles = miles;
        this.oilLevel = oilLevel;
        this.temperature = temperature;
        this.timestamp = timestamp;
        this.tirePressure = tirePressure;
        this.vehicleId = vehicleId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getEngineCoolantTemp() {
        return engineCoolantTemp;
    }

    public void setEngineCoolantTemp(String engineCoolantTemp) {
        this.engineCoolantTemp = engineCoolantTemp;
    }

    public String getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(String fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public String getMileage() {
        return mileage;
    }

    public void setMileage(String mileage) {
        this.mileage = mileage;
    }

    public String getMiles() {
        return miles;
    }

    public void setMiles(String miles) {
        this.miles = miles;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTirePressure() {
        return tirePressure;
    }

    public void setTirePressure(String tirePressure) {
        this.tirePressure = tirePressure;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String toString(){
        return "VehicleLog [rpm = "+ rpm
        +"speed = "+speed
        +"latitude = "+latitude
        +"longitude = "+longitude
        +"timestamp = "+timestamp
        +"vehicleId = "+vehicleId
        +"fuelLevel = "+fuelLevel
        +"oilLevel = "+oilLevel
        +"temperature = "+temperature
        +"miles = "+miles
        +"mileage = "+mileage
        +"tirePressure = "+tirePressure+"]";
    }
}
