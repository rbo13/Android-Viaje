package models;

/**
 * Created by richard on 01/01/2017.
 */

public class Emergency {

    private String email;
    private String status;
    private String latitude;
    private String longitude;
    private String description;


    public Emergency() {  }

    public Emergency(String email, String status, String latitude, String longitude, String description) {
        this.email = email;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
