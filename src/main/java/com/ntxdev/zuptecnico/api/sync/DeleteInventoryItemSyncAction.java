package com.ntxdev.zuptecnico.api.sync;

import android.os.Parcel;

import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.api.errors.SyncErrors;
import com.ntxdev.zuptecnico.entities.responses.DeleteInventoryItemResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit.RetrofitError;

public class DeleteInventoryItemSyncAction extends SyncAction implements InventorySyncAction {
    public int categoryId;
    public int itemId;

    public static final Creator<DeleteInventoryItemSyncAction> CREATOR = new Creator<DeleteInventoryItemSyncAction>() {
        @Override
        public DeleteInventoryItemSyncAction createFromParcel(Parcel source) {
            return new DeleteInventoryItemSyncAction(source);
        }

        @Override
        public DeleteInventoryItemSyncAction[] newArray(int size) {
            return new DeleteInventoryItemSyncAction[size];
        }
    };

    public DeleteInventoryItemSyncAction() {
        super();
    }

    public DeleteInventoryItemSyncAction(Parcel in) {
        super(in);
        categoryId = in.readInt();
        itemId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(categoryId);
        dest.writeInt(itemId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public DeleteInventoryItemSyncAction(JSONObject object, ObjectMapper mapper) throws JSONException {
        this.categoryId = object.getInt("categoryId");
        this.itemId = object.getInt("item_id");
        this.inventory_item_id = this.itemId;

        if (object.has("error"))
            setError(object.getString("error"));
    }

    public DeleteInventoryItemSyncAction(int categoryId, int id) {
        this.categoryId = categoryId;
        this.itemId = id;
        this.inventory_item_id = id;
    }

    public boolean onPerform() {
        try {
            DeleteInventoryItemResponse result = Zup.getInstance().getService().deleteInventoryItem(categoryId, itemId);
            if (result == null) {
                return false;
            }
            if (result.error != null) {
                setError(result.error);
                return false;
            }
            Zup.getInstance().getInventoryItemService().deleteInventoryItem(itemId);
            return true;
        } catch (
                RetrofitError ex) {
            try {
                Crashlytics.setString("request", serialize().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            int errorType =  ex.getResponse() != null ? ex.getResponse().getStatus() : 0;
            Crashlytics.logException(SyncErrors.build(errorType, ex));
            setError(ex.getMessage());
            return false;
        }
    }

    @Override
    protected JSONObject serialize() throws JSONException {
        JSONObject result = new JSONObject();
        result.put("categoryId", categoryId);
        result.put("item_id", itemId);
        result.put("error", getError());

        return result;
    }
}
