package com.lfdb.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * Created by WINDOWS on 31/08/2016.
 */
public class Namespace implements Parcelable {
  private int id;
  private String name;

  public Namespace() {
  }

  public Namespace(Parcel in) {
    id = in.readInt();
    name = in.readString();
  }

  public static final Creator<Namespace> CREATOR = new Creator<Namespace>() {
    @Override public Namespace createFromParcel(Parcel in) {
      return new Namespace(in);
    }

    @Override public Namespace[] newArray(int size) {
      return new Namespace[size];
    }
  };

  @JsonGetter("id")
  public int getId() {
    return id;
  }

  @JsonSetter("id")
  public void setId(int id) {
    this.id = id;
  }

  @JsonGetter("name")
  public String getName() {
    return name;
  }

  @JsonSetter("name")
  public void setName(String name) {
    this.name = name;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(name);
  }
}
