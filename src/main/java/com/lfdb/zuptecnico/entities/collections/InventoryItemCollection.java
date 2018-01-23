package com.lfdb.zuptecnico.entities.collections;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.MapCluster;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryItemCollection
{
    public InventoryItem[] items;
    public MapCluster[] clusters;
    public String error;
}
