package com.lfdb.zuptecnico.fragments.cases;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.FullScreenImageActivity;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.entities.Flow;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.InventoryItemImage;
import com.lfdb.zuptecnico.tasks.InventoryItemLoaderTask;
import com.lfdb.zuptecnico.util.FieldUtils;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 11/12/2015.
 */
public class CaseFormViewFragment extends Fragment
    implements InventoryItemLoaderTask.ItemLoadedListener {
  InventoryItem inventoryItem;
  private static boolean isLoadingInventoryItem = false;

  Flow.Step getFlowStep() {
    return (Flow.Step) getArguments().getParcelable("flowStep");
  }

  Case.Step getItem() {
    return (Case.Step) getArguments().getParcelable("caseStep");
  }

  Case getCaseItem() {
    return (Case) getArguments().getParcelable("case");
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
    ViewGroup parent = (ViewGroup) getView();
    ViewGroup container = (ViewGroup) parent.findViewById(R.id.container);
    View title = container.findViewById(R.id.inventory_item_section_title);
    container.removeAllViews();
    container.addView(title);
    fillData(parent);
  }

  void fillData(ViewGroup root) {
    if (getFlowStep() == null || getItem() == null) return;

    Flow.Step section = getFlowStep();
    Case.Step item = getItem();
    Case theCase = getCaseItem();
    ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
    ObjectMapper mapper = new ObjectMapper();

    TextView txtHeader = (TextView) root.findViewById(R.id.inventory_item_section_title);
    txtHeader.setText(section.title);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    params.topMargin = (int) getResources().getDimension(R.dimen.report_card_margin_between_items);

    LinearLayout.LayoutParams valueParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    valueParams.topMargin = (int) getResources().getDimension(R.dimen.textview_padding);

    if (section.fields == null) {
      return;
    }
    Object value = item.getDataFieldByType("inventory_item");
    Integer id = -1;
    if (value != null && value instanceof List) {
      List items = (List) value;
      if (!items.isEmpty()) {
        id = (Integer) items.get(0);
        loadInventoryItem(id);
      }
    }

    InventoryCategory category = null;
    if (inventoryItem != null) {
      category = Zup.getInstance()
          .getInventoryCategoryService()
          .getInventoryCategory(inventoryItem.inventory_category_id);
    }

    for (int j = 0; j < section.fields.size(); j++) {
      Flow.Step.Field field = section.fields.get(j);

      TextView fieldTitle =
          new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyTitle));
      fieldTitle.setLayoutParams(params);
      fieldTitle.setText(field.title);
      container.addView(fieldTitle);
      if (field.field_type == null) {
        continue;
      }
      String type = field.field_type;
      value = "";
      if (field.field_type.equals("inventory_field")) {
        Case.Step.DataField dataField = item.getDataField(field.id);
        InventoryCategory.Section.Field inventoryField =
            category == null ? null : category.getField(field.origin_field_id);
        type = inventoryField == null ? "text" : inventoryField.kind;
        if (inventoryItem != null) {
          value = inventoryField == null ? item.getDataFieldValue(field.id)
              : inventoryItem.getFieldValue(inventoryField.id);
          if (value != null && (type.equals("checkbox") || type.equals("radio") || type.equals(
              "select"))) {
            ArrayList<String> values = new ArrayList<>();
            ArrayList<Integer> idValues = new ArrayList<>();
            if (value instanceof List<?>) {
              idValues = (ArrayList<Integer>) value;
            } else if (value instanceof Integer) {
              idValues.add((Integer) value);
            }

            for (int index = 0; index < idValues.size(); index++) {
              InventoryCategory.Section.Field sectionField = category.getField(inventoryField.id);
              InventoryCategory.Section.Field.Option option =
                  sectionField == null ? null : sectionField.getOption(idValues.get(index));
              if (option != null) {
                values.add(option.value);
              }
            }
            value = values;
          }
        } else if (!isLoadingInventoryItem){
          Zup.getInstance().loadInventoryItem(this, fieldTitle, "Carregando", id);
          isLoadingInventoryItem = true;
        }
      } else if (field.field_type.equals("previous_field")) {
        type =
            theCase != null && theCase.getField(field.origin_field_id) != null ? theCase.getField(
                field.origin_field_id).field_type : "previous_field";
        if (type.equals("attachment")) {
          value = theCase.getAttachmentsDataField(field.origin_field_id);
        } else if (type.equals("image") || type.equals("images")) {
          value = theCase.getImagesField(field.origin_field_id);
        } else {
          value = theCase.getDataFieldValue(field.origin_field_id);
        }
      } else if (field.field_type.equals("attachment")) {
        value = FieldUtils.isListOfString((List<?>) item.getDataFieldValue(field.id))
            ? item.getAttachmentDataField(field.id) : item.getDataFieldValue(field.id);
      } else if (field.field_type.equals("image") || field.field_type.equals("images")) {
        value = FieldUtils.isListOfString((List<?>) item.getDataFieldValue(field.id))
            ? item.getImagesDataField(field.id) : item.getDataFieldValue(field.id);
      } else {
        value = item.getDataFieldValue(field.id);
      }
      if (type.equals("checkbox") || type.equals("radio") || type.equals("select")) {
        createOptionsFieldLayout(value, container, mapper, valueParams);
      } else if (type.equals("image") || type.equals("images")) {
        createImageFieldLayout(value, container, mapper);
      } else if (type.equals("previous_field")) {
        createPreviousFieldLayout(value, container, valueParams);
      } else if (type.equals("inventory_item")) {
        createInventoryItemFieldLayout(value, container, valueParams);
      } else if (type.equals("report_item")) {
        createReportItemFieldLayout(value, container, mapper, valueParams);
      } else if (type.equals("attachment")) {
        createAttachmentFieldLayout(value, container, valueParams);
      } else {
        createTextFieldLayout(value, container, valueParams);
      }
    }
  }

  private void loadInventoryItem(Integer id) {
    if (Zup.getInstance().getInventoryItemService().getInventoryItem(id * -1) != null && !(getCaseItem().getStatus().equals("finished"))) {
      inventoryItem = Zup.getInstance().getInventoryItemService().getInventoryItem(id * -1);
      isLoadingInventoryItem = false;
    }
  }

  @Override public void onEmptyResultLoaded() {
    inventoryItem = null;
    isLoadingInventoryItem = false;
  }

  @Override public void onItemLoaded(InventoryItem item) {
    this.inventoryItem = item;
    refresh();
    isLoadingInventoryItem = false;
  }

  private void createAttachmentFieldLayout(Object value, ViewGroup container,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value == null) {
      fieldValue.setText("-");
      container.addView(fieldValue);
    } else if (value instanceof Case.Step.DataField.FileAttachment[]) {
      Case.Step.DataField.FileAttachment[] data = (Case.Step.DataField.FileAttachment[]) value;
      for (Case.Step.DataField.FileAttachment file : data) {
        createAttachmentItemField(container, valueParams, file.file_name, file.url);
      }
    } else if (value instanceof List<?>) {
      List<InventoryItemImage> data = (List<InventoryItemImage>) value;
      for (InventoryItemImage file : data) {
        createAttachmentItemField(container, valueParams, file.file_name, file.url);
      }
    }
  }

  private void createAttachmentItemField(ViewGroup container, LinearLayout.LayoutParams valueParams,
      String name, final String url) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (!TextUtils.isEmpty(name)) {
      fieldValue.setText(name);
      if (!TextUtils.isEmpty(url)) {
        fieldValue.setOnClickListener(new View.OnClickListener() {
          @Override public void onClick(View v) {
            final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
            startActivity(intent);
          }
        });
      }
      fieldValue.setTextColor(ContextCompat.getColor(getActivity(), R.color.zupblue));
      container.addView(fieldValue);
    }
  }

  private void createTextFieldLayout(Object value, ViewGroup container,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value != null) {
      fieldValue.setText(value.toString());
    } else {
      fieldValue.setText("-");
    }
    container.addView(fieldValue);
  }

  private void createInventoryItemFieldLayout(Object value, ViewGroup container,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value == null || value.toString().isEmpty() || value.equals("[]")) {
      fieldValue.setText("-");
    } else {
      List<Integer> ids = new ArrayList<>();
      if (value instanceof List<?>) {
        ids = (List<Integer>) value;
      } else {
        ids.add(Integer.parseInt(value.toString()));
      }
      if (inventoryItem == null && !isLoadingInventoryItem) {
        Zup.getInstance().showInventoryItemInto(fieldValue, "", ids);
      } else if (inventoryItem != null) {
        fieldValue.setText(inventoryItem.title);
      }
    }
    container.addView(fieldValue);
  }

  private void createPreviousFieldLayout(Object value, ViewGroup container,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value != null) {
      fieldValue.setText(value.toString());
    } else {
      fieldValue.setText("-");
    }
    container.addView(fieldValue);
  }

  private void createReportItemFieldLayout(Object value, ViewGroup container, ObjectMapper mapper,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value == null || value.toString().isEmpty() || value.equals("[]")) {
      fieldValue.setText("-");
    } else {
      List<Integer> ids = new ArrayList<>();
      if (value instanceof List<?>) {
        ids = (List<Integer>) value;
      } else {
        ids.add(Integer.parseInt(value.toString()));
      }
      String strValue = "";
      for (int index = 0; index < ids.size(); index++) {
        strValue += getActivity().getString(R.string.report_number) + ids.get(index);
        if (index != ids.size() - 1) {
          strValue += "\n";
        }
      }
      fieldValue.setText(strValue);
    }
    container.addView(fieldValue);
  }

  private void createOptionsFieldLayout(Object value, ViewGroup container, ObjectMapper mapper,
      LinearLayout.LayoutParams valueParams) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (value == null || value.toString().isEmpty() || value.equals("[]")) {
      fieldValue.setText("-");
    } else {
      List<Object> ids = new ArrayList<>();
      if (value instanceof List<?>) {
        ids = (List<Object>) value;
      } else {
        ids.add(value.toString());
      }
      String strValue = "";
      for (int index = 0; index < ids.size(); index++) {
        strValue += ids.get(index).toString();
        if (index != ids.size() - 1) {
          strValue += "\n";
        }
      }
      fieldValue.setText(strValue);
    }
    container.addView(fieldValue);
  }

  private void createImageFieldLayout(Object value, ViewGroup container, ObjectMapper mapper) {
    HorizontalScrollView scroll = new HorizontalScrollView(getActivity());
    scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    LinearLayout fieldView = new LinearLayout(getActivity());
    fieldView.setPadding(20, 20, 20, 20);
    fieldView.setOrientation(LinearLayout.HORIZONTAL);
    fieldView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    if (value == null) {
      return;
    }
    final InventoryItemImage[] images;
    if (value instanceof List<?> || value instanceof InventoryItemImage[]) {
      images = mapper.convertValue(value, InventoryItemImage[].class);
    } else if (value instanceof InventoryItemImage) {
      images = new InventoryItemImage[] { mapper.convertValue(value, InventoryItemImage.class) };
    } else {
      return;
    }

    for (int im = 0; im < images.length; im++) {
      final InventoryItemImage image = images[im];
      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
      layoutParams.setMargins(0, 0, 15, 0);

      final ImageView imageView = new ImageView(getActivity());
      imageView.setLayoutParams(layoutParams);
      imageView.setBackgroundColor(0xffcccccc);
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
      imageView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          Parcelable[] imgs = new Parcelable[images.length];
          for (int x = 0; x < images.length; x++) {
            InventoryItemImage img = images[x];
            imgs[x] = img;
          }

          Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
          intent.putExtra("image", (Parcelable) image);
          intent.putExtra("images", imgs);
          startActivity(intent);
        }
      });

      if (image.content != null) {
        byte[] data = Base64.decode(image.content, Base64.NO_WRAP);
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        imageView.setImageBitmap(bitmap);
      } else if (image.versions != null && image.versions.thumb != null) {
        Picasso.with(getActivity()).load(image.versions.thumb).into(imageView);
      }

      fieldView.addView(imageView);
    }

    scroll.addView(fieldView);
    container.addView(scroll);
  }
}
