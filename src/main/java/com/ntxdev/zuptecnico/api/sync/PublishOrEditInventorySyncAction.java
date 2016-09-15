package com.ntxdev.zuptecnico.api.sync;

import android.os.Parcel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ntxdev.zuptecnico.entities.InventoryItem;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 20/01/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
@JsonSubTypes({ @JsonSubTypes.Type(value = EditInventoryItemSyncAction.class, name = "EditInventoryItemSyncAction"),
        @JsonSubTypes.Type(value = PublishInventoryItemSyncAction.class, name = "PublishInventoryItemSyncAction") })
public abstract class PublishOrEditInventorySyncAction extends SyncAction implements InventorySyncAction {
    public InventoryItem item;

    public PublishOrEditInventorySyncAction(InventoryItem item) {
        this.item = item;
        this.inventory_item_id = item.id;
    }

    public PublishOrEditInventorySyncAction() {
        super();
    }

    public PublishOrEditInventorySyncAction(JSONObject object, ObjectMapper mapper) throws IOException {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser(object.toString());
        item = new InventoryItem();
        while (jParser.nextToken() != JsonToken.END_OBJECT) {

            String fieldname = jParser.getCurrentName();
            if (fieldname == null || fieldname.equals("item")) {
                continue;
            }
            jParser.nextToken();
            switch (fieldname) {
                case "id":
                    item.id = jParser.getIntValue();
                    inventory_item_id = item.id;
                    break;
                case "title":
                    item.title = jParser.getText();
                    break;
                case "position":
                    item.position = new InventoryItem.Coordinates();
                    while (jParser.nextToken() != JsonToken.END_OBJECT) {
                        String fieldObjectName = jParser.getCurrentName();
                        jParser.nextToken();
                        if (fieldObjectName.equals("latitude")) {
                            item.position.latitude = jParser.getFloatValue();
                        } else if (fieldObjectName.equals("longitude")) {
                            item.position.longitude = jParser.getFloatValue();
                        }
                    }
                    break;
                case "inventory_category_id":
                    String categoryId = jParser.getValueAsString();
                    if (categoryId != null) {
                        item.inventory_category_id = Integer.parseInt(categoryId);
                    }
                    break;
                case "inventory_status_id":
                    String id = jParser.getValueAsString();
                    if (id != null) {
                        item.inventory_status_id = Integer.parseInt(id);
                    }
                    break;
                case "address":
                    item.address = jParser.getText();
                    break;
                case "data":
                    item.data = new ArrayList<>();
                    TypeReference<List<InventoryItem.Data>> tRef = new TypeReference<List<InventoryItem.Data>>() {};
                    item.data = mapper.readValue(jParser, tRef);
                    break;
                case "error":
                    setError(jParser.getText());
                    break;
            }

        }
        jParser.close();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(item, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public PublishOrEditInventorySyncAction(Parcel in) {
        super(in);
        item = in.readParcelable(InventoryItem.class.getClassLoader());
    }

    @Override
    protected JSONObject serialize() throws Exception {
        JsonFactory jfactory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonGenerator jGenerator = jfactory.createGenerator(baos, JsonEncoding.UTF8);
        jGenerator.writeStartObject();

        jGenerator.writeObjectFieldStart("item");
        jGenerator.writeNumberField("id", item.id);
        jGenerator.writeStringField("title", item.title);

        jGenerator.writeObjectFieldStart("position");
        jGenerator.writeNumberField("latitude", item.position.latitude);
        jGenerator.writeNumberField("longitude", item.position.longitude);
        jGenerator.writeEndObject();

        if (item.inventory_category_id != null) {
            jGenerator.writeNumberField("inventory_category_id", item.inventory_category_id);
        } else {
            jGenerator.writeNullField("inventory_category_id");
        }
        if (item.inventory_status_id != null) {
            jGenerator.writeNumberField("inventory_status_id", item.inventory_status_id);
        } else {
            jGenerator.writeNullField("inventory_status_id");
        }
        jGenerator.writeStringField("address", item.address);

        jGenerator.writeArrayFieldStart("data");
        for (InventoryItem.Data data : item.data) {
            jGenerator.writeStartObject();

            jGenerator.writeNumberField("id", data.id);
            jGenerator.writeNumberField("inventory_field_id", data.inventory_field_id);
            jGenerator.writeRaw(",\"content\":");
            jGenerator.writeRaw(mapper.writeValueAsString(data.content));
            jGenerator.writeEndObject();
        }
        jGenerator.writeEndArray();

        jGenerator.writeEndObject();

        jGenerator.writeStringField("error", getError());

        jGenerator.writeEndObject();
        jGenerator.close();

        return new JSONObject(baos.toString());
    }
}
