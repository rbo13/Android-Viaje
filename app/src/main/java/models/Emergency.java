package models;

/**
 * Created by richard on 01/01/2017.
 */

public class Emergency {

    private String email;
    private String status;
    private double latitude;
    private double longitude;
    private String description;
    private String safezoneType;


    public Emergency() {  }

    public Emergency(String email, String status, double latitude, double longitude, String description, String safezoneType) {
        this.email = email;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
        this.safezoneType = safezoneType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSafezoneType() {
        return safezoneType;
    }

    public void setSafezoneType(String safezoneType) {
        this.safezoneType = safezoneType;
    }
}
