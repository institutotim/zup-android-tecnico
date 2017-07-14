package com.ntxdev.zuptecnico.fragments.inventory;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.FullScreenImageActivity;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.entities.InventoryItemImage;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by igorlira on 7/18/15.
 */
public class InventoryItemSectionFragment extends Fragment {
  InventoryCategory.Section getSection() {
    return (InventoryCategory.Section) getArguments().getSerializable("section");
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

  void fillData(ViewGroup root) {
    if (getSection() == null || getItem() == null) return;

    InventoryCategory.Section section = getSection();
    InventoryItem item = getItem();
    ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
    ObjectMapper mapper = new ObjectMapper();

    TextView txtHeader = (TextView) root.findViewById(R.id.inventory_item_section_title);
    txtHeader.setText(section.title);
    sortSectionFields(section);

    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    params.topMargin = (int) getResources().getDimension(R.dimen.report_card_margin_between_items);

    LinearLayout.LayoutParams valueParams =
        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    valueParams.topMargin = (int) getResources().getDimension(R.dimen.textview_padding);

    for (int j = 0; j < section.fields.length; j++) {
      if (!Zup.getInstance()
          .getAccess()
          .canViewInventoryField(item.inventory_category_id, section.fields[j].id)) {
        continue;
      }
      InventoryCategory.Section.Field field = section.fields[j];
      if (field.disabled) {
        continue;
      }

      TextView fieldTitle =
          new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyTitle));
      fieldTitle.setLayoutParams(params);
      fieldTitle.setText(field.label != null ? field.label : field.title);
      container.addView(fieldTitle);

      if (field.kind != null
          && field.kind.equals("images")
          && item.getFieldValue(field.id) != null) {
        createImageFieldLayout(item, container, mapper, field);
      } else if (field.kind.equals("checkbox") || field.kind.equals("radio") || field.kind.equals(
          "select")) {
        createOptionsFieldLayout(item, container, mapper, valueParams, field);
      } else {
        createTextFieldLayout(item, container, valueParams, field);
      }
    }
  }

  private void createTextFieldLayout(InventoryItem item, ViewGroup container,
      LinearLayout.LayoutParams valueParams, InventoryCategory.Section.Field field) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    if (item.getFieldValue(field.id) != null) {
      fieldValue.setText(item.getFieldValue(field.id).toString());
    } else {
      fieldValue.setText(R.string.not_filled);
    }
    container.addView(fieldValue);
  }

  private void createOptionsFieldLayout(InventoryItem item, ViewGroup container,
      ObjectMapper mapper, LinearLayout.LayoutParams valueParams,
      InventoryCategory.Section.Field field) {
    TextView fieldValue =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
    fieldValue.setLayoutParams(valueParams);
    Integer[] selected = null;
    Object raw = item.getFieldValue(field.id);
    if (raw instanceof List<?>) {
      selected = mapper.convertValue(raw, Integer[].class);
    } else if (raw != null && raw instanceof Number) {
      selected = new Integer[] { mapper.convertValue(raw, Integer.class) };
    }

    String strvalue = "";
    if (selected != null) {
      for (int x = 0; x < selected.length; x++) {
        Integer val = selected[x];
        if (val == null) {
          continue;
        }

        InventoryCategory.Section.Field.Option option = field.getOption(val);
        String val_str = "";

        if (option != null) {
          val_str = option.value;
        }

        if (x > 0) {
          strvalue += "\n";
        }

        strvalue += val_str;
      }
      fieldValue.setText(strvalue);
    } else {
      fieldValue.setText(R.string.not_filled);
    }

    container.addView(fieldValue);
  }

  private void createImageFieldLayout(InventoryItem item, ViewGroup container, ObjectMapper mapper,
      InventoryCategory.Section.Field field) {
    HorizontalScrollView scroll = new HorizontalScrollView(getActivity());
    scroll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    LinearLayout fieldView = new LinearLayout(getActivity());
    fieldView.setPadding(20, 20, 20, 20);
    fieldView.setOrientation(LinearLayout.HORIZONTAL);
    fieldView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));

    final ArrayList images = (ArrayList) item.getFieldValue(field.id);
    for (int im = 0; im < images.size(); im++) {
      final InventoryItemImage image;
      if (images.get(im) instanceof InventoryItemImage) {
        image = (InventoryItemImage) images.get(im);
      } else {
        Object map = images.get(im);
        image = mapper.convertValue(map, InventoryItemImage.class);
      }

      LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(150, 150);
      layoutParams.setMargins(0, 0, 15, 0);

      final ImageView imageView = new ImageView(getActivity());
      imageView.setLayoutParams(layoutParams);
      imageView.setBackgroundColor(0xffcccccc);
      imageView.setScaleType(ImageView.ScaleType.FIT_XY);
      imageView.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          Parcelable[] imgs = new Parcelable[images.size()];
          for (int x = 0; x < images.size(); x++) {
            InventoryItemImage img;
            if (images.get(x) instanceof InventoryItemImage) {
              img = (InventoryItemImage) images.get(x);
            } else {
              Object map = images.get(x);
              ObjectMapper mapper = new ObjectMapper();
              img = mapper.convertValue(map, InventoryItemImage.class);
            }

            imgs[x] = img;
          }

          Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
          intent.putExtra("image", (Parcelable) image);
          intent.putExtra("images", imgs);
          startActivity(intent);
        }
      });

      if (image.content != null) {
        Bitmap bitmap = BitmapFactory.decodeFile(image.content);
        if (bitmap != null) {
          imageView.setImageBitmap(bitmap);
        } else {
          continue;
        }
      } else if (image.versions != null && image.versions.thumb != null) {
        Picasso.with(getActivity()).load(image.versions.thumb).into(imageView);
      }

      fieldView.addView(imageView);
    }
    scroll.addView(fieldView);
    container.addView(scroll);
  }

  void sortSectionFields(InventoryCategory.Section section) {
    Arrays.sort(section.fields, new Comparator<InventoryCategory.Section.Field>() {
      @Override public int compare(InventoryCategory.Section.Field section,
          InventoryCategory.Section.Field section2) {
        int pos1 = 0;
        int pos2 = 0;

        if (section.position != null) {
          pos1 = section.position;
        }
        if (section2.position != null) {
          pos2 = section2.position;
        }

        if (section.position == null) pos1 = pos2;

        if (section2.position == null) pos2 = pos1;

        if (pos1 < pos2) {
          return -1;
        } else if (pos1 == pos2) {
          return 0;
        } else {
          return 1;
        }
      }
    });
  }
}
