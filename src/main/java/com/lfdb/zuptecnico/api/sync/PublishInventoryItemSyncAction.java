package com.lfdb.zuptecnico.api.sync;

import android.content.Intent;
import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.errors.SyncErrors;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.InventoryItemImage;
import com.lfdb.zuptecnico.entities.requests.PublishInventoryItemRequest;
import com.lfdb.zuptecnico.entities.responses.PublishInventoryItemResponse;

import com.lfdb.zuptecnico.util.Utilities;
import java.util.ArrayList;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import retrofit.RetrofitError;

public class PublishInventoryItemSyncAction extends PublishOrEditInventorySyncAction {
  public static final String ITEM_PUBLISHED = "inventory_item_published";

  public static final Creator<PublishInventoryItemSyncAction> CREATOR =
      new Creator<PublishInventoryItemSyncAction>() {
        @Override public PublishInventoryItemSyncAction createFromParcel(Parcel source) {
          return new PublishInventoryItemSyncAction(source);
        }

        @Override public PublishInventoryItemSyncAction[] newArray(int size) {
          return new PublishInventoryItemSyncAction[size];
        }
      };

  public PublishInventoryItemSyncAction() {
    super();
  }

  public PublishInventoryItemSyncAction(Parcel in) {
    super(in);
  }

  public PublishInventoryItemSyncAction(InventoryItem item) {
    super(item);
  }

  public PublishInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
    super(object, mapper);
  }

  public boolean onPerform() {
    try {
      PublishInventoryItemRequest request = new PublishInventoryItemRequest();
      request.inventory_status_id = item.inventory_status_id;
      request.data = new Hashtable<>();
      for (InventoryItem.Data data : item.data) {

        if (data.content != null) {
          if (data.content instanceof List) {
            List<Object> items = (List<Object>) data.content;
            List<InventoryItemImage> images = new ArrayList<>();
            for (Object item : items) {
              if (item instanceof InventoryItemImage) {
                InventoryItemImage image = (InventoryItemImage) item;
                if (image.content != null) {
                  image.content = Utilities.encodeBase64(image.content);
                }
                images.add(image);
              } else {
                break;
              }
            }
            request.data.put(Integer.toString(data.getFieldId()), images.isEmpty() ? items : images);
            continue;
          }
          request.data.put(Integer.toString(data.getFieldId()), data.content);
        }
      }
      PublishInventoryItemResponse response =
          Zup.getInstance().getService().publishInventoryItem(item.inventory_category_id, request);
      if (response == null) {
        return false;
      }
      if (response.error != null) {
        setError(getError(response));
      }
      if (getError() == null) {
        Intent intent = new Intent();
        intent.putExtra("item", response.item);
        item = response.item;
        Zup.getInstance().getInventoryItemService().deleteInventoryItem(inventory_item_id);
        inventory_item_id = item.id;
        broadcastAction(ITEM_PUBLISHED, intent);
        return true;
      }
    } catch (RetrofitError ex) {
      try {
        Crashlytics.setInt("category", item.inventory_category_id);
        Crashlytics.setInt("status", item.inventory_status_id);
        Crashlytics.setString("request", serialize().toString());
      } catch (Exception e) {
        e.printStackTrace();
      }
      int errorType = ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
      Crashlytics.logException(SyncErrors.build(errorType, ex));
      PublishInventoryItemResponse response =
          (PublishInventoryItemResponse) ex.getBodyAs(PublishInventoryItemResponse.class);
      if (response != null && response.error != null) {
        setError(getError(response));
      }
      if (getError() == null) {
        setError(ex.getMessage());
      }
    }
    return false;
  }

  private String getError(PublishInventoryItemResponse response) {
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
