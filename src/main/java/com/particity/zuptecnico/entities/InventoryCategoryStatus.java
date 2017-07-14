package com.ntxdev.zuptecnico.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Created by igorlira on 4/19/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class InventoryCategoryStatus implements Serializable {
    public int id;
    public String title;
    public String color;
    public int inventory_category_id;

    public int getColor()
    {
        /*String redBit = color.substring(0, 2);
        String greenBit = color.substring(2, 4);
        String blueBit = color.substring(4, 6);*/

        if(color == null || color.length() == 0)
            return 0;

        return (Integer.parseInt(color.substring(1).toUpperCase(), 16)) | 0xff000000;
    }
}
