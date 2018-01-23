package com.lfdb.zuptecnico.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Base64;
import android.util.DisplayMetrics;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.MapCluster;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

/**
 * Created by igorlira on 5/8/15.
 */
public class BitmapUtil {
    private static Hashtable<String, Bitmap> bitmapCache;

    public static BitmapDescriptor getMarkerBitmapFactory(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }

    public static String convertToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public static Bitmap getMapClusterBitmap(MapCluster cluster, DisplayMetrics metrics, String color) {
        if (bitmapCache == null)
            bitmapCache = new Hashtable<>();

        //InventoryCategory category = Zup.getInstance().getInventoryCategory(cluster.categoryId);

        String bmpName = "cluster_" + color + "_" + cluster.count;
        if (bitmapCache.containsKey(bmpName))
            return bitmapCache.get(bmpName);

        String s = Integer.toString(cluster.count);
        Rect bounds = new Rect();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(16 * metrics.density);
        paint.setColor(0xffffffff);
        paint.getTextBounds(s, 0, s.length(), bounds);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);

        Paint borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setColor(0xffffffff);

        Paint fillPaint = new Paint();
        fillPaint.setAntiAlias(true);
        fillPaint.setColor(0xff2ab4dc);
        if (color != null) {
            fillPaint.setColor(Color.parseColor(color));
        }

        int border = (int) (5 * metrics.density);
        int padding = (int) (10 * metrics.density);
        int size = Math.max(bounds.width(), bounds.height()) + border * 2 + padding * 2;

        Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        canvas.drawCircle(size / 2, size / 2, size / 2, borderPaint);
        canvas.drawCircle(size / 2, size / 2, (size - border * 2) / 2, fillPaint);

        int yPos = (int) ((size / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(s, size / 2, yPos, paint);

        bitmapCache.put(bmpName, result);

        return result;
    }

    public static Bitmap getMapClusterBitmap(MapCluster cluster, DisplayMetrics metrics) {
        String color = null;
        if (cluster.category_id != null) {
            InventoryCategory category = Zup.getInstance().getInventoryCategoryService().getInventoryCategory(cluster.category_id);
            color = category.color;
        }
        return getMapClusterBitmap(cluster, metrics, color);
    }
}
