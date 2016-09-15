package com.ntxdev.zuptecnico.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import br.com.rezende.mascaras.Mask;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.squareup.picasso.Picasso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FieldUtils {
  public static View createSectionHeader(ViewGroup parent, LayoutInflater inflater,
      String section) {
    ViewGroup sectionHeader =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_section_header, parent, false);

    TextView sectionTitle =
        (TextView) sectionHeader.findViewById(R.id.inventory_item_section_title);
    sectionTitle.setText(section);

    return sectionHeader;
  }

  public static boolean isListOfString(List<?> list) {
    if (list == null || list.isEmpty()) {
      return true;
    }
    return list.get(0) instanceof String;
  }

  public static View createFieldView(ViewGroup parent, ObjectMapper mapper, final Context context,
      final LayoutInflater inflater, final InventoryCategory.Section.Field field,
      boolean createMode, InventoryItem item, View.OnClickListener pictureListener) {
    if (field == null || field.disabled) {
      return null;
    }
    String label;
    if (field.label != null) {
      label = field.label;
    } else {
      label = field.title;
    }

    if (field.kind.equals("radio")) {
      return FieldUtils.createRadiosForField(parent, mapper, label, context, inflater, field,
          createMode, item);
    } else if (field.kind.equals("checkbox")) {
      return FieldUtils.createCheckboxesForField(parent, mapper, label, context, inflater, field,
          createMode, item);
    } else if (field.kind.equals("images")) {
      return FieldUtils.createImagesField(parent, label, mapper, createMode, item, field, inflater,
          pictureListener);
    } else if (field.kind.equals("decimal")
        || field.kind.equals("integer")
        || field.kind.equals("meters")
        || field.kind.equals("centimeters")
        || field.kind.equals("kilometers")
        || field.kind.equals("years")
        || field.kind.equals("months")
        || field.kind.equals("days")
        || field.kind.equals("hours")
        || field.kind.equals("seconds")
        || field.kind.equals("angle")) {
      return FieldUtils.createNumberField(parent, context, label, field, inflater, createMode,
          item);
    } else if (field.kind.equals("select")) {
      return FieldUtils.createSelectField(parent, mapper, label, field, inflater, createMode, item,
          new View.OnClickListener() {
            @Override public void onClick(View view) {
              FieldDialogUtils.createSelectDialog(context, field, view, inflater).show();
            }
          });
    } else if (field.kind.equals("date")) {
      return FieldUtils.createDateField(parent, label, field, inflater, createMode, item,
          new View.OnClickListener() {
            @Override public void onClick(View view) {
              FieldDialogUtils.createDatePickerDialog(context, field, view).show();
            }
          });
    } else if (field.kind.equals("time")) {
      return FieldUtils.createTimeField(parent, label, field, inflater, createMode, item,
          new View.OnClickListener() {
            @Override public void onClick(View view) {
              FieldDialogUtils.createTimePickerDialog(context, field, view).show();
            }
          });
    } else if (field.kind.equals("cpf") || field.kind.equals("cnpj")) {
      return FieldUtils.createCPForCNPJField(parent, label, field, inflater, createMode, item);
    } else if (field.kind.equals("url") || field.kind.equals("email")) {
      return FieldUtils.createURLorEmailField(parent, label, field, inflater, createMode, item);
    } else {
      return FieldUtils.createTextField(parent, context, label, field, inflater, createMode, item);
    }
  }

  public static View createRadiosForField(ViewGroup parent, ObjectMapper mapper, String label,
      Context context, LayoutInflater inflater, InventoryCategory.Section.Field field,
      boolean createMode, InventoryItem item) {
    ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    fieldTitle.setText(label);

    ViewGroup radiocontainer =
        (ViewGroup) fieldView.findViewById(R.id.inventory_item_radio_container);
    if (field.field_options != null) {
      RadioGroup group = new RadioGroup(context);
      group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT));
      for (int x = 0; x < field.field_options.length; x++) {
        InventoryCategory.Section.Field.Option option = field.field_options[x];

        if (option.disabled) {
          continue;
        }

        RadioButton button = new RadioButton(context);
        button.setText(option.value);
        button.setTag(R.id.tag_button_value, option.id);
        if (Utilities.isTablet(context)) {
          button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        } else {
          button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }

        button.setMinimumHeight(60);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //params.setMargins(15, 0, 0, 0);
        button.setLayoutParams(params);

        group.addView(button);

        Integer[] selected = null;
        if (!createMode) {
          Object raw = item != null ? item.getFieldValue(field.id) : new ArrayList<Integer>();
          if (raw instanceof List<?>) {
            selected = mapper.convertValue(raw, Integer[].class);
          } else if (raw != null && raw instanceof Number) {
            selected = new Integer[] { mapper.convertValue(raw, Integer.class) };
          }
        }

        if (!createMode && selected != null && Utilities.arrayContains(selected, option.id)) {
          button.setChecked(true);
        }
      }
      radiocontainer.addView(group);
    }
    return fieldView;
  }

  public static View createCheckboxesForField(ViewGroup parent, ObjectMapper mapper, String label,
      Context context, LayoutInflater inflater, InventoryCategory.Section.Field field,
      boolean createMode, InventoryItem item) {
    ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    fieldTitle.setText(label);

    ViewGroup radiocontainer =
        (ViewGroup) fieldView.findViewById(R.id.inventory_item_radio_container);
    if (field.field_options != null) {
      for (int x = 0; x < field.field_options.length; x++) {
        InventoryCategory.Section.Field.Option option = field.field_options[x];

        if (option.disabled) {
          continue;
        }

        CheckBox button = new CheckBox(
            context);//(RadioButton)radioElement.findViewById(R.id.inventory_item_item_radio_radio);
        button.setText(option.value);
        button.setTag(R.id.tag_button_value, option.id);

        if (Utilities.isTablet(context)) {
          button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
        } else {
          button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        }

        button.setMinimumHeight(60);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        //params.setMargins(15, 0, 0, 0);
        button.setLayoutParams(params);

        Integer[] selected = null;
        if (!createMode && item != null) {
          Object raw = item.getFieldValue(field.id);
          if (raw instanceof List<?>) {
            selected = mapper.convertValue(raw, Integer[].class);
          } else if (raw != null && raw instanceof Number) {
            selected = new Integer[] { mapper.convertValue(raw, Integer.class) };
          }
        }

        radiocontainer.addView(button);

        if (!createMode && selected != null && Utilities.arrayContains(selected, option.id)) {
          button.setChecked(true);
        }
      }
    }
    return fieldView;
  }

  public static View createImagesField(ViewGroup parent, String label, ObjectMapper mapper,
      boolean createMode, InventoryItem item, InventoryCategory.Section.Field field,
      LayoutInflater inflater, View.OnClickListener addButtonListener) {
    ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_images_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    if (!createMode && item != null) {
      ViewGroup imagesContent =
          (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);
      Object value = item.getFieldValue(field.id);
      InventoryItemImage[] images = null;
      if (value instanceof List<?>) {
        images = mapper.convertValue(value, InventoryItemImage[].class);
      } else if (value != null && value instanceof InventoryItemImage) {
        images = new InventoryItemImage[] { mapper.convertValue(value, InventoryItemImage.class) };
      }

      if (images != null) {
        for (int i = 0; i < images.length; i++) {
          imagesContent.addView(addImage(inflater, imagesContent, images[i], null));
        }
      }
    }

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    fieldTitle.setText(label);
    View addButton = fieldView.findViewById(R.id.inventory_item_images_button);
    addButton.setOnClickListener(addButtonListener);

    return fieldView;
  }

  public static View addImage(LayoutInflater inflater, final ViewGroup parent,
      InventoryItemImage imageData, Bitmap image) {
    if (imageData == null) {
      return null;
    }
    boolean tagPath = image != null;
    final ViewGroup imageLayout =
        (ViewGroup) inflater.inflate(R.layout.report_item_create_image, parent, false);
    imageLayout.findViewById(R.id.progressBar6).setVisibility(View.GONE);
    ImageView imageView = (ImageView) imageLayout.findViewById(R.id.report_image);
    ViewGroup remove = (ViewGroup) imageLayout.findViewById(R.id.report_image_remove);

    imageLayout.setTag(tagPath ? imageData.url : imageData);
    if (tagPath) {
      imageView.setImageBitmap(image);
    } else if (!TextUtils.isEmpty(imageData.url)) {
      Picasso.with(inflater.getContext()).load(imageData.url).into(imageView);
    }

    remove.setOnClickListener(tagPath ? new View.OnClickListener() {
      @Override public void onClick(View view) {
        parent.removeView(imageLayout);
      }
    } : new View.OnClickListener() {
      @Override public void onClick(View view) {
        InventoryItemImage image = (InventoryItemImage) imageLayout.getTag();
        image.destroy = true;
        imageLayout.setTag(image);
        imageLayout.findViewById(R.id.content).setVisibility(View.GONE);
      }
    });

    return imageLayout;
  }

  public static View createNumberField(ViewGroup parent, Context context, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item) {
    ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
    TextView fieldExtra = (TextView) fieldView.findViewById(R.id.inventory_item_text_extra);
    fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        fieldValue.requestFocus();
      }
    });

    int flags = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
    if (field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals(
        "centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle")) {
      flags |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
    }

    fieldTitle.setText(label);
    fieldValue.setInputType(flags);

    String pkgName = context.getClass().getPackage().getName();
    int resId = context.getResources()
        .getIdentifier("inventory_item_extra_" + field.kind, "string", pkgName);
    if (resId != 0) {
      fieldExtra.setVisibility(View.VISIBLE);
      fieldExtra.setText(context.getResources().getText(resId));
    }

    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
    }

    return fieldView;
  }

  public static void setFieldValue(ViewGroup childContainer, InventoryCategory.Section.Field field,
      InventoryItem item, LayoutInflater inflater) {
    if (item == null || field == null) {
      return;
    }
    Object value = item.getFieldValue(field.id);
    if (value == null) {
      return;
    }
    ObjectMapper mapper = new ObjectMapper();
    if (field.kind.equals("radio")) {
      ViewGroup radioContainer =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
      RadioGroup radioGroup = (RadioGroup) radioContainer.getChildAt(0);

      Integer[] selected = null;
      if (value instanceof List<?>) {
        selected = mapper.convertValue(value, Integer[].class);
      } else if (value != null && value instanceof Number) {
        selected = new Integer[] { mapper.convertValue(value, Integer.class) };
      }
      if (selected != null) {
        for (int index = 0; index < radioGroup.getChildCount(); index++) {
          RadioButton button = (RadioButton) radioGroup.getChildAt(index);
          if (button != null && Utilities.arrayContains(selected,
              button.getTag(R.id.tag_button_value))) {
            button.setChecked(true);
          }
        }
      }
    } else if (field.kind.equals("checkbox")) {
      ViewGroup radioContainer =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
      Integer[] selected = null;
      if (value instanceof List<?>) {
        selected = mapper.convertValue(value, Integer[].class);
      } else if (value != null && value instanceof Number) {
        selected = new Integer[] { mapper.convertValue(value, Integer.class) };
      }
      if (selected != null) {
        for (int i = 0; i < radioContainer.getChildCount(); i++) {
          if (!(radioContainer.getChildAt(i) instanceof CheckBox)) continue;

          CheckBox checkBox = (CheckBox) radioContainer.getChildAt(i);
          if (checkBox != null && Utilities.arrayContains(selected,
              checkBox.getTag(R.id.tag_button_value))) {
            checkBox.setChecked(true);
          }
        }
      }
    } else if (field.kind.equals("integer") || field.kind.equals("years") || field.kind.equals(
        "months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals(
        "seconds")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      txtValue.setText(value.toString());
    } else if (field.kind.equals("decimal") || field.kind.equals("meters") ||
        field.kind.equals("centimeters") || field.kind.equals("kilometers") || field.kind.equals(
        "angle")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      txtValue.setText(value.toString());
    } else if (field.kind.equals("images")) {
      ViewGroup imagesContent =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_images_container);
      InventoryItemImage[] images = null;
      if (value instanceof List<?>) {
        images = mapper.convertValue(value, InventoryItemImage[].class);
      } else if (value != null && value instanceof InventoryItemImage) {
        images = new InventoryItemImage[] { mapper.convertValue(value, InventoryItemImage.class) };
      }
      if (images != null) {
        for (int i = 0; i < images.length; i++) {
          imagesContent.addView(addImage(inflater, imagesContent, images[i], null));
        }
      }
    } else if (field.kind.equals("date") || field.kind.equals("time")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      txtValue.setText(value.toString());
    } else if (field.kind.equals("select")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      txtValue.setText(value.toString());
    } else if (field.kind == null
        || field.kind.equals("text")
        || field.kind.equals("cpf")
        || field.kind.equals("cnpj")
        || field.kind.equals("url")
        || field.kind.equals("email")
        || field.kind.equals("textarea")) {
      EditText txtValue = (EditText) childContainer.findViewById(R.id.inventory_item_text_value);
      txtValue.setText(value.toString());
    }
  }

  public static Object getFieldValue(ViewGroup childContainer,
      InventoryCategory.Section.Field field) {
    Object value = null;
    if (field.kind.equals("radio")) {
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
    } else if (field.kind.equals("checkbox")) {
      ArrayList<Integer> result = new ArrayList<Integer>();

      ViewGroup radioContainer =
          (ViewGroup) childContainer.findViewById(R.id.inventory_item_radio_container);
      for (int i = 0; i < radioContainer.getChildCount(); i++) {
        if (!(radioContainer.getChildAt(i) instanceof CheckBox)) continue;

        CheckBox checkBox = (CheckBox) radioContainer.getChildAt(i);
        if (checkBox.isChecked()) {
          result.add((Integer) checkBox.getTag(R.id.tag_button_value));
        }
      }

      if (result.size() > 0) value = result;
    } else if (field.kind.equals("integer") || field.kind.equals("years") || field.kind.equals(
        "months") || field.kind.equals("days") || field.kind.equals("hours") || field.kind.equals(
        "seconds")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      try {
        value = Integer.parseInt(txtValue.getText().toString());
      } catch (NumberFormatException ex) {
        value = null;
      }
    } else if (field.kind.equals("decimal") || field.kind.equals("meters") || field.kind.equals(
        "centimeters") || field.kind.equals("kilometers") || field.kind.equals("angle")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      try {
        value = Float.parseFloat(txtValue.getText().toString());
      } catch (NumberFormatException ex) {
        value = null;
      }
    } else if (field.kind.equals("images")) {
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
            if (!imageData.destroy) {
              continue;
            }
          } else {
            String path = (String) imageView.getTag();
            byte[] buffer = IOUtil.readFile(path);

            imageData.content = Base64.encodeToString(buffer, Base64.NO_WRAP);
          }
          result.add(imageData);
        } catch (IOException ex) {
        }
      }

      value = result;
    } else if (field.kind.equals("date") || field.kind.equals("time")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

      String tvalue = (String) txtValue.getTag();
      if (tvalue != null && tvalue.length() > 0) value = tvalue;
    } else if (field.kind.equals("select")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);

      Integer tvalue = (Integer) txtValue.getTag();
      if (tvalue != null) {
        value = new int[] { tvalue };
      }
    } else if (field.kind == null
        || field.kind.equals("text")
        || field.kind.equals("cpf")
        || field.kind.equals("cnpj")
        || field.kind.equals("url")
        || field.kind.equals("email")
        || field.kind.equals("textarea")) {
      TextView txtValue = (TextView) childContainer.findViewById(R.id.inventory_item_text_value);
      if (txtValue.getText().length() > 0) value = txtValue.getText().toString();
    }

    return value;
  }

  public static View createSelectField(ViewGroup parent, ObjectMapper mapper, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item, final View.OnClickListener listener) {
    final ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

    fieldTitle.setText(label);

    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      ArrayList selected = mapper.convertValue(item.getFieldValue(field.id), ArrayList.class);
      if (selected.size() > 0) {
        Integer id = mapper.convertValue(selected.get(0), Integer.class);
        InventoryCategory.Section.Field.Option option = field.getOption(id);

        if (option != null) {
          fieldValue.setText(option.value);
          fieldValue.setTag(id);
        } else {
          fieldValue.setText("Valor inválido");
        }
      } else {
        fieldValue.setText("Escolha uma opção...");
      }
      //fieldValue.setText(item.getFieldValue(field.id).toString());
      //fieldValue.setTag(item.getFieldValue(field.id).toString());
    } else {
      fieldValue.setText("Escolha uma opção...");
    }

    fieldValue.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });
    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });

    return fieldView;
  }

  public static View createDateField(ViewGroup parent, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item, final View.OnClickListener listener) {
    final ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

    fieldTitle.setText(label);

    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setTag(item.getFieldValue(field.id).toString());
    } else {
      fieldValue.setText("Escolha uma data...");
    }

    fieldValue.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });
    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });

    return fieldView;
  }

  public static View createTimeField(ViewGroup parent, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item, final View.OnClickListener listener) {
    final ViewGroup fieldView =
        (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
    fieldTitle.setText(label);

    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
      fieldValue.setTag(item.getFieldValue(field.id).toString());
    } else {
      fieldValue.setText("Escolha um tempo...");
    }

    fieldValue.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });
    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        if (listener != null) listener.onClick(fieldView);
      }
    });

    return fieldView;
  }

  public static View createCPForCNPJField(ViewGroup parent, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item) {
    View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);

    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
    fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
    fieldValue.setInputType(InputType.TYPE_CLASS_NUMBER);
    if (field.kind.equals("cpf")) {
      fieldValue.addTextChangedListener(Mask.insert("###.###.###-##", (EditText) fieldValue));
    } else if (field.kind.equals("cnpj")) {
      fieldValue.addTextChangedListener(Mask.insert("##.###.###/####-##", (EditText) fieldValue));
    }

    fieldTitle.setText(label);
    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
    }

    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        fieldValue.requestFocus();
      }
    });

    return fieldView;
  }

  public static View createURLorEmailField(ViewGroup parent, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item) {
    View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);
    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
    fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
    fieldTitle.setText(label);
    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
    }

    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        fieldValue.requestFocus();
      }
    });

    return fieldView;
  }

  public static View createTextField(ViewGroup parent, Context context, String label,
      InventoryCategory.Section.Field field, LayoutInflater inflater, boolean createMode,
      InventoryItem item) {
    View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
    fieldView.setTag(R.id.inventory_item_create_fieldid, field.id);
    TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
    final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
    fieldValue.setHint(context.getString(R.string.fill_text_hint) + " " + label);

    if (field.kind != null
        && !field.kind.equals("text")
        && !field.kind.equals("textarea")
        && !field.kind.equals("integer")) {
      label += " (Unknown field kind: " + field.kind + ")";
      fieldValue.setEnabled(false);
    }

    if (field.kind != null && field.kind.equals("textarea")) {
      EditText editText = (EditText) fieldValue;
      editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
      editText.setLines(3);
      editText.setGravity(Gravity.TOP | Gravity.START);
      editText.setLayoutParams(
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
    }

    fieldTitle.setText(label);

    if (!createMode && item != null && item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
    }

    fieldView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        fieldValue.requestFocus();
      }
    });

    return fieldView;
  }
}
