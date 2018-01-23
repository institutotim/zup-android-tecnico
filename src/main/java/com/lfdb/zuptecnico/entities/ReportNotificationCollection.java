package com.particity.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportNotificationCollection implements Parcelable {
    public ReportNotificationItem[] notifications;

    public ReportNotificationCollection() {
    }

    public ReportNotificationCollection(Parcel in) {
        notifications = ReportNotificationItem.toMyObjects(in.readParcelableArray(ReportNotificationItem.class.getClassLoader()));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(notifications, flags);
    }

    public static final Creator<ReportNotificationCollection> CREATOR = new Creator<ReportNotificationCollection>() {
        @Override
        public ReportNotificationCollection createFromParcel(Parcel in) {
            return new ReportNotificationCollection(in);
        }

        @Override
        public ReportNotificationCollection[] newArray(int size) {
            return new ReportNotificationCollection[size];
        }
    };

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ReportNotificationItem implements Parcelable {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class NotificationType implements Parcelable {
            public String title;

            public NotificationType() {
            }

            public NotificationType(Parcel in) {
                title = in.readString();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(title);
            }

            public static final Creator<NotificationType> CREATOR = new Creator<NotificationType>() {
                @Override
                public NotificationType createFromParcel(Parcel source) {
                    return new NotificationType(source);
                }

                @Override
                public NotificationType[] newArray(int size) {
                    return new NotificationType[size];
                }
            };
        }

        public int id;
        @JsonProperty("notification_type")
        public NotificationType notificationType;
        @JsonProperty("deadline_in_days")
        public int deadlineInDays;
		public boolean current;
        public String content;
        @JsonProperty("days_to_deadline")
        public int daysToDeadline;
        @JsonProperty("created_at")
        public String createdAt;
        @JsonProperty("updated_at")
        public String updatedAt;
        @JsonProperty("overdue_at")
        public String overdueAt;

        public ReportNotificationItem() {
        }

        public static ReportNotificationItem[] toMyObjects(Parcelable[] parcelables) {
            ReportNotificationItem[] objects = new ReportNotificationItem[parcelables.length];
            System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
            return objects;
        }

        protected ReportNotificationItem(Parcel in) {
            id = in.readInt();
            notificationType = in.readParcelable(NotificationType.class.getClassLoader());
            deadlineInDays = in.readInt();
            content = in.readString();
            daysToDeadline = in.readInt();
			current = in.readByte() != 0;
            createdAt = in.readString();
            updatedAt = in.readString();
            overdueAt = in.readString();
        }

        public static final Creator<ReportNotificationItem> CREATOR = new Creator<ReportNotificationItem>() {
            @Override
            public ReportNotificationItem createFromParcel(Parcel in) {
                return new ReportNotificationItem(in);
            }

            @Override
            public ReportNotificationItem[] newArray(int size) {
                return new ReportNotificationItem[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeInt(id);
            parcel.writeParcelable(notificationType, i);
            parcel.writeInt(deadlineInDays);
            parcel.writeString(content);
            parcel.writeInt(daysToDeadline);
			parcel.writeByte((byte) (current ? 1 : 0));
            parcel.writeString(createdAt);
            parcel.writeString(updatedAt);
            parcel.writeString(overdueAt);
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
