package com.ntxdev.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.errors.SyncErrors;
import com.ntxdev.zuptecnico.entities.collections.SingleReportItemCollection;
import com.ntxdev.zuptecnico.entities.requests.AssignReportToGroupRequest;

import org.json.JSONObject;

import java.io.IOException;

import retrofit.RetrofitError;

public class ChangeReportResponsableGroupSyncAction extends SyncAction implements ReportSyncAction {
    public static final String REPORT_RESPONSABLE_GROUP_ASSIGNED = "report_responsable_group_assigned";

    public static class Serializer {
        public int reportId;
        public int categoryId;
        public int groupId;
        public String comment;
        public String error;
    }

    public String comment;

    public int reportId;
    public int categoryId;
    public int groupId;

    public static final Creator<ChangeReportResponsableGroupSyncAction> CREATOR = new Creator<ChangeReportResponsableGroupSyncAction>() {
        @Override
        public ChangeReportResponsableGroupSyncAction createFromParcel(Parcel source) {
            return new ChangeReportResponsableGroupSyncAction(source);
        }

        @Override
        public ChangeReportResponsableGroupSyncAction[] newArray(int size) {
            return new ChangeReportResponsableGroupSyncAction[size];
        }
    };

    public ChangeReportResponsableGroupSyncAction() {
        super();
    }

    public ChangeReportResponsableGroupSyncAction(Parcel in) {
        super(in);
        this.reportId = in.readInt();
        this.categoryId = in.readInt();
        this.groupId = in.readInt();
        comment = in.readString();
    }

    public ChangeReportResponsableGroupSyncAction(int reportId, int categoryId, int groupId) {
        this.reportId = reportId;
        this.categoryId = categoryId;
        this.groupId = groupId;
    }

    public ChangeReportResponsableGroupSyncAction(int reportId, int categoryId, int groupId, String comment) {
        this.reportId = reportId;
        this.categoryId = categoryId;
        this.groupId = groupId;
        this.comment = comment;
    }

    public ChangeReportResponsableGroupSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.categoryId = serializer.categoryId;
        this.reportId = serializer.reportId;
        this.groupId = serializer.groupId;
        setError(serializer.error);
        this.comment = serializer.comment;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(reportId);
        dest.writeInt(categoryId);
        dest.writeInt(groupId);
        dest.writeString(comment);
    }

    @Override
    protected boolean onPerform() {
        try {
            AssignReportToGroupRequest assignReportToGroupRequest = new AssignReportToGroupRequest();
            assignReportToGroupRequest.setGroupId(groupId);
            assignReportToGroupRequest.setComment(comment);
            SingleReportItemCollection result = Zup.getInstance().getService().assignReportToGroup(this.categoryId, reportId, assignReportToGroupRequest);
            Intent intent = new Intent();
            intent.putExtra("report", result.report);
            broadcastAction(REPORT_RESPONSABLE_GROUP_ASSIGNED, intent);
        } catch (RetrofitError error) {
            int errorType =  error.getResponse() != null ? error.getResponse().getStatus() : 0;
            Crashlytics.logException(SyncErrors.build(errorType, error));
            setError(error.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected JSONObject serialize() throws Exception {
        Serializer serializer = new Serializer();
        serializer.reportId = reportId;
        serializer.error = getError();
        serializer.categoryId = categoryId;
        serializer.groupId = groupId;
        serializer.comment = comment;
        String res = Zup.getInstance().getObjectMapper().writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
