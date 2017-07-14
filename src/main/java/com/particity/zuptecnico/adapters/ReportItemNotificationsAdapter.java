package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.ReportNotificationCollection.ReportNotificationItem;
import com.ntxdev.zuptecnico.util.Utilities;

import java.util.Arrays;
import java.util.Comparator;

public class ReportItemNotificationsAdapter extends BaseAdapter {
    Context context;
    ReportNotificationItem[] items;
    SparseArray<View> viewCache;

    public ReportItemNotificationsAdapter(Context context, ReportNotificationItem[] items) {
        this.context = context;
        this.viewCache = new SparseArray<>();
        this.items = items;
        Arrays.sort(this.items, new Comparator<ReportNotificationItem>() {
            @Override
            public int compare(ReportNotificationItem lhs, ReportNotificationItem rhs) {
                return rhs.updatedAt.compareTo(lhs.updatedAt);
            }
        });
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public ReportNotificationItem getItem(int i) {
        return items[i];
    }

    @Override
    public long getItemId(int i) {
        return getItem(i).id;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        ReportNotificationItem comment = getItem(i);

        if (viewCache.get(comment.id) != null)
            return viewCache.get(comment.id);
        else {
            LayoutInflater inflater = LayoutInflater.from(context);
            View view = inflater.inflate(R.layout.report_details_notificationinfo_item, viewGroup, false);
            fillData(view, comment);

            return view;
        }
    }

    void fillData(View root, final ReportNotificationItem notification) {
        if (notification == null)
            return;

        TextView txtName = (TextView) root.findViewById(R.id.notification_name);
        TextView txtDaysToDeadline = (TextView) root.findViewById(R.id.notification_days_to_deadline);
        TextView txtCreatedAt = (TextView) root.findViewById(R.id.notification_created_at);
        TextView txtStatus = (TextView) root.findViewById(R.id.notification_status);
        TextView txtDeadlineInDays = (TextView) root.findViewById(R.id.notification_deadline_in_days);

        txtName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNotification(notification);
            }
        });

        txtCreatedAt.setText(context.getString(R.string.notification_created_at_title) + " " +
                Utilities.formatIsoDateAndTime(notification.createdAt));

        StringBuilder deadlineInDays = new StringBuilder(context.getString(R.string.notification_deadline_in_days));
        deadlineInDays.append(" ");
        deadlineInDays.append(notification.deadlineInDays);
        deadlineInDays.append(" ");
        deadlineInDays.append(context.getString(R.string.inventory_item_extra_days));
        txtDeadlineInDays.setText(deadlineInDays.toString());

        txtName.setText(notification.notificationType.title);
        StringBuilder daysToDeadLineFormatted = new StringBuilder(context.getString(R.string.notification_days_to_deadline));
        daysToDeadLineFormatted.append(" ");

        StringBuilder statusFormatted = new StringBuilder(context.getString(R.string.notification_status_title));
        statusFormatted.append(" ");

        if (!notification.current) {
            statusFormatted.append(context.getString(R.string.canceled_notification_label_text));
            daysToDeadLineFormatted.append("-");
        } else {
            statusFormatted.append(context.getString(R.string.active_notification_label_text));
            if (notification.daysToDeadline < 0) {
                daysToDeadLineFormatted.append(Math.abs(notification.daysToDeadline));
                daysToDeadLineFormatted.append(" ");
                daysToDeadLineFormatted.append(context.getString(R.string.inventory_item_extra_days));
                daysToDeadLineFormatted.append(" ");
                daysToDeadLineFormatted.append(context.getString(R.string.ago));

                statusFormatted.append(context.getString(R.string.notification_status_separator));
                statusFormatted.append(context.getString(R.string.overdue_notification_label_text));
            } else {
                daysToDeadLineFormatted.append(notification.daysToDeadline);
                daysToDeadLineFormatted.append(" ");
                daysToDeadLineFormatted.append(context.getString(R.string.inventory_item_extra_days));
            }
        }
        txtDaysToDeadline.setText(Html.fromHtml(daysToDeadLineFormatted.toString()));
        txtStatus.setText(Html.fromHtml(statusFormatted.toString()));
    }

    void showNotification(ReportNotificationItem item) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialoglayout = inflater.inflate(R.layout.report_notification_webview, null);
        WebView wv = (WebView) dialoglayout.findViewById(R.id.notification_view);
        wv.loadData(item.content, "text/html; charset=UTF-8", null);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.getSettings().setUseWideViewPort(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);

                return true;
            }
        });

        alert.setView(dialoglayout);
        alert.setNegativeButton(context.getString(R.string.close_title), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }
}