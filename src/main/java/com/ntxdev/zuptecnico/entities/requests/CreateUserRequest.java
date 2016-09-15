package com.ntxdev.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by igorlira on 7/23/15.
 */
public class CreateUserRequest {
    private String email;
    private boolean generatePassword;
    private String name;
    private String address;
    private String addressAdditional;
    private String district;
    private String city;
    private String postalCode;
    private String phone;
    private String document;

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("generate_password")
    public boolean isGeneratePassword() {
        return generatePassword;
    }

    @JsonProperty("generate_password")
    public void setGeneratePassword(boolean generatePassword) {
        this.generatePassword = generatePassword;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("address")
    public String getAddress() {
        return address;
    }

    @JsonProperty("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonProperty("address_additional")
    public String getAddressAdditional() {
        return addressAdditional;
    }

    @JsonProperty("address_additional")
    public void setAddressAdditional(String addressAdditional) {
        this.addressAdditional = addressAdditional;
    }

    @JsonProperty("district")
    public String getDistrict() {
        return district;
    }

    @JsonProperty("district")
    public void setDistrict(String district) {
        this.district = district;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }


    @JsonProperty("postal_code")
    public String getPostalCode() {
        return postalCode;
    }

    @JsonProperty("postal_code")
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @JsonProperty("phone")
    public String getPhone() {
        return phone;
    }

    @JsonProperty("phone")
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @JsonProperty("document")
    public String getDocument() {
        return document;
    }

    @JsonProperty("document")
    public void setDocument(String document) {
        this.document = document;
    }
}
