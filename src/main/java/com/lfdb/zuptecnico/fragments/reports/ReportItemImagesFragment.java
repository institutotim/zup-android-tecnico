package com.lfdb.zuptecnico.fragments.reports;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.lfdb.zuptecnico.FullScreenImageActivity;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.entities.InventoryItemImage;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.WebImageView;
import com.lfdb.zuptecnico.util.Utilities;
import java.util.ArrayList;

public class ReportItemImagesFragment extends Fragment implements View.OnClickListener {
  ReportItem getItem() {
    return (ReportItem) getArguments().getParcelable("item");
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    ViewGroup root =
        (ViewGroup) inflater.inflate(R.layout.fragment_report_details_images, container, false);
    saveImages();
    fillData(root);

    return root;
  }

  public void refresh() {
    fillData((ViewGroup) getView());
  }

  void saveImages() {
    ReportItem item = getItem();

    if (item == null || item.images == null) {
      return;
    }
    for (int i = 0; i < item.images.length; i++) {
      item.images[i].saveImageIntoCache(getActivity());
    }
  }

  void fillData(ViewGroup root) {
    ReportItem item = getItem();

    if (item == null || item.images == null) return;

    ViewGroup container = (ViewGroup) root.findViewById(R.id.imageContainer);
    container.removeAllViews();

    for (int i = 0; i < item.images.length; i++) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      View image = inflater.inflate(R.layout.report_image_item, null);
      WebImageView imageView = (WebImageView) image.findViewById(R.id.photo_content);

      if (item.images[i].getContent() == null) {
        item.images[i].loadImageInto(imageView);
      } else {
        Bitmap imageBitmap = BitmapFactory.decodeFile(item.images[i].getContent());
        if (imageBitmap == null) {
          continue;
        } else {
          imageView.setImageBitmap(imageBitmap);
        }
      }
      image.setTag(item.images[i]);

      if (TextUtils.isEmpty(item.images[i].getTitle())) {
        image.findViewById(R.id.photo_subtitle).setVisibility(View.GONE);
      } else {
        TextView subtitle = (TextView) image.findViewById(R.id.photo_subtitle);
        subtitle.setText(item.images[i].getTitle());
        subtitle.setVisibility(View.VISIBLE);
      }

      TextView tvDate = (TextView) image.findViewById(R.id.photo_date);
      String date = Utilities.formatIsoDateAndTime(item.images[i].date);
      if (TextUtils.isEmpty(date)) {
        tvDate.setVisibility(View.INVISIBLE);
      } else {
        tvDate.setText(getString(R.string.photo_date_title).concat(date));
        tvDate.setVisibility(View.VISIBLE);
      }
      image.setOnClickListener(this);
      container.addView(image);
    }
  }

  InventoryItemImage toInventoryItemImage(ReportItem.Image image) {
    InventoryItemImage img = new InventoryItemImage();
    img.versions = new InventoryItemImage.Versions();
    img.versions.high = image.high;
    img.versions.low = image.low;
    img.versions.thumb = image.thumb;
    img.url = image.original;
    img.content = image.getContent();

    return img;
  }

  InventoryItemImage[] toInventoryItemImageArray() {
    ArrayList<InventoryItemImage> result = new ArrayList<>();

    for (int i = 0; i < getItem().images.length; i++) {
      ReportItem.Image image = getItem().images[i];

      InventoryItemImage img = toInventoryItemImage(image);
      result.add(img);
    }

    InventoryItemImage[] resultArray = new InventoryItemImage[result.size()];
    result.toArray(resultArray);

    return resultArray;
  }

  @Override public void onClick(View view) {
    ReportItem.Image image = (ReportItem.Image) view.getTag();

    // TODO refatorar essa classe pra suportar qualquer tipo de imagem
    InventoryItemImage[] images = toInventoryItemImageArray();
    InventoryItemImage img = toInventoryItemImage(image);

    Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
    intent.putExtra("images", images);
    intent.putExtra("image", (Parcelable) img);
    startActivity(intent);
  }
}
