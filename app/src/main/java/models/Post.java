package models;

/**
 * Created by richard on 04/02/2017.
 */

public class Post {

    private double lat;
    private double lng;
    private String text;
    private String timestamp;
    private Motorist user;

    public Post() {  }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Motorist getUser() {
        return user;
    }

    public void setUser(Motorist motorist) {
        this.user = motorist;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
