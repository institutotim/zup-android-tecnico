package com.particity.zuptecnico.util;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Base64OutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

    public static String convertToBase64(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }

        InputStream inputStream = null;//You can get an inputStream using any IO API
        inputStream = new FileInputStream(file.getAbsolutePath());
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Base64OutputStream output64 = new Base64OutputStream(output, Base64.DEFAULT);
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output64.write(buffer, 0, bytesRead);
        }
        String attachedFile = output.toString();
        output64.close();
        inputStream.close();

        output.close();
        return attachedFile;
    }

    public static File getImagesFolder(Context context) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images");
    }

    public static File getImagesFolder(Context context, String subfolder) {
        return new File(context.getFilesDir() + File.separator + "images" + File.separator + "images" + File.separator + subfolder);
    }

    public static File getTempImagesFolder() {
        File imagesFolder = new File(Environment.getExternalStorageDirectory() + File.separator + "ZUP" + File.separator + "temp");
        if (!imagesFolder.exists()) {
            imagesFolder.mkdirs();
        }

        return imagesFolder;
    }

}

