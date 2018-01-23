package com.lfdb.zuptecnico.widgets;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.lfdb.zuptecnico.config.Constants;
import com.lfdb.zuptecnico.entities.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PlacesAutoCompleteAdapter extends ArrayAdapter<Place> implements Filterable {
    private ArrayList<Place> resultList;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private Class<?> latLngProvider;

    public PlacesAutoCompleteAdapter(Context context, int textViewResourceId, Class<?> latLngProvider) {
        super(context, textViewResourceId);
        this.latLngProvider = latLngProvider;
    }

    @Override
    public int getCount() {
        if (resultList == null) return 0;
        return resultList.size();
    }

    @Override
    public Place getItem(int index) {
		if (resultList != null && resultList.size() > index)
			return resultList.get(index);
		return null;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    // Retrieve the autocomplete results.
                    resultList = autocomplete(constraint.toString());

                    // Assign the data to the FilterResults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }};
        return filter;
    }

    private ArrayList<Place> autocomplete(String input) {
        ArrayList<Place> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?sensor=true&key=" + Constants.PLACES_KEY);
            sb.append("&components=country:br");
            sb.append("&language=pt-BR");
            //sb.append("&types=(regions)");
            sb.append("&location=").append(getLatitude()).append(",").append(getLongitude());
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e("ZUP", "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e("ZUP", "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList<Place>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(new Place(predsJsonArray.getJSONObject(i).getString("description"), predsJsonArray.getJSONObject(i).getString("reference")));
            }
        } catch (JSONException e) {
            Log.e("ZUP", "Cannot process JSON results", e);
        }

        return resultList;
    }

    private double getLatitude() {
        try {
            Field f = latLngProvider.getField("latitude");
            return f.getDouble(null);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return 0.0;
        }
    }

    private double getLongitude() {
        try {
            Field f = latLngProvider.getField("longitude");
            return f.getDouble(null);
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
            return 0.0;
        }
    }
}