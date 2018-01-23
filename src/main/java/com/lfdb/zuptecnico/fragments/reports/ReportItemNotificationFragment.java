package com.particity.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.ReportItemNotificationsAdapter;
import com.particity.zuptecnico.entities.ReportNotificationCollection.ReportNotificationItem;
import com.particity.zuptecnico.ui.ScrollLessListView;

public class ReportItemNotificationFragment extends Fragment {
    ReportNotificationItem[] notifications;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_notification, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notifications = ReportNotificationItem.toMyObjects(getArguments().getParcelableArray("notifications"));
        if(notifications != null) {
            ReportItemNotificationsAdapter adapter = new ReportItemNotificationsAdapter(getActivity(), notifications);
            ((ScrollLessListView) getView().findViewById(R.id.notification_listview)).setAdapter(adapter);
            hideLoading();
        }else {
            showLoading();
        }
    }

    void showLoading() {
        getView().findViewById(R.id.notificationinfo_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        getView().findViewById(R.id.notificationinfo_loading).setVisibility(View.GONE);
    }
}
