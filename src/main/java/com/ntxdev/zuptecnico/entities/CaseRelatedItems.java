package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by renan on 08/06/16.
 */
public class CaseRelatedItems implements Parcelable {
  @JsonProperty("report_items") public ReportItem[] reportItems;

  public static final Creator<CaseRelatedItems> CREATOR = new Creator<CaseRelatedItems>() {
    @Override public CaseRelatedItems createFromParcel(Parcel in) {
      return new CaseRelatedItems(in);
    }

    @Override public CaseRelatedItems[] newArray(int size) {
      return new CaseRelatedItems[size];
    }
  };

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelableArray(reportItems, flags);
  }

  public CaseRelatedItems() {
  }

  public CaseRelatedItems(Parcel in) {
    reportItems = ReportItem.toMyObjects(in.readParcelableArray(ReportItem.class.getClassLoader()));
  }
}
