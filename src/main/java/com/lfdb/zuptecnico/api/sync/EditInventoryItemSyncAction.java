package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.errors.SyncErrors;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.requests.PublishInventoryItemRequest;
import com.lfdb.zuptecnico.entities.responses.EditInventoryItemErrorResponse;
import com.lfdb.zuptecnico.entities.responses.EditInventoryItemResponse;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;
import retrofit.RetrofitError;

public class EditInventoryItemSyncAction extends PublishOrEditInventorySyncAction {

  public static final String ITEM_EDITED = "inventory_item_edited";

  public static final Creator<EditInventoryItemSyncAction> CREATOR =
      new Creator<EditInventoryItemSyncAction>() {
        @Override public EditInventoryItemSyncAction createFromParcel(Parcel source) {
          return new EditInventoryItemSyncAction(source);
        }

        @Override public EditInventoryItemSyncAction[] newArray(int size) {
          return new EditInventoryItemSyncAction[size];
        }
      };

  public EditInventoryItemSyncAction() {
    super();
  }

  public EditInventoryItemSyncAction(Parcel in) {
    super(in);
  }

  public EditInventoryItemSyncAction(InventoryItem item) {
    super(item);
  }

  public EditInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
    super(object, mapper);
  }

  public boolean onPerform() {
    try {
      PublishInventoryItemRequest request = new PublishInventoryItemRequest();
      request.inventory_status_id = item.inventory_status_id;
      request.data = new Hashtable<>();
      for (InventoryItem.Data data : item.data) {
        if (data.content != null) {
          request.data.put(Integer.toString(data.getFieldId()), data.content);
        }
      }
      EditInventoryItemResponse response = Zup.getInstance()
          .getService()
          .editInventoryItem(item.inventory_category_id, item.id, request);
      Intent intent = new Intent();
      intent.putExtra("item", response.item);
      item = response.item;
      broadcastAction(ITEM_EDITED, intent);
      return true;
    } catch (RetrofitError ex) {
      try {
        Crashlytics.setInt("item", item.id);
        Crashlytics.setInt("category", item.inventory_category_id);
        Crashlytics.setInt("status", item.inventory_status_id);
        Crashlytics.setString("request", serialize().toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
      Crashlytics.logException(SyncErrors.build(errorType, ex));
      if (errorType == SyncErrors.NOT_FOUND_ERROR_CODE) {
        setError(ex.getMessage());
        return false;
      }
      EditInventoryItemErrorResponse response =
          (EditInventoryItemErrorResponse) ex.getBodyAs(EditInventoryItemErrorResponse.class);
      if (response.error != null) {
        setError(getError(response));
      }
      if (getError() == null) {
        setError(ex.getMessage());
      }
      return false;
    }
  }

  private String getError(EditInventoryItemErrorResponse response) {
    String message = "";
    if (response.error instanceof Map) {
      int j = 0;
      for (Object key : ((Map) response.error).keySet()) {
        if (j > 0) {
          message += "\r\n\r\n";
        }
        InventoryCategory.Section.Field field = Zup.getInstance()
            .getInventoryCategoryService()
            .getInventoryCategory(item.inventory_category_id)
            .getField(key.toString());
        String fieldName;
        if (field == null) {
          fieldName = key.toString();
        } else {
          fieldName = field.label;
        }
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
}
