package com.particity.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by igorlira on 3/16/14.
 */
@JsonIgnoreProperties(ignoreUnknown = true) public class User implements Parcelable {
  public static final int NEEDS_TO_BE_CREATED = -1;

  public int id;
  public String name;
  public String email;
  public String phone;
  public String document;
  public String address;
  public String address_additional;
  public String postal_code;
  public String city;
  public String district;
  public String created_at;
  public String updated_at;
  public Namespace namespace;
  public Group[] groups;

  @Override public boolean equals(Object o) {
    if (o instanceof User) {
      return ((User) o).id == this.id;
    }
    return false;
  }

  @Override public int describeContents() {
    return 0;
  }

  public User() {
  }

  protected User(Parcel in) {
    id = in.readInt();
    name = in.readString();
    email = in.readString();
    phone = in.readString();
    document = in.readString();
    address = in.readString();
    address_additional = in.readString();
    postal_code = in.readString();
    city = in.readString();
    district = in.readString();
    created_at = in.readString();
    updated_at = in.readString();
    groups = (Group[]) in.readSerializable();
    namespace = in.readParcelable(Namespace.class.getClassLoader());
  }

  public static final Creator<User> CREATOR = new Creator<User>() {
    @Override public User createFromParcel(Parcel in) {
      return new User(in);
    }

    @Override public User[] newArray(int size) {
      return new User[size];
    }
  };

  @Override public void writeToParcel(Parcel parcel, int i) {
    parcel.writeInt(id);
    parcel.writeString(name);
    parcel.writeString(email);
    parcel.writeString(phone);
    parcel.writeString(document);
    parcel.writeString(address);
    parcel.writeString(address_additional);
    parcel.writeString(postal_code);
    parcel.writeString(city);
    parcel.writeString(district);
    parcel.writeString(created_at);
    parcel.writeString(updated_at);
    parcel.writeSerializable(groups); //TODO make groups parcelable
    parcel.writeParcelable(namespace, i);
  }
}
