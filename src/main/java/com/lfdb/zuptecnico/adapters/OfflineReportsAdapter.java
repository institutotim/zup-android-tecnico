package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportItem;

/**
 * Created by Igor on 8/3/2015.
 */
public class OfflineReportsAdapter extends ReportsAdapter {
    View noItemsView;
    RecyclerView.ViewHolder holder;

    public OfflineReportsAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        int count = Zup.getInstance().getReportItemService().getReportItemCount();
        if(count == 0)
            return 1;
        else
            return count;
    }

    @Override
    public ReportItem getItem(int i) {
        if(Zup.getInstance().getReportItemService().getReportItemCount() == 0)
            return null;

        return Zup.getInstance().getReportItemService().getReportItemAtIndex(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position == 0 && Zup.getInstance().getReportItemService().getReportItemCount() == 0) {
            if(noItemsView != null)
                return noItemsView;
            else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                return noItemsView = inflater.inflate(R.layout.listview_noitems, parent, false);
            }
        }
        else {
            return super.getView(position, convertView, parent);
        }
    }

    @Override
    public void load() {
        // We don't want to load from the API
        if(this.getListener() != null)
            this.getListener().onItemsLoaded();
    }

    @Override
    public void reset() {
        super.reset();

        if(this.getListener() != null)
            this.getListener().onItemsLoaded();
    }
}
