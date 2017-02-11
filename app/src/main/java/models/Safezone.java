package models;

/**
 * Created by b0kn0y on 1/18/2017.
 */

public class Safezone {

    Address address;
    String contact_number;
    String email_address;
    String owner;
    String service_information_type;
    String shop_name;
    String type;
    String username;
    String key;


    public Safezone() {  }

    public Safezone(Address address, String contact_number, String email_address, String owner, String service_information_type, String shop_name, String type, String username) {
        this.address = address;
        this.contact_number = contact_number;
        this.email_address = email_address;
        this.owner = owner;
        this.service_information_type = service_information_type;
        this.shop_name = shop_name;
        this.type = type;
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setContact_number(String contact_number) {
        this.contact_number = contact_number;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setService_information_type(String service_information_type) {
        this.service_information_type = service_information_type;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Address getAddress() {
        return address;
    }

    public String getContact_number() {
        return contact_number;
    }

    public String getEmail_address() {
        return email_address;
    }

    public String getOwner() {
        return owner;
    }

    public String getService_information_type() {
        return service_information_type;
    }

    public String getShop_name() {
        return shop_name;
    }

    public String getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public static class Address {

        String address;
        double lat;
        double lng;

        public String getAddress() {

            return address;
        }

        public double getLat() {

            return lat;
        }

        public double getLng() {

            return lng;
        }
    }
}
