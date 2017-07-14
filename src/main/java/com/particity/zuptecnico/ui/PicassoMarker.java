package com.ntxdev.zuptecnico.ui;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by igorlira on 8/26/15.
 */
public class PicassoMarker implements Target {
    Marker mMarker;
    Object mTag;

    public PicassoMarker(Marker marker) {
        mMarker = marker;
    }

    @Override
    public int hashCode() {
        if(mMarker != null) {
            return mMarker.hashCode();
        }
        else {
            return 0;
        }
    }

    @Override
    public boolean equals(Object o) {
        if(mMarker != null && o instanceof PicassoMarker) {
            Marker marker = ((PicassoMarker) o).mMarker;
            return mMarker.equals(marker);
        } else {
            return false;
        }
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        try {
            if(mMarker != null) {
                mMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
            }
        }
        catch (Exception ex) {
            // The marker is not on the map anymore, ignore error
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }

    public Marker getMarker() {
        return mMarker;
    }

    public void dispose() {
        mMarker = null;
    }

    public void setTag(Object tag) {
        mTag = tag;
    }

    public Object getTag() {
        return mTag;
    }
}
