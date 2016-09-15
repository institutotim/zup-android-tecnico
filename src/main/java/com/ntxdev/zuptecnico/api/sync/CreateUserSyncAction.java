package com.ntxdev.zuptecnico.api.sync;

import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import java.io.IOException;

import retrofit.RetrofitError;

public class CreateUserSyncAction extends SyncAction {
    public static class Serializer {
        public String email;
        public String password;
        public String password_confirmation;
        public String name;
        public String phone;
        public String document;
        public String address;
        public String address_additional;
        public String postal_code;
        public String district;
        public String city;

        public String error;
    }

    String email;
    String password;
    String password_confirmation;
    String name;
    String phone;
    String document;
    String address;
    String address_additional;
    String postal_code;
    String district;
    String city;

    public static final Creator<CreateUserSyncAction> CREATOR = new Creator<CreateUserSyncAction>() {
        @Override
        public CreateUserSyncAction createFromParcel(Parcel source) {
            return new CreateUserSyncAction(source);
        }

        @Override
        public CreateUserSyncAction[] newArray(int size) {
            return new CreateUserSyncAction[size];
        }
    };

    public CreateUserSyncAction() {
        super();
    }

    public CreateUserSyncAction(Parcel in) {
        super(in);
        email = in.readString();
        password = in.readString();
        password_confirmation = in.readString();
        name = in.readString();
        phone = in.readString();
        document = in.readString();
        address = in.readString();
        address_additional = in.readString();
        postal_code = in.readString();
        district = in.readString();
        city = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(password_confirmation);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(document);
        dest.writeString(address);
        dest.writeString(address_additional);
        dest.writeString(postal_code);
        dest.writeString(district);
        dest.writeString(city);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public CreateUserSyncAction(String email, String password, String password_confirmation,
                                String name, String phone, String document, String address,
                                String address_additional, String postal_code, String district,
                                String city) {
        this.email = email;
        this.password = password;
        this.password_confirmation = password_confirmation;
        this.name = name;
        this.phone = phone;
        this.document = document;
        this.address = address;
        this.address_additional = address_additional;
        this.postal_code = postal_code;
        this.district = district;
        this.city = city;
    }

    public CreateUserSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.email = serializer.email;
        this.password = serializer.password;
        this.password_confirmation = serializer.password_confirmation;
        this.name = serializer.name;
        this.phone = serializer.phone;
        this.document = serializer.document;
        this.address = serializer.address;
        this.address_additional = serializer.address_additional;
        this.postal_code = serializer.postal_code;
        this.district = serializer.district;
        this.city = serializer.city;
        setError(serializer.error);
    }

    @Override
    protected boolean onPerform() {
        try {
            // Need to merge retrofit branch
            return false;
        } catch (RetrofitError ex) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Crashlytics.logException(ex);
            setError(ex.getMessage());
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.email = this.email;
        serializer.password = password;
        serializer.password_confirmation = password_confirmation;
        serializer.name = name;
        serializer.phone = phone;
        serializer.document = document;
        serializer.address = address;
        serializer.address_additional = address_additional;
        serializer.postal_code = postal_code;
        serializer.district = district;
        serializer.city = city;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }
}
