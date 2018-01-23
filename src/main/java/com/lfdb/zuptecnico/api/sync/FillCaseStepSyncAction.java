package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.errors.SyncErrors;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.entities.collections.SingleCaseCollection;
import com.lfdb.zuptecnico.entities.requests.UpdateCaseStepRequest;
import com.lfdb.zuptecnico.entities.responses.FillCaseStepErrorResponse;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

public class FillCaseStepSyncAction extends SyncAction implements CaseSyncAction {
    public static final String FILL_STEP = "fill_step";

    public int caseId;
    public int stepId;
    public int responsibleUserId;
    public int nextResponsibleUserId;
    public boolean isGroupId;
    public boolean isNextGroupId;
    public ArrayList<UpdateCaseStepRequest.FieldValue> fields;

    public static final Creator<FillCaseStepSyncAction> CREATOR = new Creator<FillCaseStepSyncAction>() {
        @Override
        public FillCaseStepSyncAction createFromParcel(Parcel source) {
            return new FillCaseStepSyncAction(source);
        }

        @Override
        public FillCaseStepSyncAction[] newArray(int size) {
            return new FillCaseStepSyncAction[size];
        }
    };

    public FillCaseStepSyncAction() {
        super();
    }

    public FillCaseStepSyncAction(Parcel in) {
        super(in);
        caseId = in.readInt();
        stepId = in.readInt();
        responsibleUserId = in.readInt();
        nextResponsibleUserId = in.readInt();
        fields = (ArrayList<UpdateCaseStepRequest.FieldValue>) in.readSerializable();
        isGroupId = in.readByte() != 0;
        isNextGroupId = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(caseId);
        dest.writeInt(stepId);
        dest.writeInt(responsibleUserId);
        dest.writeSerializable(fields);
        dest.writeByte((byte) (isGroupId ? 1 : 0));
        dest.writeByte((byte) (isNextGroupId ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public FillCaseStepSyncAction(int caseId, int stepId, int responsibleUserId, int nextResponsibleUserId, boolean isGroupId, boolean isNextGroupId, ArrayList<UpdateCaseStepRequest.FieldValue> fields) {
        this.caseId = caseId;
        this.stepId = stepId;
        this.isNextGroupId = isNextGroupId;
        this.isGroupId = isGroupId;
        this.responsibleUserId = responsibleUserId;
        this.nextResponsibleUserId = nextResponsibleUserId;
        this.fields = fields;
        try {
            Log.d("Serializing case steps", serialize().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FillCaseStepSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser(object.toString());

        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = jParser.getCurrentName();
            if (fieldname == null) {
                continue;
            }
            jParser.nextToken();
            switch (fieldname) {
                case "case_id":
                    caseId = jParser.getIntValue();
                    break;
                case "responsible_user_id":
                    responsibleUserId = jParser.getIntValue();
                    break;
                case "next_responsible_user_id":
                    nextResponsibleUserId = jParser.getIntValue();
                    break;
                case "step_id":
                    stepId = jParser.getIntValue();
                    break;
                case "error":
                    setError(jParser.getText());
                    break;
                case "isGroupId":
                    isGroupId = jParser.getBooleanValue();
                    break;
                case "isNextGroupId":
                    isNextGroupId = jParser.getBooleanValue();
                    break;
                case "fields":
                    fields = new ArrayList<>();
                    TypeReference<List<UpdateCaseStepRequest.FieldValue>> tRef = new TypeReference<List<UpdateCaseStepRequest.FieldValue>>() {
                    };
                    fields = mapper.readValue(jParser, tRef);
                    break;
            }

        }
        jParser.close();
    }

    public boolean onPerform() {
        UpdateCaseStepRequest request = new UpdateCaseStepRequest();
        request.fields = fields;
        if (isGroupId) {
            request.responsibleGroupId = responsibleUserId;
        } else {
            request.responsibleUserId = responsibleUserId;
        }
        request.stepId = stepId;
        try {
            SingleCaseCollection result = Zup.getInstance().getService().updateCaseStep(this.caseId, request);
            Case flowCase = result.flowCase;
            if (flowCase != null && flowCase.nextSteps != null && flowCase.nextSteps.length > 0) {
                UpdateCaseStepRequest secondRequest = new UpdateCaseStepRequest();
                if (isNextGroupId) {
                    secondRequest.responsibleGroupId = nextResponsibleUserId;
                } else {
                    secondRequest.responsibleUserId = nextResponsibleUserId;
                }
                secondRequest.stepId = flowCase.nextSteps[0].id;
                result = Zup.getInstance().getService().updateCaseStep(this.caseId, secondRequest);
            }
            Intent intent = new Intent();
            intent.putExtra("case", result.flowCase);
            broadcastAction(FILL_STEP, intent);
        } catch (RetrofitError error) {
            try {
                Crashlytics.setString("request", new Gson().toJson(request));
                int errorType = error.getResponse() != null ? error.getResponse().getStatus() : 0;
                Crashlytics.logException(SyncErrors.build(errorType, error));
                setError(error.getMessage());
                if (errorType == 404 || errorType == 504) {
                    setError(error.getMessage());
                    return false;
                }
                FillCaseStepErrorResponse response = (FillCaseStepErrorResponse) error.getBodyAs(FillCaseStepErrorResponse.class);
                if (response != null && response.error != null) {
                    setError(getError(response));
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (getError() == null) {
                setError(error.getMessage());
            }
            return false;
        }
        return true;
    }

    private String getError(FillCaseStepErrorResponse response) {
        String error = null;
        if (response.getFields() != null) {
            error = "Tipo: " + response.type + "\n";
            Object fieldsObj = response.getFields();
            if (fieldsObj instanceof String[]) {
                String[] fields = (String[]) fieldsObj;
                for (int index = 0; index < fields.length; index++) {
                    error = error.concat(fields[index]);
                    error = error.concat("\n");
                }
            } else if (fieldsObj instanceof String) {
                error = error.concat(fieldsObj.toString());
            }
        }
        return error;
    }

    @Override
    protected JSONObject serialize() throws Exception {
        JsonFactory jfactory = new JsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator jGenerator = jfactory.createGenerator(baos, JsonEncoding.UTF8);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        jGenerator.writeStartObject();
        jGenerator.writeNumberField("case_id", caseId);
        jGenerator.writeNumberField("step_id", stepId);
        jGenerator.writeNumberField("responsible_user_id", responsibleUserId);
        jGenerator.writeNumberField("next_responsible_user_id", nextResponsibleUserId);
        jGenerator.writeBooleanField("isGroupId", isGroupId);
        jGenerator.writeBooleanField("isNextGroupId", isNextGroupId);

        jGenerator.writeArrayFieldStart("fields");
        for (UpdateCaseStepRequest.FieldValue field : fields) {
            jGenerator.writeStartObject();

            jGenerator.writeNumberField("id", field.id);
            jGenerator.writeRaw(",\"value\":");
            jGenerator.writeRaw(mapper.writeValueAsString(field.value));

            jGenerator.writeEndObject();
        }

        jGenerator.writeEndArray();

        jGenerator.writeStringField("error", getError());

        jGenerator.writeEndObject();
        jGenerator.close();

        return new JSONObject(baos.toString());
    }
}
