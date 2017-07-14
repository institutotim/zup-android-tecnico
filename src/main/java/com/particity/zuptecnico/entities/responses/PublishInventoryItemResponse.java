package com.particity.zuptecnico.entities.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.particity.zuptecnico.entities.InventoryItem;

/**
 * Created by igorlira on 3/17/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishInventoryItemResponse {
    public String message;
    public InventoryItem item;
    //public Hashtable<String, ArrayList<String>> error;
    public Object error;
}
