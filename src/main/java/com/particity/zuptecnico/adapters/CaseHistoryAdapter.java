package com.particity.zuptecnico.adapters;

import android.content.Context;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.collections.CaseHistoryCollection;
import com.particity.zuptecnico.entities.collections.CaseHistoryItem;
import com.particity.zuptecnico.util.Utilities;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Renan on 05/11/2015.
 */
public class CaseHistoryAdapter extends BaseAdapter
        implements Callback<CaseHistoryCollection> {
    int itemId;
    Context context;
    List<CaseHistoryItem> items;
    SparseArray<View> viewCache;
    View loadingView;
    ObjectMapper mapper;

    boolean areMoreItemsAvailable;

    public CaseHistoryAdapter(Context context, Case item, CaseHistoryItem[] history) {
        this.itemId = item.id;
        this.context = context;
        this.viewCache = new SparseArray<>();
        this.items = new ArrayList<>();
        this.mapper = new ObjectMapper();

        if (history != null) {
            for (int i = 0; i < history.length; i++) {
                this.items.add(history[i]);
            }

            this.areMoreItemsAvailable = false;
        } else {
            this.resetAndLoadFirstPage();
        }
    }

    void resetAndLoadFirstPage() {
        this.areMoreItemsAvailable = true;
        this.items.clear();
        this.notifyDataSetInvalidated();

        Zup.getInstance().getService().retrieveCaseItemHistory(itemId, this);
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        if (this.areMoreItemsAvailable)
            return items.size() + 1;
        else
            return items.size();
    }

    @Override
    public CaseHistoryItem getItem(int i) {
        if (areMoreItemsAvailable && getLoadingItemIndex() == i)
            return null;
        else
            return items.get(i);
    }

    int getLoadingItemIndex() {
        return this.items.size();
    }

    @Override
    public long getItemId(int i) {
        if (getItem(i) == null)
            return -1;

        return getItem(i).id;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        if (areMoreItemsAvailable && i == getLoadingItemIndex()) {
            if (loadingView != null)
                return loadingView;
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                loadingView = inflater.inflate(R.layout.listview_loadingmore_small, viewGroup, false);

                return loadingView;
            }
        } else {
            CaseHistoryItem item = getItem(i);

            if (viewCache.get(item.id) != null)
                return viewCache.get(item.id);
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                View view = inflater.inflate(R.layout.report_history_item, viewGroup, false);
                fillData(view, item);

                return view;
            }
        }
    }

    void fillData(View view, CaseHistoryItem item) {
        TextView txtDate = (TextView) view.findViewById(R.id.history_date);
        TextView txtText = (TextView) view.findViewById(R.id.history_text);

        txtDate.setText(Utilities.formatIsoDateAndTime(item.createdAt));
        txtText.setText(Html.fromHtml(item.getHtml(context, mapper)));
    }

    @Override
    public void success(CaseHistoryCollection result, Response response) {
        this.areMoreItemsAvailable = false;

        for (int i = 0; i < result.histories.length; i++) {
            this.items.add(result.histories[i]);
        }

        if (Zup.getInstance().getCaseItemService().hasCaseItem(itemId)) {
            CaseHistoryItem[] histories = new CaseHistoryItem[items.size()];
            histories = items.toArray(histories);
            Zup.getInstance().getCaseItemService().setCaseItemHistory(itemId, histories);
        }

        this.notifyDataSetInvalidated();
    }

    @Override
    public void failure(RetrofitError error) {

    }
}