package com.particity.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;
import com.crashlytics.android.Crashlytics;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.api.errors.SyncErrors;
import com.particity.zuptecnico.entities.collections.SingleReportItemCollection;
import com.particity.zuptecnico.entities.responses.PublishReportResponse;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit.RetrofitError;

public class ChangeReportStatusSyncAction extends SyncAction {
  public static final String CASE_DELETED = "case_deleted";
  public int caseResponsible;
  public int itemId;
  public int categoryId;
  public int statusId;

  public static final Creator<ChangeReportStatusSyncAction> CREATOR =
      new Creator<ChangeReportStatusSyncAction>() {
        @Override public ChangeReportStatusSyncAction createFromParcel(Parcel source) {
          return new ChangeReportStatusSyncAction(source);
        }

        @Override public ChangeReportStatusSyncAction[] newArray(int size) {
          return new ChangeReportStatusSyncAction[size];
        }
      };

  public ChangeReportStatusSyncAction() {
    super();
  }

  public ChangeReportStatusSyncAction(Parcel in) {
    super(in);
    itemId = in.readInt();
    categoryId = in.readInt();
    statusId = in.readInt();
    caseResponsible = in.readInt();
  }

  public ChangeReportStatusSyncAction(int id, int categoryId, int statusId, int caseResponsible) {
    this.itemId = id;
    this.categoryId = categoryId;
    this.statusId = statusId;
    this.caseResponsible = caseResponsible;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(itemId);
    dest.writeInt(categoryId);
    dest.writeInt(statusId);
    dest.writeInt(caseResponsible);
  }

  @Override protected boolean onPerform() {
    try {
      SingleReportItemCollection report;
      if (caseResponsible <= 0) {
        report = Zup.getInstance().getService().changeReportStatus(categoryId, itemId, statusId);
      } else {
        report = Zup.getInstance()
            .getService()
            .changeReportStatus(categoryId, itemId, statusId, caseResponsible);
      }

      Intent intent = new Intent();
      intent.putExtra("report", report.report);
      broadcastAction(EditReportItemSyncAction.REPORT_EDITED, intent);

      return true;
    } catch (RetrofitError ex) {
      try {
        Crashlytics.setString("request", serialize().toString());
      } catch (Exception e) {
        e.printStackTrace();
      }

      int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
      Crashlytics.logException(SyncErrors.build(errorType, ex));
      if (errorType == SyncErrors.NOT_FOUND_ERROR_CODE || errorType == 504) {
        setError(ex.getMessage());
        return false;
      }
      try {
        PublishReportResponse response =
            (PublishReportResponse) ex.getBodyAs(PublishReportResponse.class);
        if (response.error != null) {
          setError(getError(response));
          return false;
        }
      } catch (Exception error) {
        error.printStackTrace();
      }
      if (getError() == null) {
        setError(ex.getMessage());
      }
      return false;
    }
  }

  private String getError(PublishReportResponse response) {
    String message = "";
    if (response.error instanceof Map) {
      int j = 0;
      for (Object key : ((Map) response.error).keySet()) {
        if (j > 0) {
          message += "\r\n\r\n";
        }
        String fieldName = key.toString();
        message += fieldName + "\r\n";
        List lst = (List) ((Map) response.error).get(key.toString());
        int i = 0;
        for (Object msg : lst) {
          if (i > 0) {
            message += "\r\n";
          }
          message += " - " + msg;
          i++;
        }
        j++;
      }
      Crashlytics.setString("error", message);
      return message;
    } else if (response.error != null) {
      return response.error.toString();
    }
    return null;
  }

  @Override protected JSONObject serialize() throws JSONException {
    JSONObject result = new JSONObject();
    result.put("item_id", itemId);
    result.put("error", getError());

    return result;
  }

  @Override public int describeContents() {
    return 0;
  }
}
