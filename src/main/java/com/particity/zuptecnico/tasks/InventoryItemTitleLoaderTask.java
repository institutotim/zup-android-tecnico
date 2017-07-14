package com.particity.zuptecnico.tasks;

import android.os.AsyncTask;
import android.widget.TextView;

import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 05/02/2016.
 */
public class InventoryItemTitleLoaderTask extends AsyncTask<Integer, Void, List<InventoryItem>> {
    TextView textView;
    String prefix;

    List<Integer> itemsId;

    public InventoryItemTitleLoaderTask(TextView textView, String prefix, List<Integer> itemsId) {
        this.textView = textView;
        this.prefix = prefix;
        this.itemsId = itemsId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        textView.setText(prefix + "Carregando...");
    }

    @Override
    protected List<InventoryItem> doInBackground(Integer... integers) {
        List<InventoryItem> items = new ArrayList<>();
        int size = itemsId == null ? 0 : itemsId.size();
        try {
            for (int index = 0;index<size;index++) {
                items.add(Zup.getInstance().getService().getInventoryItemTitle(itemsId.get(index)).item);
            }
        } catch (RuntimeException error) {
            error.printStackTrace();
        }
        return items;
    }

    @Override
    protected void onPostExecute(List<InventoryItem> result) {
        super.onPostExecute(result);
        int size = result.size();
        String titles = "";
        for (int index = 0; index < size; index++) {
            titles += result.get(index).title;
            if (index != size -1) {
                titles += "\n";
            }
        }
            textView.setText(prefix + titles);
    }
}
