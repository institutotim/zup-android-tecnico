package com.lfdb.zuptecnico.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.lfdb.zuptecnico.api.Zup;
import java.io.Serializable;
import java.util.ArrayList;

@JsonIgnoreProperties(ignoreUnknown = true) public class Case implements Parcelable {

  @JsonIgnoreProperties(ignoreUnknown = true) public static class Step implements Parcelable {

    @JsonIgnoreProperties(ignoreUnknown = true) public static class DataField
        implements Serializable {

      public static class FileAttachment implements Serializable {
        public int id;
        public String file_name;
        public String url;
      }

      public int id;
      public Object value;
      public Flow.Step.Field field;
      public FileAttachment[] case_step_data_attachments;
      public InventoryItemImage[] case_step_data_images;
      public int fieldId;

      public DataField() {
      }

      public int getFieldId() {
        if (field != null) {
          return field.id;
        } else {
          return fieldId;
        }
      }
    }

    public int id;
    @JsonProperty("step_id") public int stepId;
    @JsonProperty("step_version") public int stepVersion;
    @JsonProperty("case_step_data_fields") public ArrayList<DataField> caseStepDataFields;
    @JsonProperty("responsible_user") public User responsableUser;
    @JsonProperty("responsible_group") public Group responsibleGroup;
    public boolean executed;
    @JsonProperty("my_step") public Flow.Step flowStep;

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

    public Step(Parcel source) {
      id = source.readInt();
      stepId = source.readInt();
      stepVersion = source.readInt();
      caseStepDataFields = (ArrayList<DataField>) source.readSerializable();
      responsableUser = source.readParcelable(User.class.getClassLoader());
      executed = source.readByte() != 0;
      flowStep = source.readParcelable(Flow.Step.class.getClassLoader());
      responsibleGroup = (Group) source.readSerializable();
    }

    public static Step[] toMyObjects(Parcelable[] parcelables) {
      if (parcelables == null) {
        return null;
      }
      Step[] objects = new Step[parcelables.length];
      System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
      return objects;
    }

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(id);
      dest.writeInt(stepId);
      dest.writeInt(stepVersion);
      dest.writeSerializable(caseStepDataFields);
      dest.writeParcelable(responsableUser, flags);
      dest.writeByte((byte) (executed ? 1 : 0));
      dest.writeParcelable(flowStep, flags);
      dest.writeSerializable(responsibleGroup);
    }

    public boolean hasResponsableUser() {
      return responsableUser != null;
    }

    public boolean hasResponsibleGroup() {
      return responsibleGroup != null;
    }

    public boolean hasDataField(int fieldId) {
      if (caseStepDataFields == null) return false;

      for (int i = 0; i < caseStepDataFields.size(); i++) {
        if (caseStepDataFields.get(i).getFieldId() == fieldId) return true;
      }

      return false;
    }

    public DataField getDataField(int fieldId) {
      if (caseStepDataFields == null) return null;

      for (int i = 0; i < caseStepDataFields.size(); i++) {
        if (caseStepDataFields.get(i).getFieldId() == fieldId) return caseStepDataFields.get(i);
      }

      return null;
    }

    public Object getDataFieldValue(int fieldId) {
      if (caseStepDataFields == null) return null;

      for (int i = 0; i < caseStepDataFields.size(); i++) {
        if (caseStepDataFields.get(i).getFieldId() == fieldId) {
          return caseStepDataFields.get(i).value;
        }
      }

      return null;
    }

    public Flow.Step.Field getField(int fieldId) {
      if (flowStep == null || flowStep.fields == null) return null;

      for (int i = 0; i < flowStep.fields.size(); i++) {
        if (flowStep.fields.get(i).id == fieldId) {
          return flowStep.fields.get(i);
        }
      }

      return null;
    }

    public DataField.FileAttachment[] getAttachmentDataField(int fieldId) {
      if (caseStepDataFields == null) return null;

      for (int i = 0; i < caseStepDataFields.size(); i++) {
        if (caseStepDataFields.get(i).getFieldId() == fieldId) {
          return caseStepDataFields.get(i).case_step_data_attachments;
        }
      }

      return null;
    }

    public InventoryItemImage[] getImagesDataField(int fieldId) {
      if (caseStepDataFields == null) return null;

      for (int i = 0; i < caseStepDataFields.size(); i++) {
        if (caseStepDataFields.get(i).getFieldId() == fieldId) {
          return caseStepDataFields.get(i).case_step_data_images;
        }
      }

      return null;
    }

    public Object getDataFieldByType(String type) {
      if (flowStep == null || flowStep.fields == null) {
        return null;
      }

      for (int i = 0; i < flowStep.fields.size(); i++) {
        if (flowStep.fields.get(i).field_type.equals(type)) {
          return getDataFieldValue(flowStep.fields.get(i).id);
        }
      }

      return null;
    }
  }

  public int id;
  @JsonProperty("created_at") public String createdAt;
  @JsonProperty("updated_at") public String updatedAt;
  @JsonProperty("initial_flow_id") public int initialFlowId;

  @JsonProperty("initial_flow") public Flow initialFlow;

  @JsonProperty("get_responsible_user") public User responsibleUser;

  @JsonProperty("total_steps") public int totalSteps;

  @JsonProperty("flow_version") public int flowVersion;
  @JsonProperty("next_step_id") private Integer nextStepId;
  private String status;
  @JsonProperty("current_step") private Step currentStep;

  @JsonProperty("next_steps") public Flow.Step[] nextSteps;

  @JsonProperty("case_steps") public Step[] caseSteps;

  @JsonProperty("related_entities") public CaseRelatedItems relatedItems;

  public static final Parcelable.Creator<Case> CREATOR = new Parcelable.Creator<Case>() {
    @Override public Case createFromParcel(Parcel source) {
      return new Case(source);
    }

    @Override public Case[] newArray(int size) {
      return new Case[size];
    }
  };

  public static Case[] toMyObjects(Parcelable[] parcelables) {
    if (parcelables == null) {
      return null;
    }
    Case[] objects = new Case[parcelables.length];
    System.arraycopy(parcelables, 0, objects, 0, parcelables.length);
    return objects;
  }

  public Case() {
  }

  public Case(Parcel source) {
    id = source.readInt();
    totalSteps = source.readInt();
    responsibleUser = source.readParcelable(User.class.getClassLoader());
    createdAt = source.readString();
    updatedAt = source.readString();
    initialFlowId = source.readInt();
    initialFlow = source.readParcelable(Flow.class.getClassLoader());
    flowVersion = source.readInt();
    nextStepId = (Integer) source.readValue(Integer.class.getClassLoader());
    status = source.readString();
    currentStep = source.readParcelable(Step.class.getClassLoader());
    nextSteps = Flow.Step.toMyObjects(source.readParcelableArray(Flow.Step.class.getClassLoader()));
    caseSteps = Step.toMyObjects(source.readParcelableArray(Step.class.getClassLoader()));
    relatedItems = source.readParcelable(CaseRelatedItems.class.getClassLoader());
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeInt(totalSteps);
    dest.writeParcelable(responsibleUser, flags);
    dest.writeString(createdAt);
    dest.writeString(updatedAt);
    dest.writeInt(initialFlowId);
    dest.writeParcelable(initialFlow, flags);
    dest.writeInt(flowVersion);
    dest.writeValue(nextStepId);
    dest.writeString(status);
    dest.writeParcelable(currentStep, flags);
    dest.writeParcelableArray(nextSteps, flags);
    dest.writeParcelableArray(caseSteps, flags);
    dest.writeParcelable(relatedItems, flags);
  }

  public String getStatus() {
    if (Zup.getInstance().getSyncActionService().hasSyncActionRelatedToCase(id)) {
      return "sync_pending";
    } else if (totalSteps > 0 && (nextSteps == null || nextSteps.length == 0)) {
      return "finished";
    }
    return this.status;
  }

  public Flow.Step[] getSteps() {
    int previousSize = caseSteps == null ? 0 : caseSteps.length;
    int nextSize = nextSteps == null ? 0 : nextSteps.length;
    int size = previousSize + nextSize;
    Flow.Step[] steps = new Flow.Step[size];
    for (int i = 0; i < previousSize; i++) {
      steps[i] = caseSteps[i].flowStep;
    }
    for (int i = previousSize; i < size; i++) {
      int j = i - previousSize;
      steps[i] = nextSteps[j];
    }
    return steps;
  }

  public Step.DataField getDataField(int fieldId) {
    if (caseSteps != null) {
      for (int index = 0; index < caseSteps.length; index++) {
        Step.DataField value = caseSteps[index].getDataField(fieldId);
        if (value != null) {
          return value;
        }
      }
    }
    if (currentStep != null) {
      return currentStep.getDataField(fieldId);
    }
    return null;
  }

  public Object getDataFieldValue(int fieldId) {
    if (caseSteps != null) {
      for (int index = 0; index < caseSteps.length; index++) {
        Object value = caseSteps[index].getDataFieldValue(fieldId);
        if (value != null) {
          return value;
        }
      }
    }
    if (currentStep != null) {
      return currentStep.getDataFieldValue(fieldId);
    }
    return null;
  }

  public InventoryItemImage[] getImagesField(int fieldId) {
    if (caseSteps != null) {
      for (int index = 0; index < caseSteps.length; index++) {
        InventoryItemImage[] value = caseSteps[index].getImagesDataField(fieldId);
        if (value != null) {
          return value;
        }
      }
    }

    if (currentStep != null) {
      return currentStep.getImagesDataField(fieldId);
    }

    return null;
  }

  public Step.DataField.FileAttachment[] getAttachmentsDataField(int fieldId) {
    if (caseSteps != null) {
      for (int index = 0; index < caseSteps.length; index++) {
        Step.DataField.FileAttachment[] value = caseSteps[index].getAttachmentDataField(fieldId);
        if (value != null) {
          return value;
        }
      }
    }

    if (currentStep != null) {
      return currentStep.getAttachmentDataField(fieldId);
    }

    return null;
  }

  public Flow.Step.Field getField(int fieldId) {
    if (caseSteps == null) {
      return null;
    }

    for (int index = 0; index < caseSteps.length; index++) {
      Flow.Step.Field value = caseSteps[index].getField(fieldId);
      if (value != null) {
        return value;
      }
    }
    return null;
  }

  @JsonGetter("next_step_id") public Integer getNextStepId() {
    return nextStepId;
  }

  @JsonSetter("next_step_id") public void setNextStepId(Integer nextStepId) {
    this.nextStepId = nextStepId;
  }

  @JsonGetter("current_step") public Step getCurrentStep() {
    return currentStep;
  }

  @JsonSetter("current_step") public void setCurrentStep(Step currentStep) {
    this.currentStep = currentStep;
  }

  public void finishCurrentStep() {
    if (currentStep == null) {
      status = "finished";
      return;
    }
    Flow flow = initialFlow;
    if (nextStepId == null) {
      currentStep = null;
      status = "finished";
      return;
    }
    Flow.Step step = flow.getStep(nextStepId);
    currentStep.stepId = step.id;
    currentStep.stepVersion = step.version_id;

    Flow.Step nextStep = flow.getStepAfter(currentStep.stepId);
    if (nextStep == null) {
      nextStepId = null;
      return;
    }
    nextStepId = nextStep.id;
  }

  public boolean isCurrentStep(int stepId) {
    if (currentStep == null) {
      return false;
    }
    return currentStep.stepId == stepId;
  }

  public boolean isAfterCurrentStep(int stepId) {
    if (nextSteps == null) {
      return false;
    }
    for (int index = 0; index < nextSteps.length; index++) {
      Flow.Step step = nextSteps[index];
      if (step != null && step.id == stepId) {
        return true;
      }
    }
    return false;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Step getStep(int stepId) {
    if (currentStep != null && currentStep.stepId == stepId) {
      return currentStep;
    }
    if (caseSteps == null) return null;

    for (int i = 0; i < caseSteps.length; i++) {
      if (caseSteps[i].stepId == stepId) return caseSteps[i];
    }

    return null;
  }

  public Flow.Step getFlowStep(int stepId) {
    if (caseSteps == null) {
      return null;
    }

    for (int index = 0; index < caseSteps.length; index++) {
      Flow.Step step = caseSteps[index].flowStep;
      if (step != null && step.id == stepId) {
        return step;
      }
    }
    return null;
  }

  @Override public int describeContents() {
    return 0;
  }
}
