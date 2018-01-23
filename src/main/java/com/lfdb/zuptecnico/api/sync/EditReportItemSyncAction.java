package com.particity.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.errors.SyncErrors;
import com.particity.zuptecnico.entities.ImageItem;
import com.particity.zuptecnico.entities.Position;
import com.particity.zuptecnico.entities.ReportItem;
import com.particity.zuptecnico.entities.User;
import com.particity.zuptecnico.entities.collections.SingleReportItemCollection;
import com.particity.zuptecnico.entities.requests.CreateArbitraryReportItemRequest;
import com.particity.zuptecnico.entities.requests.CreateReportItemRequest;
import com.particity.zuptecnico.entities.requests.CreateUserRequest;
import com.particity.zuptecnico.entities.responses.PublishReportResponse;

import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;

public class EditReportItemSyncAction extends SyncAction implements ReportSyncAction {
    public static final String REPORT_EDITED = "report_edited";

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Serializer {
        public boolean isArbitraryPosition;

        public int reportId;
        public Double latitude;
        public Double longitude;
        public int category_id;
        public int inventory_item_id;
        public String description;
        public String reference;
        public String address;
        public String number;
        public String district;
        public String postalCode;
        public String city;
        public String state;
        public String country;
        public ReportItem.Image[] images;
        public int currentCategoryId;
        public int statusId;
        public int caseConductorId = -1;
        public HashMap<Integer, String> custom_fields;

        public User user;

        public String error;

        public Serializer() {
        }
    }

    public int reportId;
    public boolean isArbitraryPosition;
    public Double latitude;
    public Double longitude;
    public int categoryId;
    public int inventoryItemId;
    public int statusId;
    public String description;
    public String reference;
    public String address;
    public String number;
    public String postalCode;
    public String district;
    public String city;
    public String state;
    public String country;
    public ReportItem.Image[] images;
    public User user;
    public HashMap<Integer, String> custom_fields;
    public int caseConductorId = -1;
    public int currentCategoryId;

    public static final Creator<EditReportItemSyncAction> CREATOR = new Creator<EditReportItemSyncAction>() {
        @Override
        public EditReportItemSyncAction createFromParcel(Parcel source) {
            return new EditReportItemSyncAction(source);
        }

        @Override
        public EditReportItemSyncAction[] newArray(int size) {
            return new EditReportItemSyncAction[size];
        }
    };

    public EditReportItemSyncAction() {
        super();
    }

    public EditReportItemSyncAction(Parcel in) {
        super(in);
        reportId = in.readInt();
        isArbitraryPosition = in.readByte() != 0;
        latitude = in.readDouble();
        longitude = in.readDouble();
        categoryId = in.readInt();
        inventoryItemId = in.readInt();
        description = in.readString();
        reference = in.readString();
        address = in.readString();
        number = in.readString();
        postalCode = in.readString();
        district = in.readString();
        city = in.readString();
        state = in.readString();
        country = in.readString();
        images = ReportItem.Image.toMyObjects(in.readParcelableArray(ReportItem.Image.class.getClassLoader()));
        user = in.readParcelable(User.class.getClassLoader());
        statusId = in.readInt();
        currentCategoryId = in.readInt();
        custom_fields = (HashMap<Integer, String>) in.readSerializable();
        caseConductorId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(reportId);
        dest.writeByte((byte) (isArbitraryPosition ? 1 : 0));
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(categoryId);
        dest.writeInt(inventoryItemId);
        dest.writeString(description);
        dest.writeString(reference);
        dest.writeString(address);
        dest.writeString(number);
        dest.writeString(postalCode);
        dest.writeString(district);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(country);
        dest.writeParcelableArray(images, flags);
        dest.writeParcelable(user, flags);
        dest.writeInt(statusId);
        dest.writeInt(currentCategoryId);
        dest.writeSerializable(custom_fields);
        dest.writeInt(caseConductorId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public EditReportItemSyncAction(int id, Double latitude, Double longitude, int categoryId,
                                    String description, String reference, String address, String number,
                                    String postalCode, String district, String city, String state, String country,
                                    ReportItem.Image[] images, User user, int currentCategoryId,
                                    int statusId, HashMap<Integer, String> custom_fields, boolean isArbitraryPosition, int caseConductorId) {
        init(id, latitude, longitude, categoryId, description, reference, address, number, postalCode, district, city,
                state, country, images, user, currentCategoryId, statusId, custom_fields, isArbitraryPosition, caseConductorId);
    }

    public EditReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        if (serializer.isArbitraryPosition) {
            init(serializer.reportId, serializer.latitude, serializer.longitude, serializer.category_id,
                    serializer.description, serializer.reference, serializer.address, serializer.number, serializer.postalCode,
                    serializer.district, serializer.city, serializer.state, serializer.country,
                    serializer.images, serializer.user, serializer.currentCategoryId, serializer.statusId, serializer.custom_fields, true, serializer.caseConductorId);
        } else {
            init(serializer.reportId, serializer.inventory_item_id, serializer.category_id,
                    serializer.description, serializer.reference, serializer.images, serializer.user,
                    serializer.currentCategoryId, serializer.statusId, serializer.custom_fields, serializer.caseConductorId);
        }
        setError(serializer.error);
    }

    void init(int id, Double latitude, Double longitude, int category_id, String description,
              String reference, String address, String number, String postalCode, String district, String city,
              String state, String country, ReportItem.Image[] images, User user, int currentCategoryId,
              int statusId, HashMap<Integer, String> custom_fields, boolean isArbitraryPosition, int caseConductorId) {
        this.isArbitraryPosition = isArbitraryPosition;
        this.reportId = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.categoryId = category_id;
        this.description = description;
        this.reference = reference;
        this.address = address;
        this.number = number;
        this.postalCode = postalCode;
        this.district = district;
        this.city = city;
        this.state = state;
        this.country = country;
        this.images = images;
        this.user = user;
        this.currentCategoryId = currentCategoryId;
        this.statusId = statusId;
        this.custom_fields = custom_fields;
        this.caseConductorId = caseConductorId;
    }

    void init(int id, int inventory_item_id, int category_id, String description,
              String reference, ReportItem.Image[] images, User user, int currentCategoryId,
              int statusId, HashMap<Integer, String> custom_fields, int caseConductorId) {
        this.reportId = id;
        this.isArbitraryPosition = false;
        this.inventoryItemId = inventory_item_id;
        this.categoryId = category_id;
        this.description = description;
        this.reference = reference;
        this.images = images;
        this.user = user;
        this.currentCategoryId = currentCategoryId;
        this.statusId = statusId;
        this.custom_fields = custom_fields;
        this.caseConductorId = caseConductorId;
    }

    public ReportItem convertToReportItem() {
        ReportItem item = new ReportItem();
        item.id = reportId;
        item.reference = reference;
        item.category_id = categoryId;
        item.images = images;
        item.city = city;
        item.state = state;
        item.country = country;
        item.district = district;
        item.description = description;
        item.address = address;
        item.number = number;
        item.postalCode = postalCode;
        item.user = user;
        item.userId = user.id;
        item.custom_fields = custom_fields;
        item.position = new Position();
        return item;
    }

    @Override
    protected boolean onPerform() {
        try {
            if (this.user != null && this.user.id == User.NEEDS_TO_BE_CREATED) {
                CreateUserRequest request = new CreateUserRequest();
                request.setGeneratePassword(true);
                request.setEmail(this.user.email);
                request.setName(this.user.name);
                request.setAddress(this.user.address);
                request.setAddressAdditional(this.user.address_additional);
                request.setDistrict(this.user.district);
                request.setCity(this.user.city);
                request.setPostalCode(this.user.postal_code);
                request.setPhone(this.user.phone);
                request.setDocument(this.user.document);

                this.user = Zup.getInstance().getService().createUser(request).user;
            }
            SingleReportItemCollection result;
            if (isArbitraryPosition) {
                CreateArbitraryReportItemRequest request = new CreateArbitraryReportItemRequest();
                request.setLatitude(this.latitude);
                request.setLongitude(this.longitude);
                request.setCategoryId(this.currentCategoryId);
                request.setDescription(this.description);
                request.setReference(this.reference);
                request.setAddress(this.address);
                request.setNumber(this.number);
                request.setPostalCode(this.postalCode);
                request.setDistrict(this.district);
                request.setCity(this.city);
                request.setState(this.state);
                request.setStatusId(String.valueOf(statusId));
                request.setCustomFields(custom_fields);
                if (caseConductorId != -1) {
                    request.setCaseConductorId(caseConductorId);
                }

                request.setCountry(this.country);
                request.setImages(toImagesItem());
                if (this.user != null)
                    request.setUserId(this.user.id);

                result = Zup.getInstance().getService()
                        .updateReportItem(this.categoryId, this.reportId, request);
            } else {
                CreateReportItemRequest request = new CreateReportItemRequest();
                request.setCategoryId(this.currentCategoryId);
                request.setDescription(this.description);
                request.setReference(this.reference);
                request.setStatusId(String.valueOf(statusId));
                request.setCustomFields(custom_fields);
                request.setImages(toImagesItem());
                if (caseConductorId != -1) {
                    request.setCaseConductorId(caseConductorId);
                }
                if (this.user != null)
                    request.setUserId(this.user.id);

                result = Zup.getInstance().getService()
                        .updateReportItem(this.categoryId, this.reportId, request);

            }

            updateLocalReportItem(result.report);
            Intent intent = new Intent();
            intent.putExtra("report", result.report);
            broadcastAction(REPORT_EDITED, intent);

            return true;
        } catch (RetrofitError ex) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }

            int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
            Crashlytics.logException(SyncErrors.build(errorType, ex));
            if (errorType == SyncErrors.NOT_FOUND_ERROR_CODE || errorType == 504) {
                setError(ex.getMessage());
                return false;
            }
            try {
                PublishReportResponse response = (PublishReportResponse) ex.getBodyAs(PublishReportResponse.class);
                if (response.error != null) {
                    setError(getError(response));
                    return false;
                }
            } catch (Exception error) {
                error.printStackTrace();
            }
            if (getError() == null) {
                setError(ex.getMessage());
            }
            return false;
        }
    }

    private String getError(PublishReportResponse response) {
        String message = "";
        if (response.error instanceof Map) {
            int j = 0;
            for (Object key : ((Map) response.error).keySet()) {
                if (j > 0) {
                    message += "\r\n\r\n";
                }
                String fieldName = key.toString();
                message += fieldName + "\r\n";
                List lst = (List) ((Map) response.error).get(key.toString());
                int i = 0;
                for (Object msg : lst) {
                    if (i > 0) {
                        message += "\r\n";
                    }
                    message += " - " + msg;
                    i++;
                }
                j++;
            }
            Crashlytics.setString("error", message);
            return message;
        } else if (response.error != null) {
            return response.error.toString();
        }
        return null;
    }

    private ImageItem[] toImagesItem() {
        int size = images.length;
        ImageItem[] imgs = new ImageItem[size];
        for (int index = 0; index < size; index++) {
            imgs[index] = images[index].getImageItem();
        }
        return imgs;
    }

    private void updateLocalReportItem(ReportItem item) {
        if (!Zup.getInstance().getReportItemService().hasReportItem(this.reportId)) {
            return;
        }
        Zup.getInstance().getReportItemService().deleteReportItem(this.reportId);
        Zup.getInstance().getReportItemService().addReportItem(item);
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.isArbitraryPosition = this.isArbitraryPosition;
        serializer.latitude = this.latitude;
        serializer.longitude = this.longitude;
        serializer.inventory_item_id = this.inventoryItemId;
        serializer.category_id = this.categoryId;
        serializer.description = this.description;
        serializer.reference = this.reference;
        serializer.address = this.address;
        serializer.number = this.number;
        serializer.postalCode = this.postalCode;
        serializer.district = this.district;
        serializer.city = this.city;
        serializer.state = this.state;
        serializer.country = this.country;
        serializer.images = this.images;
        serializer.error = getError();
        serializer.user = this.user;
        serializer.reportId = this.reportId;
        serializer.currentCategoryId = this.currentCategoryId;
        serializer.statusId = statusId;
        serializer.custom_fields = custom_fields;
        serializer.caseConductorId = caseConductorId;

        ObjectMapper mapper = new ObjectMapper();
        String res = mapper.writeValueAsString(serializer);

        return new JSONObject(res);
    }
}
