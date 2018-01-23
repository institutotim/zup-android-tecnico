package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
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
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.Position;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.PicassoMarker;
import com.lfdb.zuptecnico.util.BitmapUtil;
import com.squareup.picasso.Picasso;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemMapFragment extends Fragment implements OnMapReadyCallback {
  SupportMapFragment mapFragment;
  Marker marker;
  private GoogleMap mMap;

  ReportItem getItem() {
    return (ReportItem) getArguments().getParcelable("item");
  }

  InventoryItem getInventoryItem() {
    return (InventoryItem) getArguments().getParcelable("inventory");
  }

  Position getItemPosition() {
    if (getItem() == null && getInventoryItem() == null) {
      return null;
    }
    if (getItem() == null) {
      InventoryItem.Coordinates position = getInventoryItem().position;
      return position == null ? null : new Position(position.latitude, position.longitude);
    }
    return getItem().position;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    ViewGroup root =
        (ViewGroup) inflater.inflate(R.layout.fragment_report_details_map, container, false);
    mapFragment =
        (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);

    int checkGooglePlayServices =
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
    if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
      GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices, getActivity(), 1122).show();
    } else {
      mapFragment.getMapAsync(this);
    }

    refresh();

    return root;
  }

  public void refresh() {
    if (mapFragment == null || mMap == null) {
      return;
    }
    mMap.clear();
    if (getItemPosition() == null) {
      return;
    }

    CameraPosition position = new CameraPosition.Builder().target(
        new LatLng(getItemPosition().latitude, getItemPosition().longitude)).zoom(15).build();

    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));

    setupMarker();
  }

  void setupMarker() {
    if (mMap == null) return;
    if (getItem() == null && getInventoryItem() == null) {
      return;
    }
    String url;
    BitmapDescriptor descriptor = BitmapDescriptorFactory.defaultMarker();
    if (getItem() == null) {
      InventoryCategory category = Zup.getInstance()
          .getInventoryCategoryService()
          .getInventoryCategory(getInventoryItem().inventory_category_id);
      url = category.getMarkerUrl();
      descriptor = BitmapUtil.getMarkerBitmapFactory(category.color);
    } else {
      url = Zup.getInstance()
          .getReportCategoryService()
          .getReportCategory(getItem().category_id)
          .getMarkerURL();
    }
    if (getItemPosition() == null) {
      return;
    }
    MarkerOptions options = new MarkerOptions();
    options.icon(descriptor);
    options.position(new LatLng(getItemPosition().latitude, getItemPosition().longitude));
    options.draggable(false);

    this.marker = mMap.addMarker(options);
    if (url != null) {
      PicassoMarker picassoMarker = new PicassoMarker(marker);
      Picasso.with(getActivity()).load(url).into(picassoMarker);
    }
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    mMap.getUiSettings().setMyLocationButtonEnabled(false);
    mMap.getUiSettings().setAllGesturesEnabled(false);
    mMap.getUiSettings().setZoomControlsEnabled(false);
  }
}
