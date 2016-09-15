package com.ntxdev.zuptecnico.tasks;

import android.os.AsyncTask;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.ntxdev.zuptecnico.util.ItemsAdapterListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 06/02/2016.
 */
public class InventoryItemLoaderTask extends AsyncTask<Integer, Void, InventoryItem> {
  TextView textView;
  String prefix;
  Integer itemId;
  ItemLoadedListener listener;

  public interface ItemLoadedListener {
    void onEmptyResultLoaded();

    void onItemLoaded(InventoryItem item);
  }

  public InventoryItemLoaderTask(ItemLoadedListener listener, TextView textView, String prefix,
      Integer itemId) {
    this.textView = textView;
    this.prefix = prefix;
    this.itemId = itemId;
    this.listener = listener;
  }

  @Override protected void onPreExecute() {
    super.onPreExecute();
    if (textView != null) {
      textView.setText(prefix + textView.getContext().getString(R.string.loading));
    }
  }

  @Override protected InventoryItem doInBackground(Integer... integers) {
    try {
      SingleInventoryItemCollection item = Zup.getInstance().getService().getInventoryItem(itemId);
      return item != null ? item.item : null;
    } catch (RuntimeException error) {
      error.printStackTrace();
    }
    return null;
  }

  @Override protected void onPostExecute(InventoryItem result) {
    super.onPostExecute(result);
    textView.setText(prefix);
    if (listener == null) {
      return;
    }
    if (result == null) {
      listener.onEmptyResultLoaded();
    } else {
      listener.onItemLoaded(result);
    }
  }
}
