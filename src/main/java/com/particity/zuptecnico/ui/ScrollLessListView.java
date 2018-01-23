package com.particity.zuptecnico.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

/**
 * Created by igorlira on 7/24/15.
 */
public class ScrollLessListView extends LinearLayout {
    private Adapter adapter;

    class DataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            ScrollLessListView.this.onChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            ScrollLessListView.this.onInvalidated();
        }
    }

    public ScrollLessListView(Context context) {
        super(context);
    }

    public ScrollLessListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollLessListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScrollLessListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        this.adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                ScrollLessListView.this.onChanged();
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
                ScrollLessListView.this.onInvalidated();
            }
        });
        fillItems();
    }

    private void fillItems() {
        for(int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, this);
            this.addView(view);
        }
    }

    void onChanged() {
        // TODO should this be done a better way?
        this.removeAllViews();
        this.fillItems();
    }

    void onInvalidated() {
        this.removeAllViews();
        this.fillItems();
    }
}
