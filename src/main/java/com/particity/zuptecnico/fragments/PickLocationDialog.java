package com.particity.zuptecnico.fragments;

import android.content.DialogInterface;
import android.location.Address;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.util.Utilities;

public class PickLocationDialog extends DialogFragment
        implements PickLocationFragment.OnLocationValidatedListener {

    public interface OnLocationSetListener {
        void onLocationSet(double latitude, double longitude, Address address, String reference);
    }

    private OnLocationSetListener listener;
    private PickLocationFragment fragment;
    private boolean isValidPosition;

    public void setOnLocationSetListener(OnLocationSetListener listener) {
        this.listener = listener;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        View view = inflater.inflate(R.layout.dialog_pick_location, container, false);
        View confirm = view.findViewById(R.id.confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        return view;
    }

    void showOffLineError() {
        getView().findViewById(R.id.offline_warning).setVisibility(View.VISIBLE);
    }

    void hideOfflineError() {
        getView().findViewById(R.id.offline_warning).setVisibility(View.GONE);
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        dialog.dismiss();
    }

    void confirm() {
        if (!isValidPosition) {
            if (!Utilities.isConnected(getActivity())) {
                ZupApplication.toast(getActivity().findViewById(android.R.id.content), R.string.offline_gps_error).show();
            } else {
                ZupApplication.toast(getActivity().findViewById(android.R.id.content), R.string.online_gps_error).show();
            }
            return;
        }
        Address address = fragment.getAddress();
        if(TextUtils.isEmpty(address.getThoroughfare())) {
            ZupApplication.toast(getActivity().findViewById(android.R.id.content), R.string.address_required_error).show();
            return;
        }

        if (this.listener != null) {
            double latitude = fragment.latitude;
            double longitude = fragment.longitude;
            String reference = fragment.getReference();
            this.listener.onLocationSet(latitude, longitude, address, reference);
        }

        dismiss();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.fragment = new PickLocationFragment();
        this.fragment.setArguments(getArguments());
        this.fragment.setListener(this);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_container, this.fragment, "pick_location_fragment").commit();
        view.findViewById(R.id.offline_warning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Utilities.isConnected(getActivity())){
                    hideOfflineError();
                    fragment.reload();
                }else{
                    ZupApplication.toast(getActivity().findViewById(android.R.id.content), R.string.error_no_internet_toast).show();
                }
            }
        });
        if(Utilities.isConnected(getActivity())){
            hideOfflineError();
        }else{
            showOffLineError();
        }
    }

    void setValidPosition(boolean valid) {
        this.isValidPosition = valid;
        View button = getView().findViewById(R.id.confirm);

        if (valid) {
            button.setAlpha(1);
        } else {
            button.setAlpha(.5f);
        }
    }

    @Override
    public void onValidLocationSet() {
        setValidPosition(true);
    }

    @Override
    public void onInvalidLocationSet() {
        setValidPosition(false);
    }
}
