package com.particity.zuptecnico.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Case;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.InventoryItemImage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import br.com.rezende.mascaras.Mask;

/**
 * Created by Renan on 29/01/2016.
 */
public class CaseFieldUtils {
    public static View createRadiosForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, Flow.Step.Field field, Case item, boolean canEdit) {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        ViewGroup radiocontainer = (ViewGroup) fieldView.findViewById(R.id.inventory_item_radio_container);
        if (field.values != null) {
            RadioGroup group = new RadioGroup(context);
            group.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            for (int x = 0; x < field.values.size(); x++) {
                String option = field.values.get(x);

                if (TextUtils.isEmpty(option)) {
                    continue;
                }

                RadioButton button = new RadioButton(context);
                button.setText(option);
                button.setTag(R.id.tag_button_value, option);
                if (Utilities.isTablet(context))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                button.setMinimumHeight(60);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //params.setMargins(15, 0, 0, 0);
                button.setLayoutParams(params);

                group.addView(button);

                String[] selected = null;
                Object raw = item.getDataFieldValue(field.id);
                if (raw != null) {
                    if (raw instanceof List<?>) {
                        selected = mapper.convertValue(raw, String[].class);
                    } else {
                        selected = new String[]{raw.toString()};
                    }
                }

                if (selected != null && Utilities.arrayContains(selected, option))
                    button.setChecked(true);

                button.setActivated(canEdit);
                button.setClickable(canEdit);
            }
            radiocontainer.addView(group);
        }
        return fieldView;
    }

    public static View createRadiosForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, Flow.Step.Field field, Case item) {
        return createRadiosForField(parent, mapper, label, context, inflater, field, item, true);
    }

    public static View createPreviousField(ViewGroup parent, ObjectMapper mapper, Context context, String label, LayoutInflater inflater, Flow.Step.Field originalField, Case item) {
        if (originalField == null || originalField.field_type == null) {
            return null;
        }

        if (originalField.field_type.equals("radio")) {
            return createRadiosForField(parent, mapper, label, context, inflater, originalField, item, false);
        }

        if (originalField.field_type.equals("checkbox")) {
            return createCheckboxesForField(parent, mapper, label, context, inflater, originalField, item, false);
        }

        if (originalField.field_type.equals("image")) {
            return createImagesField(parent, label, mapper, item, originalField, inflater, null, false);
        }

        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, originalField.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        Object row = item.getDataFieldValue(originalField.id);
        if (row == null || row.toString().isEmpty() || row.equals("[]")) {
            fieldValue.setText("-");
            return fieldView;
        }
        if (originalField.field_type.equals("inventory_item") || originalField.field_type.equals("report_item")) {
            List<Integer> ids = new ArrayList<>();
            if (row instanceof List<?>) {
                ids = (List<Integer>) row;
            } else {
                ids.add(Integer.parseInt(row.toString()));
            }
            if (ids.size() > 0) {
                if (originalField.field_type.equals("inventory_item")) {
                    Zup.getInstance().showInventoryItemInto(fieldValue, "", ids);
                } else {
                    String strValue = "";
                    for (int index = 0; index < ids.size(); index++) {
                        strValue += context.getString(R.string.report_number) + ids.get(index);
                        if (index != ids.size() - 1) {
                            strValue += "\n";
                        }
                    }
                    fieldValue.setText(strValue);
                }
            }
        } else {
            fieldValue.setText(row.toString());
        }
        return fieldView;
    }

    public static View createCheckboxesForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, Flow.Step.Field field, Case item) {
        return createCheckboxesForField(parent, mapper, label, context, inflater, field, item, true);
    }

    public static View createCheckboxesForField(ViewGroup parent, ObjectMapper mapper, String label, Context context, LayoutInflater inflater, Flow.Step.Field field, Case item, boolean canEdit) {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_radio_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        ViewGroup radiocontainer = (ViewGroup) fieldView.findViewById(R.id.inventory_item_radio_container);
        if (field.values != null) {
            for (int x = 0; x < field.values.size(); x++) {
                String option = field.values.get(x);

                if (TextUtils.isEmpty(option)) {
                    continue;
                }

                CheckBox button = new CheckBox(context);//(RadioButton)radioElement.findViewById(R.id.inventory_item_item_radio_radio);
                button.setText(option);
                button.setTag(R.id.tag_button_value, option);

                if (Utilities.isTablet(context))
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);

                button.setMinimumHeight(60);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                //params.setMargins(15, 0, 0, 0);
                button.setLayoutParams(params);

                String[] selected = null;

                Object raw = item.getDataFieldValue(field.id);
                if (raw != null) {
                    if (raw instanceof List<?>) {
                        selected = mapper.convertValue(raw, String[].class);
                    } else if (raw != null && raw instanceof Number) {
                        selected = new String[]{mapper.convertValue(raw, String.class)};
                    }
                }

                radiocontainer.addView(button);

                if (selected != null && Utilities.arrayContains(selected, option))
                    button.setChecked(true);

                button.setClickable(canEdit);
                button.setActivated(canEdit);
            }
        }
        return fieldView;
    }


    public static View createImagesField(ViewGroup parent, String label, ObjectMapper mapper, Case item, Flow.Step.Field field, LayoutInflater inflater, View.OnClickListener addButtonListener) {
        return createImagesField(parent, label, mapper, item, field, inflater, addButtonListener, true);
    }

    public static View createImagesField(ViewGroup parent, String label, ObjectMapper mapper, Case item, Flow.Step.Field field, LayoutInflater inflater, View.OnClickListener addButtonListener, boolean canEdit) {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_images_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);


        ViewGroup imagesContent = (ViewGroup) fieldView.findViewById(R.id.inventory_item_images_container);
        InventoryItemImage[] images = item.getImagesField(field.id);
        if (images != null) {
            for (int i = 0; i < images.length; i++) {
                imagesContent.addView(addImage(inflater, imagesContent, images[i], null));
            }
        }

        InventoryItemImage[] localImages = item.getDataFieldValue(field.id) != null ?
                mapper.convertValue(item.getDataFieldValue(field.id), InventoryItemImage[].class) : null;
        if (localImages != null) {
            for (int i = 0; i < localImages.length; i++) {
                byte[] data = Base64.decode(localImages[i].content, Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                localImages[i].url = null;
                //Bitmap bitmap = BitmapFactory.decodeFile(localImages[i].url);
                imagesContent.addView(addImage(inflater, imagesContent, localImages[i], bitmap));
            }
        }


        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        fieldTitle.setText(label);

        View addButton = fieldView.findViewById(R.id.inventory_item_images_button);
        addButton.setOnClickListener(addButtonListener);

        if (!canEdit) {
            addButton.setVisibility(View.GONE);
        } else {
            addButton.setVisibility(View.VISIBLE);
        }

        return fieldView;
    }

    public static View addAttachment(LayoutInflater inflater, final ViewGroup parent, Object value) {
        if (value == null) {
            return null;
        }

        final View view = inflater.inflate(R.layout.case_inventory_item_item, parent, false);
        TextView name = (TextView) view.findViewById(R.id.case_inventory_item_item_name);
        View remove = view.findViewById(R.id.case_inventory_item_item_remove);
        String filename;
        if (value instanceof InventoryItemImage) {
            InventoryItemImage attachment = (InventoryItemImage) value;
            view.setTag(attachment);
            filename = attachment.file_name.substring(attachment.file_name.lastIndexOf("/") + 1);
        } else {
            Case.Step.DataField.FileAttachment attachment = (Case.Step.DataField.FileAttachment) value;
            filename = attachment.file_name;
            remove.setVisibility(View.GONE);
        }

        name.setText(filename);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parent.removeView(view);
                if (parent.getParent() == null) {
                    return;
                }
                ViewGroup parentContainer = (ViewGroup) parent.getParent();
                List<InventoryItemImage> data = (List<InventoryItemImage>) parentContainer.getTag(R.id.tag_button_value);
                if (data != null && view.getTag() != null && data.contains(view.getTag())) {
                    data.remove(view.getTag());
                }
                parentContainer.setTag(R.id.tag_button_value, data);
            }
        });
        return view;
    }

    public static View addImage(LayoutInflater inflater, final ViewGroup parent, InventoryItemImage imageData, Bitmap image) {
        if (imageData == null) {
            return null;
        }
        boolean tagPath = image != null;
        final ViewGroup imageLayout = (ViewGroup) inflater.inflate(R.layout.report_item_create_image, parent, false);
        imageLayout.findViewById(R.id.progressBar6).setVisibility(View.GONE);
        ImageView imageView = (ImageView) imageLayout.findViewById(R.id.report_image);
        ViewGroup remove = (ViewGroup) imageLayout.findViewById(R.id.report_image_remove);

        imageLayout.setTag(tagPath && TextUtils.isEmpty(imageData.content) ? imageData.url : imageData);
        if (tagPath) {
            imageView.setImageBitmap(image);
        } else if (!TextUtils.isEmpty(imageData.url)) {
            Picasso.with(inflater.getContext()).load(imageData.url).into(imageView);
        }

        remove.setOnClickListener(tagPath ? new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parent.removeView(imageLayout);
            }
        } : new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InventoryItemImage image = (InventoryItemImage) imageLayout.getTag();
                image.destroy = true;
                imageLayout.setTag(image);
                imageLayout.findViewById(R.id.content).setVisibility(View.GONE);
            }
        });

        return imageLayout;
    }

    public static View createNumberField(ViewGroup parent, Context context, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item) {
        ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_text_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
        TextView fieldExtra = (TextView) fieldView.findViewById(R.id.inventory_item_text_extra);
        fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        int flags = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED;
        if (field.field_type.equals("decimal") || field.field_type.equals("meter") || field.field_type.equals("centimeter") || field.field_type.equals("kilometer") || field.field_type.equals("angle")) {
            flags |= InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }

        fieldTitle.setText(label);
        fieldValue.setInputType(flags);

        String pkgName = context.getClass().getPackage().getName();
        int resId = context.getResources().getIdentifier("inventory_item_extra_" + field.field_type, "string", pkgName);
        if (resId != 0) {
            fieldExtra.setVisibility(View.VISIBLE);
            fieldExtra.setText(context.getResources().getText(resId));
        }

        if (item.getDataFieldValue(field.id) != null)
            fieldValue.setText(item.getDataFieldValue(field.id).toString());

        return fieldView;
    }

    public static View createSelectField(ViewGroup parent, ObjectMapper mapper, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item, final View.OnClickListener listener) {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);

        if (item.getDataFieldValue(field.id) != null) {
            String selected = mapper.convertValue(item.getDataFieldValue(field.id), String.class);
            if (selected != null) {
                fieldValue.setText(selected);
                fieldValue.setTag(selected);
            } else {
                fieldValue.setText("Valor inválido");
            }
        } else {
            fieldValue.setText("Escolha uma opção...");
        }

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createDateField(ViewGroup parent, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item, final View.OnClickListener listener) {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);

        fieldTitle.setText(label);

        if (item.getDataFieldValue(field.id) != null) {
            fieldValue.setText(item.getDataFieldValue(field.id).toString());
            fieldValue.setTag(item.getDataFieldValue(field.id).toString());
        } else
            fieldValue.setText("Escolha uma data...");

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createTimeField(ViewGroup parent, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item, final View.OnClickListener listener) {
        final ViewGroup fieldView = (ViewGroup) inflater.inflate(R.layout.inventory_item_item_select_edit, parent, false);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        fieldTitle.setText(label);

        if (item.getDataFieldValue(field.id) != null) {
            fieldValue.setText(item.getDataFieldValue(field.id).toString());
            fieldValue.setTag(item.getDataFieldValue(field.id).toString());
        } else
            fieldValue.setText("Escolha um tempo...");

        fieldValue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });
        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onClick(fieldView);
            }
        });

        return fieldView;
    }

    public static View createCPForCNPJField(ViewGroup parent, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item) {
        View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
        fieldView.setTag(R.id.tag_field_id, field.id);

        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
        fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
        fieldValue.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (field.field_type.equals("cpf"))
            fieldValue.addTextChangedListener(Mask.insert("###.###.###-##", fieldValue));
        else if (field.field_type.equals("cnpj"))
            fieldValue.addTextChangedListener(Mask.insert("##.###.###/####-##", fieldValue));

        fieldTitle.setText(label);
        if (item.getDataFieldValue(field.id) != null)
            fieldValue.setText(item.getDataFieldValue(field.id).toString());

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }

    public static View createURLorEmailField(ViewGroup parent, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item) {
        View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
        fieldView.setTag(R.id.tag_field_id, field.id);
        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
        fieldValue.setHint(parent.getContext().getString(R.string.fill_text_hint) + " " + label);
        fieldTitle.setText(label);
        if (field.field_type != null && field.field_type.equals("email")) {
            fieldValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        } else {
            fieldValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        }
        if (item.getDataFieldValue(field.id) != null)
            fieldValue.setText(item.getDataFieldValue(field.id).toString());

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }

    public static View createTextField(ViewGroup parent, Context context, String label, Flow.Step.Field field, LayoutInflater inflater, Case.Step item) {
        View fieldView = inflater.inflate(R.layout.inventory_item_item_text_edit, null);
        fieldView.setTag(R.id.tag_field_id, field.id);
        TextView fieldTitle = (TextView) fieldView.findViewById(R.id.inventory_item_text_name);
        final EditText fieldValue = (EditText) fieldView.findViewById(R.id.inventory_item_text_value);
        fieldValue.setHint(context.getString(R.string.fill_text_hint) + " " + label);

        if (field.field_type != null && !field.field_type.equals("text") && !field.field_type.equals("textarea") && !field.field_type.equals("integer")) {
            label += " (Unknown field kind: " + field.field_type + ")";
            fieldValue.setEnabled(false);
        }

        if (field.field_type != null && field.field_type.equals("textarea") || field.multiple) {
            fieldValue.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            fieldValue.setLines(3);
            fieldValue.setGravity(Gravity.TOP | Gravity.START);
            fieldValue.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100));
        }

        fieldTitle.setText(label);
        if (item.getDataFieldValue(field.id) != null) {
            fieldValue.setText(item.getDataFieldValue(field.id).toString());
        }

        fieldView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldValue.requestFocus();
            }
        });

        return fieldView;
    }
}
