package com.particity.zuptecnico.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.InventoryItem;
import com.particity.zuptecnico.entities.collections.SingleInventoryItemCollection;

import java.util.List;

/**
 * Created by Renan on 23/01/2016.
 */
public class InventoryItemDownloader extends AsyncTask<Void, Void, Boolean> {
    public interface Listener {
        void onProgress(float progress);

        void onFinished();

        void onError();
    }

    private Listener listener;

    private List<InventoryItem> selectedItems;
    private int size;
    private int downloadedItems;

    private Context mContext;

    public InventoryItemDownloader(Context context, List<InventoryItem> selectedItems, Listener listener) {
        this.mContext = context;
        this.selectedItems = selectedItems;
        size = selectedItems.size();
        downloadedItems = 0;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        // Get item info
        for (int index = 0; index < size; index++) {
            InventoryItem mItem = selectedItems.get(index);
            SingleInventoryItemCollection result = Zup.getInstance().getService()
                    .getInventoryItem(mItem.inventory_category_id, mItem.id);


            if (result == null || result.item == null) {
                continue;
            }

            selectedItems.set(index, result.item);
            downloadedItems++;
            this.publishProgress();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

        float progress = (downloadedItems + 1) / 2.f * size;
        if (this.listener != null)
            this.listener.onProgress(progress);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        super.onPostExecute(success);
        if (success) {
            for (int index = 0; index < size; index++) {
                Zup.getInstance().getInventoryItemService().addInventoryItem(selectedItems.get(index));
                if (listener != null) {
                    float progress = (size + index + 1) / 2.f * size;
                    listener.onProgress(progress);
                }
            }

            if (listener != null) {
                listener.onProgress(1.0f);
                listener.onFinished();
            }
        } else {
            if (listener != null) {
                listener.onError();
            }
        }
    }
}
