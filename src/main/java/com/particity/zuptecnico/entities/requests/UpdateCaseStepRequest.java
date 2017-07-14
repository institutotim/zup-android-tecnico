package com.ntxdev.zuptecnico.entities.requests;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by igorlira on 8/8/14.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateCaseStepRequest {
    public static class FieldValue implements Serializable {
        public int id;
        public Object value;

        public FieldValue(int id, Object value) {
            this.id = id;
            this.value = value;
        }

        public FieldValue(){}
    }

    public UpdateCaseStepRequest(){}

    @JsonProperty("step_id")
    public int stepId;
    public List<FieldValue> fields;
    @JsonProperty("responsible_user_id")
    public Integer responsibleUserId;

    @JsonProperty("responsible_group_id")
    public Integer responsibleGroupId;
}
