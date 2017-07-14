package com.ntxdev.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ntxdev.zuptecnico.entities.InventoryCategory;

/**
 * Created by igorlira on 3/3/14.
 */
public class InventoryCategoryCollection
{
    public InventoryCategory[] categories;

    @JsonIgnore(true)
    public String etag;
}
