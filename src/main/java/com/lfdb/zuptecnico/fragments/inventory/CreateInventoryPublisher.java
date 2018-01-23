package com.lfdb.zuptecnico.fragments.inventory;

import android.view.View;

import com.lfdb.zuptecnico.entities.InventoryItem;

/**
 * Created by Renan on 14/01/2016.
 */
public interface CreateInventoryPublisher {
    public View validateSection(View error);
    public InventoryItem createItemFromData(InventoryItem item);
}
