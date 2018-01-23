package com.particity.zuptecnico.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.collections.CaseCollection;
import com.particity.zuptecnico.util.Utilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CasesAdapter extends BaseAdapter implements Callback<CaseCollection> {
    public static final int ORDER_BY_NAME = 1;
    public static final int ORDER_BY_LAST_EDITION = 2;
    public static final int ORDER_BY_CREATION = 3;

    private SparseArray<ViewGroup> viewCache;
    private boolean moreItemsAvailable;
    private View loadingView;

    private List<Case> items;
    private List<Case> allItems;
    private int pageId;
    private int flowId;
    private int orderBy;
    private String query;

    private Context context;
    private CasesAdapterListener listener;
    String filter;

    public void orderBy(int orderBy) {
        this.orderBy = orderBy;
        notifyDataSetChanged();
    }

    @Override
    public void success(CaseCollection caseCollection, Response response) {
        Case[] cases = caseCollection.cases;
        if (cases == null) {
            setMoreItemsAvailable(false);
            if (listener != null)
                listener.onNetworkError();
            return;
        }

        if (cases.length == 0) {
            if (pageId == 1) {
                listener.onEmptyResultsLoaded();
                return;
            }
        }

        for (int i = 0; i < cases.length; i++) {
            allItems.add(cases[i]);
        }

        pageId = pageId + 1;

        if (listener != null) {
            listener.onCasesLoaded();
        }

        setMoreItemsAvailable(cases.length > 0);
        filterItems();
    }

    protected void filterItems() {
        items.clear();
        if (filter == null || filter.isEmpty()) {
            items.addAll(allItems);
        } else {
            for (int index = 0; index < allItems.size(); index++) {
                Case flowCase = allItems.get(index);
                if (flowCase.getStatus().equals(filter)) {
                    items.add(flowCase);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setFilter(String filter) {
        this.filter = filter;
        filterItems();
    }

    @Override
    public void notifyDataSetChanged() {
        sortItems();
        super.notifyDataSetChanged();
    }

    private void sortItems() {
        Comparator<Case> comparator;
        switch (orderBy) {
            case ORDER_BY_NAME:
                comparator = new Comparator<Case>() {
                    @Override
                    public int compare(Case firstCase, Case secondCase) {
                        return firstCase.id - secondCase.id;
                    }
                };
                break;
            case ORDER_BY_CREATION:
                comparator = new Comparator<Case>() {
                    @Override
                    public int compare(Case firstCase, Case secondCase) {
                        return firstCase.createdAt.compareTo(secondCase.createdAt);
                    }
                };
                break;
            case ORDER_BY_LAST_EDITION:
            default:
                comparator = new Comparator<Case>() {
                    @Override
                    public int compare(Case firstCase, Case secondCase) {
                        return secondCase.updatedAt.compareTo(firstCase.updatedAt);
                    }
                };
                break;
        }
        Collections.sort(items, comparator);
    }

    @Override
    public void failure(RetrofitError error) {
        Log.e("RETROFIT", "Could not load cases list", error);
        listener.onNetworkError();
    }

    public interface CasesAdapterListener {
        void onCasesLoaded();

        void onEmptyResultsLoaded();

        void onNetworkError();
    }

    public void setListener(CasesAdapterListener listener) {
        this.listener = listener;
    }

    protected CasesAdapterListener getListener() {
        return this.listener;
    }

    public CasesAdapter(Context context) {
        this.context = context;
        query = "";
        items = new ArrayList<>();
        allItems = new ArrayList<>();
        viewCache = new SparseArray<>();
        moreItemsAvailable = false;
        pageId = 1;
        flowId = -1;
    }

    public void setQuery(String query) {
        this.query = query;
        load();
    }

    public void setFlowId(int flowId) {
        this.flowId = flowId;
        reset();
    }

    public int getFlowId() {
        return flowId;
    }

    public void load() {
        if (!Utilities.isConnected(getContext())) {
            if (listener != null) {
                listener.onNetworkError();
                return;
            }
        }
        pageId = 1;
        loadMore();
    }

    public void reset() {
        items.clear();
        allItems.clear();
        notifyDataSetInvalidated();
        moreItemsAvailable = false;
        load();
    }

    public void clear() {
        items.clear();
        allItems.clear();
        notifyDataSetInvalidated();
        moreItemsAvailable = false;
    }

    public void setMoreItemsAvailable(boolean value) {
        boolean oldValue = this.moreItemsAvailable;
        this.moreItemsAvailable = value;
        if (oldValue != value)
            this.notifyDataSetChanged();
    }

    @Override
    public Case getItem(int i) {
        if (moreItemsAvailable && i == getCount() - 1)
            return null;

        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        Case item = getItem(i);
        if (item != null)
            return item.id;
        else
            return 0;
    }

    @Override
    public int getCount() {
        int count = items.size();
        if (moreItemsAvailable) {
            count++;
        }

        return count;
    }

    void loadMore() {
        try {
            if (query.isEmpty()) {
                if (flowId == -1) {
                    Zup.getInstance().getService().retrieveCases(pageId, this);
                } else {
                    Zup.getInstance().getService().retrieveCases(flowId, pageId, this);
                }
            } else {
                if (flowId == -1) {
                    Zup.getInstance().getService().retrieveCases(pageId, query, this);
                } else {
                    Zup.getInstance().getService().retrieveCases(flowId, pageId, query, this);
                }
            }
        } catch (RetrofitError ex) {
            Log.e("Retrofit", "Could not load case items", ex);
        }
    }

    public Context getContext() {
        return context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (moreItemsAvailable && position == getCount() - 1) { // loading
            loadMore();

            if (loadingView != null)
                return loadingView;
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                return loadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
            }
        }

        return loadItem(position, parent);
    }

    protected ViewGroup loadItem(int position, ViewGroup parent) {
        Case item = getItem(position);
        ViewGroup root;
        if (viewCache.get(position, null) != null) {
            root = viewCache.get(position);
            setUpCaseView(root, item);
        } else {
            LayoutInflater inflater = LayoutInflater.from(context);
            root = (ViewGroup) inflater.inflate(R.layout.cases_list_item, parent, false);

            viewCache.put(position, root);

            setUpCaseView(root, item);
        }
        return root;
    }

    private View setUpCaseView(ViewGroup rootView, Case item) {
        Flow flow = item.initialFlow;
        rootView.setTag(R.id.tag_item_id, item.id);
        TextView title = (TextView) rootView.findViewById(R.id.fragment_document_title);
        TextView flowText = (TextView) rootView.findViewById(R.id.fragment_document_type);
        TextView description = (TextView) rootView.findViewById(R.id.fragment_document_desc);
        TextView state = (TextView) rootView.findViewById(R.id.fragment_document_statedesc);
        ImageView stateicon = (ImageView) rootView.findViewById(R.id.fragment_document_stateicon);
        ImageView downloadIcon = (ImageView) rootView.findViewById(R.id.download_icon);

        if (Zup.getInstance().getCaseItemService().hasCaseItem(item.id)) {
            downloadIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cloud_done_black_24dp));
        } else {
            downloadIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_cloud_download_black_24dp));
        }

        stateicon.setImageDrawable(ContextCompat.getDrawable(context, Zup.getInstance().getCaseStatusDrawable(item.getStatus())));
        state.setTextColor(Zup.getInstance().getCaseStatusColor(context, item.getStatus()));
        state.setText(Zup.getInstance().getCaseStatusString(context, item.getStatus()));

        title.setText(context.getString(R.string.case_title) + " #" + item.id);
        if (flow != null) {
            flowText.setText(flow.title);
        }
        description.setText(context.getString(R.string.created_at) +
                Zup.getInstance().formatIsoDate(item.createdAt)); //context.getString(R.string.creation_date_title)

        return rootView;
    }
}
