package com.particity.zuptecnico.storage;

import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.ReportHistoryItem;
import com.particity.zuptecnico.entities.ReportItem;
import com.particity.zuptecnico.entities.collections.ReportHistoryItemCollection;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Igor on 8/3/2015.
 */
public class ReportItemService extends BaseService {
    public ReportItemService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("report_items");
    }

    public int getReportItemCount() {
        List<Integer> ids = getObjectList("report_items", Integer.class);
        if(ids == null)
            return 0;

        return ids.size();
    }

    public boolean hasReportItem(int id) {
        try {
            List<Integer> ids = getObjectList("report_items", Integer.class);
            if (ids == null)
                return false;

            return ids.contains(id);
        } catch (Exception e) {
            return false;
        }
    }

    public ReportItem getReportItemAtIndex(int index) {
        List<Integer> ids = getObjectList("report_items", Integer.class);
        if(ids == null || index >= ids.size())
            return null;

        int id = ids.get(index);
        return getReportItem(id);
    }

    public ReportItem getReportItem(int id) {
        ReportItem item = getObject("report_item_" + id, ReportItem.class);
        if (item == null) {
            return null;
        }
        prepareItemToRead(item);
        return item;
    }

    public List<ReportItem> getReportItems(){
        List<ReportItem> items = new ArrayList<ReportItem>();
        for(int index = 0; index < getReportItemCount(); index++){
            items.add(getReportItemAtIndex(index));
        }
        return items;
    }

    public void addReportItem(ReportItem item) {
        List<Integer> ids = getObjectList("report_items", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(item.id)) {
            ids.add(item.id);
            setList("report_items", ids);
        }

        saveReportItem(item);
    }

    public void deleteReportItem(int id) {
        deleteObject("report_item_" + id);
        List<Integer> ids = getObjectList("report_items", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove((Integer) id);
            setList("report_items", ids);
        }
    }

    private void saveReportItem(ReportItem item) {
        // Temporarily remove some properties we don't want to save
        prepareItemToStore(item);
        setObject("report_item_" + item.id, item);

        // Restore removed properties
        prepareItemToRead(item);
    }

    public void setReportItemHistory(int itemId, ReportHistoryItem[] histories) {
        ReportHistoryItemCollection collection = new ReportHistoryItemCollection();
        collection.histories = histories;

        setObject("report_item_" + itemId + "_histories", collection);
    }

    public ReportHistoryItem[] getReportItemHistory(int itemId) {
        ReportHistoryItemCollection collection = getObject("report_item_" + itemId + "_histories", ReportHistoryItemCollection.class);
        if(collection == null)
            return null;

        return collection.histories;
    }

    private void prepareItemToStore(ReportItem item) {
        if(item == null)
            return;

        if(item.assignedUser != null) {
            item.assignedUserId = item.assignedUser.id;
            Zup.getInstance().getUserService().addUser(item.assignedUser);
            item.assignedUser = null;
        }
        else {
            item.assignedUserId = 0;
        }

        if(item.assignedGroup != null) {
            item.assignedGroupId = item.assignedGroup.getId();
            Zup.getInstance().getGroupService().addGroup(item.assignedGroup);
            item.assignedGroup = null;
        }
        else {
            item.assignedGroupId = 0;
        }

        if(item.user != null) {
            item.userId = item.user.id;
            Zup.getInstance().getUserService().addUser(item.user);
            item.user = null;
        }
        else {
            item.userId = 0;
        }

        if(item.reporter != null) {
            item.reporterId = item.reporter.id;
            Zup.getInstance().getUserService().addUser(item.reporter);
            item.reporter = null;
        }
        else {
            item.reporterId = 0;
        }
    }

    private void prepareItemToRead(ReportItem item) {
        if(item == null)
            return;

        if(item.assignedUserId != -1) {
            item.assignedUser = Zup.getInstance().getUserService().getUser(item.assignedUserId);
        }
        if(item.assignedGroupId != -1) {
            item.assignedGroup = Zup.getInstance().getGroupService().getGroup(item.assignedGroupId);
        }
        if(item.userId != -1) {
            item.user = Zup.getInstance().getUserService().getUser(item.userId);
        }
        if(item.reporterId != -1) {
            item.reporter = Zup.getInstance().getUserService().getUser(item.reporterId);
        }
    }
}
