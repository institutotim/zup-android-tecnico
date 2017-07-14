package com.particity.zuptecnico.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.activities.RootActivity;
import com.particity.zuptecnico.api.sync.SyncAction;

public class UIHelper {
    public interface UpdateDrawerStatus {
        void updateDrawerStatus();
    }

    static class Receiver extends BroadcastReceiver {
        AppCompatActivity activity;

        public Receiver(AppCompatActivity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SyncAction.ACTION_SYNC_BEGIN)) {
                UIHelper.showSyncProcess(activity);
            } else if (intent.getAction().equals(SyncAction.ACTION_SYNC_END)) {
                UIHelper.hideSyncProcess(activity);
            }
        }
    }

    public static void initActivity(final AppCompatActivity activity) {
        initActionBar(activity);

        if (activity.getSupportActionBar() != null) {
            final ViewGroup actionBar = (ViewGroup) activity.getSupportActionBar().getCustomView();
            ImageView drawer = (ImageView) actionBar.findViewById(R.id.sidebar_drawer);
            drawer.setClickable(true);
            if (activity.getClass().getSuperclass().equals(RootActivity.class)) {
                drawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((RootActivity) activity).toggleSidebar();
                    }
                });
                drawer.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_menu_white_24dp));
            } else {
                drawer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        activity.onBackPressed();
                    }
                });
                drawer.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.ic_arrow_back_white_24dp));
            }

            LocalBroadcastManager manager = LocalBroadcastManager.getInstance(activity);

            manager.registerReceiver(new Receiver(activity), new IntentFilter(SyncAction.ACTION_SYNC_BEGIN));
            manager.registerReceiver(new Receiver(activity), new IntentFilter(SyncAction.ACTION_SYNC_END));
        }
    }

    private static void showSyncProcess(AppCompatActivity activity) {
        activity.findViewById(R.id.actionbar_sync_progress).setVisibility(View.VISIBLE);
    }

    private static void hideSyncProcess(AppCompatActivity activity) {
        activity.findViewById(R.id.actionbar_sync_progress).setVisibility(View.GONE);
    }

    private static void initActionBar(AppCompatActivity activity) {
        if (activity.getSupportActionBar() == null) {
            return;
        }

        ViewGroup actionBarLayout = (ViewGroup) activity.getLayoutInflater().inflate(R.layout.action_bar, null);

        android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    }

    public static void setTitle(final AppCompatActivity activity, String title) {
        ViewGroup actionBar = (ViewGroup) activity.getSupportActionBar().getCustomView();
        TextView textTitle = (TextView) actionBar.findViewById(R.id.actionbar_title);
        textTitle.setText(title);
    }

    public static android.support.v7.widget.PopupMenu initMenu(AppCompatActivity activity) {
        ViewGroup actionBar = (ViewGroup) activity.getSupportActionBar().getCustomView();
        View drawer = actionBar.findViewById(R.id.sidebar_drawer);
        View arrow = actionBar.findViewById(R.id.actionbar_title_arrow);
        TextView textTitle = (TextView) actionBar.findViewById(R.id.actionbar_title);

        final android.support.v7.widget.PopupMenu menu = new android.support.v7.widget.PopupMenu(activity, drawer);
        textTitle.setClickable(true);
        textTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.show();
            }
        });
        arrow.setVisibility(View.VISIBLE);

        return menu;
    }
}
