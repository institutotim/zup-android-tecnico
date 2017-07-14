package com.ntxdev.zuptecnico.fragments.reports;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.config.Constants;
import com.ntxdev.zuptecnico.entities.MapCluster;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.collections.ReportItemCollection;
import com.ntxdev.zuptecnico.ui.PicassoMarker;
import com.ntxdev.zuptecnico.util.BitmapUtil;
import com.ntxdev.zuptecnico.util.GeoUtils;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorlira on 8/26/15.
 */
public class ReportsMapFragment extends Fragment
    implements GoogleMap.OnCameraChangeListener, OnMapReadyCallback {
  @Override public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    setupMap();
  }

  public interface Listener {
    void openReportItem(int id);
  }

  GoogleMap mMap;
  SparseArray<PicassoMarker> mMarkers;
  List<Marker> mClusters;
  Tasker mTask;
  Listener mListener;

  boolean mHasMyLocation;

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mMarkers = new SparseArray<>();
    mClusters = new ArrayList<>();
  }

  public void setListener(Listener listener) {
    mListener = listener;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reports_map, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    SupportMapFragment mapFragment =
        (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
    mapFragment.getMapAsync(this);
  }

  private void setupMap() {
    if (mMap == null) return;
    mMap.setPadding(0, 0, 0, 160);
    mMap.setMyLocationEnabled(true);
    mMap.getUiSettings().setMyLocationButtonEnabled(true);
    mMap.getUiSettings().setAllGesturesEnabled(true);
    mMap.getUiSettings().setZoomControlsEnabled(true);
    mMap.getUiSettings().setCompassEnabled(true);
    mMap.setOnCameraChangeListener(this);

    mMap.setOnCameraChangeListener(this);
    mMap.moveCamera(
        CameraUpdateFactory.newLatLngZoom(new LatLng(Constants.DEFAULT_LAT, Constants.DEFAULT_LON),
            Constants.MAP_DEFAULT_ZOOM));

    mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
      @Override public void onMyLocationChange(Location location) {
        if (!mHasMyLocation) {
          mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
              new LatLng(location.getLatitude(), location.getLongitude()),
              Constants.MAP_DEFAULT_ZOOM));

          mHasMyLocation = true;
        }
      }
    });

    mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
      @Override public void onInfoWindowClick(Marker marker) {
        markerWindowClicked(marker);
      }
    });
  }

  private void markerWindowClicked(Marker marker) {
    if (mListener == null) {
      return;
    }

    for (int i = 0; i < mMarkers.size(); i++) {
      int key = mMarkers.keyAt(i);
      PicassoMarker picassoMarker = mMarkers.get(key);
      ReportItem item = (ReportItem) picassoMarker.getTag();

      if (picassoMarker.getMarker().equals(marker)) {
        mListener.openReportItem(item.id);
      }
    }
  }

  @Override public void onCameraChange(CameraPosition cameraPosition) {
    if (mMap == null) return;
    VisibleRegion region = mMap.getProjection().getVisibleRegion();
    double distance =
        GeoUtils.distance(region.latLngBounds.northeast, region.latLngBounds.southwest);
    double latitude = cameraPosition.target.latitude;
    double longitude = cameraPosition.target.longitude;
    int zoom = (int) cameraPosition.zoom;

    if (mTask != null) {
      mTask.cancel(true);
      mTask = null;
    }

    mTask = new Tasker((float) latitude, (float) longitude, (float) distance, zoom);
    mTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }

  private void fillMap(ReportItemCollection items) {
    if (!isAdded() || mMap == null) {
      return;
    }
    List<Integer> markersToRemove = new ArrayList<>();

    for (int i = 0; i < this.mMarkers.size(); i++) {
      int key = this.mMarkers.keyAt(i);
      markersToRemove.add(key);
    }

    for (ReportItem item : items.reports) {
      markersToRemove.remove((Integer) item.id);

      ReportCategory category =
          Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
      BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker();
      String categoryName = String.valueOf(item.category_id);

      if (category != null) {
        categoryName = category.title;
      }

      int oldCategoryId = -1;
      int newCategoryId = item.category_id;

      PicassoMarker picassoMarker = mMarkers.get(item.id);
      if (picassoMarker == null) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(false);
        markerOptions.icon(icon);
        markerOptions.position(new LatLng(item.position.latitude, item.position.longitude));
        markerOptions.title(categoryName);

        Marker marker = mMap.addMarker(markerOptions);

        picassoMarker = new PicassoMarker(marker);
        picassoMarker.setTag(item);
        this.mMarkers.put(item.id, picassoMarker);
      } else {
        Marker marker = picassoMarker.getMarker();
        if (marker.getPosition().latitude != item.position.latitude
            || marker.getPosition().longitude != item.position.longitude) {
          marker.setPosition(new LatLng(item.position.latitude, item.position.longitude));
        }
        marker.setTitle(categoryName);

        oldCategoryId = ((ReportItem) picassoMarker.getTag()).category_id;
        picassoMarker.setTag(item);
      }

      if (category != null && oldCategoryId != newCategoryId) {
        Picasso.with(getActivity()).load(category.getMarkerURL()).into(picassoMarker);
      }
    }

    for (int i = 0; i < mClusters.size(); i++) {
      mClusters.get(i).remove();
    }
    mClusters.clear();

    for (int i = 0; i < markersToRemove.size(); i++) {
      PicassoMarker marker = mMarkers.get(markersToRemove.get(i));
      marker.getMarker().remove();
      marker.dispose();

      mMarkers.remove(markersToRemove.get(i));
    }

    for (MapCluster cluster : items.clusters) {
      String color = null;
      ReportCategory category = null;

      if (cluster.category_id != null) {
        category =
            Zup.getInstance().getReportCategoryService().getReportCategory(cluster.category_id);
      }

      if (category != null) {
        color = category.color;
      }

      Bitmap bmp =
          BitmapUtil.getMapClusterBitmap(cluster, getResources().getDisplayMetrics(), color);

      MarkerOptions markerOptions = new MarkerOptions();
      markerOptions.draggable(false);
      markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bmp));
      markerOptions.position(new LatLng(cluster.position[0], cluster.position[1]));

      mMap.addMarker(markerOptions);
    }
  }

  class Tasker extends AsyncTask<Void, Void, ReportItemCollection> {
    private float latitude, longitude, distance;
    private int zoom;

    public Tasker(float latitude, float longitude, float distance, int zoom) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.distance = distance;
      this.zoom = zoom;
    }

    @Override protected ReportItemCollection doInBackground(Void... voids) {
      try {
        // Is this the final position?
        Thread.sleep(1000);
        return Zup.getInstance()
            .getService()
            .retrieveReportItems(latitude, longitude, distance, 100, zoom);
      } catch (Exception ex) {
        return null;
      }
    }

    @Override protected void onPostExecute(ReportItemCollection reportItems) {
      super.onPostExecute(reportItems);

      if (reportItems == null) {
        return;
      }

      fillMap(reportItems);
    }
  }
}
