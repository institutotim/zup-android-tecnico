package com.ntxdev.zuptecnico.storage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snappydb.SnappydbException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by igorlira on 7/18/15.
 */
public class BaseService {
    ObjectMapper mapper;
    StorageServiceManager mManager;

    public BaseService(StorageServiceManager manager) {
        this.mapper = new ObjectMapper();
        this.mManager = manager;
    }

    protected <T> T getObject(String key, Class<T> objClass) {
        synchronized (mManager) {
            try {
                String raw = mManager.getDB().get(key);
                return mapper.readValue(raw, objClass);

                //return mManager.getDB().getObject(key, objClass);
            } catch (Exception ex) {
                Log.e("Snappydb", "get object " + key, ex);
                return null;
            }
        }
    }

    protected <T> T[] getObjectArray(String key, Class<T> objClass) {
        synchronized (mManager) {
            try {
                String raw = mManager.getDB().get(key);
                return mapper.readValue(raw, mapper.getTypeFactory().constructArrayType(objClass));

                //return mManager.getDB().getObjectArray(key, objClass);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    protected <T> List<T> getObjectList(String key, Class<T> objClass) {
        T[] array = getObjectArray(key, objClass);
        if (array == null)
            return new ArrayList<>();

        ArrayList<T> result = new ArrayList<>(array.length);
        for (int i = 0; i < array.length; i++) {
            result.add(array[i]);
        }

        return result;
    }

    protected void deleteObject(String key) {
        synchronized (mManager) {
            try {
                mManager.getDB().del(key);
                mManager.commit();
            } catch (SnappydbException e) {
                Log.e("Snappydb", "delete object " + key, e);
            }
        }
    }

    protected void setObject(String key, Object value) {
        synchronized (mManager) {
            try {
                String raw = mapper.writeValueAsString(value);
                mManager.getDB().put(key, raw);
                //mManager.getDB().put(key, value);
                mManager.commit();
            } catch (Exception ex) {
                Log.e("Snappydb", "set object " + key, ex);
            }
        }
    }

    protected void setArray(String key, Object[] value) {
        synchronized (mManager) {
            try {
                String raw = mapper.writeValueAsString(value);
                mManager.getDB().put(key, raw);
                //mManager.getDB().put(key, value);
                mManager.commit();
            } catch (Exception ex) {
                Log.e("Snappydb", "set object " + key, ex);
            }
        }
    }

    protected void setList(String key, List value) {
        synchronized (mManager) {
            try {
                String raw = mapper.writeValueAsString(value);
                mManager.getDB().put(key, raw);
                //Object[] values = new Object[value.size()];
                //value.toArray(values);

                //mManager.getDB().put(key, values);
                mManager.commit();
            } catch (Exception ex) {
                Log.e("Snappydb", "set object " + key, ex);
            }
        }
    }

    protected void setBitmap(String filename, Bitmap value) {
        File file = new File(mManager.getContext().getCacheDir(), filename);
        try {
            if (!file.exists())
                file.createNewFile();

            FileOutputStream stream = new FileOutputStream(file);
            value.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();
        } catch (Exception ex) {
            Log.e("CACHE", "Could not create file", ex);
        }
    }

    protected Bitmap getBitmap(String filename) {
        File file = new File(mManager.getContext().getCacheDir(), filename);
        return BitmapFactory.decodeFile(file.getAbsolutePath());
    }
}
