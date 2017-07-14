package com.ntxdev.zuptecnico.entities.originalcase;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;

import java.util.HashMap;
import java.util.LinkedHashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlowOriginal implements Parcelable {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step implements Parcelable {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Field implements Parcelable {
            public int id;
            public String title;
            public String field_type;
            public int category_inventory_id;
            public int category_report_id;
            public int origin_field_id;
            public boolean active;
            public int step_id;
            public boolean multiple;

            public LinkedHashMap requirements;
            public int order_number;
            public LinkedHashMap values;

            public int version_id;

            public static final Creator<Field> CREATOR = new Creator<Field>() {
                @Override
                public Field createFromParcel(Parcel source) {
                    return new Field(source);
                }

                @Override
                public Field[] newArray(int size) {
                    return new Field[size];
                }
            };

            public Field() {
            }

            public Field(Parcel in) {
                id = in.readInt();
                title = in.readString();
                field_type = in.readString();
                category_inventory_id = in.readInt();
                category_report_id = in.readInt();
                origin_field_id = in.readInt();
                active = in.readByte() != 0;
                step_id = in.readInt();
                multiple = in.readByte() != 0;
                requirements = (LinkedHashMap) in.readSerializable();
                order_number = in.readInt();
                values = (LinkedHashMap) in.readSerializable();
                version_id = in.readInt();
            }

            public static Field[] toMyObjects(Parcelable[] parcelables) {
                Field[] objects = new Field[parcelables.length];
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
                dest.writeString(title);
                dest.writeString(field_type);
                dest.writeInt(category_inventory_id);
                dest.writeInt(category_report_id);
                dest.writeInt(origin_field_id);
                dest.writeByte((byte) (active ? 1 : 0));
                dest.writeInt(step_id);
                dest.writeByte((byte) (multiple ? 1 : 0));
                dest.writeSerializable(requirements);
                dest.writeInt(order_number);
                dest.writeSerializable(values);
                dest.writeInt(version_id);
            }
        }

        public int id;
        public String title;
        @JsonProperty("step_type")
        public String stepType;

        @JsonProperty("my_fields")
        public Field[] fields;

        @JsonProperty("order_number")
        public int orderNumber;
        public boolean active;

        public int version_id;

        public Step[] list_versions;

        public int child_flow_id;
        public int child_flow_version;

        public FlowOriginal my_child_flow;

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

        public Step() {
        }

        public Step(Parcel in) {
            id = in.readInt();
            title = in.readString();
            stepType = in.readString();
            fields = Field.toMyObjects(in.readParcelableArray(Field.class.getClassLoader()));
            orderNumber = in.readInt();
            active = in.readByte() != 0;
            version_id = in.readInt();
            list_versions = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
            child_flow_id = in.readInt();
            child_flow_version = in.readInt();
            my_child_flow = in.readParcelable(FlowOriginal.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeString(title);
            dest.writeString(stepType);
            dest.writeParcelableArray(fields, flags);
            dest.writeInt(orderNumber);
            dest.writeByte((byte) (active ? 1 : 0));
            dest.writeInt(version_id);
            dest.writeParcelableArray(list_versions, flags);
            dest.writeInt(child_flow_id);
            dest.writeInt(child_flow_version);
            dest.writeParcelable(my_child_flow, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static Step[] toMyObjects(Parcelable[] parcelables) {
            if (parcelables == null) {
                return null;
            }
            Step[] objects = new Step[parcelables.length];
            System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
            return objects;
        }

        public int getChildFlowId() {
            if (my_child_flow != null)
                return my_child_flow.id;
            else
                return child_flow_id;
        }

        public int getChildFlowVersion() {
            if (my_child_flow != null && my_child_flow.version_id != null)
                return my_child_flow.version_id;
            else
                return child_flow_version;
        }

        public Field getField(int id) {
            if (fields == null)
                return null;

            for (int i = 0; i < fields.length; i++) {
                if (fields[i].id == id)
                    return fields[i];
            }

            return null;
        }

        public boolean areFieldsDownloaded() {
            if (fields == null)
                return false;

            return true;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ResolutionState implements Parcelable {
        public int id;
        @JsonProperty("flow_id")
        public int flowId;
        public String title;
        @JsonProperty("default")
        public boolean isDefault;
        public boolean active;
        public String created_at;
        public String updated_at;
        public Integer last_version;
        public Integer last_version_id;

        public static final Creator<ResolutionState> CREATOR = new Creator<ResolutionState>() {
            @Override
            public ResolutionState createFromParcel(Parcel source) {
                return new ResolutionState(source);
            }

            @Override
            public ResolutionState[] newArray(int size) {
                return new ResolutionState[size];
            }
        };

        public ResolutionState() {
        }

        public ResolutionState(Parcel source) {
            id = source.readInt();
            flowId = source.readInt();
            title = source.readString();
            isDefault = source.readByte() != 0;
            active = source.readByte() != 0;
            created_at = source.readString();
            updated_at = source.readString();
            last_version = source.readInt();
            last_version_id = source.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(id);
            dest.writeInt(flowId);
            dest.writeString(title);
            dest.writeByte((byte) (isDefault ? 1 : 0));
            dest.writeByte((byte) (active ? 1 : 0));
            dest.writeString(created_at);
            dest.writeString(updated_at);
            dest.writeInt(last_version);
            dest.writeInt(last_version_id);
        }

        public static ResolutionState[] toMyObjects(Parcelable[] parcelables) {
            ResolutionState[] objects = new ResolutionState[parcelables.length];
            System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
            return objects;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepCollection implements Parcelable {
        public Step[] steps;

        public static final Creator<StepCollection> CREATOR = new Creator<StepCollection>() {
            @Override
            public StepCollection createFromParcel(Parcel source) {
                return new StepCollection(source);
            }

            @Override
            public StepCollection[] newArray(int size) {
                return new StepCollection[size];
            }
        };

        public StepCollection() {
        }

        public StepCollection(Parcel in) {
            steps = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelableArray(steps, flags);
        }
    }

    public int id;
    public String title;
    public String description;
    public boolean initial;
    public int total_cases;
    public Step[] steps;
    @JsonProperty("resolution_states")
    public ResolutionState[] resolution_states;
    public User created_by;
    public User updated_by;
    public String created_at;
    public String updated_at;
    public FlowOriginal[] list_versions;

    public HashMap<String, Integer> steps_versions;

    public String status;
    public Integer version_id;

    public static final Creator<FlowOriginal> CREATOR = new Creator<FlowOriginal>() {
        @Override
        public FlowOriginal createFromParcel(Parcel source) {
            return new FlowOriginal(source);
        }

        @Override
        public FlowOriginal[] newArray(int size) {
            return new FlowOriginal[size];
        }
    };

    public FlowOriginal() {
    }

    public FlowOriginal(Parcel in) {
        id = in.readInt();
        title = in.readString();
        description = in.readString();
        total_cases = in.readInt();
        initial = in.readByte() != 0;
        steps_versions = (HashMap<String, Integer>) in.readSerializable();
        created_by = in.readParcelable(User.class.getClassLoader());
        updated_by = in.readParcelable(User.class.getClassLoader());
        resolution_states = ResolutionState.toMyObjects(in.readParcelableArray(ResolutionState.class.getClassLoader()));
        status = in.readString();
        version_id = in.readInt();
        created_at = in.readString();
        updated_at = in.readString();
        steps = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
        list_versions = FlowOriginal.toMyObjects(in.readParcelableArray(FlowOriginal.class.getClassLoader()));
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeInt(total_cases);
        dest.writeByte((byte) (initial ? 1 : 0));
        dest.writeSerializable(steps_versions);
        dest.writeParcelable(created_by, flags);
        dest.writeParcelable(updated_by, flags);
        dest.writeParcelableArray(resolution_states, flags);
        dest.writeString(status);
        dest.writeInt(version_id);
        dest.writeString(created_at);
        dest.writeString(updated_at);
        dest.writeParcelableArray(steps, flags);
        dest.writeParcelableArray(list_versions, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static FlowOriginal[] toMyObjects(Parcelable[] parcelables) {
        FlowOriginal[] objects = new FlowOriginal[parcelables.length];
        System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
        return objects;
    }

    public boolean areStepsDownloaded() {
        if (steps == null || steps.length == 0)
            return false;

        for (Step step : steps) {
            if (step.stepType.equals("form") && (step.fields == null || step.fields.length == 0))
                return false;
        }

        return true;
    }

  /*  public Step getStep(int id) {
        if (steps == null)
            return null;

        for (int i = 0; i < steps.length; i++) {
            Step step = steps[i];
            if (step != null && step.stepType.equals("flow")) {
                FlowOriginal flow = Zup.getInstance().getFlowService().getFlow(step.child_flow_id, step.child_flow_version);
                if(flow.getStep(id) == null) {
                    continue;
                }
                return flow.getStep(id);
            }
            if (steps[i].id == id)
                return steps[i];
        }
        return null;
    } */

    private Step getLocalStep(int id) {
        if (steps == null)
            return null;

        for (int i = 0; i < steps.length; i++) {
            if (steps[i].id == id)
                return steps[i];
        }
        return null;
    }
/*
    public Step getStepAfter(int stepId) {
        if (steps == null || getStep(stepId) == null)
            return null;

        for (int i = 0; i < steps.length; i++) {
            Step step = steps[i];
            if (step != null && step.stepType.equals("flow")) {
                FlowOriginal flow = Zup.getInstance().getFlowService().getFlow(step.child_flow_id, step.child_flow_version);
                if (flow.getStepAfter(stepId) == null) {
                    if (flow.getLocalStep(stepId) != null) {
                        if (i + 1 < steps.length) {
                            return steps[i + 1];
                        }
                        return null;
                    }
                    continue;
                }
                return flow.getStepAfter(stepId);
            }
            if (steps[i].id == stepId) {
                if (i + 1 < steps.length) {
                    return steps[i + 1];
                }
            }
        }
        return null;
    }*/
}
