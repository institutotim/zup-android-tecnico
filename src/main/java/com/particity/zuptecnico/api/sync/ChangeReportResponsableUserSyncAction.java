package com.particity.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.collections.SingleReportItemCollection;

import org.json.JSONObject;

import java.io.IOException;

import retrofit.RetrofitError;

public class ChangeReportResponsableUserSyncAction extends SyncAction implements ReportSyncAction {
    public static final String REPORT_RESPONSABLE_USER_ASSIGNED = "report_responsable_user_assigned";

    public static class Serializer {
        public int reportId;
        public int categoryId;
        public int userId;

        public String error;
    }

    public int reportId;
    public int categoryId;
    public int userId;

    public static final Creator<ChangeReportResponsableUserSyncAction> CREATOR = new Creator<ChangeReportResponsableUserSyncAction>() {
        @Override
        public ChangeReportResponsableUserSyncAction createFromParcel(Parcel source) {
            return new ChangeReportResponsableUserSyncAction(source);
        }

        @Override
        public ChangeReportResponsableUserSyncAction[] newArray(int size) {
            return new ChangeReportResponsableUserSyncAction[size];
        }
    };

    public ChangeReportResponsableUserSyncAction() {
        super();
    }

    public ChangeReportResponsableUserSyncAction(Parcel in) {
        super(in);
        this.reportId = in.readInt();
        this.categoryId = in.readInt();
        this.userId = in.readInt();
    }

    public ChangeReportResponsableUserSyncAction(int reportId, int categoryId, int userId) {
        this.reportId = reportId;
        this.categoryId = categoryId;
        this.userId = userId;
    }

    public ChangeReportResponsableUserSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

        this.categoryId = serializer.categoryId;
        this.reportId = serializer.reportId;
        this.userId = serializer.userId;
        setError(serializer.error);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(reportId);
        dest.writeInt(categoryId);
        dest.writeInt(userId);
    }

    @Override
    protected boolean onPerform() {
        try {
            SingleReportItemCollection result = Zup.getInstance().getService().assignReportToUser(categoryId, reportId, userId);
            Intent intent = new Intent();
            intent.putExtra("report", result.report);
            broadcastAction(REPORT_RESPONSABLE_USER_ASSIGNED, intent);
        } catch (RetrofitError error) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Crashlytics.logException(error);
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
        serializer.userId = userId;
        String res = Zup.getInstance().getObjectMapper().writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
