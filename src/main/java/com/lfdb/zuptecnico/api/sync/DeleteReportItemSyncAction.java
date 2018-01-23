package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.errors.SyncErrors;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;

public class DeleteReportItemSyncAction extends SyncAction implements ReportSyncAction {
    public static final String REPORT_DELETED = "report_deleted";

    public int itemId;

    public static final Creator<DeleteReportItemSyncAction> CREATOR = new Creator<DeleteReportItemSyncAction>() {
        @Override
        public DeleteReportItemSyncAction createFromParcel(Parcel source) {
            return new DeleteReportItemSyncAction(source);
        }

        @Override
        public DeleteReportItemSyncAction[] newArray(int size) {
            return new DeleteReportItemSyncAction[size];
        }
    };

    public DeleteReportItemSyncAction() {
        super();
    }

    public DeleteReportItemSyncAction(Parcel in) {
        super(in);
        itemId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(itemId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public DeleteReportItemSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException {
        this.itemId = object.getInt("item_id");

        if (object.has("error"))
            setError(object.getString("error"));
    }

    public DeleteReportItemSyncAction(int id) {
        this.itemId = id;
    }

    public boolean onPerform() {
        try {
            Zup.getInstance().getService().deleteReportItem(this.itemId);
            Zup.getInstance().getReportItemService().deleteReportItem(this.itemId);

            Intent intent = new Intent();
            intent.putExtra("report_id", itemId);
            broadcastAction(REPORT_DELETED, intent);

            return true;
        } catch (RetrofitError error) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            int errorType =  error.getResponse() != null ? error.getResponse().getStatus() : 0;
            Crashlytics.logException(SyncErrors.build(errorType, error));
            if (error.getKind() == RetrofitError.Kind.NETWORK) {
                setError(ZupApplication.getContext().getString(R.string.error_network));
            } else {
                setError(error.getLocalizedMessage());
            }
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("item_id", itemId);
        result.put("error", getError());

        return result;
    }
}
