package com.ntxdev.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.ntxdev.zuptecnico.entities.ImageItem;
import com.ntxdev.zuptecnico.entities.ReportItem;

import java.util.HashMap;

/**
 * Created by igorlira on 7/23/15.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateReportItemRequest {
    @JsonProperty("category_id")
    private int categoryId;
    private String description;
    private String reference;
    private ImageItem[] images;
    private int userId;
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

    @JsonGetter("images")
    public ImageItem[] getImages() {
        return images;
    }

    @JsonSetter("images")
    public void setImages(ImageItem[] images) {
        this.images = images;
    }

    @JsonGetter("user_id")
    public int getUserId() {
        return userId;
    }

    @JsonSetter("user_id")
    public void setUserId(int userId) {
        this.userId = userId;
    }
}
