package com.lfdb.zuptecnico.fragments.inventory;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.InventoryItemImage;
import com.lfdb.zuptecnico.fragments.PickLocationDialog;
import com.lfdb.zuptecnico.util.FieldUtils;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Created by Renan on 14/01/2016.
 */
public class CreateInventoryItemSectionFragment extends Fragment
    implements PickLocationDialog.OnLocationSetListener, CreateInventoryPublisher,
    ImagePickerCallback {
  private static final String URL_PATTERN =
      "^(https?:\\/\\/)?([\\da-zA-Z0-9\\.-]+)\\.([a-zA-Z0-9\\.]{2,6})([\\/\\w \\.-]*)*\\/?$";
  private static final String EMAIL_PATTERN =
      "[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9A-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";

  int pickImageFieldId;
  String tempImagePath;
  private String addressTmp;

  ImagePicker mImagePicker;
  CameraImagePicker mCameraImagePicker;

  InventoryCategory.Section getSection() {
    return (InventoryCategory.Section) getArguments().getSerializable("section");
  }

  InventoryCategory getCategory() {
    return (InventoryCategory) getArguments().getSerializable("category");
  }

  boolean isCreateMode() {
    return getArguments().getBoolean("create_mode");
  }

  InventoryItem getItem() {
    return getArguments().getParcelable("item");
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    ViewGroup root =
        (ViewGroup) inflater.inflate(R.layout.fragment_inventory_details_section, container, false);
    fillData(root);
    return root;
  }

  public void refresh() {
    fillData((ViewGroup) getView());
  }

  public View validateSection(View error) {
    if (!isAdded()) {
      return null;
    }
    View firstFieldError = null;
    ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);

    int childCount = container.getChildCount();
    for (int i = 0; i < childCount; i++) {
      View child = container.getChildAt(i);

      if (child.getTag(R.id.inventory_item_create_fieldid) == null) continue;

      ViewGroup childContainer = (ViewGroup) child;

      int fieldId = ((Integer) child.getTag(R.id.inventory_item_create_fieldid)).intValue();
      InventoryCategory.Section.Field field = getCategory().getField(fieldId);

      TextView fieldTitle = (TextView) childContainer.findViewById(R.id.inventory_item_text_name);

      Object value = FieldUtils.getFieldValue(childContainer, field);

      if ((((field.required != null && field.required)) && (value == null)) || (value != null
          && !validateField(field, childContainer))) {
        fieldTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.field_label_error));
        if (error == null) {
          child.requestFocus();
          firstFieldError = child;
        } else {
          firstFieldError = error;
        }
      } else {
        fieldTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.offline_warning_text));
      }
    }

    return firstFieldError;
  }

  public InventoryItem createItemFromData(InventoryItem item) {
    if (!isAdded()) {
      return null;
    }
    ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);
    int childCount = container.getChildCount();

    for (int i = 0; i < childCount; i++) {
      View child = container.getChildAt(i);
      if (child.getTag(R.id.inventory_item_create_fieldid) == null) continue;

      ViewGroup childContainer = (ViewGroup) child;
      int fieldId = (Integer) childContainer.getTag(R.id.inventory_item_create_fieldid);

      Object value = FieldUtils.getFieldValue(childContainer, getCategory().getField(fieldId));
      if (value != null) item.setFieldValue(fieldId, value);
    }

    if (getSection().isLocationSection()) {
      item.position = new InventoryItem.Coordinates();
      if (item.getFieldValue(getCategory().getField("latitude").id) != null) {
        item.position.latitude =
            Float.parseFloat(item.getFieldValue(getCategory().getField("latitude").id).toString());
      }
      if (item.getFieldValue(getCategory().getField("longitude").id) != null) {
        item.position.longitude =
            Float.parseFloat(item.getFieldValue(getCategory().getField("longitude").id).toString());
      }
      item.address = String.valueOf(item.getFieldValue(getCategory().getField("address").id));
    }

    return item;
  }

  boolean validateField(InventoryCategory.Section.Field field, ViewGroup fieldView) {
    Object value = FieldUtils.getFieldValue(fieldView, field);
    if (value == null) return true;

    boolean validationResult;

    if (field.kind.equals("cpf")) {
      validationResult = value.toString().length() == "000.000.000-00".length();
    } else if (field.kind.equals("cnpj")) {
      validationResult = value.toString().length() == "00.000.000/0000-00".length();
    } else if (field.kind.equals("url")) {
      validationResult = value.toString().matches(URL_PATTERN);
    } else if (field.kind.equals("email")) {
      validationResult = value.toString().matches(EMAIL_PATTERN);
    } else {
      validationResult = true;
    }

    boolean minimumResult;
    boolean maximumResult;

    if (field.kind.equals("integer")
        || field.kind.equals("years")
        || field.kind.equals("months")
        || field.kind.equals("days")
        || field.kind.equals("hours")
        || field.kind.equals("seconds")) {
      int val = (Integer) value;
      if (field.minimum != null) {
        minimumResult = val >= field.minimum;
      } else {
        minimumResult = true;
      }

      if (field.maximum != null) {
        maximumResult = val <= field.maximum;
      } else {
        maximumResult = true;
      }
    } else if (field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals(
        "centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle")) {
      float val = (Float) value;
      if (field.minimum != null) {
        minimumResult = val >= field.minimum;
      } else {
        minimumResult = true;
      }

      if (field.maximum != null) {
        maximumResult = val <= field.maximum;
      } else {
        maximumResult = true;
      }
    } else {
      String val = value.toString();

      if (field.minimum != null) {
        minimumResult = val.length() >= field.minimum;
      } else {
        minimumResult = true;
      }

      if (field.maximum != null) {
        maximumResult = val.length() <= field.maximum;
      } else {
        maximumResult = true;
      }
    }

    boolean rangeResult = minimumResult && maximumResult;
    return validationResult && rangeResult;
  }

  void fillData(ViewGroup root) {
    if (getSection() == null || getCategory() == null) return;

    boolean createMode = isCreateMode();
    InventoryCategory.Section section = getSection();
    InventoryItem item = getItem();
    ViewGroup sectionParent = (ViewGroup) root.findViewById(R.id.container);
    ObjectMapper mapper = new ObjectMapper();

    TextView txtHeader = (TextView) root.findViewById(R.id.inventory_item_section_title);
    txtHeader.setText(section.title);
    section.sortFields();

    if (section.isLocationSection()) {
      ViewGroup fieldView = (ViewGroup) getActivity().getLayoutInflater()
          .inflate(R.layout.inventory_item_create_field_button, null, false);

      Button btn = (Button) fieldView.findViewById(R.id.inventory_item_create_field_button_button);

      Button fillPos = (Button) fieldView.findViewById(R.id.inventory_item_fill_pos_button);
      fillPos.setClickable(true);
      fillPos.setEnabled(false);
      fillPos.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          fillPos();
        }
      });

      btn.setClickable(true);
      btn.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          showMap();
        }
      });

      sectionParent.addView(fieldView);
    }

    for (int j = 0; j < section.fields.length; j++) {
      final InventoryCategory.Section.Field field = section.fields[j];
      View view = FieldUtils.createFieldView(sectionParent, mapper, getActivity(),
          getLayoutInflater(getArguments()), field, createMode, getItem(),
          new View.OnClickListener() {
            @Override public void onClick(View mView) {
              mView.setFocusable(true);
              mView.requestFocus();
              pickImageFieldId = field.id;
              pickImage();
            }
          });
      if (view == null) {
        continue;
      }
      sectionParent.addView(view);
    }

    mImagePicker = new ImagePicker(this);
    mImagePicker.setImagePickerCallback(this);

    mCameraImagePicker = new CameraImagePicker(this);
    mCameraImagePicker.setImagePickerCallback(this);
  }

  private void pickImage() {
    if (!isAdded()) {
      return;
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getString(R.string.add_image_title));
    builder.setItems(new String[] {
        getString(R.string.add_image_from_gallery), getString(R.string.add_image_from_camera)
    }, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        if (i == 0) {
          mImagePicker.pickImage();
        } else {
          tempImagePath = mCameraImagePicker.pickImage();
        }
      }
    });
    builder.show();
  }

  void showMap() {
    ViewGroup latitudeContainer = getFieldView("latitude");
    ViewGroup longitudeContainer = getFieldView("longitude");
    ViewGroup referenceContainner = getFieldView("field_ponto_de_referencia");
    ViewGroup postalCodeContainner = getFieldView("postal_code");
    ViewGroup streetNameContainner = getFieldView("address");
    ViewGroup neighborhoodContainner = getFieldView("district");
    TextView latitudeText =
        (TextView) latitudeContainer.findViewById(R.id.inventory_item_text_value);
    TextView longitudeText =
        (TextView) longitudeContainer.findViewById(R.id.inventory_item_text_value);

    String reference = "";
    String postalCode = "";
    String streetName = "";
    String neighborhood = "";
    String streetNumber = "";

    if (referenceContainner != null) {
      reference = referenceContainner.findViewById(R.id.inventory_item_text_value) != null
          ? ((EditText) referenceContainner.findViewById(R.id.inventory_item_text_value)).getText()
          .toString() : "";
    }

    if (postalCodeContainner != null) {
      postalCode = postalCodeContainner.findViewById(R.id.inventory_item_text_value) != null
          ? ((EditText) postalCodeContainner.findViewById(R.id.inventory_item_text_value)).getText()
          .toString() : "";
    }

    if (streetNameContainner != null) {
      streetName = streetNameContainner.findViewById(R.id.inventory_item_text_value) != null
          ? ((EditText) streetNameContainner.findViewById(R.id.inventory_item_text_value)).getText()
          .toString() : "";
      if (!TextUtils.isEmpty(streetName) && streetName.contains(",")) {
        String[] address = streetName.split(",");
        streetNumber = address[address.length - 1];
        streetName = address[0].replace("," + streetNumber, "");
      }
    }

    if (neighborhoodContainner != null) {
      neighborhood = neighborhoodContainner.findViewById(R.id.inventory_item_text_value) != null
          ? ((EditText) neighborhoodContainner.findViewById(
          R.id.inventory_item_text_value)).getText().toString() : "";
    }

    double latitude = 0, longitude = 0;
    boolean positionValid = false;
    try {
      latitude = Double.parseDouble(latitudeText.getText().toString());
      longitude = Double.parseDouble(longitudeText.getText().toString());
      positionValid = true;
    } catch (Exception ex) {
      // should we do anything?
    }

    Bundle args = new Bundle();
    args.putInt("inventory_category_id", getCategory().id);

    Address address = new Address(Locale.getDefault());

    if (positionValid) {
      address.setLongitude(longitude);
      address.setLatitude(latitude);
    }

    address.setThoroughfare(streetName);
    address.setFeatureName(streetNumber);
    address.setSubLocality(neighborhood);
    address.setPostalCode(postalCode);
    if (reference != null) {
      args.putString("reference", reference);
    }
    args.putParcelable("address", address);

    PickLocationDialog dialog = new PickLocationDialog();
    dialog.setArguments(args);
    dialog.setOnLocationSetListener(this);
    dialog.show(getActivity().getSupportFragmentManager(), "location_picker_dialog");
  }

  void fillPos() {
    if (!isAdded()) {
      return;
    }
    ViewGroup latitudeContainer = getFieldView("latitude");
    ViewGroup longitudeContainer = getFieldView("longitude");
    TextView latitudeText =
        (TextView) latitudeContainer.findViewById(R.id.inventory_item_text_value);
    TextView longitudeText =
        (TextView) longitudeContainer.findViewById(R.id.inventory_item_text_value);

    Location lastLocation = null;
    if (lastLocation == null) return;

    double latitude = lastLocation.getLatitude();
    double longitude = lastLocation.getLongitude();

    latitudeText.setText(Double.toString(latitude));
    longitudeText.setText(Double.toString(longitude));
  }

  private ViewGroup getFieldView(String fieldName) {
    View root = getView();
    if (root == null || !isAdded()) {
      return null;
    }

    ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
    for (int i = 0; i < container.getChildCount(); i++) {
      View child = container.getChildAt(i);
      if (child.getTag(R.id.inventory_item_create_fieldid) == null) continue;

      ViewGroup childContainer = (ViewGroup) child;

      int fieldId = (Integer) child.getTag(R.id.inventory_item_create_fieldid);
      InventoryCategory.Section.Field field = getCategory().getField(fieldId);
      if (field.title != null && field.title.equals(fieldName)) return childContainer;
    }

    return null;
  }

  @Override
  public void onLocationSet(double latitude, double longitude, Address address, String reference) {
    ViewGroup latitudeContainer = getFieldView("latitude");
    ViewGroup longitudeContainer = getFieldView("longitude");
    TextView latitudeText =
        (TextView) latitudeContainer.findViewById(R.id.inventory_item_text_value);
    TextView longitudeText =
        (TextView) longitudeContainer.findViewById(R.id.inventory_item_text_value);

    latitudeText.setText(Double.toString(latitude));
    longitudeText.setText(Double.toString(longitude));

    fillAddress(address);
  }

  void addImage(String path) {
    Bitmap bitmap = BitmapFactory.decodeFile(path);

    ViewGroup fieldView = getFieldView(pickImageFieldId);
    if (fieldView == null) {
      return;
    }
    final ViewGroup container =
        (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);
    InventoryItemImage imageData = new InventoryItemImage();
    imageData.url = path;
    imageData.content = path;

    final View image =
        FieldUtils.addImage(LayoutInflater.from(getActivity()), container, imageData, bitmap);

    if (image != null) {
      container.addView(image);
    }
  }

  private ViewGroup getFieldView(int id) {
    View root = getView();
    if (root == null || !isAdded()) {
      return null;
    }
    ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
    for (int i = 0; i < container.getChildCount(); i++) {
      View child = container.getChildAt(i);
      if (child.getTag(R.id.inventory_item_create_fieldid) == null) continue;

      ViewGroup childContainer = (ViewGroup) child;

      int fieldId = ((Integer) child.getTag(R.id.inventory_item_create_fieldid)).intValue();
      if (fieldId == id) {
        return childContainer;
      }
    }

    return null;
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
      if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        if (result != null) {
          Exception error = result.getError();
          onError(error);
        }
      }
      return;
    }
    switch (requestCode) {
      case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        Uri resultUri = result.getUri();
        String path = resultUri.getPath();
        if (path == null) {
          tempImagePath = null;
          return;
        }

        try {
          File file = new File(path);
          if (file.exists()) {
            addImage(path);
          } else {
            tempImagePath = null;
            return;
          }
        } catch (NullPointerException e) {
          tempImagePath = null;
          return;
        }
        break;
      case Picker.PICK_IMAGE_DEVICE:
        if (mImagePicker == null) {
          mImagePicker = new ImagePicker(this);
          mImagePicker.setImagePickerCallback(this);
        }
        mImagePicker.submit(data);
        break;
      case Picker.PICK_IMAGE_CAMERA:
        if (mCameraImagePicker == null) {
          mCameraImagePicker = new CameraImagePicker(this, tempImagePath);
          mCameraImagePicker.setImagePickerCallback(this);
        }
        mCameraImagePicker.submit(data);
        break;
    }
  }

  void fillAddress(Address addressData) {
    if (addressData == null) addressData = new Address(Locale.getDefault());

    addressTmp = "";
    if (addressData.getThoroughfare() != null) addressTmp += addressData.getThoroughfare() + ", ";
    if (addressData.getFeatureName() != null) addressTmp += addressData.getFeatureName();

    final String address = addressTmp;
    final String city = addressData.getSubAdminArea();
    final String state = addressData.getAdminArea();
    final String postalCode = addressData.getPostalCode();
    final String district = addressData.getSubLocality();

    View addressView = getFieldView("address");
    TextView addressText = (TextView) addressView.findViewById(R.id.inventory_item_text_value);
    View postalCodeView = getFieldView("postal_code");
    TextView postalCodeText =
        (TextView) postalCodeView.findViewById(R.id.inventory_item_text_value);
    View districtView = getFieldView("district");
    TextView districtText = (TextView) districtView.findViewById(R.id.inventory_item_text_value);
    View cityView = getFieldView("city");
    TextView cityText = (TextView) cityView.findViewById(R.id.inventory_item_text_value);
    View stateView = getFieldView("state");
    TextView stateText = (TextView) stateView.findViewById(R.id.inventory_item_text_value);

    addressText.setText(address);
    postalCodeText.setText(postalCode);
    districtText.setText(district);
    cityText.setText(city);
    stateText.setText(state);
  }

  private void onError(Throwable e) {
    Crashlytics.logException(e);
    ZupApplication.toast(getView(), R.string.error_find_image);
    e.printStackTrace();
  }

  @Override public void onImagesChosen(List<ChosenImage> list) {
    if (list == null || list.isEmpty()) {
      return;
    }
    ChosenImage image = list.get(0);
    tempImagePath = image.getOriginalPath() == null ? tempImagePath : image.getOriginalPath();

    try {
      File imageFile = new File(tempImagePath);
      Uri imageUri = Uri.fromFile(imageFile);

      CropImage.activity(imageUri)
          .setActivityTitle(getString(R.string.edit_image))
          .start(getContext(), this);
    } catch (Exception e) {
      onError(e);
    }
  }

  @Override public void onError(String s) {
    Log.d("ERROR", "Camera: " + s);
  }
}
