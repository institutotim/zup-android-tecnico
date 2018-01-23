package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.entities.User;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemUserInfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_userinfo, container, false);
        return root;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        User user = (User) getArguments().getParcelable("user");
        if(user != null)
            fillData(view, user);
        else
            showLoading();
    }

    void fillData(View root, User user) {
        if(user == null)
            return;

        hideLoading();

        TextView txtName = (TextView) root.findViewById(R.id.full_name);
        TextView txtEmail = (TextView) root.findViewById(R.id.email_address);
        TextView txtPhone = (TextView) root.findViewById(R.id.phone_number);

        txtName.setText(user.name);
        txtEmail.setText(user.email);
        txtPhone.setText(user.phone);
    }

    void showLoading() {
        getView().findViewById(R.id.userinfo_loading).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.userinfo_container).setVisibility(View.GONE);
    }

    void hideLoading() {
        getView().findViewById(R.id.userinfo_loading).setVisibility(View.GONE);
        getView().findViewById(R.id.userinfo_container).setVisibility(View.VISIBLE);
    }
}
