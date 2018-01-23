package com.lfdb.zuptecnico.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.lfdb.zuptecnico.R;
import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by igorlira on 6/29/15.
 */
public class Utilities {
  public static String join(Integer[] values) {
    String joined = "";
    for (int i = 0; i < values.length; i++) {
      joined += values[i];
      if (i + 1 < values.length) joined += ",";
    }

    return joined;
  }

  public static boolean isConnected(Context ctx) {
    ConnectivityManager conMgr = ctx == null ? null
        : (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (conMgr == null) {
      return false;
    }
    NetworkInfo i = conMgr.getActiveNetworkInfo();
    if (i == null || !i.isConnected() || !i.isAvailable()) {
      return false;
    }
    return true;
  }

  public static String joinAsInteger(Object[] values) {
    String joined = "";
    for (int i = 0; i < values.length; i++) {
      joined += Integer.toString((Integer) values[i]);
      if (i + 1 < values.length) joined += ",";
    }

    return joined;
  }

  public static boolean isTablet(Context context) {
    return context.getResources().getBoolean(R.bool.isTablet);
  }

  public static boolean arrayContains(Integer[] array, int value) {
    if (array == null) {
      return false;
    }
    for (Integer v : array) {
      if (v != null && v == value) return true;
    }

    return false;
  }

  public static boolean arrayIsEmpty(Integer[] array) {
    if (array == null) {
      return true;
    }
    return array.length == 0;
  }

  public static int getColorFromHex(String color) {
    try {
      return Color.parseColor(color);
    } catch (Exception ex) {
      Log.e("UI", "Could not parse color: " + color, ex);
      return 0xffff0000;
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  public static void setBackgroundDrawable(View view, Drawable drawable) {
    int sdk = android.os.Build.VERSION.SDK_INT;
    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
      view.setBackgroundDrawable(drawable);
    } else {
      view.setBackground(drawable);
    }
  }

  public static String formatIsoDateAndTime(String isoDate) {
    if (isoDate == null) return "";

    try {
      ISO8601DateFormat fmt = new ISO8601DateFormat();
      Date date = fmt.parse(isoDate);

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm'h'");
      return dateFormat.format(date);
    } catch (ParseException ex) {
      return isoDate;
    }
  }

  public static <T> boolean arrayContains(T[] array, T object) {
    return Arrays.asList(array).contains(object);
  }

  public static String encodeBase64(String path) {
    try {
      File file = new File(path);
      FileInputStream imageInFile = new FileInputStream(file);
      byte imageData[] = new byte[(int) file.length()];
      imageInFile.read(imageData);
      imageInFile.close();

      return Base64.encodeToString(imageData, Base64.DEFAULT);
    } catch (Exception e) {
      Log.e("ENCODING", e.getMessage() != null ? e.getMessage() : "NullPointerException", e);
      Crashlytics.logException(e);
      return null;
    }
  }

  public static Bitmap decodeBase64(String encodedImage) {
    byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
    return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
  }
}
