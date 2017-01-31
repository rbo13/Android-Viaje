package models;


/**
 * Created by papua on 27/12/2016.
 */

public class Motorist {

    private String motorist_id;
    private String username;
    private String email_address;
    private String family_name;
    private String given_name;
    private String contact_number;
    private String address;
    private String license_number;
    private String vehicle_information_model_year;
    private String vehicle_information_plate_number;
    private String vehicle_information_vehicle_type;
    private String type;
    private String profile_pic;


    public Motorist() {  }

    public Motorist(String motorist_id, String username, String email_address, String family_name, String given_name, String contact_number, String address, String license_number, String vehicle_information_model_year, String vehicle_information_plate_number, String vehicle_information_vehicle_type, String type, String profile_pic, String key) {
        this.motorist_id = motorist_id;
        this.username = username;
        this.email_address = email_address;
        this.family_name = family_name;
        this.given_name = given_name;
        this.contact_number = contact_number;
        this.address = address;
        this.license_number = license_number;
        this.vehicle_information_model_year = vehicle_information_model_year;
        this.vehicle_information_plate_number = vehicle_information_plate_number;
        this.vehicle_information_vehicle_type = vehicle_information_vehicle_type;
        this.type = type;
        this.profile_pic = profile_pic;
    }

    public String getMotorist_id() {
        return motorist_id;
    }

    public void setMotorist_id(String motorist_id) {
        this.motorist_id = motorist_id;
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

    public String getVehicle_information_model_year() {
        return vehicle_information_model_year;
    }

    public void setVehicle_information_model_year(String vehicle_information_model_year) {
        this.vehicle_information_model_year = vehicle_information_model_year;
    }

    public String getVehicle_information_plate_number() {
        return vehicle_information_plate_number;
    }

    public void setVehicle_information_plate_number(String vehicle_information_plate_number) {
        this.vehicle_information_plate_number = vehicle_information_plate_number;
    }

    public String getVehicle_information_vehicle_type() {
        return vehicle_information_vehicle_type;
    }

    public void setVehicle_information_vehicle_type(String vehicle_information_vehicle_type) {
        this.vehicle_information_vehicle_type = vehicle_information_vehicle_type;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }
}
