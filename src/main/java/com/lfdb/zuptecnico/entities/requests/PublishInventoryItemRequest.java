package com.particity.zuptecnico.entities.requests;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Hashtable;

/**
 * Created by igorlira on 3/17/14.
 */
public class PublishInventoryItemRequest {
    public Hashtable<String, Object> data;
    public Integer inventory_status_id;

    public PublishInventoryItemRequest() {
    }

    @JsonGetter("data")
    public Hashtable<String, Object> getData() {
        return data;
    }

    @JsonSetter("data")
    public void setData(Hashtable<String, Object> data) {
        this.data = data;
    }

    @JsonGetter("inventory_status_id")
    public Integer getInventory_status_id() {
        return inventory_status_id;
    }

    @JsonSetter("inventory_status_id")
    public void setInventory_status_id(Integer inventory_status_id) {
        this.inventory_status_id = inventory_status_id;
    }
}
