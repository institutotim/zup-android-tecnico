package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.api.sync.CaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.DeleteInventoryItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.EditReportItemSyncAction;
import com.ntxdev.zuptecnico.api.sync.FillCaseStepSyncAction;
import com.ntxdev.zuptecnico.api.sync.FinishCaseSyncAction;
import com.ntxdev.zuptecnico.api.sync.InventorySyncAction;
import com.ntxdev.zuptecnico.api.sync.PublishOrEditInventorySyncAction;
import com.ntxdev.zuptecnico.api.sync.ReportSyncAction;
import com.ntxdev.zuptecnico.api.sync.SyncAction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 23/01/2016.
 */
public class SyncActionService extends BaseService {
    public SyncActionService(StorageServiceManager manager) {
        super(manager);
    }

    public void clear() {
        deleteObject("sync_actions");
        deleteObject("case_sync_actions");
        deleteObject("inventory_sync_actions");
        deleteObject("report_sync_actions");
        deleteObject("successful_sync_actions");
        deleteObject("unsuccessful_sync_actions");
    }

    private SyncAction getSyncActionAtIndex(String list, int index) {
        List<Integer> ids = getObjectList(list, Integer.class);
        if(ids == null || index >= ids.size())
            return null;

        int id = ids.get(index);
        return getSyncAction(id);
    }

    public SyncAction getSyncAction(int id) {
        SyncAction item = getObject("sync_action_" + id, SyncAction.class);
        return item;
    }

    public List<SyncAction> getSyncActions(){
        return getSyncActions("sync_actions");
    }

    private List<SyncAction> getSyncActions(String list) {
        List<SyncAction> items = new ArrayList<>();
        int index = getSyncActionsCount(list) -1;
        while (index >= 0) {
            SyncAction action = getSyncActionAtIndex(list, index);
            if (action == null) {
                index--;
                continue;
            }
            items.add(action);
            index--;
        }
        return items;
    }

    private int getSyncActionsCount(String list) {
        List<Integer> ids = getObjectList(list, Integer.class);
        if(ids == null)
            return 0;

        return ids.size();
    }

    public void addSyncAction(SyncAction item) {
        List<Integer> ids = getObjectList("sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if (item.getId() == 0) {
            do {
                int id = item.getId();
                id++;
                item.setId(id);
            } while (hasSyncAction(item.getId()));
        }
        if(!ids.contains(item.getId())) {
            ids.add(item.getId());
            setList("sync_actions", ids);
        }

        saveSyncAction(item);
    }

    public boolean hasSyncAction(int id) {
        try {
            List<Integer> ids = getObjectList("sync_actions", Integer.class);
            if (ids == null)
                return false;

            return ids.contains(id);
        } catch (Exception e) {
            return false;
        }
    }
    
    private void addSuccessfulSyncAction(Integer id) {
        List<Integer> ids = getObjectList("successful_sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(id)) {
            ids.add(id);
            setList("successful_sync_actions", ids);
        }
    }

    private void addUnsuccessfulSyncAction(Integer id) {
        List<Integer> ids = getObjectList("unsuccessful_sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(id)) {
            ids.add(id);
            setList("unsuccessful_sync_actions", ids);
        }
    }

    private void deleteSuccessfulSyncAction(Integer id) {
        List<Integer> ids = getObjectList("successful_sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove(id);
            setList("successful_sync_actions", ids);
        }
    }

    private void deleteUnsuccessfulSyncAction(Integer id) {
        List<Integer> ids = getObjectList("unsuccessful_sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove(id);
            setList("unsuccessful_sync_actions", ids);
        }
    }


    private void addInventorySyncAction(Integer id) {
        List<Integer> ids = getObjectList("inventory_sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(id)) {
            ids.add(id);
            setList("inventory_sync_actions", ids);
        }
    }

    private void deleteInventorySyncAction(Integer id) {
        List<Integer> ids = getObjectList("inventory_sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove(id);
            setList("inventory_sync_actions", ids);
        }
    }

    private void addReportSyncAction(Integer id) {
        List<Integer> ids = getObjectList("report_sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(id)) {
            ids.add(id);
            setList("report_sync_actions", ids);
        }
    }

    private void deleteReportSyncAction(Integer id) {
        List<Integer> ids = getObjectList("report_sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove(id);
            setList("report_sync_actions", ids);
        }
    }

    private void addCaseSyncAction(Integer id) {
        List<Integer> ids = getObjectList("case_sync_actions", Integer.class);
        if(ids == null)
            ids = new ArrayList<>();

        if(!ids.contains(id)) {
            ids.add(id);
            setList("case_sync_actions", ids);
        }
    }

    private void deleteCaseSyncAction(Integer id) {
        List<Integer> ids = getObjectList("case_sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove(id);
            setList("case_sync_actions", ids);
        }
    }

    public boolean hasSyncActionRelatedToInventoryItem(int id) {
        List<SyncAction> actions = getSyncActions("inventory_sync_actions");
        for (int i = 0; i < actions.size(); i++) {
            SyncAction action = actions.get(i);
            if (action instanceof PublishOrEditInventorySyncAction) {
                if (((PublishOrEditInventorySyncAction)action).item.id == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSyncActionRelatedToReportItem(int id) {
        List<SyncAction> actions = getSyncActions("report_sync_actions");
        for (int i = 0; i < actions.size(); i++) {
            SyncAction action = actions.get(i);
            if (action instanceof EditReportItemSyncAction) {
                if (((EditReportItemSyncAction)action).reportId == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSyncActionRelatedToCase(int id) {
        List<SyncAction> actions = getSyncActions("case_sync_actions");
        for (int i = 0; i < actions.size(); i++) {
            SyncAction action = actions.get(i);
            if (action instanceof FinishCaseSyncAction) {
                if (((FinishCaseSyncAction)action).caseId == id) {
                    return true;
                } else if (((FillCaseStepSyncAction)action).caseId == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<SyncAction> getSuccesfullSyncActions() {
        return getSyncActions("successful_sync_actions");
    }

    public List<SyncAction> getUnsuccesfullSyncActions() {
        return getSyncActions("unsuccessful_sync_actions");
    }

    public void updateSyncAction(SyncAction action) {
        addSyncAction(action);
        if (action.wasSuccessful()) {
            deleteUnsuccessfulSyncAction(action.getId());
            addSuccessfulSyncAction(action.getId());
            if (action instanceof InventorySyncAction) {
                deleteInventorySyncAction(action.getId());
            } else if (action instanceof ReportSyncAction) {
                deleteReportSyncAction(action.getId());
            } else if (action instanceof CaseSyncAction) {
                deleteCaseSyncAction(action.getId());
            }
        } else {
            deleteSuccessfulSyncAction(action.getId());
            addUnsuccessfulSyncAction(action.getId());
            if (action instanceof InventorySyncAction) {
                addInventorySyncAction(action.getId());
            } else if (action instanceof ReportSyncAction) {
                addReportSyncAction(action.getId());
            } else if (action instanceof CaseSyncAction) {
                addCaseSyncAction(action.getId());
            }
        }
    }

    public void clearSuccessfulItems() {
        List<SyncAction> actions = getSuccesfullSyncActions();
        int size = actions.size();
        for (int index = 0; index < size; index++) {
            removeSyncAction(actions.get(index).getId());
        }
    }

    public boolean hasPendingSyncActions() {
        return getSyncActionsCount("unsuccessful_sync_actions") > 0;
    }


    public void removeSyncAction(int id) {
        deleteObject("sync_action_" + id);
        prepareItemToRemove(id);
        List<Integer> ids = getObjectList("sync_actions", Integer.class);
        if(ids == null)
            return;

        if(ids.contains(id)) {
            ids.remove((Integer) id);
            setList("sync_actions", ids);
        }
    }

    private void saveSyncAction(SyncAction item) {
        // Add id due to type
        prepareItemToStore(item);
        setObject("sync_action_" + item.getId(), item);
    }


    private void prepareItemToStore(SyncAction item) {
        if(item == null)
            return;
        
        if (item.wasSuccessful()) {
            addSuccessfulSyncAction(item.getId());
        } else {
            addUnsuccessfulSyncAction(item.getId());
            if (item instanceof InventorySyncAction) {
                addInventorySyncAction(item.getId());
            } else if (item instanceof ReportSyncAction) {
                addReportSyncAction(item.getId());
            } else if (item instanceof CaseSyncAction) {
                addCaseSyncAction(item.getId());
            }
        }
    }
    
    private void prepareItemToRemove(int id) {
        deleteUnsuccessfulSyncAction(id);
        deleteSuccessfulSyncAction(id);
        deleteCaseSyncAction(id);
        deleteReportSyncAction(id);
        deleteInventorySyncAction(id);
    }

}
