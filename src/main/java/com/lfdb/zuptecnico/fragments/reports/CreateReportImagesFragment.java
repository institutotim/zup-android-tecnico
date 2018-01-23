package com.lfdb.zuptecnico.fragments.reports;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.entities.ImageItem;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.WebImageView;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CreateReportImagesFragment extends Fragment implements ImagePickerCallback {
  ArrayList<Image> images;
  ViewGroup imagesListLayout;
  View imageLayout;
  private String tempImagePath;

  ImagePicker mImagePicker;
  CameraImagePicker mCameraImagePicker;

  class Image {
    public String url;
    public View view;
    public ImageItem imageDetails;
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (tempImagePath != null) {
      outState.putString("image_path", tempImagePath);
    }
    outState.putParcelableArray("images", storeArrayOfImageDetails());
  }

  private void loadArrayOfImage(ImageItem[] imageItems) {
    for (int index = 0; index < imageItems.length; index++) {
      addImage(imageItems[index]);
    }
  }

  private ImageItem[] storeArrayOfImageDetails() {
    ImageItem[] imagesArray = new ImageItem[getAddableCount()];
    int i = 0;
    for (Image imageItem : images) {
      if (!TextUtils.isEmpty(imageItem.url)) {
        continue;
      }
      imagesArray[i] = imageItem.imageDetails;
      i++;
    }
    return imagesArray;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_create_report_images, container, false);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    init(getView(), savedInstanceState);
  }

  public int getAddableCount() {
    int count = 0;
    for (int i = 0; i < images.size(); i++) {
      if (!TextUtils.isEmpty(images.get(i).imageDetails.getContent())) {
        count++;
      }
    }

    return count;
  }

  public int getCount() {
    return images.size();
  }

  public ImageItem getItem(int i) {
    return images.get(i).imageDetails;
  }

  public ImageItem getImageItemAt(int i) {
    ImageItem item = getItem(i);
    if (TextUtils.isEmpty(item.getContent())) { // It's a pre-added image
      return null;
    }
    return item;
  }

  void init(View view, Bundle savedInstanceState) {
    imagesListLayout = (ViewGroup) view.findViewById(R.id.report_images_container);

    View addButton = view.findViewById(R.id.report_addimage);
    addButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        pickImage();
      }
    });

    images = new ArrayList<>();
    if (savedInstanceState != null) {
      tempImagePath = savedInstanceState.getString("image_path");
      if (savedInstanceState.containsKey("images")) {
        loadArrayOfImage(ImageItem.toMyObjects(savedInstanceState.getParcelableArray("images")));
      }
    }

    mImagePicker = new ImagePicker(this);
    mImagePicker.setImagePickerCallback(this);

    mCameraImagePicker = new CameraImagePicker(this);
    mCameraImagePicker.setImagePickerCallback(this);
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

  private void inflateImageLayout() {
    imageLayout = LayoutInflater.from(getActivity())
        .inflate(R.layout.create_report_images_item, imagesListLayout, false);
    imageLayout.findViewById(R.id.progressBar6).setVisibility(View.VISIBLE);
    imagesListLayout.addView(imageLayout);
  }

  private void dismissImageLayout() {
    if (imageLayout != null) {
      imagesListLayout.removeView(imageLayout);
      imageLayout = null;
    }
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
      dismissImageLayout();
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
        return;
      case Picker.PICK_IMAGE_CAMERA:
        if (mCameraImagePicker == null) {
          mCameraImagePicker = new CameraImagePicker(this);
          mCameraImagePicker.setImagePickerCallback(this);
          mCameraImagePicker.reinitialize(tempImagePath);
        }
        mCameraImagePicker.submit(data);
        return;
      case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
        CropImage.ActivityResult result = CropImage.getActivityResult(data);
        Uri resultUri = result.getUri();

        String path = resultUri.getPath();
        if (path == null) {
          dismissImageLayout();
          return;
        }

        try {
          File file = new File(path);
          if (file.exists()) {
            addImage(file);
          } else {
            dismissImageLayout();
          }
        } catch (NullPointerException e) {
          dismissImageLayout();
        }
        break;
    }
  }

  String compressImage(String path) {
    Bitmap original = BitmapFactory.decodeFile(path);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    original.compress(Bitmap.CompressFormat.JPEG, 100, out);
    return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
  }

  void pickImage() {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(getActivity().getString(R.string.add_image_title));
    builder.setItems(new String[] {
        getActivity().getString(R.string.add_image_from_gallery),
        getActivity().getString(R.string.add_image_from_camera)
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

  public void addImage(File file) {
    Image img = new Image();
    ImageItem image = new ImageItem();
    image.setFilename(file.getPath());
    image.setContent(compressImage(file.getPath()));
    img.imageDetails = image;

    addImage(img);
    fillImageData(img);
  }

  public void addImage(ImageItem image) {
    Image img = new Image();
    if (image instanceof ReportItem.Image) {
      for (Image imageItem : images) {
        if (imageItem.url != null && imageItem.url.equals(((ReportItem.Image) image).original)) {
          return;
        }
      }
      img.url = ((ReportItem.Image) image).original;
    }
    img.imageDetails = image;
    addImage(img);
    fillImageData(img);
  }

  private void fillImageData(final Image img) {
    View view = img.view;
    if (view != null) {
      final TextView tvCounter = (TextView) view.findViewById(R.id.char_counter);
      EditText etSubtitle = (EditText) view.findViewById(R.id.photo_subtitle);

      if (!TextUtils.isEmpty(img.imageDetails.getTitle())) {
        etSubtitle.setText(img.imageDetails.getTitle());
      }
      if (!TextUtils.isEmpty(img.url)) {
        etSubtitle.setFocusable(false);
        etSubtitle.setEnabled(false);
        etSubtitle.setHint("");
        tvCounter.setVisibility(View.GONE);
      } else {
        etSubtitle.setFocusableInTouchMode(true);
        etSubtitle.setEnabled(true);
        etSubtitle.setHint(R.string.photo_subtitle_hint);
        tvCounter.setVisibility(View.VISIBLE);
      }
      String counter = String.valueOf(etSubtitle.getText().toString().length()) + " / 120";
      tvCounter.setText(counter);
      CounterTextWatcher mTextEditorWatcher = new CounterTextWatcher(tvCounter, img);
      etSubtitle.addTextChangedListener(mTextEditorWatcher);
    }
  }

  void addImage(Image img) {
    if (img != null || !TextUtils.isEmpty(img.url)) {
      inflateImageLayout();
      WebImageView imageView = (WebImageView) imageLayout.findViewById(R.id.report_image);
      View removeButton = imageLayout.findViewById(R.id.report_image_remove);
      removeButton.setClickable(true);
      removeButton.setTag(img); // index
      removeButton.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View view) {
          if (view.getTag() != null) {
            removeImage((Image) view.getTag());
          }
        }
      });

      imageLayout.findViewById(R.id.progressBar6).setVisibility(View.GONE);
      if (!TextUtils.isEmpty(img.imageDetails.getContent())) {
        Bitmap imageBitmap = BitmapFactory.decodeFile(img.imageDetails.getContent());
        if (imageBitmap != null) {
          imageView.setImageBitmap(imageBitmap);
        }
      } else {
        Picasso.with(getActivity().getApplicationContext()).load(img.url).into(imageView);
        removeButton.setVisibility(View.GONE);
      }

      img.view = imageLayout;
      this.images.add(img);
      imageLayout = null;
    }
  }

  void removeImage(Image image) {
    this.images.remove(image);
    View view = image.view;
    imagesListLayout.removeView(view);
  }

  private class CounterTextWatcher implements TextWatcher {
    TextView tvCounter;
    Image img;

    public CounterTextWatcher(TextView tvCounter, Image img) {
      this.tvCounter = tvCounter;
      this.img = img;
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
      EditText etSubtitle = (EditText) img.view.findViewById(R.id.photo_subtitle);
      if (s != etSubtitle.getEditableText()) {
        return;
      }
      String counter = String.valueOf(s.length()) + " / 120";
      tvCounter.setText(counter);
      img.imageDetails.setTitle(s.toString());
    }
  }
}
