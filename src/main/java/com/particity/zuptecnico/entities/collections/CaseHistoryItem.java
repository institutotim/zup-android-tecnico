package com.ntxdev.zuptecnico.entities.collections;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CaseHistoryItem implements Parcelable{
    //TODO adicionar os campos que vão ter. Ainda não existe na API, por isso não foi criado aqui
    public int id;
    @JsonProperty("created_at")
    public String createdAt;

    public CaseHistoryItem(){}

    public CaseHistoryItem(Parcel in) {
        id = in.readInt();
        createdAt = in.readString();
    }

    public static final Creator<CaseHistoryItem> CREATOR = new Creator<CaseHistoryItem>() {
        @Override
        public CaseHistoryItem createFromParcel(Parcel in) {
            return new CaseHistoryItem(in);
        }

        @Override
        public CaseHistoryItem[] newArray(int size) {
            return new CaseHistoryItem[size];
        }
    };

    public static CaseHistoryItem[] toMyObjects(Parcelable[] parcelables) {
        if(parcelables == null){
            return null;
        }
        CaseHistoryItem[] objects = new CaseHistoryItem[parcelables.length];
        System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
        return objects;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(createdAt);
    }

    public String getHtml(Context ctx, ObjectMapper mapper) {
        StringBuilder result = new StringBuilder();
        return result.toString();
    }
}
