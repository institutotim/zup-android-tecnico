package com.particity.zuptecnico.fragments.inventory;

import android.view.View;

import com.particity.zuptecnico.entities.InventoryItem;

/**
 * Created by Renan on 14/01/2016.
 */
public interface CreateInventoryPublisher {
    public View validateSection(View error);
    public InventoryItem createItemFromData(InventoryItem item);
}
