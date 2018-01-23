package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;

/**
 * Created by Renan on 11/02/2016.
 */
public class DeleteCaseSyncAction extends SyncAction implements CaseSyncAction {
    public static final String CASE_DELETED = "case_deleted";

    public int itemId;

    public static final Creator<DeleteCaseSyncAction> CREATOR = new Creator<DeleteCaseSyncAction>() {
        @Override
        public DeleteCaseSyncAction createFromParcel(Parcel source) {
            return new DeleteCaseSyncAction(source);
        }

        @Override
        public DeleteCaseSyncAction[] newArray(int size) {
            return new DeleteCaseSyncAction[size];
        }
    };

    public DeleteCaseSyncAction() {
        super();
    }

    public DeleteCaseSyncAction(Parcel in) {
        super(in);
        itemId = in.readInt();
    }

    public DeleteCaseSyncAction(int id) {
        this.itemId = id;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(itemId);
    }

    public DeleteCaseSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException {
        this.itemId = object.getInt("item_id");

        if (object.has("error"))
            setError(object.getString("error"));
    }

    @Override
    protected boolean onPerform() {
        try {
            Zup.getInstance().getService().deleteCase(this.itemId);
            Zup.getInstance().getCaseItemService().deleteCaseItem(this.itemId);

            Intent intent = new Intent();
            intent.putExtra("caseId", itemId);
            broadcastAction(CASE_DELETED, intent);

            return true;
        } catch (RetrofitError error) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Crashlytics.logException(error);
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

    @Override
    public int describeContents() {
        return 0;
    }
}
