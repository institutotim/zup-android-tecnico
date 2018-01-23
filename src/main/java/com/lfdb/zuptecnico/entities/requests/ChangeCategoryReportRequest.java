package com.lfdb.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by Renan on 21/10/2015.
 */
public class ChangeCategoryReportRequest {
    @JsonProperty("new_category_id")
    private int newCategoryId;
    @JsonProperty("new_status_id")
    private int newStatusId;

    @JsonGetter("new_category_id")
    public int getNewCategoryId() {
        return newCategoryId;
    }

    @JsonSetter("new_category_id")
    public void setNewCategoryId(int newCategoryId) {
        this.newCategoryId = newCategoryId;
    }

    @JsonGetter("new_status_id")
    public int getNewStatusId() {
        return newStatusId;
    }

    @JsonSetter("new_status_id")
    public void setNewStatusId(int newStatusId) {
        this.newStatusId = newStatusId;
    }
}
