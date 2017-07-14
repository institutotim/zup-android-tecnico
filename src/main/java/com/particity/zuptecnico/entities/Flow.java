package com.ntxdev.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ntxdev.zuptecnico.api.Zup;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true) public class Flow implements Parcelable {
  @JsonIgnoreProperties(ignoreUnknown = true) public static class Step implements Parcelable {
    @JsonIgnoreProperties(ignoreUnknown = true) public static class Field implements Serializable {

      public static class Requirements implements Serializable {
        public boolean presence;
        public boolean multiline;

        public Requirements() {
        }

        public Requirements(Parcel in) {
          presence = in.readByte() != 0;
          multiline = in.readByte() != 0;
        }
      }

      public int id;
      public String title;
      public String field_type;
      public int category_report_id;
      public InventoryCategory[] category_inventory;
      public int origin_field_id;
      public boolean active;
      public int step_id;
      public boolean multiple;
      public InventoryCategory.Section.Field category_inventory_field;

      public Requirements requirements;

      public ArrayList<String> values;

      public Field() {
      }
    }

    @JsonIgnoreProperties(ignoreUnknown = true) public static class FlowPermissions implements Serializable {
      @JsonIgnoreProperties(ignoreUnknown = true) public static class Permission implements Serializable {
        public int id;
        public String name;
      }
      @JsonProperty("can_view_step")
      public Permission[] canViewStep;
      @JsonProperty("can_execute_step")
      public Permission[] canExecuteStep;
    }
    public int id;
    public String title;
    @JsonProperty("step_type") public String stepType;

    public boolean conduction_mode_open;

    @JsonProperty("my_fields") public ArrayList<Field> fields;
    public boolean active;

    public int version_id;

    public Step[] list_versions;

    public int child_flow_id;
    public int child_flow_version;

    public Flow my_child_flow;
    public FlowPermissions permissions;

    public int user_id;

    public static final Parcelable.Creator<Step> CREATOR = new Parcelable.Creator<Step>() {
      @Override public Step createFromParcel(Parcel source) {
        return new Step(source);
      }

      @Override public Step[] newArray(int size) {
        return new Step[size];
      }
    };

    public Step() {
    }

    public Step(Parcel in) {
      id = in.readInt();
      title = in.readString();
      conduction_mode_open = in.readByte() != 0;
      stepType = in.readString();
      fields = (ArrayList<Field>) in.readSerializable();
      active = in.readByte() != 0;
      version_id = in.readInt();
      list_versions = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
      child_flow_id = in.readInt();
      child_flow_version = in.readInt();
      my_child_flow = in.readParcelable(Flow.class.getClassLoader());
      permissions = (FlowPermissions) in.readSerializable();
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(id);
      dest.writeString(title);
      dest.writeByte((byte) (conduction_mode_open ? 1 : 0));
      dest.writeString(stepType);
      dest.writeSerializable(fields);
      dest.writeByte((byte) (active ? 1 : 0));
      dest.writeInt(version_id);
      dest.writeParcelableArray(list_versions, flags);
      dest.writeInt(child_flow_id);
      dest.writeInt(child_flow_version);
      dest.writeParcelable(my_child_flow, flags);
      dest.writeSerializable(permissions);
    }

    @Override public int describeContents() {
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
      if (my_child_flow != null) {
        return my_child_flow.id;
      } else {
        return child_flow_id;
      }
    }

    public int getChildFlowVersion() {
      if (my_child_flow != null && my_child_flow.version_id != 0) {
        return my_child_flow.version_id;
      } else {
        return child_flow_version;
      }
    }

    public Field getField(Integer id) {
      if (fields == null || id == null) return null;

      for (int i = 0; i < fields.size(); i++) {
        if (fields.get(i).id == id) return fields.get(i);
      }

      return null;
    }

    public boolean areFieldsDownloaded() {
      if (fields == null) return false;

      return true;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class ResolutionState
      implements Parcelable {
    public int id;
    @JsonProperty("flow_id") public int flowId;
    public String title;
    @JsonProperty("default") public boolean isDefault;
    public boolean active;
    public String created_at;
    public String updated_at;

    public static final Parcelable.Creator<ResolutionState> CREATOR =
        new Parcelable.Creator<ResolutionState>() {
          @Override public ResolutionState createFromParcel(Parcel source) {
            return new ResolutionState(source);
          }

          @Override public ResolutionState[] newArray(int size) {
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
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(id);
      dest.writeInt(flowId);
      dest.writeString(title);
      dest.writeByte((byte) (isDefault ? 1 : 0));
      dest.writeByte((byte) (active ? 1 : 0));
      dest.writeString(created_at);
      dest.writeString(updated_at);
    }

    public static ResolutionState[] toMyObjects(Parcelable[] parcelables) {
      if (parcelables == null) {
        return null;
      }
      ResolutionState[] objects = new ResolutionState[parcelables.length];
      System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
      return objects;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true) public static class StepCollection
      implements Parcelable {
    public Step[] steps;

    public static final Parcelable.Creator<StepCollection> CREATOR =
        new Parcelable.Creator<StepCollection>() {
          @Override public StepCollection createFromParcel(Parcel source) {
            return new StepCollection(source);
          }

          @Override public StepCollection[] newArray(int size) {
            return new StepCollection[size];
          }
        };

    public StepCollection() {
    }

    public StepCollection(Parcel in) {
      steps = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeParcelableArray(steps, flags);
    }
  }

  public int id;
  public String title;
  public String description;
  public boolean initial;
  public int total_cases;
  public Step[] steps;
  @JsonProperty("my_resolution_states") public ResolutionState[] resolution_states;
  public User created_by;
  public User updated_by;
  public String created_at;
  public String updated_at;
  public Flow[] list_versions;

  public HashMap<String, Integer> steps_versions;

  public String status;
  public int version_id;

  public static final Parcelable.Creator<Flow> CREATOR = new Parcelable.Creator<Flow>() {
    @Override public Flow createFromParcel(Parcel source) {
      return new Flow(source);
    }

    @Override public Flow[] newArray(int size) {
      return new Flow[size];
    }
  };

  public Flow() {
  }

  public Flow(Parcel in) {
    id = in.readInt();
    title = in.readString();
    description = in.readString();
    total_cases = in.readInt();
    initial = in.readByte() != 0;
    steps_versions = (HashMap<String, Integer>) in.readSerializable();
    created_by = in.readParcelable(User.class.getClassLoader());
    updated_by = in.readParcelable(User.class.getClassLoader());
    resolution_states =
        ResolutionState.toMyObjects(in.readParcelableArray(ResolutionState.class.getClassLoader()));
    status = in.readString();
    version_id = in.readInt();
    created_at = in.readString();
    updated_at = in.readString();
    steps = Step.toMyObjects(in.readParcelableArray(Step.class.getClassLoader()));
    list_versions = Flow.toMyObjects(in.readParcelableArray(Flow.class.getClassLoader()));
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
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

  @Override public int describeContents() {
    return 0;
  }

  public static Flow[] toMyObjects(Parcelable[] parcelables) {
    if (parcelables == null) {
      return null;
    }
    Flow[] objects = new Flow[parcelables.length];
    System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
    return objects;
  }

  public boolean areStepsDownloaded() {
    if (steps == null || steps.length == 0) return false;

    for (Step step : steps) {
      if (step.stepType.equals("form") && (step.fields == null || step.fields.isEmpty())) {
        return false;
      }
    }

    return true;
  }

  public Step getStep(int id) {
    if (steps == null) return null;

    for (int i = 0; i < steps.length; i++) {
      Step step = steps[i];
      if (step != null && step.stepType.equals("flow")) {
        Flow flow =
            Zup.getInstance().getFlowService().getFlow(step.child_flow_id, step.child_flow_version);
        if (flow.getStep(id) == null) {
          continue;
        }
        return flow.getStep(id);
      }
      if (steps[i].id == id) return steps[i];
    }
    return null;
  }

  private Step getLocalStep(int id) {
    if (steps == null) return null;

    for (int i = 0; i < steps.length; i++) {
      if (steps[i].id == id) return steps[i];
    }
    return null;
  }

  public Step getStepAfter(int stepId) {
    if (steps == null || getStep(stepId) == null) return null;

    for (int i = 0; i < steps.length; i++) {
      Step step = steps[i];
      if (step != null && step.stepType.equals("flow")) {
        Flow flow =
            Zup.getInstance().getFlowService().getFlow(step.child_flow_id, step.child_flow_version);
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
  }
}
