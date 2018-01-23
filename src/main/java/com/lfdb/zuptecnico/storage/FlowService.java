package com.lfdb.zuptecnico.storage;

import com.lfdb.zuptecnico.entities.Flow;

import java.util.ArrayList;
import java.util.List;

public class FlowService extends BaseService {
    public FlowService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("flow_items");
    }

    public int getFlowCount() {
        List<String> ids = getObjectList("flow_items", String.class);
        if (ids == null)
            return 0;

        return ids.size();
    }

    public boolean hasFlow(int id, int version) {
        List<String> ids = getObjectList("flow_items", String.class);
        if (ids == null) {
            return false;
        }
        return ids.contains(String.valueOf(id) + "_" + version);
    }

    public Flow getFlowAtIndex(int index) {
        List<String> ids = getObjectList("flow_items", String.class);
        if (ids == null || index >= ids.size())
            return null;

        String[] id = ids.get(index).split("_");
        return getFlow(Integer.parseInt(id[0]), Integer.parseInt(id[1]));
    }

    public Flow getFlow(int id, int version) {
        Flow item = getObject("flow_item_" + id + "_version_" + version, Flow.class);
        return item;
    }

    public void addFlow(Flow item) {
        List<String> ids = getObjectList("flow_items", String.class);
        if (ids == null) {
            ids = new ArrayList<>();
        }
        if (item.version_id == 0) {
            item.version_id = 1;
        }

        String itemId = String.valueOf(item.id) + "_" + item.version_id;
        if (!ids.contains(itemId)) {
            ids.add(itemId);
            setList("flow_items", ids);
        }

        saveFlow(item);
    }

    public Flow[] getFlows() {
        Flow[] flows = new Flow[getFlowCount()];
        for (int index = 0; index < getFlowCount(); index++) {
            flows[index] = getFlowAtIndex(index);
        }
        return flows;
    }

    public void deleteFlow(int id, int version) {
        deleteObject("flow_item_" + id + "_version_" + version);
        List<String> ids = getObjectList("flow_items", String.class);
        if (ids == null) {
            return;
        }

        String itemId = String.valueOf(id) + "_" + version;
        if (ids.contains(itemId)) {
            ids.remove(itemId);
            setList("flow_items", ids);
        }
    }

    private void saveFlow(Flow item) {
        setObject("flow_item_" + item.id + "_version_" + item.version_id, item);
    }

    public void updateFlow(int id, Integer version, Flow item) {
        if (hasFlow(id, version)) {
            setObject("flow_item_" + item.id + "_version_" + item.version_id, item);
        } else {
            addFlow(item);
        }
    }
}
