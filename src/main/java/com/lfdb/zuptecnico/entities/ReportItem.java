package com.lfdb.zuptecnico.entities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.ImageView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lfdb.zuptecnico.R;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true) public class ReportItem implements Parcelable {

  public static ReportItem[] toMyObjects(Parcelable[] parcelables) {
    if (parcelables == null) {
      return null;
    }
    ReportItem[] objects = new ReportItem[parcelables.length];
    System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
    return objects;
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Image extends ImageItem
      implements Parcelable {
    public String high;
    public String low;
    public String thumb;
    public String original;
    public String date;

    public Image(String bitmap) {
      setContent(bitmap);
    }

    @JsonIgnore public Bitmap getBitmap() {
      byte[] data = Base64.decode(getContent(), Base64.DEFAULT);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      return BitmapFactory.decodeByteArray(data, 0, data.length, options);
    }

    public Image() {

    }

    @JsonIgnore public ImageItem getImageItem() {
      return new ImageItem(getContent(), getFilename(), getTitle());
    }

    public void saveImageIntoCache(Context context) {
      if (!TextUtils.isEmpty(thumb)) {
        Picasso.with(context).load(thumb).fetch();
      }

      if (!TextUtils.isEmpty(original)) {
        Picasso.with(context).load(original).fetch();
      }

      if (!TextUtils.isEmpty(low)) {
        Picasso.with(context).load(low).fetch();
      }

      if (!TextUtils.isEmpty(high)) {
        Picasso.with(context).load(thumb).fetch();
      }
    }

    public void loadImageInto(ImageView imageView) {
      Picasso.with(imageView.getContext())
          .load(original)
          .placeholder(R.color.tab_pressed)
          .into(imageView);
    }

    public Image(ImageItem item) {
      setContent(item.getContent());
      setFilename(item.getFilename());
      setTitle(item.getTitle());
    }

    public static Image[] toMyObjects(Parcelable[] parcelables) {
      if (parcelables == null) {
        return null;
      }
      Image[] objects = new Image[parcelables.length];
      System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
      return objects;
    }

    public Image(Parcel in) {
      super(in);
      high = in.readString();
      low = in.readString();
      thumb = in.readString();
      original = in.readString();
      date = in.readString();
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeString(high);
      dest.writeString(low);
      dest.writeString(thumb);
      dest.writeString(original);
      dest.writeString(date);
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
      @Override public Image createFromParcel(Parcel source) {
        return new Image(source);
      }

      @Override public Image[] newArray(int size) {
        return new Image[size];
      }
    };

    @JsonIgnore public void setData(ImageItem base64) {
      setTitle(base64.getTitle());
      setContent(base64.getContent());
      setFilename(base64.getFilename());
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Comment implements Parcelable {
    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_INTERNAL = 2;
    public int id;
    public int visibility;
    public User author;
    public String message;
    public String created_at;

    public boolean isFake;

    public Comment() {
    }

    @Override public boolean equals(Object o) {
      if (o == null || !(o instanceof Comment)) {
        return false;
      }
      return id == ((Comment) o).id;
    }

    public Comment(Parcel in) {
      id = in.readInt();
      visibility = in.readInt();
      author = in.readParcelable(User.class.getClassLoader());
      message = in.readString();
      created_at = in.readString();
      isFake = in.readByte() != 0;
    }

    public static Comment[] toMyObjects(Parcelable[] parcelables) {
      if (parcelables == null) {
        return null;
      }
      Comment[] objects = new Comment[parcelables.length];
      System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
      return objects;
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(id);
      dest.writeInt(visibility);
      dest.writeParcelable(author, flags);
      dest.writeString(message);
      dest.writeString(created_at);
      dest.writeByte((byte) (isFake ? 1 : 0));
    }

    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
      @Override public Comment createFromParcel(Parcel source) {
        return new Comment(source);
      }

      @Override public Comment[] newArray(int size) {
        return new Comment[size];
      }
    };
  }

  public int id = -1;
  public String protocol;
  public boolean overdue;

  @JsonProperty("assigned_user") public User assignedUser;
  public int assignedUserId = -1;

  @JsonProperty("assigned_group") public Group assignedGroup;
  public int assignedGroupId = -1;

  public String address;
  public String number;
  public String reference;
  public String district;

  @JsonProperty("postal_code") public String postalCode;

  public String city;
  public String state;
  public String country;
  public boolean confidential;
  public Position position;
  public String description;
  // category_icon

  public User user;
  public int userId = -1;

  public User reporter;
  public int reporterId = -1;

  public int inventory_item_id = -1;
  public int status_id = -1;
  public int category_id = -1;
  public Image[] images;
  public int inventory_item_category_id = -1;
  public String created_at;
  public String updated_at;
  public Comment[] comments;
  public ReportNotificationCollection notification;
  public HashMap<Integer, String> custom_fields;
  @JsonProperty("related_entities") public RelatedEntities relatedEntities;

  public static final Parcelable.Creator<ReportItem> CREATOR =
      new Parcelable.Creator<ReportItem>() {
        @Override public ReportItem createFromParcel(Parcel source) {
          return new ReportItem(source);
        }

        @Override public ReportItem[] newArray(int size) {
          return new ReportItem[size];
        }
      };

  public ReportItem() {
  }

  public ReportItem(ReportItem item) {
    if (item != null) {
      id = item.id;
      protocol = item.protocol;
      overdue = item.overdue;
      assignedUser = item.assignedUser;
      assignedUserId = item.assignedUserId;
      assignedGroup = item.assignedGroup;

      assignedGroupId = item.assignedGroupId;
      address = item.address;
      number = item.number;
      reference = item.reference;
      district = item.district;
      postalCode = item.postalCode;

      city = item.city;
      state = item.state;
      country = item.country;
      confidential = item.confidential;
      position = new Position();
      if (item.position != null) {
        position.latitude = item.position.latitude;
        position.longitude = item.position.longitude;
      }
      description = item.description;
      user = item.user;
      userId = item.userId;
      reporter = item.reporter;
      reporterId = item.reporterId;

      status_id = item.status_id;
      category_id = item.category_id;
    }
  }

  public ReportItem(Parcel in) {
    id = in.readInt();
    protocol = in.readString();
    overdue = in.readByte() != 0;
    assignedUser = in.readParcelable(User.class.getClassLoader());
    assignedUserId = in.readInt();
    assignedGroup = (Group) in.readSerializable();

    assignedGroupId = in.readInt();
    address = in.readString();
    number = in.readString();
    reference = in.readString();
    district = in.readString();
    postalCode = in.readString();

    city = in.readString();
    state = in.readString();
    country = in.readString();
    confidential = in.readByte() != 0;
    position = in.readParcelable(Position.class.getClassLoader());
    description = in.readString();
    user = in.readParcelable(User.class.getClassLoader());
    userId = in.readInt();
    reporter = in.readParcelable(User.class.getClassLoader());
    reporterId = in.readInt();
    relatedEntities = in.readParcelable(RelatedEntities.class.getClassLoader());

    inventory_item_id = in.readInt();
    status_id = in.readInt();
    category_id = in.readInt();
    images = Image.toMyObjects(in.readParcelableArray(Image.class.getClassLoader()));
    inventory_item_category_id = in.readInt();
    created_at = in.readString();
    updated_at = in.readString();
    comments = Comment.toMyObjects(in.readParcelableArray(Comment.class.getClassLoader()));
    notification = in.readParcelable(ReportNotificationCollection.class.getClassLoader());
    custom_fields = (HashMap<Integer, String>) in.readSerializable();
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(protocol);
    dest.writeByte((byte) (overdue ? 1 : 0));
    dest.writeParcelable(assignedUser, flags);
    dest.writeInt(assignedUserId);
    dest.writeSerializable(assignedGroup);
    dest.writeInt(assignedGroupId);
    dest.writeString(address);
    dest.writeString(number);
    dest.writeString(reference);
    dest.writeString(district);
    dest.writeString(postalCode);
    dest.writeString(city);
    dest.writeString(state);
    dest.writeString(country);
    dest.writeByte((byte) (confidential ? 1 : 0));
    dest.writeParcelable(position, flags);
    dest.writeString(description);
    dest.writeParcelable(user, flags);
    dest.writeInt(userId);
    dest.writeParcelable(reporter, flags);
    dest.writeInt(reporterId);
    dest.writeParcelable(relatedEntities, flags);
    dest.writeInt(inventory_item_id);
    dest.writeInt(status_id);
    dest.writeInt(category_id);
    dest.writeParcelableArray(images, flags);
    dest.writeInt(inventory_item_category_id);
    dest.writeString(created_at);
    dest.writeString(updated_at);
    dest.writeParcelableArray(comments, flags);
    dest.writeParcelable(notification, flags);
    dest.writeSerializable(custom_fields);
  }

  @JsonIgnore public String getFullAddress() {
    StringBuilder result = new StringBuilder();
    ArrayList<String> components = new ArrayList<>();

    if (address != null) components.add(address);

    if (number != null) components.add(number);

    if (district != null) components.add(district);

    if (city != null) components.add(city);

    if (state != null) components.add(state);

    if (postalCode != null) components.add(postalCode);

    if (country != null) components.add(country);

    for (int i = 0; i < components.size(); i++) {
      if (i > 0) result.append(", ");

      result.append(components.get(i));
    }

    return result.toString();
  }

  private boolean containsComment(int id) {
    if (this.comments == null) {
      return false;
    } else {
      for (Comment comment : this.comments) {
        if (comment.id == id) {
          return true;
        }
      }

      return false;
    }
  }

  public void addComment(Comment comment) {
    if (containsComment(comment.id)) {
      return;
    }

    if (this.comments == null) {
      this.comments = new Comment[] { comment };
    } else {
      this.comments = Arrays.copyOf(this.comments, this.comments.length + 1);
      this.comments[this.comments.length - 1] = comment;
    }
  }

  public void removeComment(int id) {
    if (containsComment(id)) {
      Comment[] newArray = new Comment[this.comments.length - 1];
      int j = 0;
      for (Comment comment : this.comments) {
        if (comment.id != id) {
          newArray[j++] = comment;
        }
      }

      this.comments = newArray;
    }
  }

  @Override public boolean equals(Object o) {
    return (o instanceof ReportItem && ((ReportItem) o).id == id);
  }
}
