package com.lfdb.zuptecnico.entities;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;

import java.util.Map;

/**
 * Created by igorlira on 7/30/15.
 */
public class ReportHistoryItem implements Parcelable {
    private static final String KIND_OVERDUE = "overdue";
    private static final String KIND_STATUS = "status";
    private static final String KIND_CATEGORY = "category";
    private static final String KIND_FORWARD = "forward";
    private static final String KIND_USER_ASSIGN = "user_assign";
    private static final String KIND_COMMENT = "comment";
    private static final String KIND_ADDRESS = "address";
    private static final String KIND_DESCRIPTION = "description";
    private static final String KIND_CREATION = "creation";
    private static final String KIND_NOTIFICATION = "notification";
    private static final String KIND_NOTIFICATION_RESTART = "notification_restart";

    public int id;
    public User user;
    public String kind;
    public String action;
    public Map changes; // We don't know what to expect
    public String created_at;


    public ReportHistoryItem() {

    }


    public static ReportHistoryItem[] toMyObjects(Parcelable[] parcelables) {
        if(parcelables == null){
            return null;
        }
        ReportHistoryItem[] objects = new ReportHistoryItem[parcelables.length];
        System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
        return objects;
    }

    protected ReportHistoryItem(Parcel in) {
        id = in.readInt();
        user = (User) in.readParcelable(User.class.getClassLoader());
        kind = in.readString();
        action = in.readString();
        changes = in.readHashMap(ClassLoader.getSystemClassLoader());
        created_at = in.readString();
    }

    public static final Creator<ReportHistoryItem> CREATOR = new Creator<ReportHistoryItem>() {
        @Override
        public ReportHistoryItem createFromParcel(Parcel in) {
            return new ReportHistoryItem(in);
        }

        @Override
        public ReportHistoryItem[] newArray(int size) {
            return new ReportHistoryItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeParcelable(user, i);
        parcel.writeString(kind);
        parcel.writeString(action);
        parcel.writeMap(changes);
        parcel.writeString(created_at);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getHtml(Context ctx, ObjectMapper mapper, int categoryId) {
        StringBuilder result = new StringBuilder();

        switch (this.kind) {
            case KIND_NOTIFICATION_RESTART:
                result.append("<b>");
                result.append(this.user.name);
                result.append("</b> ");
                result.append(" ");
                result.append(this.action);
                break;
            case KIND_NOTIFICATION:
                Map statusTitle = (Map) this.changes.get("new");
                String notificationTitle = mapper.convertValue(statusTitle.get("title"), String.class);
                result.append("<b>");
                result.append(this.user.name);
                result.append("</b> ");
                result.append(" ");
                result.append(ZupApplication.getContext().getString(R.string.sent_notification));
                result.append(" ");
                result.append("<b>");
                result.append(notificationTitle);
                result.append("</b> ");
                break;
            case KIND_OVERDUE:
                result.append(action);
                Map statusMap = (Map) this.changes.get("new");
                Integer statusId = mapper.convertValue(statusMap.get("id"), Integer.class);
                ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);
                if (category != null) {
                    ReportCategory.Status status = null;
                    if (statusId != null)
                        status = category.getStatus(statusId);
                    if (status != null) {
                        result.append(" ");
                        result.append("<b>");
                        result.append(status.getTitle());
                        result.append("</b>");
                    }
                }
                break;
            case KIND_STATUS:
                Map oldStatus = (Map) this.changes.get("old");
                Map newStatus = (Map) this.changes.get("new");

                String newStatusName = mapper.convertValue(newStatus.get("title"), String.class);

                if (this.user != null) {
                    result.append("<b>");
                    result.append(this.user.name);
                    result.append("</b> ");

                    result.append(ctx.getString(R.string.report_history_someone_changedstatus));
                    result.append(" ");
                } else {
                    result.append(ctx.getString(R.string.report_history_changedstatus));
                    result.append(" ");
                }

                if (oldStatus != null) {
                    String oldStatusName = mapper.convertValue(oldStatus.get("title"), String.class);

                    result.append(ctx.getString(R.string.report_history_from));
                    result.append(" ");
                    result.append("<b>");
                    result.append(oldStatusName);
                    result.append("</b> ");
                }

                result.append(ctx.getString(R.string.report_history_to));
                result.append(" ");

                result.append("<b>");
                result.append(newStatusName);
                result.append("</b>");
                break;

            case KIND_CATEGORY:
                Map oldCategory = (Map) this.changes.get("old");
                Map newCategory = (Map) this.changes.get("new");

                String newCategoryName = mapper.convertValue(newCategory.get("title"), String.class);

                if (this.user != null) {
                    result.append("<b>");
                    result.append(this.user.name);
                    result.append("</b> ");

                    result.append(ctx.getString(R.string.report_history_someone_changedcategory));
                    result.append(" ");
                } else {
                    result.append(ctx.getString(R.string.report_history_changedcategory));
                    result.append(" ");
                }

                if (oldCategory != null) {
                    String oldCategoryName = mapper.convertValue(oldCategory.get("title"), String.class);

                    result.append(ctx.getString(R.string.report_history_from));
                    result.append(" ");
                    result.append("<b>");
                    result.append(oldCategoryName);
                    result.append("</b> ");
                }

                result.append(ctx.getString(R.string.report_history_to));
                result.append(" ");
                result.append("<b>");
                result.append(newCategoryName);
                result.append("</b>");
                break;

            case KIND_FORWARD:
                Map oldGroup = (Map) this.changes.get("old");
                Map newGroup = (Map) this.changes.get("new");

                String newGroupName = mapper.convertValue(newGroup.get("name"), String.class);

                if (this.user != null) {
                    result.append("<b>");
                    result.append(this.user.name);
                    result.append("</b> ");

                    result.append(ctx.getString(R.string.report_history_someone_forwarded));
                    result.append(" ");
                } else {
                    result.append(ctx.getString(R.string.report_history_forwarded));
                    result.append(" ");
                }

                if (oldGroup != null) {
                    String oldGroupName = mapper.convertValue(oldGroup.get("name"), String.class);

                    result.append(ctx.getString(R.string.report_history_fromgroup));
                    result.append(" ");
                    result.append("<b>");
                    result.append(oldGroupName);
                    result.append("</b> ");
                }

                result.append(ctx.getString(R.string.report_history_togroup));
                result.append(" ");
                result.append("<b>");
                result.append(newGroupName);
                result.append("</b>");
                break;

            case KIND_USER_ASSIGN:
                Map oldUser = (Map) this.changes.get("old");
                Map newUser = (Map) this.changes.get("new");

                String newUserName = mapper.convertValue(newUser.get("name"), String.class);

                if (this.user != null) {
                    result.append("<b>");
                    result.append(this.user.name);
                    result.append("</b> ");

                    result.append(ctx.getString(R.string.report_history_someone_assigneduser));
                    result.append(" ");
                } else {
                    result.append(ctx.getString(R.string.report_history_assigneduser));
                    result.append(" ");
                }

                if (oldUser != null) {
                    String oldUserName = mapper.convertValue(oldUser.get("name"), String.class);

                    result.append(ctx.getString(R.string.report_history_fromuser));
                    result.append(" ");
                    result.append("<b>");
                    result.append(oldUserName);
                    result.append("</b> ");
                }

                result.append(ctx.getString(R.string.report_history_touser));
                result.append(" ");
                result.append("<b>");
                result.append(newUserName);
                result.append("</b>");
                break;

            case KIND_COMMENT:
                Map newComment = (Map) this.changes.get("new");
                String newMessage = mapper.convertValue(newComment.get("message"), String.class);
                int newVisibility = mapper.convertValue(newComment.get("visibility"), Integer.class);

                result.append("<b>");
                result.append(this.user.name);
                result.append("</b> ");

                result.append(ctx.getString(R.string.report_history_inserted));
                result.append(" ");

                if (newVisibility == ReportItem.Comment.TYPE_INTERNAL) {
                    result.append(ctx.getString(R.string.report_history_internalcommend));
                } else if (newVisibility == ReportItem.Comment.TYPE_PRIVATE) {
                    result.append(ctx.getString(R.string.report_history_privatecomment));
                } else if (newVisibility == ReportItem.Comment.TYPE_PUBLIC) {
                    result.append(ctx.getString(R.string.report_history_publiccoment));
                }

                result.append(" ");
                result.append("<b>");
                result.append(newMessage);
                result.append("</b>");

                break;

            case KIND_CREATION:
                Map newState = (Map) this.changes.get("new");

                result.append("<b>");
                result.append(this.user.name);
                result.append("</b> ");

                result.append(ctx.getString(R.string.report_history_created));

                if (newState != null) {
                    String newStateName = mapper.convertValue(newState.get("title"), String.class);
                    result.append(" ");
                    result.append(ctx.getString(R.string.report_history_withstatus));
                    result.append(" ");
                    result.append("<b>");
                    result.append(newStateName);
                    result.append("</b>");
                }
                break;

            case KIND_ADDRESS:
            case KIND_DESCRIPTION:
                String oldValue = (String) this.changes.get("old");
                String newValue = (String) this.changes.get("new");

                if (this.user != null) {
                    result.append("<b>");
                    result.append(this.user.name);
                    result.append("</b> ");

                    result.append(ctx.getString(R.string.report_history_someone_changedproperty));
                    result.append(" ");
                } else {
                    result.append(ctx.getString(R.string.report_history_changedproperty));
                    result.append(" ");
                }

                int propNameId = ctx.getResources()
                        .getIdentifier("report_property_name_" + this.kind, "string",
                                ctx.getPackageName());
                String propName = this.kind;
                if (propNameId > 0) {
                    propName = ctx.getString(propNameId);
                }

                result.append("<b>");
                result.append(propName);
                result.append("</b> ");

                result.append(ctx.getString(R.string.report_history_from));
                result.append(" ");
                result.append("<b>");
                result.append(oldValue);
                result.append("</b> ");

                result.append(ctx.getString(R.string.report_history_to));
                result.append(" ");
                result.append("<b>");
                result.append(newValue);
                result.append("</b> ");
                break;
        }

        return result.toString();
    }
}
