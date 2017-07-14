package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by igorlira on 3/3/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class InventoryItem implements Parcelable {
  public static final int LOCAL_MASK = 0x80000000;

  public InventoryItem() {
    this.data = new ArrayList<Data>();
  }

  public int id;
  public String title;
  public Coordinates position;
  public Integer inventory_category_id;
  public Integer inventory_status_id;
  public ArrayList<Data> data;
  public String created_at;
  public String updated_at;
  public String address;

  @JsonProperty("related_entities") public RelatedEntities relatedEntities;

  @JsonIgnore public boolean isLocal;
  @JsonIgnore public boolean syncError;
  @JsonIgnoreProperties public int publishToken;

  public InventoryItem(Integer id) {
    this.id = id;
  }

  public InventoryItem(Parcel in) {
    id = in.readInt();
    title = in.readString();
    position = (Coordinates) in.readSerializable();
    inventory_category_id = (Integer) in.readValue(Integer.class.getClassLoader());
    inventory_status_id = (Integer) in.readValue(Integer.class.getClassLoader());
    data = (ArrayList<Data>) in.readSerializable();
    created_at = in.readString();
    updated_at = in.readString();
    address = in.readString();
    relatedEntities = in.readParcelable(RelatedEntities.class.getClassLoader());
    publishToken = in.readInt();
    isLocal = in.readByte() != 0;
    syncError = in.readByte() != 0;
  }

  public static final Creator<InventoryItem> CREATOR = new Creator<InventoryItem>() {
    @Override public InventoryItem createFromParcel(Parcel in) {
      return new InventoryItem(in);
    }

    @Override public InventoryItem[] newArray(int size) {
      return new InventoryItem[size];
    }
  };

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(title);
    dest.writeSerializable(position);
    dest.writeValue(inventory_category_id);
    dest.writeValue(inventory_status_id);
    dest.writeSerializable(data);
    dest.writeString(created_at);
    dest.writeString(updated_at);
    dest.writeString(address);
    dest.writeParcelable(relatedEntities, flags);
    dest.writeInt(publishToken);
    dest.writeByte((byte) (isLocal ? 1 : 0));
    dest.writeByte((byte) (syncError ? 1 : 0));
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Coordinates //TODO usar Position em vez de essa classe
      implements Serializable {
    public float latitude;
    public float longitude;
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Data implements Serializable {
    public int id;
    public InventoryCategory.Section.Field field;
    //public int inventory_field_id;
    public Object content;

    // ------------- //
    //@JsonIgnore
    public int inventory_field_id;

    public int getFieldId() {
      if (field != null) {
        return field.id;
      } else {
        return inventory_field_id;
      }
    }

    public void setFieldId(int id) {
      inventory_field_id = id;
    }
  }

  public void updateInfo(InventoryItem copyFrom) {
    this.id = copyFrom.id;
    if (copyFrom.title != null) this.title = copyFrom.title;
    if (copyFrom.position != null) this.position = copyFrom.position;
    if (copyFrom.inventory_category_id != null) {
      this.inventory_category_id = copyFrom.inventory_category_id;
    }
    if (copyFrom.data != null) this.data = copyFrom.data;
    if (copyFrom.created_at != null) this.created_at = copyFrom.created_at;
  }

  public Object getFieldValue(int id) {
    for (int i = 0; i < this.data.size(); i++) {
      if (this.data.get(i).getFieldId() == id) {
        return this.data.get(i).content;
      }
    }

    return null;
  }

  public void setFieldValue(int id, Object value) {
    Data found = null;
    for (int i = 0; i < this.data.size(); i++) {
      if (this.data.get(i).getFieldId() == id) {
        found = this.data.get(i);
      }
    }

    if (found == null) {
      found = new Data();

      //found.inventory_field_id = id;
      found.setFieldId(id);
      data.add(found);
    }

    found.content = value;
  }
}
