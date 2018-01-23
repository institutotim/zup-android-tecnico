package com.particity.zuptecnico.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.particity.zuptecnico.R;

/**
 * Created by igorlira on 3/18/14.
 */
public class InfinityScrollView extends ScrollView {
    boolean disableScroll = false;

    public interface OnScrollViewListener {
        void onScrollChanged(InfinityScrollView v, int l, int t, int oldl, int oldt );
    }

    private OnScrollViewListener mOnScrollViewListener;

    public void setOnScrollViewListener(OnScrollViewListener l) {
        this.mOnScrollViewListener = l;
    }

    public InfinityScrollView(Context context)
    {
        super(context);
    }

    public InfinityScrollView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);

        TypedArray ta = context.obtainStyledAttributes(attributeSet, R.styleable.InfinityScrollView, 0, 0);
        try {
            this.disableScroll = ta.getBoolean(R.styleable.InfinityScrollView_disableScroll, false);
        }
        finally {
            ta.recycle();
        }
    }

    public InfinityScrollView(Context context, AttributeSet attributeSet, int defStyle)
    {
        super(context, attributeSet, defStyle);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(this.disableScroll)
            return false;

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(mOnScrollViewListener != null)
            mOnScrollViewListener.onScrollChanged(this, l, t, oldl, oldt);

        super.onScrollChanged(l, t, oldl, oldt);
    }
}
