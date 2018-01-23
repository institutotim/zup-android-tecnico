package com.particity.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.particity.zuptecnico.entities.ImageItem;
import java.util.HashMap;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateArbitraryReportItemRequest {
    private double latitude;
    private double longitude;
    @JsonProperty("category_id")
    private int categoryId;
    private String description;
    private String reference;
    private String address;
    private String number;
    private String district;
    @JsonProperty("postal_code")
    private String postalCode;
    private String city;
    private String state;
    private String country;
    private ImageItem[] images;
    private Integer userId;
    @JsonProperty("status_id")
    private String statusId;
    @JsonProperty("custom_fields")
    private HashMap<Integer, String> customFields;

    @JsonProperty("case_conductor_id")
    private Integer caseConductorId;

    @JsonSetter("case_conductor_id")
    public void setCaseConductorId(Integer caseConductorId) {
        this.caseConductorId = caseConductorId;
    }

    @JsonGetter("case_conductor_id")
    public Integer getCaseConductorId() {
        return caseConductorId;
    }

    @JsonGetter("custom_fields")
    public HashMap<Integer, String> getCustomFields() {
        return customFields;
    }

    @JsonSetter("custom_fields")
    public void setCustomFields(HashMap<Integer, String> customFields) {
        this.customFields = customFields;
    }

    @JsonGetter("status_id")
    public String getStatusId() {
        return statusId;
    }

    @JsonSetter("status_id")
    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    @JsonGetter("number")
    public String getNumber() {
        return number;
    }

    @JsonSetter("number")
    public void setNumber(String number) {
        this.number = number;
    }

    @JsonGetter("postal_code")
    public String getPostalCode() {
        return postalCode;
    }

    @JsonSetter("postal_code")
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @JsonGetter("latitude")
    public double getLatitude() {
        return latitude;
    }

    @JsonSetter("latitude")
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @JsonGetter("longitude")
    public double getLongitude() {
        return longitude;
    }

    @JsonSetter("longitude")
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @JsonGetter("category_id")
    public int getCategoryId() {
        return categoryId;
    }

    @JsonSetter("category_id")
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonGetter("reference")
    public String getReference() {
        return reference;
    }

    @JsonSetter("reference")
    public void setReference(String reference) {
        this.reference = reference;
    }

    @JsonGetter("address")
    public String getAddress() {
        return address;
    }

    @JsonSetter("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @JsonGetter("district")
    public String getDistrict() {
        return district;
    }

    @JsonSetter("district")
    public void setDistrict(String district) {
        this.district = district;
    }

    @JsonGetter("city")
    public String getCity() {
        return city;
    }

    @JsonSetter("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonGetter("state")
    public String getState() {
        return state;
    }

    @JsonSetter("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonGetter("country")
    public String getCountry() {
        return country;
    }

    @JsonSetter("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonGetter("images")
    public ImageItem[] getImages() {
        return images;
    }

    @JsonSetter("images")
    public void setImages(ImageItem[] images) {
        this.images = images;
    }

    @JsonGetter("user_id")
    public Integer getUserId() {
        return userId;
    }

    @JsonSetter("user_id")
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
