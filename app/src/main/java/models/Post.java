package models;

import java.util.Map;

/**
 * Created by richard on 04/02/2017.
 */

public class Post {

    public Map<String, Comment> comments;
    private double lat;
    private double lng;
    private String text;
    private Long timestamp;
    private Motorist user;


    public Post() {  }

    public Map<String, Comment> getComments() {
        return comments;
    }

    public void setComments(Map<String, Comment> comments) {
        this.comments = comments;
    }

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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Motorist getUser() {
        return user;
    }

    public void setUser(Motorist user) {
        this.user = user;
    }

    public static class Comment {

        String text;
        Long timestamp;
        Motorist user;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Long timestamp) {
            this.timestamp = timestamp;
        }

        public Motorist getUser() {
            return user;
        }

        public void setUser(Motorist user) {
            this.user = user;
        }
    }
}
