package com.lfdb.zuptecnico.adapters;

import android.content.Context;

import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryItem;

import java.util.List;

public class OfflineInventoryAdapter extends InventoryItemsAdapter {
    public OfflineInventoryAdapter(Context context) {
        super(context);
    }

    public OfflineInventoryAdapter(Context context, int categoryId) {
        super(context, categoryId);
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public InventoryItem getItem(int i) {
        return mItems.get(i);
    }

    @Override
    protected void loadAnotherPage() {
        mMoreItemsAvailable = false;
        mItems.clear();
        List<InventoryItem> itemIterator = Zup.getInstance().getInventoryItemService().getInventoryItems();
        int size = itemIterator.size();
        if (size == 0) {
            if (getListener() != null) {
                getListener().onEmptyResultsLoaded();
            }
            return;
        }
        for (int i = 0; i < size; i++) {
            mItems.add(itemIterator.get(i));
        }
        if (getListener() != null) {
            getListener().onItemsLoaded();
        }
        notifyDataSetChanged();
    }

    @Override
    public void removeSelection() {
        super.removeSelection();
        reset();
    }

    @Override
    public void reset() {
        super.reset();
        loadAnotherPage();
    }
}
