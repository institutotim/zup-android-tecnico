package com.particity.zuptecnico.entities.originalcase;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.User;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CaseOriginal implements Parcelable {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step implements Parcelable {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class DataField implements Parcelable {
            public int id;
            public String value;
            public Flow.Step.Field field;
            @JsonIgnore
            public int fieldId;

            public static DataField[] toMyObjects(Parcelable[] parcelables) {
                if(parcelables == null) {
                    return null;
                }
                DataField[] objects = new DataField[parcelables.length];
                System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
                return objects;
            }

            public static final Creator<DataField> CREATOR = new Creator<DataField>() {
                @Override
                public DataField createFromParcel(Parcel source) {
                    return new DataField(source);
                }

                @Override
                public DataField[] newArray(int size) {
                    return new DataField[size];
                }
            };

            public DataField(){}

            public DataField(Parcel source) {
                id = source.readInt();
                value = source.readString();
                field = (Flow.Step.Field) source.readSerializable();
                fieldId = source.readInt();
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeInt(id);
                dest.writeString(value);
                dest.writeSerializable(field);
                dest.writeInt(fieldId);
            }

            public int getFieldId() {
                if (field != null) {
                    return field.id;
                }else {
                    return fieldId;
                }
            }
        }

        public int id;
        @JsonProperty("step_id")
        public int stepId;
        @JsonProperty("step_version")
        public int stepVersion;
        @JsonProperty("case_step_data_fields")
        public DataField[] caseStepDataFields;
        @JsonProperty("responsible_user_id")
        public Integer responsableUserId;
        public boolean executed;

        public static final Creator<Step> CREATOR = new Creator<Step>() {
            @Override
            public Step createFromParcel(Parcel source) {
                return new Step(source);
            }

            @Override
            public Step[] newArray(int size) {
                return new Step[size];
            }
        };

        public Step(){}

        public Step(Parcel source) {
            id = source.readInt();
            stepId = source.readInt();
            stepVersion = source.readInt();
            caseStepDataFields = DataField.toMyObjects(source.readParcelableArray(DataField.class.getClassLoader()));
            responsableUserId = source.readInt();
            executed= source.readByte() != 0;
        }

        public static Step[] toMyObjects(Parcelable[] parcelables) {
            Step[] objects = new Step[parcelables.length];
            System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
            return objects;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(stepId);
            dest.writeInt(stepVersion);
            dest.writeParcelableArray(caseStepDataFields, flags);
            dest.writeInt(responsableUserId);
            dest.writeByte((byte) (executed ? 1 : 0));
        }

        public boolean hasResponsableUser() {
            return responsableUserId != null && responsableUserId > 0;
        }

        public boolean hasDataField(int fieldId) {
            if (caseStepDataFields == null)
                return false;

            for (int i = 0; i < caseStepDataFields.length; i++) {
                if (caseStepDataFields[i].getFieldId() == fieldId)
                    return true;
            }

            return false;
        }

        public Object getDataField(int fieldId) {
            if (caseStepDataFields == null)
                return null;

            for (int i = 0; i < caseStepDataFields.length; i++) {
                if (caseStepDataFields[i].getFieldId() == fieldId)
                    return caseStepDataFields[i].value;
            }

            return null;
        }
    }

    public int id;
    @JsonProperty("created_at")
    public String createdAt;
    @JsonProperty("updated_at")
    public String updatedAt;
    @JsonProperty("initial_flow_id")
    public int initialFlowId;

    @JsonProperty("get_responsible_user")
    public User responsibleUser;

    @JsonProperty("total_steps")
    public int totalSteps;

    @JsonProperty("flow_version")
    public int flowVersion;
    @JsonProperty("next_step_id")
    private Integer nextStepId;
    private String status;
    @JsonProperty("current_step")
    private Step currentStep;

    public Step[] steps;

    public static final Creator<CaseOriginal> CREATOR = new Creator<CaseOriginal>() {
        @Override
        public CaseOriginal createFromParcel(Parcel source) {
            return new CaseOriginal(source);
        }

        @Override
        public CaseOriginal[] newArray(int size) {
            return new CaseOriginal[size];
        }
    };

    public CaseOriginal(){}

    public CaseOriginal(Parcel source){
        id = source.readInt();
        totalSteps = source.readInt();
        responsibleUser = source.readParcelable(User.class.getClassLoader());
        createdAt = source.readString();
        updatedAt = source.readString();
        initialFlowId = source.readInt();
        flowVersion = source.readInt();
        nextStepId = (Integer) source.readValue(Integer.class.getClassLoader());
        status = source.readString();
        currentStep = source.readParcelable(Step.class.getClassLoader());
        steps = Step.toMyObjects(source.readParcelableArray(Step.class.getClassLoader()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(totalSteps);
        dest.writeParcelable(responsibleUser, flags);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeInt(initialFlowId);
        dest.writeInt(flowVersion);
        dest.writeValue(nextStepId);
        dest.writeString(status);
        dest.writeParcelable(currentStep, flags);
        dest.writeParcelableArray(steps, flags);
    }

    public String getStatus() {
        if (Zup.getInstance().getSyncActionService().hasSyncActionRelatedToCase(id)) {
            return "sync_pending";
        } else {
            return this.status;
        }
    }

    @JsonGetter("next_step_id")
    public Integer getNextStepId() {
        return nextStepId;
    }

    @JsonSetter("next_step_id")
    public void setNextStepId(Integer nextStepId) {
        this.nextStepId = nextStepId;
    }

    @JsonGetter("current_step")
    public Step getCurrentStep() {
        return currentStep;
    }

    @JsonSetter("current_step")
    public void setCurrentStep(Step currentStep) {
        this.currentStep = currentStep;
    }

    public void finishCurrentStep() {
        if(currentStep == null) {
            status = "finished";
            return;
        }
        Flow flow = Zup.getInstance().getFlowService().getFlow(initialFlowId, flowVersion);
        if(nextStepId == null) {
            currentStep = null;
            status = "finished";
            return;
        }
        Flow.Step step = flow.getStep(nextStepId);
        currentStep.stepId = step.id;
        currentStep.stepVersion = step.version_id;

        Flow.Step nextStep = flow.getStepAfter(currentStep.stepId);
        if(nextStep == null) {
            nextStepId = null;
            return;
        }
        nextStepId = nextStep.id;
    }

    public boolean isAfterCurrentStep(int stepId) {
        if(currentStep == null || steps == null) {
            return false;
        }
        if(currentStep.stepId == stepId) {
            return false;
        }
        boolean afterCurrentStep = false;
        for (int i = 0; i < steps.length; i++) {
            if(steps[i].stepId == currentStep.id) {
                afterCurrentStep = true;
                continue;
            }
            if (steps[i].stepId == stepId)
                return afterCurrentStep;
        }
        return afterCurrentStep;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Step getStep(int stepId) {
        if (steps == null)
            return null;

        for (int i = 0; i < steps.length; i++) {
            if (steps[i].stepId == stepId)
                return steps[i];
        }

        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
