package com.particity.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.particity.zuptecnico.entities.InventoryCategory;

/**
 * Created by igorlira on 3/3/14.
 */
public class SingleInventoryCategoryCollection
{
    public InventoryCategory category;

    @JsonIgnore(true)
    public String etag;
}
