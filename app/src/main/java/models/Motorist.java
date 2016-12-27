package models;

/**
 * Created by papua on 27/12/2016.
 */

public class Motorist {

    private String username;
    private String email_address;
    private String family_name;
    private String given_name;
    private String contact_number;
    private String address;
    private String license_number;
    private String type;

    public Motorist() {  }

    public Motorist(String username, String email_address, String family_name, String given_name, String contact_number, String address, String license_number) {
        this.username = username;
        this.email_address = email_address;
        this.family_name = family_name;
        this.given_name = given_name;
        this.contact_number = contact_number;
        this.address = address;
        this.license_number = license_number;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getFamily_name() {
        return family_name;
    }

    public void setFamily_name(String family_name) {
        this.family_name = family_name;
    }

    public String getGiven_name() {
        return given_name;
    }

    public void setGiven_name(String given_name) {
        this.given_name = given_name;
    }

    public String getContact_number() {
        return contact_number;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLicense_number() {
        return license_number;
    }

    public void setLicense_number(String license_number) {
        this.license_number = license_number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
