package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.ntxdev.zuptecnico.entities.Position;
import com.ntxdev.zuptecnico.util.GeoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by igorlira on 7/22/15.
 */
public class AddressAdapter extends ArrayAdapter<String> implements Filterable
{
    public interface PositionManager {
        double getLatitude();
        double getLongitude();
    }
    private ArrayList<JSONObject> resultList;
    //private ArrayList<String> resultList;

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private PositionManager manager;

    public AddressAdapter(Context context, int resource, PositionManager manager) {
        super(context, resource);
        this.manager = manager;
    }

    @Override
    public int getCount() {
        if(resultList == null)
            return 0;

        return resultList.size();
    }

    @Override
    public String getItem(int position) {
        //return resultList.get(position);
        try {
            JSONArray terms = resultList.get(position).getJSONArray("terms");
            return terms.getJSONObject(0).getString("value");
        } catch (Exception ex) {
            return "";
        }
    }

    public String getFullItem(int position) {
        try {
            return resultList.get(position).getString("description");
        } catch (Exception ex) {
            return "";
        }
    }

    private ArrayList<JSONObject> autocomplete(String input) {
        ArrayList<JSONObject> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?sensor=true&key=" + GeoUtils.PLACES_KEY);
            sb.append("&components=country:br");
            sb.append("&language=pt-BR");
            //sb.append("&types=(regions)");
            sb.append("&location=").append(manager.getLatitude()).append(",").append(manager.getLongitude());
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
        } catch (Exception e) {
            Log.e("ZUP", e.getMessage(), e);
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
            resultList = new ArrayList<>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i));
                //JSONArray terms = predsJsonArray.getJSONObject(i).getJSONArray("terms");
                //resultList.add(terms.getJSONObject(0).getString("value"));
                //resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e("ZUP", "Cannot process JSON results", e);
        }

        return resultList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
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
            protected void publishResults(CharSequence charSequence, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}