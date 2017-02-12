package models;

/**
 * Created by richard on 10/02/2017.
 */

public class Advertisement {

    public String img;
    public String text;
    public Long timestamp;
    public String title;
    public Safezone user;
    public String username;

    public String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Safezone getUser() {
        return user;
    }

    public void setUser(Safezone user) {
        this.user = user;
    }
}
