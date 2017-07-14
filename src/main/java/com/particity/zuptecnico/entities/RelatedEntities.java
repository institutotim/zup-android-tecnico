package com.particity.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Renan on 02/03/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelatedEntities implements Parcelable {
    public Case[] cases;

    public RelatedEntities(){}

    public RelatedEntities(Parcel in) {
        cases = Case.toMyObjects(in.readParcelableArray(Case.class.getClassLoader()));
    }


    public static final Creator<RelatedEntities> CREATOR = new Creator<RelatedEntities>() {
        @Override
        public RelatedEntities createFromParcel(Parcel in) {
            return new RelatedEntities(in);
        }

        @Override
        public RelatedEntities[] newArray(int size) {
            return new RelatedEntities[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(cases, flags);
    }
}
