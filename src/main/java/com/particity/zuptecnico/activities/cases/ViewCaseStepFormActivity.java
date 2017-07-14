package com.particity.zuptecnico.activities.cases;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.sync.FillCaseStepSyncAction;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.Group;
import com.particity.zuptecnico.entities.InventoryCategory;
import com.particity.zuptecnico.entities.InventoryItem;
import com.particity.zuptecnico.entities.InventoryItemImage;
import com.particity.zuptecnico.entities.ReportCategory;
import com.particity.zuptecnico.entities.ReportItem;
import com.particity.zuptecnico.entities.User;
import com.particity.zuptecnico.entities.collections.SingleInventoryItemCollection;
import com.particity.zuptecnico.entities.requests.UpdateCaseStepRequest;
import com.particity.zuptecnico.fragments.GroupPickerDialog;
import com.particity.zuptecnico.fragments.UserPickerDialog;
import com.particity.zuptecnico.fragments.cases.InventoryPickerDialog;
import com.particity.zuptecnico.fragments.cases.ReportPickerDialog;
import com.particity.zuptecnico.ui.UIHelper;
import com.particity.zuptecnico.util.CaseFieldDialogUtils;
import com.particity.zuptecnico.util.CaseFieldUtils;
import com.particity.zuptecnico.util.FieldUtils;
import com.particity.zuptecnico.util.FileUtils;
import com.particity.zuptecnico.util.IOUtil;
import com.particity.zuptecnico.util.Utilities;
import com.particity.zuptecnico.util.ViewUtils;
import com.obsez.android.lib.filechooser.ChooserDialog;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ViewCaseStepFormActivity extends AppCompatActivity
    implements Callback<SingleInventoryItemCollection>, ImagePickerCallback {
  private static final String URL_PATTERN =
      "^(https?:\\/\\/)?([\\da-zA-Z0-9\\.-]+)\\.([a-zA-Z0-9\\.]{2,6})([\\/\\w \\.-]*)*\\/?$";
  private static final String EMAIL_PATTERN =
      "[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9A-Z!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9A-Z](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";

  Case theCase;
  Case.Step caseStep;
  int syncActionId = -1;

  int pickImageFieldId;
  String tempImagePath;
  View pickImageFieldView;
  InventoryItem inventoryItem;

  ImagePicker mImagePicker;
  CameraImagePicker mCameraImagePicker;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_case_step_form);

    Zup.getInstance().initStorage(this);
    UIHelper.initActivity(this);

    theCase = getIntent().getParcelableExtra("case");
    int stepId = getIntent().getIntExtra("stepId", -1);
    syncActionId = getIntent().getIntExtra("action", -1);

    if (theCase == null || stepId == -1) {
      finish();
      return;
    }

    caseStep = theCase.getStep(stepId);
    fillData();

    findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        goOnline();
      }
    });
    if (Utilities.isConnected(this)) {
      hideNoConnectionBar();
    } else {
      showNoConnectionBar();
    }

    mImagePicker = new ImagePicker(this);
    mImagePicker.setImagePickerCallback(this);

    mCameraImagePicker = new CameraImagePicker(this);
    mCameraImagePicker.setImagePickerCallback(this);
  }

  void goOnline() {
    if (!Utilities.isConnected(this)) {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
          .show();
      return;
    }
    hideNoConnectionBar();
  }

  void showNoConnectionBar() {
    findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
  }

  void hideNoConnectionBar() {
    findViewById(R.id.offline_warning).setVisibility(View.GONE);
  }

  void fillData() {
    ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
    container.removeAllViews();

    Flow.Step step = caseStep.flowStep;
    container.addView(createEditableResponsibleUserView());
    container.addView(FieldUtils.createSectionHeader(container, getLayoutInflater(), step.title));

    Object value = caseStep.getDataFieldByType("inventory_item");
    if (value != null && value instanceof List) {
      List items = (List) value;
      if (!items.isEmpty()) {
        loadInventoryItem((Integer) items.get(0));
      }
    }

    for (int i = 0; i < step.fields.size(); i++) {
      Flow.Step.Field field = step.fields.get(i);

      View view = createFieldView(field, container);
      if (view != null) {
        container.addView(view);
      }
    }
  }

  private void loadInventoryItem(Integer id) {
    if (Zup.getInstance().getInventoryItemService().getInventoryItem(id * -1) != null) {
      inventoryItem = Zup.getInstance().getInventoryItemService().getInventoryItem(id * -1);
    }
  }

  View createFieldView(final Flow.Step.Field field, ViewGroup sectionParent) {
    View view;
    ObjectMapper mapper = new ObjectMapper();
    String label = field.title;
    if (!field.active) {
      return null;
    }

    if (field.field_type.equals("radio")) {
      view = CaseFieldUtils.createRadiosForField(sectionParent, mapper, label, this,
          getLayoutInflater(), field, theCase);
    } else if (field.field_type.equals("checkbox")) {
      view = CaseFieldUtils.createCheckboxesForField(sectionParent, mapper, label, this,
          getLayoutInflater(), field, theCase);
    } else if (field.field_type.equals("image")) {
      view = CaseFieldUtils.createImagesField(sectionParent, label, mapper, theCase, field,
          getLayoutInflater(), new View.OnClickListener() {
            @Override public void onClick(View view) {
              view.setFocusable(true);
              view.requestFocus();
              pickImageFieldId = field.id;
              pickImageFieldView = (View) view.getParent();
              pickImage();
            }
          });
    } else if (field.field_type.equals("decimal")
        || field.field_type.equals("integer")
        || field.field_type.equals("meter")
        || field.field_type.equals("centimeter")
        || field.field_type.equals("kilometer")
        || field.field_type.equals("year")
        || field.field_type.equals("month")
        || field.field_type.equals("day")
        || field.field_type.equals("hour")
        || field.field_type.equals("second")
        || field.field_type.equals("angle")) {
      view =
          CaseFieldUtils.createNumberField(sectionParent, this, label, field, getLayoutInflater(),
              caseStep);
    } else if (field.field_type.equals("select")) {
      view =
          CaseFieldUtils.createSelectField(sectionParent, mapper, label, field, getLayoutInflater(),
              caseStep, new View.OnClickListener() {
                @Override public void onClick(View view) {
                  CaseFieldDialogUtils.createSelectDialog(ViewCaseStepFormActivity.this, field,
                      view).show();
                }
              });
    } else if (field.field_type.equals("previous_field")) {
      view = CaseFieldUtils.createPreviousField(sectionParent, mapper, this, label,
          getLayoutInflater(), theCase.getField(field.origin_field_id), theCase);
    } else if (field.field_type.equals("date")) {
      view =
          CaseFieldUtils.createDateField(sectionParent, label, field, getLayoutInflater(), caseStep,
              new View.OnClickListener() {
                @Override public void onClick(View view) {
                  CaseFieldDialogUtils.createDatePickerDialog(ViewCaseStepFormActivity.this, field,
                      view).show();
                }
              });
    } else if (field.field_type.equals("time")) {
      view =
          CaseFieldUtils.createTimeField(sectionParent, label, field, getLayoutInflater(), caseStep,
              new View.OnClickListener() {
                @Override public void onClick(View view) {
                  CaseFieldDialogUtils.createTimePickerDialog(ViewCaseStepFormActivity.this, field,
                      view).show();
                }
              });
    } else if (field.field_type.equals("cpf") || field.field_type.equals("cnpj")) {
      view = CaseFieldUtils.createCPForCNPJField(sectionParent, label, field, getLayoutInflater(),
          caseStep);
    } else if (field.field_type.equals("url") || field.field_type.equals("email")) {
      view = CaseFieldUtils.createURLorEmailField(sectionParent, label, field, getLayoutInflater(),
          caseStep);
    } else if (field.field_type.equals("inventory_item")) {
      view = getLayoutInflater().inflate(R.layout.case_item_field_add, null);
      final View inventoryItemSelectView = view;
      view.setTag(R.id.tag_field_id, field.id);
      TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
      TextView buttonAdd = (TextView) view.findViewById(R.id.button_add_item);
      CheckBox newItem = (CheckBox) view.findViewById(R.id.new_item);
      if (field.multiple) {
        newItem.setVisibility(View.GONE);
      } else {
        newItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
              ViewGroup container =
                  (ViewGroup) inventoryItemSelectView.findViewById(R.id.report_container);
              container.removeAllViews();
              inventoryItemSelectView.setTag(R.id.tag_button_value, new ArrayList<Integer>());
            }
          }
        });
      }
      buttonAdd.setText(R.string.add_inventory_title);

      title.setText(field.title);

      buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View bview) {
          if (!Utilities.isConnected(ViewCaseStepFormActivity.this)) {
            ZupApplication.toast(findViewById(android.R.id.content),
                R.string.error_no_internet_toast).show();
            return;
          }
          showInventoryItemSearch(field.category_inventory, field, inventoryItemSelectView);
        }
      });
      Object valueList = caseStep.getDataFieldValue(field.id);
      if (valueList instanceof List<?>) {
        List<Integer> values = (List<Integer>) valueList;
        for (Integer id : values) {
          createInventoryItemView(inventoryItemSelectView, id);
        }
        inventoryItemSelectView.setTag(R.id.tag_button_value, values);
      }
    } else if (field.field_type.equals("report_item")) {
      view = getLayoutInflater().inflate(R.layout.case_report_field_add, null);
      final View reportItemSelectView = view;
      view.setTag(R.id.tag_field_id, field.id);
      TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
      View buttonAdd = view.findViewById(R.id.button_add_item);
      title.setText(field.title);

      buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View bview) {
          if (!Utilities.isConnected(ViewCaseStepFormActivity.this)) {
            ZupApplication.toast(findViewById(android.R.id.content),
                R.string.error_no_internet_toast).show();
            return;
          }
          showReportItemSearch(reportItemSelectView);
        }
      });
    } else if (field.field_type.equals("inventory_field")) {
      view = FieldUtils.createFieldView(sectionParent, mapper, this, getLayoutInflater(),
          field.category_inventory_field, false, inventoryItem, new View.OnClickListener() {
            @Override public void onClick(View mView) {
              mView.setFocusable(true);
              mView.requestFocus();
              pickImageFieldId = field.id;
              pickImageFieldView = (View) mView.getParent();
              pickImage();
            }
          });
      if (view != null) {
        view.setTag(R.id.tag_field_id, field.id);
      }
    } else if (field.field_type.equals("attachment")) {
      view = getLayoutInflater().inflate(R.layout.case_report_field_add, null);
      final View attachmentItemSelectView = view;
      view.setTag(R.id.tag_field_id, field.id);
      ((TextView) view.findViewById(R.id.inventory_item_text_name)).setText(field.title);
      TextView buttonAdd = (TextView) view.findViewById(R.id.button_add_item);
      buttonAdd.setText(R.string.search_view_hint);

      buttonAdd.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View bview) {
          showAttachmentFieldSearch(attachmentItemSelectView);
        }
      });

      if (!FieldUtils.isListOfString((List<?>) caseStep.getDataFieldValue(field.id))) {
        InventoryItemImage[] data =
            mapper.convertValue(caseStep.getDataFieldValue(field.id), InventoryItemImage[].class);
        for (int index = 0; index < data.length; index++) {
          ViewGroup container = (ViewGroup) view.findViewById(R.id.report_container);
          container.addView(
              CaseFieldUtils.addAttachment(getLayoutInflater(), container, data[index]));
        }
        view.setTag(R.id.tag_button_value, data);
      } else if (caseStep.getAttachmentDataField(field.id) != null) {
        Case.Step.DataField.FileAttachment[] data = caseStep.getAttachmentDataField(field.id);
        for (int index = 0; index < data.length; index++) {
          ViewGroup container = (ViewGroup) view.findViewById(R.id.report_container);
          container.addView(
              CaseFieldUtils.addAttachment(getLayoutInflater(), container, data[index]));
        }
        view.setTag(R.id.tag_button_value, data);
      }
    } else {
      view = CaseFieldUtils.createTextField(sectionParent, this, label, field, getLayoutInflater(),
          caseStep);
    }

    return view;
  }

  private void pickImage() {
    if (isFinishing()) {
      return;
    }
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

  private void onError(Throwable e) {
    Crashlytics.logException(e);
    ZupApplication.toast(findViewById(android.R.id.content), R.string.error_find_image);
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

      CropImage.activity(imageUri).setActivityTitle(getString(R.string.edit_image)).start(this);
    } catch (Exception e) {
      onError(e);
    }
  }

  @Override public void onError(String s) {
    Log.d("ERROR", "Camera: " + s);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    }
  }

  void addImage(String path) {
    Bitmap bitmap = BitmapFactory.decodeFile(path);

    ViewGroup fieldView = (ViewGroup) pickImageFieldView;
    if (fieldView == null) {
      return;
    }
    final ViewGroup container =
        (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);
    InventoryItemImage imageData = new InventoryItemImage();
    imageData.url = path;
    final View image =
        CaseFieldUtils.addImage(LayoutInflater.from(this), container, imageData, bitmap);

    if (image != null) {
      container.addView(image);
    }
  }

  void showInventoryItemSearch(InventoryCategory[] categories, Flow.Step.Field field,
      final View view) {
    if (view == null) {
      return;
    }
    InventoryPickerDialog dialog = new InventoryPickerDialog();

    int fieldId = (Integer) view.getTag(R.id.tag_field_id);
    final Object value =
        view.getTag(R.id.tag_button_value) == null ? caseStep.getDataFieldValue(fieldId)
            : view.getTag(R.id.tag_button_value);

    if (value != null && value instanceof List<?>) {
      dialog.setSelectedItems(new ObjectMapper().convertValue(value, Integer[].class));
    }
    dialog.setMultiple(field.multiple);
    dialog.setCategories(categories);
    dialog.show(getSupportFragmentManager(), "inventory_picker");
    dialog.setListener(new InventoryPickerDialog.OnInventoriesMultiSelectPickedListener() {
      @Override public void onInventoriesPicked(List<InventoryItem> items) {
        ViewGroup container = (ViewGroup) view.findViewById(R.id.report_container);
        List<Integer> values = new ArrayList<>();
        if (container == null) {
          return;
        }
        removeFakeItems(view);
        container.removeAllViews();
        for (int index = 0; index < items.size(); index++) {
          InventoryItem item = items.get(index);
          createInventoryItemView(view, items.get(index));
          if (!values.contains((Integer) item.id)) {
            values.add((Integer) item.id);
          }
        }
        if (items.size() > 0) {
          updateInventoryFields(items.get(items.size() - 1).id);
          ((CheckBox) view.findViewById(R.id.new_item)).setChecked(false);
        } else {
          ((CheckBox) view.findViewById(R.id.new_item)).setChecked(true);
        }

        view.setTag(R.id.tag_button_value, values);
      }

      private void removeFakeItems(View view) {
        Object values = view.getTag(R.id.tag_button_value);
        if (values != null && (values instanceof List)) {
          List ids = (List) values;
          if (ids.isEmpty()) return;
          for (int index = 0; index < ids.size(); index++) {
            Integer id = (Integer) ids.get(index);
            Zup.getInstance().getInventoryItemService().removeFakeItem(id * -1);
          }
        }
      }
    });
  }

  void updateInventoryFields(int itemId) {
    InventoryItem item = Zup.getInstance().getInventoryItemService().getInventoryItem(itemId * -1);
    if (item == null) {
      Zup.getInstance().getService().getInventoryItem(itemId, this);
      return;
    }
    inventoryItem = item;
    updateInventoryFields();
  }

  void createInventoryItemView(final View parentContainer, Object identifier) {
    final ViewGroup parent = (ViewGroup) parentContainer.findViewById(R.id.report_container);
    final View view = getLayoutInflater().inflate(R.layout.case_inventory_item_item, null);

    TextView name = (TextView) view.findViewById(R.id.case_inventory_item_item_name);
    if (identifier instanceof InventoryItem) {
      InventoryItem item = (InventoryItem) identifier;
      view.setTag(R.id.tag_item_id, item.id);
      name.setText(item.title);
    } else if (identifier instanceof Integer) {
      view.setTag(R.id.tag_item_id, identifier);
      List<Integer> ids = new ArrayList<>();
      ids.add((Integer) identifier);
      Zup.getInstance().showInventoryItemInto(name, "", ids);
    } else {
      return;
    }
    View remove = view.findViewById(R.id.case_inventory_item_item_remove);
    remove.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Integer itemId = (Integer) view.getTag(R.id.tag_item_id);
        if (itemId == null) {
          return;
        }
        CheckBox newItem = (CheckBox) parentContainer.findViewById(R.id.new_item);
        parent.removeView(view);

        Object value = parentContainer.getTag(R.id.tag_button_value);
        if (value == null && !(value instanceof List<?>)) {
          parentContainer.setTag(R.id.tag_button_value, new ArrayList<Integer>());
          newItem.setChecked(true);
          return;
        }
        List<Integer> values = (List<Integer>) value;
        for (int index = 0; index < values.size(); index++) {
          if (values.get(index).equals(itemId)) {
            values.remove(index);
            break;
          }
        }
        if (values.isEmpty()) {
          newItem.setChecked(true);
        }
        parentContainer.setTag(R.id.tag_button_value, values);
      }
    });
    parent.addView(view);
  }

  void showAttachmentFieldSearch(final View view) {
    if (view == null) {
      return;
    }
    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
    int fieldId = (Integer) view.getTag(R.id.tag_field_id);
    Object value = view.getTag(R.id.tag_button_value) == null ? caseStep.getDataFieldValue(fieldId)
        : view.getTag(R.id.tag_button_value);
    if (value != null && value instanceof InventoryItemImage) {
      InventoryItemImage data = (InventoryItemImage) value;
      path = data.file_name.substring(0, data.file_name.lastIndexOf("/"));
    }
    new ChooserDialog().with(ViewCaseStepFormActivity.this)
        .withStartFile(path)
        .withChosenListener(new ChooserDialog.Result() {
          @Override public void onChoosePath(String path, File pathFile) {
            try {
              ViewGroup container = (ViewGroup) view.findViewById(R.id.report_container);
              if (container == null) {
                return;
              }
              container.removeAllViews();
              List<InventoryItemImage> files;
              if (view.getTag(R.id.tag_button_value) != null && view.getTag(
                  R.id.tag_button_value) instanceof List<?>) {
                files = (List<InventoryItemImage>) view.getTag(R.id.tag_button_value);
              } else {
                files = new ArrayList<>();
              }
              InventoryItemImage attachment = new InventoryItemImage();
              attachment.file_name = path;
              attachment.content = FileUtils.convertToBase64(pathFile);
              if (!files.contains(attachment)) {
                files.add(attachment);
              }
              for (int index = 0; index < files.size(); index++) {
                container.addView(
                    CaseFieldUtils.addAttachment(getLayoutInflater(), container, files.get(index)));
              }
              view.setTag(R.id.tag_button_value, files);
            } catch (IOException e) {
              e.printStackTrace();
              Crashlytics.logException(e);
              ZupApplication.toast(findViewById(android.R.id.content), R.string.error_attachment)
                  .show();
            }
          }
        })
        .build()
        .show();
  }

  void showReportItemSearch(final View view) {
    if (view == null) {
      return;
    }
    ReportPickerDialog dialog = new ReportPickerDialog();

    int fieldId = (Integer) view.getTag(R.id.tag_field_id);
    Object value = view.getTag(R.id.tag_button_value) == null ? caseStep.getDataFieldValue(fieldId)
        : view.getTag(R.id.tag_button_value);

    if (value != null && value instanceof List<?>) {
      dialog.setSelectedReports(new ObjectMapper().convertValue(value, Integer[].class));
    }
    dialog.show(getSupportFragmentManager(), "report_picker");
    dialog.setListener(new ReportPickerDialog.OnReportsMultiSelectPickedListener() {
      @Override public void onReportsPicked(List<ReportItem> items) {
        List<Integer> values = new ArrayList<>();
        ViewGroup container = (ViewGroup) view.findViewById(R.id.report_container);
        if (container == null) {
          return;
        }
        container.removeAllViews();
        for (int index = 0; index < items.size(); index++) {
          ReportItem item = items.get(index);
          createReportItemView(container, item);

          if (!values.contains((Integer) item.id)) {
            values.add((Integer) item.id);
          }
        }
        view.setTag(R.id.tag_button_value, values);
      }
    });
  }

  void createReportItemView(final ViewGroup parent, ReportItem item) {
    final View view = getLayoutInflater().inflate(R.layout.case_inventory_item_item, null);
    view.setTag(R.id.tag_item_id, item.id);

    TextView name = (TextView) view.findViewById(R.id.case_inventory_item_item_name);
    View remove = view.findViewById(R.id.case_inventory_item_item_remove);
    remove.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Integer itemId = (Integer) view.getTag(R.id.tag_item_id);
        if (itemId == null) {
          return;
        }
        parent.removeView(view);
        if (parent.getParent() == null) {
          return;
        }
        ViewGroup parentContainer = (ViewGroup) parent.getParent();
        Object value = parentContainer.getTag(R.id.tag_button_value);
        if (value == null && !(value instanceof List<?>)) {
          return;
        }
        List<Integer> values = (List<Integer>) value;
        for (int index = 0; index < values.size(); index++) {
          if (values.get(index) == itemId) {
            values.remove(index);
            break;
          }
        }
        parentContainer.setTag(R.id.tag_button_value, values);
      }
    });

    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category != null) {
      name.setText(category.title + " - " + item.id);
    }
    parent.addView(view);
  }

  void showChooseResponsibleUserDialog(String header,
      UserPickerDialog.OnUserPickedListener listener) {
    if (!Utilities.isConnected(ViewCaseStepFormActivity.this)) {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
          .show();
      return;
    }
    UserPickerDialog dialog = new UserPickerDialog();
    Bundle bundle = new Bundle();
    //if (caseStep.hasResponsableUser()) {
    //bundle.putParcelable("selectedUser", caseStep.responsableUser);
    //}
    bundle.putString("header", header);
    dialog.setArguments(bundle);
    dialog.show(getSupportFragmentManager(), "user_picker");
    dialog.setListener(listener);
  }

  void showChooseResponsibleGroupDialog(String header,
      GroupPickerDialog.OnGroupPickedListener listener) {
    if (!Utilities.isConnected(ViewCaseStepFormActivity.this)) {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.error_no_internet_toast)
          .show();
      return;
    }
    GroupPickerDialog dialog;
    Bundle bundle = new Bundle();
    //if (caseStep.hasResponsibleGroup()) {

    //dialog =
    //  GroupPickerDialog.newInstance(caseStep.flowStep.permissions, caseStep.responsibleGroup);
    //} else {
    dialog = GroupPickerDialog.newInstance(caseStep.flowStep.permissions);
    //}
    dialog.show(getSupportFragmentManager(), "group_picker");
    dialog.setListener(listener);
  }

  View createEditableResponsibleUserView() {
    View view = getLayoutInflater().inflate(R.layout.inventory_item_item_select_edit, null);
    TextView title = (TextView) view.findViewById(R.id.inventory_item_text_name);
    final TextView value = (TextView) view.findViewById(R.id.inventory_item_text_value);
    value.setTag("responsible_user");

    title.setText(R.string.case_step_responsable_title);

    int userId = Zup.getInstance().getSessionUserId();
    if (caseStep != null && caseStep.hasResponsableUser()) {
      userId = caseStep.responsableUser.id;
      value.setText(caseStep.responsableUser.name);
    } else if (caseStep != null && caseStep.hasResponsibleGroup()) {
      userId = caseStep.responsibleGroup.getId();
      value.setText(caseStep.responsibleGroup.getName());
    } else if (caseStep != null && caseStep.flowStep.user_id > 0) {
      userId = caseStep.flowStep.user_id;
      Zup.getInstance().showUsernameInto(value, "", userId);
    }
    value.setTag(R.id.tag_field_id, userId);

    view.setClickable(true);
    view.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (caseStep.flowStep.conduction_mode_open) {
          showChooseResponsibleGroupDialog(getString(R.string.select_conductor),
              new GroupPickerDialog.OnGroupPickedListener() {
                @Override public void onGroupPicked(Group group) {
                  ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
                  View view = container.findViewWithTag("responsible_user");
                  if (view != null) {
                    TextView value = (TextView) view;
                    value.setText(group.getName());
                    view.setTag(R.id.tag_field_id, group.getId());
                    caseStep.responsibleGroup = group;
                  }
                }
              });
        } else {
          showChooseResponsibleUserDialog(getString(R.string.select_conductor),
              new UserPickerDialog.OnUserPickedListener() {

                @Override public void onUserPicked(User user) {
                  ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
                  View view = container.findViewWithTag("responsible_user");
                  if (view != null) {
                    TextView value = (TextView) view;
                    value.setText(user.name);
                    view.setTag(R.id.tag_field_id, user.id);
                    caseStep.responsableUser = user;
                  }
                }
              });
        }
      }
    });

    return view;
  }

  public void finishStep(View view) {
    final int userId = getResponsibleUserId();
    if (theCase.nextSteps != null && theCase.nextSteps.length > 0) {
      if (theCase.nextSteps[0].conduction_mode_open) {
        showChooseResponsibleGroupDialog(getString(R.string.next_step_conductor),
            new GroupPickerDialog.OnGroupPickedListener() {
              @Override public void onGroupPicked(Group group) {
                new StepPublisher().execute(userId, group.getId());
              }
            });
      } else {
        showChooseResponsibleUserDialog(getString(R.string.next_step_conductor),
            new UserPickerDialog.OnUserPickedListener() {
              @Override public void onUserPicked(User user) {
                new StepPublisher().execute(userId, user.id);
              }
            });
      }
    } else {
      new StepPublisher().execute(userId, userId);
    }
  }

  private ArrayList<UpdateCaseStepRequest.FieldValue> getFields() {
    ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
    ArrayList<UpdateCaseStepRequest.FieldValue> fields = new ArrayList<>();
    for (int i = 0; i < container.getChildCount(); i++) {
      View row = container.getChildAt(i);
      Integer fieldId = (Integer) row.getTag(R.id.tag_field_id);
      if (fieldId == null) continue;

      Object value = serializeField(row);
      if (value != null) {
        fields.add(new UpdateCaseStepRequest.FieldValue(fieldId, value));
      }
    }
    return fields;
  }

  @Override public void success(SingleInventoryItemCollection singleInventoryItemCollection,
      Response response) {
    inventoryItem = singleInventoryItemCollection.item;
    updateInventoryFields();
  }

  private void updateInventoryFields() {
    ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
    for (int i = 0; i < container.getChildCount(); i++) {
      View row = container.getChildAt(i);
      Integer fieldId = (Integer) row.getTag(R.id.tag_field_id);
      Flow.Step.Field field = caseStep.flowStep.getField(fieldId);
      if (field == null) continue;

      if (field.field_type.equals("inventory_field")) {
        FieldUtils.setFieldValue((ViewGroup) row, field.category_inventory_field, inventoryItem,
            getLayoutInflater());
      }
    }
  }

  @Override public void failure(RetrofitError error) {
    ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
    for (int i = 0; i < container.getChildCount(); i++) {
      View row = container.getChildAt(i);
      Integer fieldId = (Integer) row.getTag(R.id.tag_field_id);
      Flow.Step.Field field = caseStep.flowStep.getField(fieldId);
      if (field == null) continue;

      if (field.field_type.equals("inventory_field")) {
        FieldUtils.setFieldValue(container, field.category_inventory_field, new InventoryItem(),
            getLayoutInflater());
      }
    }
  }

  class StepPublisher extends AsyncTask<Integer, Void, FillCaseStepSyncAction> {
    ProgressDialog dialog;
    boolean requiredFieldsAreFailed;

    @Override protected void onPreExecute() {
      super.onPreExecute();
      if (isFinishing()) {
        return;
      }
      dialog = ViewUtils.createProgressDialog(ViewCaseStepFormActivity.this);
      dialog.setMessage(getString(R.string.validating_data_dialog_message));
      dialog.show();
      requiredFieldsAreFailed = validateFields();
      dialog.setMessage(getString(R.string.creating_item_dialog_message));
    }

    @Override protected FillCaseStepSyncAction doInBackground(Integer... values) {
      if (requiredFieldsAreFailed || isFinishing()) {
        return null;
      }
      boolean isGroupId = caseStep.flowStep.conduction_mode_open;
      boolean isNextGroupId = theCase.nextSteps != null
          && theCase.nextSteps.length > 0
          && theCase.nextSteps[0].conduction_mode_open;
      return new FillCaseStepSyncAction(theCase.id, caseStep.stepId, values[0], values[1],
          isGroupId, isNextGroupId, getFields());
    }

    private Boolean validateFields() {
      boolean validationFailed = false;
      View firstFieldError = null;
      ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
      int fieldsCount = container.getChildCount();
      for (int i = 0; i < fieldsCount; i++) {
        View row = container.getChildAt(i);
        TextView fieldTitle = (TextView) row.findViewById(R.id.inventory_item_text_name);
        Integer fieldId = (Integer) row.getTag(R.id.tag_field_id);
        Flow.Step.Field field = caseStep.flowStep.getField(fieldId);
        if (field == null) {
          continue;
        }

        if (fieldTitle != null) {
          fieldTitle.setTextColor(
              ContextCompat.getColor(ViewCaseStepFormActivity.this, R.color.offline_warning_text));
        }

        Object value = serializeField(row);
        boolean gotError = false;
        if (isEmpty(value)) {
          if ((isStepFieldOptional(field) || isInventoryFieldOptional(field))) {
            continue;
          }
          gotError = true;
        } else {
          if (isURLValidField(field, value) || isEmailValidFIeld(field, value)) {
            continue;
          }
          if (field.field_type.equals("url") || field.field_type.equals("email")) {
            gotError = true;
          }
        }

        if (gotError) {
          if (fieldTitle != null) {
            fieldTitle.setTextColor(
                ContextCompat.getColor(ViewCaseStepFormActivity.this, R.color.field_label_error));
          }
          if (firstFieldError == null) {
            firstFieldError = row;
            focusOnView(row);
            validationFailed = true;
          }
        }

        final int progress = 100 * (i + 1) / fieldsCount * 2;
        runOnUiThread(new Runnable() {
          @Override public void run() {
            if (isFinishing()) {
              return;
            }
            if (dialog != null && dialog.isShowing()) {
              dialog.setProgress(progress);
            }
          }
        });
      }

      if (validationFailed) {
        if (dialog != null && dialog.isShowing()) {
          dialog.dismiss();
        }
      }

      return validationFailed;
    }

    private final void focusOnView(final View view) {
      final ScrollView scroll = (ScrollView) findViewById(R.id.case_step_form_editbar);
      Handler handler = new Handler();
      handler.post(new Runnable() {
        @Override public void run() {
          scroll.smoothScrollTo(0, view.getBottom());
        }
      });
    }

    private boolean isEmpty(Object value) {
      return value == null || value.toString().equals("[]") || value.toString().isEmpty();
    }

    private boolean isURLValidField(Flow.Step.Field field, Object value) {
      return field.field_type.equals("url") && value.toString().matches(URL_PATTERN);
    }

    private boolean isEmailValidFIeld(Flow.Step.Field field, Object value) {
      return field.field_type.equals("email") && value.toString().matches(EMAIL_PATTERN);
    }

    private boolean isInventoryFieldOptional(Flow.Step.Field field) {
      return field.field_type.equals("inventory_field") && !field.category_inventory_field.required;
    }

    private boolean isStepFieldOptional(Flow.Step.Field field) {
      return !field.active || (field.requirements != null && !field.requirements.presence);
    }

    @Override protected void onPostExecute(FillCaseStepSyncAction result) {
      super.onPostExecute(result);
      if (result == null || isFinishing()) {
        Toast.makeText(ViewCaseStepFormActivity.this, R.string.message_fill_required_fields,
            Toast.LENGTH_LONG).show();
        return;
      }
      if (Zup.getInstance().getSyncActionService().hasSyncActionRelatedToCase(theCase.id)
          && syncActionId != -1) {
        Zup.getInstance().getSyncActionService().removeSyncAction(syncActionId);
      }
      Zup.getInstance().getSyncActionService().addSyncAction(result);

      if (dialog != null && dialog.isShowing()) {
        dialog.dismiss();
      }

      if (Utilities.isConnected(ViewCaseStepFormActivity.this)) {
        Zup.getInstance().sync();
      } else {
        ZupApplication.toast(findViewById(android.R.id.content),
            getString(R.string.case_no_connection_alert)).show();
        finishEditing(null);
      }

      Intent intent = new Intent();
      if (theCase.nextSteps == null || theCase.nextSteps.length == 0) {
        Log.d("FINISH", "Must finish");
        intent.putExtra("must_finish", true);
      }
      setResult(RESULT_OK, intent);
      finish();
    }
  }

  private Integer getResponsibleUserId() {
    try {
      ViewGroup container = (ViewGroup) findViewById(R.id.case_step_form_container);
      View view = container.findViewWithTag("responsible_user");
      if (view != null) {
        return (Integer) view.getTag(R.id.tag_field_id);
      }
      return Zup.getInstance().getSessionUserId();
    } catch (Exception error) {
      error.printStackTrace();
      return Zup.getInstance().getSessionUserId();
    }
  }

  public void finishEditing(View view) {
    ArrayList<UpdateCaseStepRequest.FieldValue> fields = getFields();
    int size = fields.size();

    if (caseStep.flowStep.conduction_mode_open) {
      caseStep.responsibleGroup =
          Zup.getInstance().getAccess().getGroupAllowedToEditStep(caseStep.id, caseStep.stepId);
    } else {
      caseStep.responsableUser = Zup.getInstance().getSessionUser();
    }
    caseStep.executed = false;
    caseStep.caseStepDataFields = new ArrayList<>();

    for (int index = 0; index < size; index++) {
      UpdateCaseStepRequest.FieldValue field = fields.get(index);
      Case.Step.DataField data = new Case.Step.DataField();
      data.id = caseStep.id * field.id * (new Random(System.currentTimeMillis()).nextInt(1337) + 1);

      data.fieldId = field.id;
      data.value = field.value;

      Flow.Step.Field stepField = caseStep.getField(field.id);
      if (stepField != null
          && stepField.category_inventory_field != null
          && stepField.field_type.equals("inventory_field")
          && inventoryItem != null) {
        inventoryItem.setFieldValue(stepField.category_inventory_field.id, field.value);
      }
      caseStep.caseStepDataFields.add(data);
    }

    Zup.getInstance().getCaseItemService().addCaseItem(theCase);
    if (inventoryItem != null) {
      if (inventoryItem.id > 0) {
        inventoryItem.id *= -1;
      }
      Zup.getInstance().getInventoryItemService().addFakeInventoryItem(inventoryItem);
    }

    Intent intent = new Intent();
    intent.putExtra("case", theCase);
    intent.putExtra("stepId", caseStep.stepId);
    intent.putExtra("ignore_loading", true);
    setResult(RESULT_OK, intent);
    finish();
  }

  Object serializeField(View childContainer) {
    Integer fieldId = (Integer) childContainer.getTag(R.id.tag_field_id);
    Flow.Step.Field field = caseStep.flowStep.getField(fieldId);
    if (field == null || field.field_type == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    Object value = null;

    if (field.field_type.equals("radio")) {
      ViewGroup radioContainer =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
      RadioGroup radioGroup = (RadioGroup) radioContainer.getChildAt(0);
      RadioButton selectedButton;
      try {
        selectedButton =
            (RadioButton) radioGroup.findViewById(radioGroup.getCheckedRadioButtonId());
      } catch (NullPointerException ex) {
        selectedButton = null;
      }

      if (selectedButton != null) {
        value = selectedButton.getTag(R.id.tag_button_value);
      }
    } else if (field.field_type.equals("inventory_field")) {
      value = FieldUtils.getFieldValue((ViewGroup) childContainer, field.category_inventory_field);
    } else if (field.field_type.equals("checkbox")) {
      ArrayList<String> result = new ArrayList<>();

      ViewGroup radioContainer =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
      for (int i = 0; i < radioContainer.getChildCount(); i++) {
        if (!(radioContainer.getChildAt(i) instanceof CheckBox)) continue;

        CheckBox checkBox = (CheckBox) radioContainer.getChildAt(i);
        if (checkBox.isChecked()) {
          result.add((String) checkBox.getTag(R.id.tag_button_value));
        }
      }

      if (result.size() > 0) value = result;
    } else if (field.field_type.equals("integer")
        || field.field_type.equals("year")
        || field.field_type.equals("month")
        || field.field_type.equals("day")
        || field.field_type.equals("hour")
        || field.field_type.equals("second")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      try {
        value = Integer.parseInt(txtValue.getText().toString());
      } catch (NumberFormatException ex) {
        value = null;
      }
    } else if (field.field_type.equals("decimal")
        || field.field_type.equals("meter")
        || field.field_type.equals("centimeter")
        || field.field_type.equals("kilometer")
        || field.field_type.equals("angle")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      try {
        value = Float.parseFloat(txtValue.getText().toString());
      } catch (NumberFormatException ex) {
        value = null;
      }
    } else if (field.field_type.equals("image")) {
      ArrayList<InventoryItemImage> result = new ArrayList<InventoryItemImage>();
      ViewGroup container =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_images_container);
      for (int i = 0; i < container.getChildCount(); i++) {
        if (!(container.getChildAt(i) instanceof FrameLayout)) continue;

        try {
          InventoryItemImage imageData = new InventoryItemImage();

          View imageView = container.getChildAt(i);
          if (imageView.getTag() instanceof InventoryItemImage) {
            imageData = (InventoryItemImage) imageView.getTag();
            if (TextUtils.isEmpty(imageData.content) && !imageData.destroy) {
              continue;
            }
          } else {
            String path = (String) imageView.getTag();
            byte[] buffer = IOUtil.readFile(path);
            imageData.file_name = path.substring(path.lastIndexOf("/") + 1);
            imageData.content = Base64.encodeToString(buffer, Base64.NO_WRAP);
          }
          result.add(imageData);
        } catch (IOException ex) {
        }
      }

      value = result;
    } else if (field.field_type.equals("date") || field.field_type.equals("time")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

      String tvalue = (String) txtValue.getTag();
      if (tvalue != null && tvalue.length() > 0) value = tvalue;
    } else if (field.field_type.equals("select")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

      value = txtValue.getTag();
    } else if (field.field_type == null
        || field.field_type.equals("text")
        || field.field_type.equals("cpf")
        || field.field_type.equals("cnpj")
        || field.field_type.equals("url")
        || field.field_type.equals("email")
        || field.field_type.equals("textarea")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      if (txtValue.getText().length() > 0) value = txtValue.getText().toString();
    } else if ((field.field_type.equals("report_item")) || (field.field_type.equals(
        "inventory_item"))) {
      if (childContainer.getTag(R.id.tag_button_value) != null) {
        value = childContainer.getTag(R.id.tag_button_value);
      } else {
        value = new ArrayList<Integer>();
      }
    } else if (field.field_type.equals("attachment")) {
      if (childContainer.getTag(R.id.tag_button_value) != null) {
        value = childContainer.getTag(R.id.tag_button_value);
      }
    }
    return value;
  }
}
