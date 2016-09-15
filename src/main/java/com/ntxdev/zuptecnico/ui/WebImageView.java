package com.ntxdev.zuptecnico.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.ntxdev.zuptecnico.api.Zup;

/**
 * Created by igorlira on 7/18/15.
 */
public class WebImageView extends ImageView implements ImageLoadedListener {
    private int resourceId = -1;
    private ImageLoadedListener callback;

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onImageLoaded(int resourceId) {
        if (resourceId == this.resourceId) {
            this.setImageBitmap(Zup.getInstance().getBitmap(resourceId));

            if (this.callback != null)
                this.callback.onImageLoaded(this.resourceId);
        }
    }
}
