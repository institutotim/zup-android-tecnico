package com.lfdb.zuptecnico;

import android.app.Application;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.lfdb.zuptecnico.api.Zup;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

/**
 * Created by igorlira on 12/30/14.
 */
public class ZupApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        context = getApplicationContext();
        Zup.getInstance().initStorage(context);
    }

    @Override
    public void onTerminate() {
        Log.e("APP", "Application is being closed");

        Zup.getInstance().close();
        super.onTerminate();
    }

    public static Snackbar toast(View view, String text) {
        return Snackbar.make(view, text, Snackbar.LENGTH_LONG);
    }

    public static Snackbar toast(View view, int resId) {
        return Snackbar.make(view, resId, Snackbar.LENGTH_LONG);
    }

    public static Context getContext()
    {
        return context;
    }
}
