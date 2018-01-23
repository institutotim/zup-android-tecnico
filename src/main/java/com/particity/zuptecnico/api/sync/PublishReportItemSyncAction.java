package com.particity.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.errors.SyncErrors;
import com.particity.zuptecnico.config.Constants;
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

/**
 * Created by igorlira on 7/13/15.
 */
public class PublishReportItemSyncAction extends SyncAction implements ReportSyncAction {
  public static final String REPORT_PUBLISHED = "report_published";

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Serializer {
    public boolean isArbitraryPosition;

    public Double latitude;
    public Double longitude;
    public int categoryId;
    public Integer inventoryItemId;
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
    public HashMap<Integer, String> custom_fields;

    public User user;

    public String error;

    public Serializer() {
    }
  }

  public boolean isArbitraryPosition;
  public Double latitude;
  public Double longitude;
  public int categoryId;
  public int inventoryItemId;
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

  public static final Creator<PublishReportItemSyncAction> CREATOR =
      new Creator<PublishReportItemSyncAction>() {
        @Override public PublishReportItemSyncAction createFromParcel(Parcel source) {
          return new PublishReportItemSyncAction(source);
        }

        @Override public PublishReportItemSyncAction[] newArray(int size) {
          return new PublishReportItemSyncAction[size];
        }
      };

  public PublishReportItemSyncAction(Parcel in) {
    super(in);
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
    images = ReportItem.Image.toMyObjects(
        in.readParcelableArray(ReportItem.Image.class.getClassLoader()));
    user = in.readParcelable(User.class.getClassLoader());
    custom_fields = (HashMap<Integer, String>) in.readSerializable();
  }

  public PublishReportItemSyncAction() {
    super();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
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
    dest.writeSerializable(custom_fields);
  }

  @Override public int describeContents() {
    return 0;
  }

  public PublishReportItemSyncAction(double latitude, double longitude, int categoryId,
      String description, String reference, String address, String number, String postalCode,
      String district, String city, String state, String country, ReportItem.Image[] images,
      User user, HashMap<Integer, String> custom_fields) {
    init(latitude, longitude, categoryId, description, reference, address, number, postalCode,
        district, city, state, country, images, user, custom_fields);
  }

  public PublishReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
    Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

    if (serializer.isArbitraryPosition) {
      init(serializer.latitude, serializer.longitude, serializer.categoryId, serializer.description,
          serializer.reference, serializer.address, serializer.number, serializer.postalCode,
          serializer.district, serializer.city, serializer.state, serializer.country,
          serializer.images, serializer.user, serializer.custom_fields);
    } else {
      init(serializer.inventoryItemId, serializer.categoryId, serializer.description,
          serializer.reference, serializer.images, serializer.user, serializer.custom_fields);
    }
    setError(serializer.error);
  }

  void init(double latitude, double longitude, int categoryId, String description, String reference,
      String address, String number, String postalCode, String district, String city, String state,
      String country, ReportItem.Image[] images, User user,
      HashMap<Integer, String> custom_fields) {
    this.isArbitraryPosition = true;
    this.latitude = latitude;
    this.longitude = longitude;
    this.categoryId = categoryId;
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
    this.custom_fields = custom_fields;
  }

  void init(int inventoryItemId, int categoryId, String description, String reference,
      ReportItem.Image[] images, User user, HashMap<Integer, String> custom_fields) {
    this.isArbitraryPosition = false;
    this.inventoryItemId = inventoryItemId;
    this.categoryId = categoryId;
    this.description = description;
    this.reference = reference;
    this.images = images;
    this.user = user;
    this.custom_fields = custom_fields;
  }

  public ReportItem convertToReportItem() {
    ReportItem item = new ReportItem();
    item.reference = reference;
    if (user != null) {
      item.assignedUserId = user.id;
      item.userId = user.id;
    }
    item.category_id = categoryId;
    item.assignedUser = user;
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
    item.position = new Position(Constants.DEFAULT_LAT, Constants.DEFAULT_LON);
    item.custom_fields = custom_fields;
    return item;
  }

  @Override protected boolean onPerform() {
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

      if (isArbitraryPosition) {
        CreateArbitraryReportItemRequest request = new CreateArbitraryReportItemRequest();
        request.setLatitude(this.latitude);
        request.setLongitude(this.longitude);
        request.setCategoryId(this.categoryId);
        request.setDescription(this.description);
        request.setReference(this.reference);
        request.setAddress(this.address);
        request.setNumber(this.number);
        request.setPostalCode(this.postalCode);
        request.setDistrict(this.district);
        request.setCity(this.city);
        request.setState(this.state);
        request.setCountry(this.country);
        request.setCustomFields(custom_fields);
        request.setImages(toImagesItem());
        if (this.user != null) request.setUserId(this.user.id);

        SingleReportItemCollection result =
            Zup.getInstance().getService().createReportItem(this.categoryId, request);
        inventoryItemId = result.report.id;
        createItem(result);
        return true;
      } else {
        CreateReportItemRequest request = new CreateReportItemRequest();
        request.setCategoryId(this.categoryId);
        request.setDescription(this.description);
        request.setReference(this.reference);
        request.setImages(toImagesItem());
        request.setCustomFields(custom_fields);
        if (this.user != null) request.setUserId(this.user.id);

        SingleReportItemCollection result =
            Zup.getInstance().getService().createReportItem(this.categoryId, request);
        inventoryItemId = result.report.id;
        createItem(result);
        return true;
      }
    } catch (RetrofitError ex) {
      try {
        Crashlytics.setString("request", serialize().toString());
      } catch (Exception e) {
        e.printStackTrace();

      } finally {
        int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
        Crashlytics.logException(SyncErrors.build(errorType, ex));
        try {
          PublishReportResponse response =
              (PublishReportResponse) ex.getBodyAs(PublishReportResponse.class);
          if (response != null && response.error != null) {
            setError(getError(response));
          }
          if (getError() == null) {
            setError(ex.getMessage());
          }
        } catch (Exception e) {
          e.printStackTrace();
          setError(ex.getMessage());
        }

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

  private void createItem(SingleReportItemCollection result) {
    Intent intent = new Intent();
    intent.putExtra("report", result.report);
    broadcastAction(REPORT_PUBLISHED, intent);
  }

  private ImageItem[] toImagesItem() {
    int size = images.length;
    ImageItem[] imgs = new ImageItem[size];
    for (int index = 0; index < size; index++) {
      imgs[index] = images[index].getImageItem();
    }
    return imgs;
  }

  @Override protected JSONObject serialize() throws Exception {
    Serializer serializer = new Serializer();
    serializer.isArbitraryPosition = this.isArbitraryPosition;
    serializer.latitude = this.latitude;
    serializer.longitude = this.longitude;
    serializer.inventoryItemId = this.inventoryItemId;
    serializer.categoryId = this.categoryId;
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
    serializer.custom_fields = custom_fields;

    ObjectMapper mapper = new ObjectMapper();
    String res = mapper.writeValueAsString(serializer);

    return new JSONObject(res);
  }
}
