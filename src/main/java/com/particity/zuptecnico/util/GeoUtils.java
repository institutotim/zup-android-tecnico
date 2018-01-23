package com.particity.zuptecnico.util;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.config.Constants;
import com.particity.zuptecnico.entities.Place;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class GeoUtils {

    public static final String PLACES_KEY = "AIzaSyCOixuls1j6rqhYizrTjw4jymX_T23KTjw";

    public static long getVisibleRadius(GoogleMap map) {
        LatLng corner = map.getProjection().getVisibleRegion().farLeft;
        LatLng center = map.getCameraPosition().target;
        return distance(corner, center);
    }

    public static long distance(LatLng point1, LatLng point2) {
        double lat1 = point1.latitude;
        double lat2 = point2.latitude;
        double lon1 = point1.longitude;
        double lon2 = point2.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return (long) (6366000 * c);
    }

    public static boolean isVisible(VisibleRegion visibleRegion, LatLng position) {
        return visibleRegion.latLngBounds.contains(position);
    }

    public static Address search(String str) throws IOException {
        Geocoder geocoder = new Geocoder(ZupApplication.getContext(), Locale.getDefault());
        List<Address> addressList = geocoder.getFromLocationName(str, 1);
        if(addressList != null && addressList.size() > 0){
            return addressList.get(0);
        }
        return null;
    }

     public static Address getFromPlace(Place place) {
        String address = "https://maps.googleapis.com/maps/api/place/details/json?reference=" +
                place.getReference() + "&sensor=true&language=" + Locale.getDefault() + "&key=" + Constants.PLACES_KEY;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();

            JSONObject jsonObject = new JSONObject(convertStreamToString(conn.getInputStream()));

            if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
                JSONObject result = jsonObject.getJSONObject("result");
                String indiStr = result.getString("formatted_address");
                Address addr = new Address(Locale.getDefault());
                addr.setAddressLine(0, indiStr);
                addr.setLatitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lat"));
                addr.setLongitude(result.getJSONObject("geometry").getJSONObject("location").getDouble("lng"));
                return addr;
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
            Log.e("ZUP", "Error calling Google geocode webservice.", e);
        } catch (JSONException e) {
            Crashlytics.logException(e);
            Log.e("ZUP", "Error parsing Google geocode webservice response.", e);
        }

        return null;
    }

    private static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}