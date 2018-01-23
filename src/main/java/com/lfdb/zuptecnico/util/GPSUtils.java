package com.lfdb.zuptecnico.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.responses.PositionValidationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GPSUtils {

    public static List<Address> getFromLocationName(Context context, String s) {
        try {
            if (!Utilities.isConnected(context)) {
                return Collections.emptyList();
            }
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> list = filterResults(geocoder.getFromLocationName(s, 10));
            if(list != null) {
                return list;
            }else{
                return Collections.emptyList();
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public static List<Address> getFromLocation(Context context, double latitude, double longitude) {
        try {
            if (!Utilities.isConnected(context)) {
                return Collections.emptyList();
            }
            Geocoder geocoder = new Geocoder(ZupApplication.getContext(), Locale.getDefault());
            List<Address> list = filterResults(geocoder.getFromLocation(latitude, longitude, 10));
            return list;
        } catch (IOException e) {
            Crashlytics.logException(e);
            Log.e("ZUP", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    private static List<Address> filterResults(List<Address> list) {
        List<Address> arraylist = new ArrayList<Address>();
        for (Address address : list) {
            if (!Strings.isTrimmedNullOrEmpty(address.getLocality()) || !Strings.isTrimmedNullOrEmpty(address.getSubAdminArea())) {
                arraylist.add(address);
            }
        }
        return arraylist;
    }

    public static String formatAddress(Address address) {
        ArrayList<String> components = new ArrayList<>();
        boolean hasAddress = false;

        if (address.getThoroughfare() != null) {
            components.add(address.getThoroughfare());
            hasAddress = true;
        }

        if (address.getFeatureName() != null) {
            components.add(address.getFeatureName());
            hasAddress = true;
        }

        if (address.getSubLocality() != null) {
            components.add(address.getSubLocality());
            hasAddress = true;
        }

        if (address.getSubAdminArea() != null) {
            components.add(address.getSubAdminArea());
            hasAddress = true;
        }

        if (address.getAdminArea() != null) {
            components.add(address.getAdminArea());
            hasAddress = true;
        }

        if (address.getPostalCode() != null) {
            components.add(address.getPostalCode());
            hasAddress = true;
        }

        if (address.getCountryName() != null) {
            components.add(address.getCountryName());
            hasAddress = true;
        }
        if (hasAddress) {
            return TextUtils.join(", ", components);
        }else {
            return String.valueOf(address.getLatitude()) + ", " + address.getLongitude();
        }
    }

    public static void validateAddress(double latitude, double longitude, final Handler handler) {
        AsyncTask<Double, Void, Boolean> task = new AsyncTask<Double, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Double... params) {
                double latitude = params[0];
                double longitude = params[1];
                PositionValidationResponse result = Zup.getInstance().getService()
                        .validatePosition(latitude, longitude);
                return result != null && ((result.inside_boundaries == null) || result.inside_boundaries);
            }
        };
        AsyncTaskCompat.executeParallel(task, latitude, longitude);
    }


}