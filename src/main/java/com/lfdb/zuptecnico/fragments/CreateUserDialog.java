package com.lfdb.zuptecnico.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;


import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.User;
import com.lfdb.zuptecnico.entities.collections.UserCreationResult;
import com.lfdb.zuptecnico.entities.requests.CreateUserRequest;
import com.lfdb.zuptecnico.util.Strings;

import br.com.rezende.mascaras.Mask;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by igorlira on 7/23/15.
 */
public class CreateUserDialog extends DialogFragment implements Callback<UserCreationResult> {
    public interface OnUserCreatedListener {
        void onUserCreated(User user);
    }

    private OnUserCreatedListener listener;

    EditText txtName;
    EditText txtEmail;
    EditText txtAddress;
    EditText txtAddressExtra;
    EditText txtDistrict;
    EditText txtCity;
    EditText txtPostalCode;
    EditText txtPhone;
    EditText txtDocument;

    public void setListener(OnUserCreatedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_usercreator, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtName = (EditText) getView().findViewById(R.id.user_name);
        txtEmail = (EditText) getView().findViewById(R.id.user_email);
        txtAddress = (EditText) getView().findViewById(R.id.user_address);
        txtAddressExtra = (EditText) getView().findViewById(R.id.user_address_extra);
        txtDistrict = (EditText) getView().findViewById(R.id.user_district);
        txtCity = (EditText) getView().findViewById(R.id.user_city);
        txtPostalCode = (EditText) getView().findViewById(R.id.user_postalcode);
        txtPhone = (EditText) getView().findViewById(R.id.user_phone);
        txtDocument = (EditText) getView().findViewById(R.id.user_document);

        txtPhone.addTextChangedListener(Mask.insert("(##) #####-####", txtPhone));
        txtPostalCode.addTextChangedListener(Mask.insert("#####-###", txtPostalCode));
        txtDocument.addTextChangedListener(Mask.insert("###.###.###-##", txtDocument));

        initErrorScreen(view);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
    }

    void confirm() {
        if (TextUtils.isEmpty(txtName.getText()) || TextUtils.isEmpty(txtEmail.getText()) ||
                TextUtils.isEmpty(txtAddress.getText()) || TextUtils.isEmpty(txtDistrict.getText()) ||
                TextUtils.isEmpty(txtCity.getText()) || TextUtils.isEmpty(txtPostalCode.getText()) ||
                TextUtils.isEmpty(txtPhone.getText()) || TextUtils.isEmpty(txtDocument.getText()) ||
                !Strings.isValidCPF(txtDocument.getText().toString())) {

            resetLabels();
            boolean firstRequireField = false;

            if (TextUtils.isEmpty(txtName.getText())) {
                showErrorFor("name", true);
                firstRequireField = true;
                txtName.requestFocus();
            }

            if (TextUtils.isEmpty(txtEmail.getText())) {
                showErrorFor("email", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtEmail.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtAddress.getText())) {
                showErrorFor("address", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtAddress.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtDistrict.getText())) {
                showErrorFor("district", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtDistrict.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtCity.getText())) {
                showErrorFor("city", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtCity.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtPostalCode.getText())) {
                showErrorFor("postal_code", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtPostalCode.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtPhone.getText())) {
                showErrorFor("phone", true);
                if (!firstRequireField) {
                    firstRequireField = true;
                    txtPhone.requestFocus();
                }
            }

            if (TextUtils.isEmpty(txtDocument.getText()) || !Strings.isValidCPF(txtDocument.getText().toString())) {
                showErrorFor("document", true);
                if (!firstRequireField) {
                    txtDocument.requestFocus();
                }
            }
        } else {
            showLoading();

            CreateUserRequest request = new CreateUserRequest();
            request.setGeneratePassword(true);
            request.setEmail(txtEmail.getText().toString());
            request.setName(txtName.getText().toString());
            request.setAddress(txtAddress.getText().toString());
            request.setAddressAdditional(txtAddressExtra.getText().toString());
            request.setDistrict(txtDistrict.getText().toString());
            request.setCity(txtCity.getText().toString());
            request.setPostalCode(txtPostalCode.getText().toString());
            request.setPhone(txtPhone.getText().toString());
            request.setDocument(txtDocument.getText().toString());

            Zup.getInstance().getService().createUser(request, this);
        }
    }

    void showLoading() {
        if (getView() == null)
            return;

        setCancelable(false);
        getView().findViewById(R.id.dialog_createuser_form).setVisibility(View.GONE);
        getView().findViewById(R.id.loading_view).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
    }

    void hideLoading() {
        if (getView() == null)
            return;

        setCancelable(true);
        getView().findViewById(R.id.dialog_createuser_form).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.loading_view).setVisibility(View.GONE);
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }

    void initErrorScreen(View root) {
        View backButton = root.findViewById(R.id.create_user_error_back);
        View cancelButton = root.findViewById(R.id.create_user_error_cancel);
        View scheduleButton = root.findViewById(R.id.create_user_error_schedule);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorScreenBack();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorScreenCancel();
            }
        });

        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorScreenSchedule();
            }
        });
    }

    void showErrorScreen() {
        if (getView() == null)
            return;

        setCancelable(false);
        getView().findViewById(R.id.dialog_createuser_form).setVisibility(View.GONE);
        getView().findViewById(R.id.loading_view).setVisibility(View.GONE);
        getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
        getView().findViewById(R.id.create_user_error_container).setVisibility(View.VISIBLE);
    }

    void errorScreenBack() {
        if (getView() == null)
            return;

        setCancelable(true);
        getView().findViewById(R.id.dialog_createuser_form).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.loading_view).setVisibility(View.GONE);
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.create_user_error_container).setVisibility(View.GONE);
    }

    void errorScreenCancel() {
        dismiss();
    }

    void errorScreenSchedule() {
        if (getView() == null)
            return;

        TextView txtName = (TextView) getView().findViewById(R.id.user_name);
        TextView txtEmail = (TextView) getView().findViewById(R.id.user_email);
        TextView txtAddress = (TextView) getView().findViewById(R.id.user_address);
        TextView txtAddressExtra = (TextView) getView().findViewById(R.id.user_address_extra);
        TextView txtDistrict = (TextView) getView().findViewById(R.id.user_district);
        TextView txtCity = (TextView) getView().findViewById(R.id.user_city);
        TextView txtPostalCode = (TextView) getView().findViewById(R.id.user_postalcode);
        TextView txtPhone = (TextView) getView().findViewById(R.id.user_phone);
        TextView txtDocument = (TextView) getView().findViewById(R.id.user_document);

        User newUser = new User();
        newUser.id = User.NEEDS_TO_BE_CREATED;
        newUser.name = txtName.getText().toString();
        newUser.email = txtEmail.getText().toString();
        newUser.address = txtAddress.getText().toString();
        newUser.address_additional = txtAddressExtra.getText().toString();
        newUser.district = txtDistrict.getText().toString();
        newUser.city = txtCity.getText().toString();
        newUser.postal_code = txtPostalCode.getText().toString();
        newUser.phone = txtPhone.getText().toString();
        newUser.document = txtDocument.getText().toString();

        if (this.listener != null)
            this.listener.onUserCreated(newUser);

        dismiss();
    }

    TextView getLabelFor(String name) {
        if (getView() == null)
            return null;

        switch (name) {
            case "name":
                return (TextView) getView().findViewById(R.id.createuser_label_name);

            case "email":
                return (TextView) getView().findViewById(R.id.createuser_label_email);

            case "address":
                return (TextView) getView().findViewById(R.id.createuser_label_address);

            case "address_additional":
                return (TextView) getView().findViewById(R.id.createuser_label_address_extra);

            case "district":
                return (TextView) getView().findViewById(R.id.createuser_label_district);

            case "city":
                return (TextView) getView().findViewById(R.id.createuser_label_city);

            case "postal_code":
                return (TextView) getView().findViewById(R.id.createuser_label_postal);

            case "phone":
                return (TextView) getView().findViewById(R.id.createuser_label_phone);

            case "document":
                return (TextView) getView().findViewById(R.id.createuser_label_document);

            default:
                return null;
        }
    }

    void showErrorFor(String name, boolean error) {
        TextView tv = getLabelFor(name);
        if (tv == null || getView() == null)
            return;

        if (error)
            tv.setTextColor(getView().getResources().getColor(R.color.field_label_error));
        else
            tv.setTextColor(getView().getResources().getColor(R.color.field_label_color));
    }

    @Override
    public void success(UserCreationResult userCreationResult, Response response) {
        if (this.listener != null)
            this.listener.onUserCreated(userCreationResult.user);

        this.dismiss();
    }

    void resetLabels() {
        showErrorFor("name", false);
        showErrorFor("email", false);
        showErrorFor("address", false);
        showErrorFor("address_additional", false);
        showErrorFor("district", false);
        showErrorFor("city", false);
        showErrorFor("postal_code", false);
        showErrorFor("phone", false);
        showErrorFor("document", false);
    }

    @Override
    public void failure(RetrofitError error) {
        String errorDescription = null;
        Log.e("RETROFIT", "Could not create user", error);

        try {
            UserCreationResult result = (UserCreationResult) error.getBodyAs(UserCreationResult.class);

            if (result.error != null) {
                resetLabels();
                for (String key : result.error.keySet()) {
                    showErrorFor(key, true);
                }

                hideLoading();
            } else {
                errorDescription = result.message;
                showErrorScreen();
            }
        } catch (Exception ex) {
            errorDescription = "Não foi possível conectar ao servidor.";
        }

        if (errorDescription != null && getView() != null) {
            TextView txtError = (TextView) getView().findViewById(R.id.create_user_error);
            txtError.setText(errorDescription);
            showErrorScreen();
        }
    }
}
