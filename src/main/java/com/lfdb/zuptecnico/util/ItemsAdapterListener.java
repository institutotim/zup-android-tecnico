package com.lfdb.zuptecnico.util;

/**
 * Created by Renan on 03/12/2015.
 */
public interface ItemsAdapterListener {
    void onItemsLoaded();
    void onEmptyResultsLoaded();
    void onNetworkError();
}
