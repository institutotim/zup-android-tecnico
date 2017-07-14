package com.ntxdev.zuptecnico.storage;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

/**
 * Created by Igor on 8/3/2015.
 */
public class StorageServiceManager {
  private DB mDB;
  private Context mContext;

  private GroupService mGroupService;
  private ReportCategoryService mReportCategoryService;
  private ReportItemService mReportItemService;
  private UserService mUserService;
  private CaseItemService mCaseItemService;
  private FlowService mFlowService;
  private InventoryCategoryService mInventoryCategoryService;
  private InventoryItemService mInventoryItemService;
  private SyncActionService mSyncActionService;
  private NamespaceService mNamespaceService;

  public StorageServiceManager(Context context) throws SnappydbException {
    this.mContext = context;

    this.mGroupService = new GroupService(this);
    this.mReportCategoryService = new ReportCategoryService(this);
    this.mReportItemService = new ReportItemService(this);
    this.mUserService = new UserService(this);
    this.mCaseItemService = new CaseItemService(this);
    this.mFlowService = new FlowService(this);
    this.mInventoryCategoryService = new InventoryCategoryService(this);
    this.mInventoryItemService = new InventoryItemService(this);
    this.mSyncActionService = new SyncActionService(this);
    this.mNamespaceService = new NamespaceService(this);

    mDB = DBFactory.open(mContext);
  }

  public void clear() {
    this.mGroupService.clear();
    this.mReportCategoryService.clear();
    this.mReportItemService.clear();
    this.mUserService.clear();
    this.mCaseItemService.clear();
    this.mFlowService.clear();
    this.mInventoryCategoryService.clear();
    this.mInventoryItemService.clear();
    this.mNamespaceService.clear();
    //this.mSyncActionService.clear();
  }

  public void close() throws SnappydbException {
    mDB.close();
  }

  protected void commit() throws SnappydbException {
    mDB.close();
    mDB = DBFactory.open(mContext);
  }

  protected DB getDB() {
    return mDB;
  }

  public Context getContext() {
    return mContext;
  }

  public GroupService group() {
    return mGroupService;
  }

  public ReportCategoryService reportCategory() {
    return mReportCategoryService;
  }

  public InventoryCategoryService inventoryCategory() {
    return mInventoryCategoryService;
  }

  public CaseItemService caseItem() {
    return mCaseItemService;
  }

  public ReportItemService reportItem() {
    return mReportItemService;
  }

  public InventoryItemService inventoryItem() {
    return mInventoryItemService;
  }

  public UserService user() {
    return mUserService;
  }

  public FlowService flow() {
    return mFlowService;
  }

  public SyncActionService action() {
    return mSyncActionService;
  }

  public NamespaceService namespaceService() {
    return mNamespaceService;
  }
}
