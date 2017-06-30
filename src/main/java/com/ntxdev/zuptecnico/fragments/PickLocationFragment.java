package com.ntxdev.zuptecnico.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.ZupApplication;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.config.Constants;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.Place;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.responses.PositionValidationResponse;
import com.ntxdev.zuptecnico.ui.WebImageView;
import com.ntxdev.zuptecnico.util.GPSUtils;
import com.ntxdev.zuptecnico.util.GeoUtils;
import com.ntxdev.zuptecnico.util.Utilities;
import com.ntxdev.zuptecnico.util.ViewUtils;
import com.ntxdev.zuptecnico.widgets.PlacesAutoCompleteAdapter;
import java.util.List;
import java.util.Locale;

public class PickLocationFragment extends Fragment
    implements AdapterView.OnItemClickListener, OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

  private TimerEndereco task;
  private GoogleMap map;
  public static double latitude, longitude;
  private AutoCompleteTextView tvAddress;
  private EditText tvNumber;
  private EditText tvReference;
  private EditText tvZipCode;
  private EditText tvNeighborhood;

  private SearchTask searchTask = null;
  private GeocoderTask geocoderTask = null;
  private Address enderecoAtual = null;
  private Location mLastLocation;

  private String streetName = "", streetNumber = "", streetZipCode = "", streetNeighborhood = "";
  private float zoomAtual;
  private boolean ignoreUpdate = false;
  private boolean valid = false;
  private boolean hasMyLocation = false;

  private OnLocationValidatedListener listener;
  private android.location.LocationListener locationListener;

  private LatLng getLastKnownPosition() {
    final LocationManager locationManager =
        (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
    Criteria criteria = new Criteria();
    String provider = locationManager.getBestProvider(criteria, true);
    if (!getPermissions()) return null;

    locationListener = new android.location.LocationListener() {
      @Override public void onLocationChanged(Location location) {
        PickLocationFragment.this.onLocationChanged(location);
      }

      @Override public void onStatusChanged(String provider, int status, Bundle extras) {

      }

      @Override public void onProviderEnabled(String provider) {

      }

      @Override public void onProviderDisabled(String provider) {

      }
    };

    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 30,
        locationListener);

    Location myLocation = locationManager.getLastKnownLocation(provider);

    if (myLocation == null) {
      return new LatLng(Constants.DEFAULT_LAT, Constants.DEFAULT_LON);
    }
    hasMyLocation = true;
    return new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
  }

  private void onLocationChanged(Location location) {
    if (location == null || !isAdded()) {
      return;
    }

    mLastLocation = location;
    latitude = mLastLocation.getLatitude();
    longitude = mLastLocation.getLongitude();
    if (map != null) {
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
          Constants.MAP_DEFAULT_ZOOM));
    }
    if (listener != null) {
      listener.onValidLocationSet();
    }
    hasMyLocation = true;
  }

  private boolean getPermissions() {
    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED
        && ActivityCompat.checkSelfPermission(getActivity(),
        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(getActivity(),
          new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 231);
      // TODO: Consider calling
      //    ActivityCompat#requestPermissions
      // here to request the missing permissions, and then overriding
      //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
      //                                          int[] grantResults)
      // to handle the case where the user grants the permission. See the documentation
      // for ActivityCompat#requestPermissions for more details.
      return false;
    }
    return true;
  }

  @Override public void onMapReady(GoogleMap googleMap) {
    map = googleMap;
    if (map != null && hasMyLocation) {
      map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
          Constants.MAP_DEFAULT_ZOOM));
    }
    setupMap();
  }

  @Override public void onCameraIdle() {
    if (map == null) {
      return;
    }

    CameraPosition cameraPosition = map.getCameraPosition();
    latitude = cameraPosition.target.latitude;
    longitude = cameraPosition.target.longitude;
    hasMyLocation = true;
    zoomAtual = cameraPosition.zoom;
    tvAddress.setAdapter(null);
    if (isAdded()) {
      tvAddress.setAdapter(
          new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion,
              PickLocationFragment.class));
    }
    tvAddress.dismissDropDown();
  }

  public interface OnLocationValidatedListener {
    void onValidLocationSet();

    void onInvalidLocationSet();
  }

  public void setListener(OnLocationValidatedListener listener) {
    this.listener = listener;
  }

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_pick_map_location_new, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().
        findFragmentById(R.id.pick_location_map);

    tvAddress = (AutoCompleteTextView) view.findViewById(R.id.address_street);
    tvNumber = (EditText) view.findViewById(R.id.address_number);
    tvNumber.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      @Override public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          String query = tvAddress.getText().toString() + "," + tvNumber.getText().toString();
          realizarBuscaAutocomplete(query);
          ViewUtils.hideKeyboard(getActivity(), tvNumber);
        }
      }
    });

    tvReference = (EditText) view.findViewById(R.id.address_reference);
    tvZipCode = (EditText) view.findViewById(R.id.address_zip_number);
    tvNeighborhood = (EditText) view.findViewById(R.id.address_neighborhood);

    setAddressLoaderVisible(false);

    int checkGooglePlayServices =
        GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity());
    if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
      GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices, getActivity(), 1122).show();
    } else {
      mapFragment.getMapAsync(this);
      if (Utilities.isConnected(getActivity())) {
        updateEditTextListeners(tvAddress, tvNumber, tvNeighborhood, tvZipCode);
      } else {
        updateViewWithData();
        mapFragment.getView().setVisibility(View.VISIBLE);
        view.findViewById(R.id.address_marker).setVisibility(View.VISIBLE);
      }
    }
  }

  void updateEditTextListeners(AutoCompleteTextView tvAddress, EditText tvNumber,
      EditText tvNeighborhood, EditText tvZipCode) {
    tvAddress.setImeActionLabel(getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
    tvNumber.setImeActionLabel(getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);
    tvNeighborhood.setImeActionLabel(getString(R.string.search_title),
        EditorInfo.IME_ACTION_SEARCH);
    tvZipCode.setImeActionLabel(getString(R.string.search_title), EditorInfo.IME_ACTION_SEARCH);

    TextView.OnEditorActionListener listener = new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          searchByAddressAndNumber();
          ViewUtils.hideKeyboard(getActivity(), v);
          handled = true;
        }
        return handled;
      }
    };

    tvAddress.setOnItemClickListener(this);
    tvAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          realizarBuscaAutocomplete(v.getText().toString());
          ViewUtils.hideKeyboard(getActivity(), v);
          handled = true;
        }
        return handled;
      }
    });
    tvAddress.setOnEditorActionListener(listener);
    tvNumber.setOnEditorActionListener(listener);
    tvZipCode.setOnEditorActionListener(listener);
    tvNeighborhood.setOnEditorActionListener(listener);
  }

  @Override public void onResume() {
    super.onResume();
    if (isAdded()) {
      if (!Utilities.isConnected(getActivity())) {
        return;
      }
      task = new TimerEndereco();
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
      } else {
        AsyncTaskCompat.executeParallel(task);
      }
    }
  }

  private ReportCategory getCategory() {
    if (!getArguments().containsKey("categoryId") || getArguments().getInt("categoryId") == -1) {
      return null;
    }

    int categoryId = getArguments().getInt("categoryId");
    return Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);
  }

  private InventoryCategory getInventoryCategory() {
    if (!getArguments().containsKey("inventory_category_id")
        || getArguments().getInt("inventory_category_id") == -1) {
      return null;
    }

    int id = getArguments().getInt("inventory_category_id");
    return Zup.getInstance().getInventoryCategoryService().getInventoryCategory(id);
  }

  private int getInventoryCategoryId() {
    return getArguments().getInt("inventory_category_id");
  }

  private void setupMap() {
    View root = getView();
    WebImageView markerView = (WebImageView) root.findViewById(R.id.address_marker);
    if (getInventoryCategory() != null) {
      getInventoryCategory().loadMarkerInto(markerView);
    } else if (getCategory() != null) {
      getCategory().loadMarkerInto(markerView);
    }

    if (getPermissions()) {
      map.setMyLocationEnabled(true);
    }
    map.setOnCameraIdleListener(this);
    if (getArguments() != null && getArguments().get("address") != null) {
      Address address = updateViewWithData();
      valid = true;
      try {
        double latitude = address.getLatitude();
        double longitude = address.getLongitude();
        if (latitude != Constants.DEFAULT_LAT && longitude != Constants.DEFAULT_LON) {
          map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude),
              Constants.MAP_DEFAULT_ZOOM));
          if (listener != null) {
            listener.onValidLocationSet();
          }
          hasMyLocation = true;
        } else if (!TextUtils.isEmpty(address.getThoroughfare())) {
          searchByAddressAndNumber();
        } else {
          LatLng latLng = getLastKnownPosition();
          map.animateCamera(
              CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude),
                  Constants.MAP_DEFAULT_ZOOM));
        }
      } catch (IllegalStateException error) {
        error.printStackTrace();
        LatLng latLng = getLastKnownPosition();
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude),
                Constants.MAP_DEFAULT_ZOOM));
      }
    } else {
      if (!hasMyLocation) {
        LatLng latLng = getLastKnownPosition();
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude),
                Constants.MAP_DEFAULT_ZOOM));
      }
    }
  }

  private Address updateViewWithData() {
    if (getArguments() == null || getArguments().get("address") == null) {
      return null;
    }
    Address address = getArguments().getParcelable("address");
    enderecoAtual = address;
    if (address != null) {
      try {
        latitude = address.getLatitude();
        longitude = address.getLongitude();
        if (!Utilities.isConnected(getActivity()) && listener != null) {
          listener.onValidLocationSet();
        }
        hasMyLocation = true;
      } catch (IllegalStateException e) {
        e.printStackTrace();
      }
      tvAddress.setText(address.getThoroughfare());
      tvNumber.setText(address.getFeatureName());
      tvNeighborhood.setText(address.getSubLocality() == null ? "" : address.getSubLocality());
      tvZipCode.setText(
          address.getPostalCode() == null ? "" : address.getPostalCode().replace("-", ""));
    }
    if (getArguments().get("reference") != null) {
      tvReference.setText(getArguments().getString("reference"));
    }
    return address;
  }

  public Address getAddress() {

    if (enderecoAtual == null) {
      enderecoAtual = new Address(Locale.getDefault());
    }
    if (validarEndereco(tvAddress.getText().toString().trim(), tvNumber.getText().toString())) {
      enderecoAtual.setFeatureName(tvNumber.getText().toString());
      enderecoAtual.setThoroughfare(tvAddress.getText().toString().trim());
      enderecoAtual.setSubLocality(tvNeighborhood.getText().toString().trim());
      enderecoAtual.setPostalCode(tvZipCode.getText().toString().trim());
      if (TextUtils.isEmpty(enderecoAtual.getSubAdminArea())) {
        enderecoAtual.setSubAdminArea(getString(R.string.default_city));
      }
      if (TextUtils.isEmpty(enderecoAtual.getAdminArea())) {
        enderecoAtual.setAdminArea(getString(R.string.default_state));
      }
      if (TextUtils.isEmpty(enderecoAtual.getCountryName())) {
        enderecoAtual.setCountryName(getString(R.string.default_country));
      }
    }
    return enderecoAtual;
  }

  public String getReference() {
    if (getView() == null) return "";

    TextView txtReference = (TextView) getView().findViewById(R.id.address_reference);
    return txtReference.getText().toString();
  }

  @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    realizarBuscaAutocomplete((Place) parent.getItemAtPosition(position));
    ViewUtils.hideKeyboard(getActivity(), tvAddress);
  }

  private void realizarBuscaAutocomplete(Place place) {
    if (geocoderTask != null) {
      geocoderTask.cancel(true);
    }

    geocoderTask = new GeocoderTask();
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
      geocoderTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, place);
    } else {
      geocoderTask.execute(place);
    }
  }

  private void realizarBuscaAutocomplete(String query) {
    if (searchTask != null) {
      searchTask.cancel(true);
    }

    searchTask = new SearchTask();
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
      searchTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
    } else {
      searchTask.execute(query);
    }
  }

  public void reload() {
    if (Utilities.isConnected(getActivity())) {
      setupMap();
    }
  }

  private class TimerEndereco extends AsyncTask<Void, String, Void> {

    private double lat, lon;
    private boolean run = true;

    public TimerEndereco() {
      lat = latitude;
      lon = longitude;
    }

    @Override protected Void doInBackground(Void... arg0) {
      while (run) {

        try {
          if (isAdded()) {
            Thread.sleep(1000);
          } else {
            return null;
          }
        } catch (Exception e) {
          Log.e("ZUP", e.getMessage(), e);
        }

        if (lat != latitude && lon != longitude) {
          lat = latitude;
          lon = longitude;

          if (ignoreUpdate) {
            ignoreUpdate = false;
            continue;
          }
          if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                setAddressLoaderVisible(true);
              }
            });
          }

          List<Address> addresses = GPSUtils.getFromLocation(getActivity(), lat, lon);
          if (!addresses.isEmpty()) {
            Address address = addresses.get(0);
            if (address.getThoroughfare() != null) {
              enderecoAtual = address;
              try {
                PositionValidationResponse result =
                    Zup.getInstance().getService().validatePosition(latitude, longitude);

                valid = result != null && ((result.inside_boundaries == null)
                    || result.inside_boundaries);
              } catch (Exception e) {
                Log.e("Boundary validation", "Failed to validate boundary", e);
              }

              if (!address.getThoroughfare().startsWith("null")) {

                publishProgress(address.getThoroughfare(), address.getFeatureName(),
                    address.getPostalCode(), address.getSubLocality());
              }
            }
          }

          if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                setAddressLoaderVisible(false);
              }
            });
          }
        }
      }
      return null;
    }

    @Override protected void onProgressUpdate(String... values) {
      streetName = values[0];
      tvAddress.setAdapter(null);
      tvAddress.setText(values[0]);

      verifyValid();

      try {
        if (!TextUtils.isEmpty(values[2])) {
          streetZipCode = values[2];
          tvZipCode.setText(values[2].replace("-", ""));
        } else {
          streetZipCode = "";
          tvZipCode.setText("");
        }
        if (!values[3].isEmpty()) {
          streetNeighborhood = values[3];
          tvNeighborhood.setText(values[3]);
        } else {
          streetNeighborhood = "";
          tvNeighborhood.setText("");
        }
        if (!values[1].isEmpty() && TextUtils.isDigitsOnly(values[1].substring(0, 1))) {
          streetNumber = values[1];
          tvNumber.setText(values[1]);
        } else {
          streetNumber = "";
          tvNumber.setText("");
        }
      } catch (Exception e) {
        Log.w("ZUP", e.getMessage() != null ? e.getMessage() : "null", e);
        streetNumber = "";
        tvNumber.setText("");
      }
      if (isAdded()) {
        tvAddress.setAdapter(
            new PlacesAutoCompleteAdapter(getActivity(), R.layout.pick_map_location_suggestion,
                PickLocationFragment.class));
        tvAddress.dismissDropDown();
      }
    }
  }

  private void verifyValid() {
    if (!isAdded()) return;

    if (listener != null && valid) {
      listener.onValidLocationSet();
    } else if (listener != null) listener.onInvalidLocationSet();

    if (!valid) {
      showInvalidPositionBar();
    } else if (valid) {
      hideInvalidPositionBar();
    }
  }

  void showInvalidPositionBar() {
    View view = getView().findViewById(R.id.pickmap_invalid);
    view.setVisibility(View.VISIBLE);
  }

  void hideInvalidPositionBar() {
    View view = getView().findViewById(R.id.pickmap_invalid);
    view.setVisibility(View.GONE);
  }

  void setAddressLoaderVisible(boolean visible) {
    if (getView() == null) return;

    tvAddress.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT);
    tvNumber.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
    tvNeighborhood.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_TEXT);
    tvZipCode.setInputType(visible ? InputType.TYPE_NULL : InputType.TYPE_CLASS_NUMBER);
    int textColor = ContextCompat.getColor(getActivity(), R.color.pick_map_location_text);
    if (visible) {
      // Trick to hide the text without actually removing it
      textColor = 0x00000000;
    }

    tvAddress.setTextColor(textColor);
    tvNumber.setTextColor(textColor);
    tvNeighborhood.setTextColor(textColor);
    tvZipCode.setTextColor(textColor);

    tvAddress.clearFocus();
    tvNumber.clearFocus();
    tvNeighborhood.clearFocus();
    tvZipCode.clearFocus();

    getView().findViewById(R.id.address_street_progress)
        .setVisibility(visible ? View.VISIBLE : View.GONE);
    getView().findViewById(R.id.address_number_progress)
        .setVisibility(visible ? View.VISIBLE : View.GONE);
    getView().findViewById(R.id.address_zip_number_progress)
        .setVisibility(visible ? View.VISIBLE : View.GONE);
    getView().findViewById(R.id.address_neighborhood_progress)
        .setVisibility(visible ? View.VISIBLE : View.GONE);
  }

  public boolean validarEndereco() {
    if (!streetName.equalsIgnoreCase(tvAddress.getText().toString())) {
      new AlertDialog.Builder(getActivity()).setTitle(
          getActivity().getString(R.string.error_invalid_address))
          .setMessage(getActivity().getString(R.string.error_invalid_address_message))
          .setNegativeButton(getActivity().getString(R.string.lab_ok), null)
          .show();
      return false;
    }

    return validarEndereco(streetName, streetNumber);
  }

  private boolean validarEndereco(final String r, final String num) {
    return !r.isEmpty();
  }

  private void searchByAddressAndNumber() {
    final String street = tvAddress.getText().toString();
    final String number = tvNumber.getText().toString();
    final String zipCode = tvZipCode.getText().toString();
    final String neighborhood = tvNeighborhood.getText().toString();

    if (validarEndereco(street, number)) {
      new AsyncTask<Void, Void, Void>() {
        @Override protected Void doInBackground(Void... params) {
          if (enderecoAtual == null) {
            return null;
          }
          StringBuilder addressBuilder = new StringBuilder(street);
          addressBuilder.append(", ");
          addressBuilder.append(number);
          addressBuilder.append(" - ");
          addressBuilder.append(neighborhood);
          addressBuilder.append(", ");
          addressBuilder.append(
              enderecoAtual.getSubAdminArea() != null ? enderecoAtual.getSubAdminArea()
                  : enderecoAtual.getLocality());
          addressBuilder.append(", ");
          addressBuilder.append(zipCode);
          List<Address> addresses =
              GPSUtils.getFromLocationName(getActivity(), addressBuilder.toString());
          if (addresses.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                ZupApplication.toast(getActivity().findViewById(android.R.id.content),
                    R.string.error_address_not_found).show();
              }
            });
          } else {
            final Address address = addresses.get(0);
            streetNumber = number;
            streetName = address.getThoroughfare();
            if (address.getPostalCode() == null || address.getPostalCode().isEmpty()) {
              streetZipCode = "";
            } else {
              streetZipCode = address.getPostalCode().replace("-", "");
            }
            if (address.getSubLocality() == null || address.getSubLocality().isEmpty()) {
              streetNeighborhood = "";
            } else {
              streetNeighborhood = address.getSubLocality();
            }

            try {
              PositionValidationResponse result = Zup.getInstance()
                  .getService()
                  .validatePosition(address.getLatitude(), address.getLongitude());
              valid = result != null && ((result.inside_boundaries == null)
                  || result.inside_boundaries);
            } catch (IllegalStateException error) {
              valid = false;
            }

            getActivity().runOnUiThread(new Runnable() {
              @Override public void run() {
                verifyValid();
                ignoreUpdate = true;
                animateCamera(address, true);
                tvAddress.setAdapter(null);
                tvAddress.setText(streetName);
                if (isAdded()) {
                  tvAddress.setAdapter(new PlacesAutoCompleteAdapter(getActivity(),
                      R.layout.pick_map_location_suggestion, PickLocationFragment.class));
                }
                tvNumber.setText(streetNumber);
                tvNeighborhood.setText(streetNeighborhood);
                tvZipCode.setText(streetZipCode);
              }
            });
          }
          return null;
        }
      }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
  }

  private class SearchTask extends AsyncTask<String, Void, Address> {

    @Override protected void onPreExecute() {
      setAddressLoaderVisible(true);
    }

    @Override protected Address doInBackground(String... params) {
      try {
        return GeoUtils.search(params[0]);
      } catch (Exception e) {
        Crashlytics.logException(e);
        Log.e("ZUP", e.getMessage(), e);
        return null;
      }
    }

    @Override protected void onPostExecute(Address addr) {
      setAddressLoaderVisible(false);
      if (!isCancelled()) {
        animateCamera(addr);
      }
    }
  }

  private void animateCamera(Address addr, boolean useCurrentZoom) {
    if (map == null) {
      return;
    }
    float zoom = useCurrentZoom ? zoomAtual : 16f;
    if (addr != null) {
      try {
        CameraPosition p =
            new CameraPosition.Builder().target(new LatLng(addr.getLatitude(), addr.getLongitude()))
                .zoom(zoom)
                .build();
        CameraUpdate update = CameraUpdateFactory.newCameraPosition(p);
        map.animateCamera(update);
      } catch (IllegalStateException error) {
        error.printStackTrace();
      }
    }
  }

  private void animateCamera(Address addr) {
    animateCamera(addr, false);
  }

  private class GeocoderTask extends AsyncTask<Place, Void, Address> {

    @Override protected void onPreExecute() {
      setAddressLoaderVisible(true);
    }

    @Override protected Address doInBackground(Place... params) {
      try {
        return GeoUtils.getFromPlace(params[0]);
      } catch (Exception e) {
        Log.e("ZUP", e.getMessage(), e);
        return null;
      }
    }

    @Override protected void onPostExecute(Address addr) {
      setAddressLoaderVisible(false);
      animateCamera(addr);
    }
  }
}
