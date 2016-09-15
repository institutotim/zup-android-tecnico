package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Case;

public class OfflineCasesAdapter extends CasesAdapter {
    View noItemsView;

    public OfflineCasesAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        int count = Zup.getInstance().getCaseItemService().getCaseItemCount();
        if (count == 0)
            return 1;
        else
            return count;
    }

    @Override
    public Case getItem(int i) {
        if (Zup.getInstance().getCaseItemService().getCaseItemCount() == 0)
            return null;

        return Zup.getInstance().getCaseItemService().getCaseItemAtIndex(i);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position == 0 && Zup.getInstance().getCaseItemService().getCaseItemCount() == 0) {
            if (noItemsView != null)
                return noItemsView;
            else {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                return noItemsView = inflater.inflate(R.layout.listview_noitems, parent, false);
            }
        } else {
            return loadItem(position, parent);
        }
    }

    @Override
    public void load() {
        // We don't want to load from the API
        if (this.getListener() != null)
            this.getListener().onCasesLoaded();
    }

    @Override
    public void reset() {
        super.reset();

        if (this.getListener() != null)
            this.getListener().onCasesLoaded();
    }
}
