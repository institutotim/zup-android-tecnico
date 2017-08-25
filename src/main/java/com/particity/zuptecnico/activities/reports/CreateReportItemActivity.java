package com.particity.zuptecnico.activities.reports;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.particity.zuptecnico.BuildConfig;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.sync.ChangeReportStatusSyncAction;
import com.particity.zuptecnico.api.sync.EditReportItemSyncAction;
import com.particity.zuptecnico.api.sync.PublishReportItemSyncAction;
import com.particity.zuptecnico.api.sync.SyncAction;
import com.particity.zuptecnico.config.Constants;
import com.particity.zuptecnico.entities.ImageItem;
import com.particity.zuptecnico.entities.Position;
import com.particity.zuptecnico.entities.ReportCategory;
import com.particity.zuptecnico.entities.ReportItem;
import com.particity.zuptecnico.entities.User;
import com.particity.zuptecnico.fragments.CreateUserDialog;
import com.particity.zuptecnico.fragments.PickLocationDialog;
import com.particity.zuptecnico.fragments.UserPickerDialog;
import com.particity.zuptecnico.fragments.reports.CreateReportImagesFragment;
import com.particity.zuptecnico.fragments.reports.ReportCategorySelectorDialog;
import com.particity.zuptecnico.fragments.reports.ReportStatusPickerDialog;
import com.particity.zuptecnico.util.GPSUtils;
import com.particity.zuptecnico.util.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class CreateReportItemActivity extends AppCompatActivity
    implements PickLocationDialog.OnLocationSetListener {
  public static final int RESULT_REPORT_CHANGED = 1;

  private ReportItem item;
  private ReportItem originalItem;
  private SyncAction action;
  private boolean isEdit;
  private int categoryId;
  private Integer caseResponsibleUserIdSelected;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_create_report_item);

    int categoryId = getIntent().getIntExtra("categoryId", -1);
    item = getIntent().getParcelableExtra("item");
    originalItem = new ReportItem(item);
    action = getIntent().getParcelableExtra("action");

    if (action == null && categoryId == -1 && this.item == null) {
      finish();
      return;
    }
    isEdit = categoryId == -1 && (item != null || (action != null
        && action instanceof EditReportItemSyncAction));
  }

  void createCustomFields() {
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category == null || category.custom_fields == null || category.custom_fields.length == 0) {
      return;
    }
    ViewGroup fieldsContainer = (ViewGroup) findViewById(R.id.custom_fields_container);
    fieldsContainer.removeAllViews();
    for (int index = 0; index < category.custom_fields.length; index++) {
      ReportCategory.CustomField field = category.custom_fields[index];
      TextView mTitle = new TextView(this);
      LinearLayout.LayoutParams params =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      params.topMargin = getResources().getDimensionPixelSize(R.dimen.report_card_padding_y);
      params.leftMargin = getResources().getDimensionPixelSize(R.dimen.report_list_item_icon);
      mTitle.setTextColor(ContextCompat.getColor(this, R.color.report_item_text_default));
      mTitle.setLayoutParams(params);
      mTitle.setText(field.title);

      fieldsContainer.addView(mTitle);

      EditText mValue =
          new EditText(new ContextThemeWrapper(this, R.style.ReportCardPropertyValue));
      mValue.setTag(field.id);
      LinearLayout.LayoutParams valueParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      valueParams.topMargin =
          getResources().getDimensionPixelSize(R.dimen.report_card_padding_item_left);
      valueParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.report_list_item_icon);
      mValue.setHint(field.title);
      if (item.custom_fields != null && item.custom_fields.containsKey(field.id)) {
        mValue.setText(item.custom_fields.get(field.id));
      }
      mValue.setSingleLine(!field.multiline);
      mValue.setLayoutParams(valueParams);
      if (isEdit() && !Zup.getInstance().getAccess().canEditReportItem(item.category_id)) {
        mValue.setEnabled(false);
      }
      mValue.setBackgroundColor(ContextCompat.getColor(this, R.color.editscreen_button));

      fieldsContainer.addView(mValue);

      View divider = new View(this);
      LinearLayout.LayoutParams dividerParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
              getResources().getDimensionPixelSize(R.dimen.divider_height));
      dividerParams.leftMargin = getResources().getDimensionPixelSize(R.dimen.sidebar_item_height);
      divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider_color));
      divider.setLayoutParams(dividerParams);

      fieldsContainer.addView(divider);
    }
  }

  public HashMap<Integer, String> getCustomFieldsValues() {
    HashMap<Integer, String> customFields = new HashMap<>();
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category.custom_fields == null || category.custom_fields.length == 0) {
      return customFields;
    }
    ViewGroup fieldsContainer = (ViewGroup) findViewById(R.id.custom_fields_container);
    for (int index = 0; index < category.custom_fields.length; index++) {
      ReportCategory.CustomField field = category.custom_fields[index];
      EditText mValue = (EditText) fieldsContainer.findViewWithTag(field.id);
      customFields.put(field.id, mValue.getText().toString().trim());
    }
    return customFields;
  }

  void updateViewByPermissions() {
    if (!Zup.getInstance().getAccess().canEditReportItem(item.category_id)
        || BuildConfig.FLAVOR.equals("unicef")) {
      findViewById(R.id.create_report_user_remove).setVisibility(View.GONE);
      hideUserButtons();
    }
    if (isEdit() && Zup.getInstance().getAccess().canAlterReportItemStatus(item.category_id)) {
      findViewById(R.id.status_container).setVisibility(View.VISIBLE);

      if (!Zup.getInstance().getAccess().canEditReportItem(item.category_id)) {
        hideUserButtons();

        CreateReportImagesFragment imagesFragment =
            (CreateReportImagesFragment) getSupportFragmentManager().findFragmentById(R.id.images);
        if (imagesFragment != null) {
          FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
          transaction.hide(imagesFragment);
          transaction.commit();
          findViewById(R.id.images_title).setVisibility(View.GONE);
        }

        findViewById(R.id.report_description).setEnabled(false);
      }
    } else {
      findViewById(R.id.status_container).setVisibility(View.GONE);
    }
  }

  public void chooseStatus(View view) {
    ReportStatusPickerDialog dialog = new ReportStatusPickerDialog();
    dialog.show(getSupportFragmentManager(), "status_picker");
    Bundle bundle = new Bundle();
    bundle.putInt("category", item.category_id);
    if (item.status_id != -1) {
      bundle.putInt("status", item.status_id);
    }
    dialog.setArguments(bundle);
    dialog.setListener(new ReportStatusPickerDialog.OnReportStatusSetListener() {
      @Override public void onReportStatusSet(int selectedStatusId) {
        ReportCategory.Status status = Zup.getInstance()
            .getReportCategoryService()
            .getReportCategory(item.category_id)
            .getStatus(selectedStatusId);
        item.status_id = selectedStatusId;
        if (status != null) {
          setStatus(status.getTitle());
          if (status.getFlow() != null) {
            chooseCaseResponsibleUser(null);
          } else {
            findViewById(R.id.case_conductor_container).setVisibility(View.GONE);
          }
        }
      }
    });
  }

  public void setCaseConductor(int conductorId, @Nullable String title) {
    findViewById(R.id.case_conductor_container).setVisibility(View.VISIBLE);
    if (title != null) {
      ((TextView) findViewById(R.id.case_conductor_name)).setText(title);
    } else {
      Zup.getInstance()
          .showUsernameInto(((TextView) findViewById(R.id.case_conductor_name)), "", conductorId);
    }
    caseResponsibleUserIdSelected = conductorId;
  }

  Address parseAddressFromItem() {
    Address address = new Address(Locale.getDefault());
    String[] fullAddress = item.address.split(",");
    address.setThoroughfare(fullAddress[0]);
    if (!TextUtils.isEmpty(item.number)) {
      address.setFeatureName(this.item.number);
    } else if (fullAddress.length > 1) {
      address.setFeatureName(fullAddress[1]);
    }

    address.setSubLocality(this.item.district);
    address.setSubAdminArea(this.item.city);
    address.setAdminArea(this.item.state);
    address.setCountryName(this.item.country);
    address.setPostalCode(this.item.postalCode);
    if (item.position == null) {
      address.setLatitude(Constants.DEFAULT_LAT);
      address.setLongitude(Constants.DEFAULT_LON);
      return address;
    }
    address.setLatitude(item.position.latitude);
    address.setLongitude(item.position.longitude);

    return address;
  }

  boolean isEdit() {
    return isEdit;
  }

  void setCategoryId(int id) {
    if (id == -1) {
      return;
    }
    item.category_id = id;
    createCustomFields();

    TextView txtCategory = (TextView) findViewById(R.id.category_title);
    ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(id);

    txtCategory.setText(category.title);
  }

  public void chooseCategory(View sender) {
    if (isEdit() && Zup.getInstance().getAccess().canEditReportItem(item.category_id)) {
      showCreateDialog();
    }
  }

  public void chooseLocation(View sender) {
    if (isEdit() && !(Zup.getInstance().getAccess().canEditReportItem(item.category_id))) {
      return;
    }
    if (Utilities.isConnected(this)) {
      chooseLocation();
    } else {
      showOfflineLocationDialog();
    }
  }

  public void chooseLocation() {
    Bundle args = new Bundle();
    args.putInt("categoryId", item.category_id);
    if (item.position != null) {
      args.putParcelable("address", parseAddressFromItem());
    }
    args.putString("reference", item.reference);

    PickLocationDialog dialog = new PickLocationDialog();
    dialog.setArguments(args);
    dialog.setOnLocationSetListener(this);
    dialog.show(getSupportFragmentManager(), "location_picker_dialog");
  }

  void showOfflineLocationDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.no_internet_location_title));
    //builder.setMessage(getString(R.string.no_internet_location_desc));

    String[] items = new String[2];
    items[0] = getString(R.string.try_again);
    items[1] = getString(R.string.manual_address_option);

    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
          case 0:
            chooseLocation(null);
            return;
          case 1:
            chooseLocation();
            return;
        }
      }
    });
    builder.show();
  }

  public void showCreateDialog() {
    ReportCategorySelectorDialog dialog = new ReportCategorySelectorDialog();
    Bundle bundle = new Bundle();
    bundle.putBoolean("isEdit", true);
    dialog.setArguments(bundle);
    dialog.show(getSupportFragmentManager(), "category_list");
    dialog.setListener(new ReportCategorySelectorDialog.OnReportCategorySetListener() {
      @Override public void onReportCategorySet(int categoryId) {
        setCategoryId(categoryId);
        item.status_id = -1;
        if (isEdit()) {
          chooseStatus(null);
        }
      }
    });
  }

  @Override
  public void onLocationSet(double latitude, double longitude, Address address, String reference) {
    setLocation(latitude, longitude, address, reference);
  }

  void setLocation(double latitude, double longitude, Address address, String reference) {
    TextView txtAddress = (TextView) findViewById(R.id.full_address);
    item.reference = reference;
    address.setLatitude(latitude);
    address.setLongitude(longitude);
    updateItemByAddress(address);
    if (address != null) {
      txtAddress.setText(GPSUtils.formatAddress(address));
      address.setLatitude(latitude);
      address.setLongitude(longitude);
    } else {
      txtAddress.setText(null);
    }
  }

  @Override protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putParcelable("item", item);
    outState.putParcelable("action", action);
    outState.putBoolean("isEdit", isEdit);
    outState.putInt("categoryId", categoryId);
  }

  @Override protected void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    if (savedInstanceState == null) {
      return;
    }

    item = savedInstanceState.containsKey("item") ? (ReportItem) savedInstanceState.getParcelable(
        "item") : null;
    action =
        savedInstanceState.containsKey("action") ? (SyncAction) savedInstanceState.getParcelable(
            "action") : null;
    isEdit = savedInstanceState.getBoolean("isEdit");
    categoryId = savedInstanceState.getInt("categoryId");
  }

  @Override protected void onResume() {
    super.onResume();
    if (action != null) {
      if (action instanceof EditReportItemSyncAction) {
        item = ((EditReportItemSyncAction) action).convertToReportItem();
        if (((EditReportItemSyncAction) action).caseConductorId != -1) {
          setCaseConductor(((EditReportItemSyncAction) action).caseConductorId, null);
        }
      } else if (action instanceof PublishReportItemSyncAction) {
        item = ((PublishReportItemSyncAction) action).convertToReportItem();
      }
    }

    if (this.item != null) {
      setCategoryId(this.item.category_id);
      if (item.position != null) {
        setLocation(this.item.position.latitude, this.item.position.longitude,
            parseAddressFromItem(), this.item.reference);
      }
      assignUser(this.item.user);
      setImages(this.item.images);
      setDescription(this.item.description);
      if (isEdit) {
        categoryId = item.category_id;
        ReportCategory.Status status = Zup.getInstance()
            .getReportCategoryService()
            .getReportCategory(item.category_id)
            .getStatus(item.status_id);
        if (status != null) {
          setStatus(status.getTitle());
        }
      }
      findViewById(R.id.create_report_user_remove).setVisibility(View.GONE);
    } else {
      item = new ReportItem();
      setCategoryId(getIntent().getIntExtra("categoryId", -1));
    }
    updateDescriptionView();
    updateViewByPermissions();
  }

  void updateDescriptionView() {
    TextWatcher watcher = new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (item == null) {
          return;
        }
        item.description = s.toString();
      }

      @Override public void afterTextChanged(Editable s) {

      }
    };
    ((EditText) findViewById(R.id.report_description)).addTextChangedListener(watcher);
  }

  void setStatus(String title) {
    ((TextView) findViewById(R.id.status_desc)).setText(title);
  }

  public void assignToMe(View sender) {
    assignUser(Zup.getInstance().getSessionUser());
  }

  public void selectUser(View sender) {
    UserPickerDialog dialog = new UserPickerDialog();
    dialog.show(getSupportFragmentManager(), "user_picker");
    dialog.setListener(new UserPickerDialog.OnUserPickedListener() {
      @Override public void onUserPicked(User user) {
        assignUser(user);
      }
    });
  }

  public void chooseCaseResponsibleUser(View sender) {
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category == null) {
      return;
    }
    ReportCategory.Status status = category.getStatus(item.status_id);
    if (status == null || status.getFlow() == null || status.getResponsibleGroupId() == null) {
      return;
    }
    Bundle bundle = new Bundle();
    bundle.putString("header", getString(R.string.case_conductor_report));
    bundle.putInt("groupId", status.getResponsibleGroupId());
    if (caseResponsibleUserIdSelected != null) {
      bundle.putInt("selectedUserId", caseResponsibleUserIdSelected);
    }
    UserPickerDialog dialog = new UserPickerDialog();
    dialog.setArguments(bundle);
    dialog.show(getSupportFragmentManager(), "user_picker");
    dialog.setCancelable(false);
    dialog.setListener(new UserPickerDialog.OnUserPickedListener() {
      @Override public void onUserPicked(User user) {
        setCaseConductor(user.id, user.name);
      }
    });
  }

  public void createUser(View sender) {
    CreateUserDialog dialog = new CreateUserDialog();
    dialog.show(getSupportFragmentManager(), "user_creator");
    dialog.setListener(new CreateUserDialog.OnUserCreatedListener() {
      @Override public void onUserCreated(User user) {
        assignUser(user);
      }
    });
  }

  public void removeUser(View sender) {
    assignUser(null);
  }

  void assignUser(User user) {
    item.user = user;
    TextView txtUserName = (TextView) findViewById(R.id.create_report_user_name);
    View btnRemove = findViewById(R.id.create_report_user_remove);

    if (user != null && !BuildConfig.FLAVOR.equals("unicef")) {
      hideUserButtons();

      txtUserName.setVisibility(View.VISIBLE);
      btnRemove.setVisibility(View.VISIBLE);
      txtUserName.setText(user.name);
    } else {
      showUserButtons();
      txtUserName.setVisibility(View.GONE);
      btnRemove.setVisibility(View.GONE);
    }
  }

  void hideUserButtons() {
    findViewById(R.id.choose_assigner).setVisibility(View.GONE);
    findViewById(R.id.create_report_button_assign_me).setVisibility(View.GONE);
    findViewById(R.id.create_report_button_select_user).setVisibility(View.GONE);
    findViewById(R.id.create_report_button_create_user).setVisibility(View.GONE);
  }

  void showUserButtons() {
    findViewById(R.id.choose_assigner).setVisibility(View.VISIBLE);
    findViewById(R.id.create_report_button_assign_me).setVisibility(View.VISIBLE);
    findViewById(R.id.create_report_button_select_user).setVisibility(View.VISIBLE);
    findViewById(R.id.create_report_button_create_user).setVisibility(View.VISIBLE);
  }

  String getDescription() {
    return ((EditText) findViewById(R.id.report_description)).getText().toString();
  }

  void setDescription(String text) {
    ((EditText) findViewById(R.id.report_description)).setText(text);
  }

  void setImages(ReportItem.Image[] images) {
    if (images == null) {
      return;
    }

    CreateReportImagesFragment imagesFragment =
        (CreateReportImagesFragment) getSupportFragmentManager().findFragmentById(R.id.images);
    for (int i = 0; i < images.length; i++) {
      imagesFragment.addImage(images[i]);
    }
  }

  void updateItemByAddress(Address address) {
    item.district = address.getSubLocality();
    item.city = address.getSubAdminArea();
    item.state = address.getAdminArea();
    item.country = address.getCountryName();
    item.postalCode = address.getPostalCode();
    item.position = new Position(address.getLatitude(), address.getLongitude());
    item.number = address.getFeatureName();
    item.address = address.getThoroughfare();
  }

  boolean hasValidAddress() {
    if (TextUtils.isEmpty(item.address)) {
      return false;
    }
    if (item.position == null) {
      return false;
    }
    if (item.position.latitude == 0 || item.position.longitude == 0) {
      return false;
    }
    return true;
  }

  private boolean hasChangedAddress() {
    return (!(item.position.equals(originalItem.position) && item.getFullAddress()
        .equals(originalItem.getFullAddress())));
  }

  public void complete(View sender) {
    if (!hasValidAddress()) {
      new AlertDialog.Builder(this).setMessage(getString(R.string.report_creation_failed_address))
          .show();

      return;
    }

    CreateReportImagesFragment imagesFragment =
        (CreateReportImagesFragment) getSupportFragmentManager().findFragmentById(R.id.images);

    ReportItem.Image[] imgs = new ReportItem.Image[imagesFragment.getAddableCount()];
    for (int i = 0, j = 0; i < imagesFragment.getCount(); i++) {
      ImageItem base64 = imagesFragment.getImageItemAt(i);
      if (base64 != null) {
        imgs[j] = new ReportItem.Image();
        imgs[j].setData(base64);
        j++;
      }
    }

    SyncAction mAction;

    if (isEdit()) {
      int caseConductorId =
          caseResponsibleUserIdSelected == null ? -1 : caseResponsibleUserIdSelected;
      if (!Zup.getInstance().getAccess().canEditReportItem(item.category_id)) {
        mAction = new ChangeReportStatusSyncAction(item.id, item.category_id, item.status_id, caseConductorId);
      } else {

        mAction =
            new EditReportItemSyncAction(item.id, item.position.latitude, item.position.longitude,
                categoryId, getDescription(), item.reference, item.address, item.number,
                item.postalCode, item.district, item.city, item.state, item.country, imgs,
                item.user, item.category_id, item.status_id, getCustomFieldsValues(),
                hasChangedAddress(), caseConductorId);

        ArrayList<ReportItem.Image> newImages = new ArrayList<>();
        if (item.images != null) {
          for (int i = 0; i < item.images.length; i++) {
            newImages.add(item.images[i]);
          }
        }

        for (int i = 0; i < imagesFragment.getCount(); i++) {
          ImageItem item = imagesFragment.getImageItemAt(i);
          if (item != null) {
            newImages.add(new ReportItem.Image(item));
          }
        }

        ReportItem.Image[] newArray = new ReportItem.Image[newImages.size()];
        newImages.toArray(newArray);

        item.images = newArray;
      }
    } else {
      mAction = new PublishReportItemSyncAction(item.position.latitude, item.position.longitude,
          item.category_id, getDescription(), item.reference, item.address, item.number,
          item.postalCode, item.district, item.city, item.state, item.country, imgs, item.user,
          getCustomFieldsValues());
    }
    if (action != null) {
      Zup.getInstance().getSyncActionService().removeSyncAction(action.getId());
    }
    Zup.getInstance().getSyncActionService().addSyncAction(mAction);

    if (Utilities.isConnected(this)) {
      if (!isEdit()) {
        ZupApplication.toast(findViewById(android.R.id.content),
            getString(R.string.creating_report)).show();
      }
      Zup.getInstance().sync();
      finish();
      return;
    }

    Intent intent;
    if (isEdit()) {
      intent = new Intent();
      intent.putExtra("item", item);
      setResult(RESULT_REPORT_CHANGED, intent);
    } else {
      intent = new Intent(this, ReportItemDetailsActivity.class);
      intent.putExtra("action", mAction);
      startActivity(intent);
    }
    finish();
  }
}
