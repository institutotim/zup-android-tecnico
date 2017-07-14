package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by igorlira on 4/30/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true) @JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryItemImage implements Serializable, Parcelable {
  @JsonIgnoreProperties(ignoreUnknown = true) public static class Versions
      implements Serializable, Parcelable {
    public String high;
    public String low;
    public String thumb;

    public Versions() {

    }

    public Versions(Parcel in) {
      high = in.readString();
      low = in.readString();
      thumb = in.readString();
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel parcel, int i) {
      parcel.writeString(high);
      parcel.writeString(low);
      parcel.writeString(thumb);
    }

    public static final Parcelable.Creator<Versions> CREATOR = new Parcelable.Creator<Versions>() {
      public Versions createFromParcel(Parcel in) {
        return new Versions(in);
      }

      public Versions[] newArray(int size) {
        return new Versions[size];
      }
    };
  }

  public InventoryItemImage() {

  }

  public InventoryItemImage(Parcel in) {
    id = in.readInt();
    inventory_item_data_id = in.readInt();
    url = in.readString();
    versions = in.readParcelable(this.getClass().getClassLoader());
    content = in.readString();
    destroy = in.readByte() != 0;
    file_name = in.readString();
  }

  public int id;
  public String file_name;
  public int inventory_item_data_id;
  public String url;
  public Versions versions;
  public String content;
  public boolean destroy = false;

  @Override public int describeContents() {
    return 0;
  }

  public boolean equals(InventoryItemImage o) {
    if (this.content != null) {
      return TextUtils.equals(this.content, o.content);
    }
    return this.versions != null && o.versions != null && TextUtils.equals(this.versions.high,
        o.versions.high) && TextUtils.equals(this.versions.low, o.versions.low) && TextUtils.equals(
        this.versions.thumb, o.versions.thumb);
  }

  @Override public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(id);
    parcel.writeInt(inventory_item_data_id);
    parcel.writeString(url);
    parcel.writeParcelable(versions, i);
    parcel.writeString(content);
    parcel.writeByte((byte) (destroy ? 1 : 0));
    parcel.writeString(file_name);
  }

  public static final Parcelable.Creator<InventoryItemImage> CREATOR =
      new Parcelable.Creator<InventoryItemImage>() {
        public InventoryItemImage createFromParcel(Parcel in) {
          return new InventoryItemImage(in);
        }

        public InventoryItemImage[] newArray(int size) {
          return new InventoryItemImage[size];
        }
      };

  @JsonIgnore public void getJson(final JsonGenerator jGenerator) throws IOException {
    jGenerator.writeStartObject();

    jGenerator.writeNumberField("id", id);
    if (!TextUtils.isEmpty(content)) {
      jGenerator.writeStringField("content", content);
    }
    if (!TextUtils.isEmpty(file_name)) {
      jGenerator.writeStringField("file_name", file_name);
    }
    if (destroy) {
      jGenerator.writeBooleanField("destroy", destroy);
    }

    jGenerator.writeEndObject();
  }
}