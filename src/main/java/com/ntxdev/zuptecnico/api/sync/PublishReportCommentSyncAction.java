package com.ntxdev.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.errors.SyncErrors;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.entities.requests.CreateReportItemCommentRequest;
import com.ntxdev.zuptecnico.entities.responses.CreateReportItemCommentResponse;
import com.ntxdev.zuptecnico.entities.responses.PublishReportResponse;

import com.snappydb.SnappydbException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit.RetrofitError;

public class PublishReportCommentSyncAction extends SyncAction implements ReportSyncAction {
  public static final String REPORT_COMMENT_CREATED = "report_comment_created";

  public static class Serializer {
    public int temporaryId;
    public int itemId;
    public int visibility;
    public String message;

    public String error;
  }

  private int temporaryId;
  public int itemId;
  public int visibility;
  public String message;

  public static final Creator<PublishReportCommentSyncAction> CREATOR =
      new Creator<PublishReportCommentSyncAction>() {
        @Override public PublishReportCommentSyncAction createFromParcel(Parcel source) {
          return new PublishReportCommentSyncAction(source);
        }

        @Override public PublishReportCommentSyncAction[] newArray(int size) {
          return new PublishReportCommentSyncAction[size];
        }
      };

  public PublishReportCommentSyncAction() {
    super();
  }

  public PublishReportCommentSyncAction(Parcel in) {
    super(in);
    temporaryId = in.readInt();
    itemId = in.readInt();
    visibility = in.readInt();
    message = in.readString();
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(temporaryId);
    dest.writeInt(itemId);
    dest.writeInt(visibility);
    dest.writeString(message);
  }

  @Override public int describeContents() {
    return 0;
  }

  public PublishReportCommentSyncAction(int itemId, int type, String message) {
    this.temporaryId = new Random(System.currentTimeMillis()).nextInt();

    this.itemId = itemId;
    this.visibility = type;
    this.message = message;
  }

  public PublishReportCommentSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
    Serializer serializer = mapper.readValue(object.toString(), Serializer.class);

    this.itemId = serializer.itemId;
    this.visibility = serializer.visibility;
    this.message = serializer.message;
    setError(serializer.error);
    this.temporaryId = serializer.temporaryId;
  }

  @Override protected boolean onPerform() {
    try {
      CreateReportItemCommentRequest request = new CreateReportItemCommentRequest();
      request.message = message;
      request.visibility = visibility;

      CreateReportItemCommentResponse response =
          Zup.getInstance().getService().createReportItemComment(itemId, request);

      this.removeFakeComment();
      this.addCommentToItem(response.comment);

      Intent intent = new Intent();
      intent.putExtra("report_id", itemId);
      intent.putExtra("comment", response.comment);

      this.broadcastAction(REPORT_COMMENT_CREATED, intent);
      return true;
    } catch (RetrofitError ex) {
      try {
        Crashlytics.setString("request", serialize().toString());
        int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
        Crashlytics.logException(SyncErrors.build(errorType, ex));
        PublishReportResponse response =
            (PublishReportResponse) ex.getBodyAs(PublishReportResponse.class);
        if (response.error != null) {
          setError(getError(response));
        }
        if (getError() == null) {
          setError(ex.getMessage());
        }
        ReportItem.Comment comment = createFakeComment();
        if (comment != null) {
          Intent intent = new Intent();
          intent.putExtra("report_id", itemId);
          intent.putExtra("comment", comment);
          this.broadcastAction(REPORT_COMMENT_CREATED, intent);
        }
      } catch (Exception e) {
        e.printStackTrace();
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
      return message;
    } else if (response.error != null) {
      return response.error.toString();
    }
    return null;
  }

  private void removeFakeComment() {
    ReportItem item = Zup.getInstance().getReportItemService().getReportItem(itemId);
    if (item != null) {
      item.removeComment(temporaryId);
      Zup.getInstance().getReportItemService().addReportItem(item);
    }
  }

  private void addCommentToItem(ReportItem.Comment comment) {
    if (Zup.getInstance().getSessionUser() == null) return;

    ReportItem item =
        Zup.getInstance().getReportItemService().hasReportItem(this.itemId) ? Zup.getInstance()
            .getReportItemService()
            .getReportItem(this.itemId) : null;
    if (item == null) return;

    item.addComment(comment);
    Zup.getInstance().getReportItemService().addReportItem(item);
  }

  private ReportItem.Comment createFakeComment() {
    if (Zup.getInstance().getSessionUser() == null) return null;

    ReportItem.Comment newComment = new ReportItem.Comment();
    newComment.id = temporaryId;
    newComment.author = Zup.getInstance().getSessionUser();
    newComment.created_at = Zup.getIsoDate(Calendar.getInstance().getTime());
    newComment.visibility = this.visibility;
    newComment.message = this.message;
    newComment.isFake = true;

    addCommentToItem(newComment);

    return newComment;
  }

  @Override protected JSONObject serialize() throws Exception {
    Serializer serializer = new Serializer();
    serializer.itemId = itemId;
    serializer.visibility = visibility;
    serializer.message = message;
    serializer.error = getError();
    serializer.temporaryId = temporaryId;

    String res = Zup.getInstance().getObjectMapper().writeValueAsString(serializer);

    return new JSONObject(res);
  }
}
