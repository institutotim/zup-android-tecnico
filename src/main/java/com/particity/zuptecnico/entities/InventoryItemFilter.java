package com.particity.zuptecnico.entities;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by igorlira on 5/25/15.
 */
public class InventoryItemFilter implements Serializable
{
    public int fieldId;
    public String type;
    public Serializable value1;
    public Serializable value2;
    public boolean isArray;

    public void serialize(Map<String, Object> map)
    {
        map.put("fields[" + fieldId + "][" + type + "]", value1);
    }
}
