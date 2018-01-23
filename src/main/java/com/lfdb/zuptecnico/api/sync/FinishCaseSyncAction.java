package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.errors.SyncErrors;
import com.lfdb.zuptecnico.entities.collections.SingleCaseCollection;

import org.json.JSONObject;

import java.io.IOException;

import retrofit.RetrofitError;

/**
 * Created by Renan on 18/12/2015.
 */
public class FinishCaseSyncAction extends SyncAction implements CaseSyncAction {
    public static final String CASE_FINISHED = "case_finished";

    public static class Serializer {
        String error;

        public int caseId;
        public int resolutionStateId;
    }

    public int caseId;
    public int resolutionStateId;

    public static final Creator<FinishCaseSyncAction> CREATOR = new Creator<FinishCaseSyncAction>() {
        @Override
        public FinishCaseSyncAction createFromParcel(Parcel source) {
            return new FinishCaseSyncAction(source);
        }

        @Override
        public FinishCaseSyncAction[] newArray(int size) {
            return new FinishCaseSyncAction[size];
        }
    };

    public FinishCaseSyncAction() { super(); }

    public FinishCaseSyncAction(Parcel in) {
        super(in);
        caseId = in.readInt();
        resolutionStateId = in.readInt();
    }

    public FinishCaseSyncAction(int caseId, int resolutionStateId) {
        this.caseId = caseId;
        this.resolutionStateId = resolutionStateId;
    }


    public FinishCaseSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        Serializer serializer = mapper.readValue(object.toString(), Serializer.class);
        setError(serializer.error);
        this.caseId = serializer.caseId;
        this.resolutionStateId = serializer.resolutionStateId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(caseId);
        dest.writeInt(resolutionStateId);
    }

    @Override
    protected boolean onPerform() {
        try {
            SingleCaseCollection result = Zup.getInstance().getService().finishCase(caseId, resolutionStateId);
            Intent intent = new Intent();
            intent.putExtra("case", result.flowCase);
            intent.putExtra("caseId", caseId);
            broadcastAction(CASE_FINISHED, intent);
        } catch (RetrofitError error) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        serializer.error = getError();
        serializer.caseId = caseId;
        serializer.resolutionStateId = resolutionStateId;
        String res = Zup.getInstance().getObjectMapper().writeValueAsString(serializer);

        return new JSONObject(res);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}

