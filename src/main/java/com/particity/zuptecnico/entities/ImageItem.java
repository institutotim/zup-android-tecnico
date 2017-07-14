package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageItem implements Parcelable {
    @JsonProperty("file_name")
    private String filename;
    private String title;
    protected String content;


    public static final Creator<ImageItem> CREATOR = new Creator<ImageItem>() {
        @Override
        public ImageItem createFromParcel(Parcel source) {
            return new ImageItem(source);
        }

        @Override
        public ImageItem[] newArray(int size) {
            return new ImageItem[size];
        }
    };

    public ImageItem() {
    }

    public ImageItem(String content, String filename, String title) {
        this.content = content;
        this.filename = filename;
        this.title = title;
    }

    @JsonGetter("file_name")
    public String getFilename() {
        return filename;
    }

    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    @JsonGetter("content")
    public String getContent() {
        return content;
    }

    @JsonSetter("file_name")
    public void setFilename(String filename) {
        this.filename = filename;
    }

    @JsonSetter("title")
    public void setTitle(String title) {
        this.title = title;
    }

    @JsonSetter("content")
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(filename);
        dest.writeString(content);
    }

    public ImageItem(Parcel in) {
        title = in.readString();
        filename = in.readString();
        content = in.readString();

    }

    public static ImageItem[] toMyObjects(Parcelable[] parcelables) {
        ImageItem[] objects = new ImageItem[parcelables.length];
        System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
        return objects;
    }
}
