package com.lfdb.zuptecnico.api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.activities.inventory.CreateInventoryItemActivity;
import com.lfdb.zuptecnico.api.sync.SyncAction;
import com.lfdb.zuptecnico.config.Constants;
import com.lfdb.zuptecnico.config.InternalConstants;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.Namespace;
import com.lfdb.zuptecnico.entities.Session;
import com.lfdb.zuptecnico.entities.User;
import com.lfdb.zuptecnico.storage.CaseItemService;
import com.lfdb.zuptecnico.storage.FlowService;
import com.lfdb.zuptecnico.storage.GroupService;
import com.lfdb.zuptecnico.storage.InventoryCategoryService;
import com.lfdb.zuptecnico.storage.InventoryItemService;
import com.lfdb.zuptecnico.storage.NamespaceService;
import com.lfdb.zuptecnico.storage.ReportCategoryService;
import com.lfdb.zuptecnico.storage.ReportItemService;
import com.lfdb.zuptecnico.storage.StorageServiceManager;
import com.lfdb.zuptecnico.storage.SyncActionService;
import com.lfdb.zuptecnico.storage.UserService;
import com.lfdb.zuptecnico.tasks.InventoryItemLoaderTask;
import com.lfdb.zuptecnico.tasks.InventoryItemTitleLoaderTask;
import com.lfdb.zuptecnico.tasks.UserLoaderTask;
import com.snappydb.SnappydbException;
import com.squareup.okhttp.OkHttpClient;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.JacksonConverter;

public class Zup {
  public class BitmapResource {
    public String url;
    public int id;
    public Bitmap bitmap;
    public boolean loaded;
  }

  private ObjectMapper objectMapper;

  private boolean isSyncing = false;
  private SimpleDateFormat dateFormat;
  private static Zup instance;
  private ArrayList<BitmapResource> bitmaps;

  private String sessionToken;
  private int sessionUserId;
  private Integer namespaceId;
  private String namespaceName;

  private StorageServiceManager storageServiceManager;

  private ZupService service;
  private ZupAccess access;

  private Zup() {
    this.objectMapper = new ObjectMapper();
    this.bitmaps = new ArrayList<>();
    this.dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    OkHttpClient client = new OkHttpClient();
    client.setConnectTimeout(InternalConstants.CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    client.setReadTimeout(InternalConstants.READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    OkClient okClient = new OkClient(client);

    RestAdapter adapter = new RestAdapter.Builder().setEndpoint(Constants.API_URL)
        .setRequestInterceptor(new RequestInterceptor() {
          @Override public void intercept(RequestFacade requestFacade) {
            if (Zup.getInstance().hasSessionToken()) {
              requestFacade.addHeader("X-App-Token", sessionToken);
            }
            if (Zup.getInstance().hasNamespaceId()) {
              requestFacade.addHeader("X-App-Namespace", String.valueOf(namespaceId));
            }
          }
        })
        .setConverter(new JacksonConverter())
        .setClient(okClient)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .build();

    this.service = adapter.create(ZupService.class);
  }

  public ObjectMapper getObjectMapper() {
    return this.objectMapper;
  }

  public ZupService getService() {
    return this.service;
  }

  public ReportCategoryService getReportCategoryService() {
    return this.storageServiceManager.reportCategory();
  }

  public NamespaceService getNamespaceService() {
    return this.storageServiceManager.namespaceService();
  }

  public InventoryCategoryService getInventoryCategoryService() {
    return this.storageServiceManager.inventoryCategory();
  }

  public SyncActionService getSyncActionService() {
    return this.storageServiceManager.action();
  }

  public FlowService getFlowService() {
    return this.storageServiceManager.flow();
  }

  public CaseItemService getCaseItemService() {
    return this.storageServiceManager.caseItem();
  }

  public ReportItemService getReportItemService() {
    return this.storageServiceManager.reportItem();
  }

  public UserService getUserService() {
    return this.storageServiceManager.user();
  }

  public GroupService getGroupService() {
    return this.storageServiceManager.group();
  }

  public InventoryItemService getInventoryItemService() {
    return this.storageServiceManager.inventoryItem();
  }

  public void clearStorage(Context context) {
    this.sessionToken = null;
    this.sessionUserId = 0;
    this.namespaceId = null;
    this.namespaceName = null;
    //this.syncActions.clear();
    clearSharedPrefs(context);

    storageServiceManager.clear();
  }

  public void initStorage(Context context) {
    try {
      storageServiceManager = new StorageServiceManager(context);
    } catch (SnappydbException ex) {
      // What should we do?
    }

    sessionToken = getSessionToken(context);
    sessionUserId = getSessionUserId(context);
    namespaceId = getNamespaceId(context);
    namespaceName = getNamespaceName(context);
    refreshAccess();
  }

  private String getSessionToken(Context context) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE);
    return sharedPref.getString("token", null);
  }

  private int getSessionUserId(Context context) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE);
    return sharedPref.getInt("id", -1);
  }

  private int getNamespaceId(Context context) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE);
    return sharedPref.getInt("namespace_id", -1);
  }

  public String getNamespaceName(Context context) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE);
    return sharedPref.getString("namespace_name", "");
  }

  public void refreshAccess() {
    if (sessionUserId == -1) {
      return;
    }
    this.access = new ZupAccess(getUserService().getUser(sessionUserId));
  }

  public void close() {
    try {
      this.storageServiceManager.close();
    } catch (SnappydbException ex) {
      Log.e("Snappydb", "Could not close db", ex);
    }
  }

  public void setSession(Context context, Session session) {
    SharedPreferences.Editor sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE).edit();

    this.sessionToken = session.token;
    this.sessionUserId = session.user.id;
    if (session.user.namespace != null) {
      this.namespaceId = session.user.namespace.getId();
      this.namespaceName = session.user.namespace.getName();
      sharedPref.putInt("namespace_id", namespaceId);
      sharedPref.putString("namespace_name", namespaceName);
    }
    sharedPref.putString("token", session.token);
    sharedPref.putInt("id", session.user.id);

    sharedPref.apply();
    this.refreshAccess();
  }

  private void clearSharedPrefs(Context context) {
    SharedPreferences.Editor sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE).edit();
    sharedPref.clear();
    sharedPref.apply();
  }

  public void setHasFullLoad(Context context) {
    SharedPreferences.Editor sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE).edit();

    sharedPref.putBoolean("full_load", true);
    sharedPref.apply();
  }

  public boolean hasFullLoad(Context context) {
    SharedPreferences sharedPref =
        context.getSharedPreferences("session_data", Context.MODE_PRIVATE);
    return sharedPref.getBoolean("full_load", false);
  }

  public ZupAccess getAccess() {
    return this.access;
  }

  public boolean hasSessionToken() {
    return this.sessionToken != null;
  }

  public boolean hasNamespaceId() {
    return this.namespaceId != null && namespaceId != -1;
  }

  public void setNamespace(Namespace namespace) {
    if (namespace == null) {
      return;
    }
    namespaceId  = namespace.getId();
    namespaceName = namespace.getName();
  }

  public void clearSessionToken() {
    this.sessionToken = null;
    SharedPreferences.Editor sharedPref = ZupApplication.getContext()
        .getSharedPreferences("session_data", Context.MODE_PRIVATE)
        .edit();

    sharedPref.putString("token", null);
    sharedPref.apply();
  }

  public SimpleDateFormat getDateFormat() {
    return dateFormat;
  }

  public String formatIsoDate(String isoDate) {
    if (isoDate == null) return "";

    try {
      ISO8601DateFormat fmt = new ISO8601DateFormat();
      Date date = fmt.parse(isoDate);

      return getDateFormat().format(date);
    } catch (ParseException ex) {
      return isoDate;
    }
  }

  public Date getIsoDate(String isoDate) {
    try {
      ISO8601DateFormat fmt = new ISO8601DateFormat();
      Date date = fmt.parse(isoDate);

      return date;
    } catch (ParseException ex) {
      return null;
    }
  }

  public static String getIsoDate(Date date) {
    ISO8601DateFormat fmt = new ISO8601DateFormat();
    return fmt.format(date);
  }

  public static Zup getInstance() {
    if (instance == null) {
      instance = new Zup();
    }

    return instance;
  }

  public int getSessionUserId() {
    return sessionUserId;
  }

  public User getSessionUser() {
    return getUserService().getUser(sessionUserId);
  }

  private BitmapResource getResource(int id) {
    for (int i = 0; i < bitmaps.size(); i++) {
      if (bitmaps.get(i).id == id) {
        return bitmaps.get(i);
      }
    }

    return null;
  }

  public Bitmap getBitmap(int id) {
    BitmapResource resource = getResource(id);
    if (resource == null || !resource.loaded) return null;

    return resource.bitmap;
  }

  public InventoryItem createInventoryItem() {
    InventoryItem item = new InventoryItem();
    item.isLocal = true;
    item.created_at = getIsoDate(Calendar.getInstance().getTime());
    int fakeId = CreateInventoryItemActivity.FAKE_MIN_ID;
    do {
      fakeId++;
    } while (Zup.getInstance().getInventoryItemService().hasInventoryItem(fakeId));
    item.id = fakeId;
    item.isLocal = true;
    getInventoryItemService().addInventoryItem(item);

    return item;
  }

  public void performSyncAction(final SyncAction action) {
    if (this.isSyncing) return;

    broadcastAction(SyncAction.ACTION_SYNC_BEGIN);

    this.isSyncing = true;
    new Thread(new Runnable() {
      @Override public void run() {
        if (action.perform()) {
          getSyncActionService().updateSyncAction(action);
        }

        isSyncing = false;
        broadcastAction(SyncAction.ACTION_SYNC_END);
      }
    }).start();
  }

  public void sync() {
    if (this.isSyncing) return;

    broadcastAction(SyncAction.ACTION_SYNC_BEGIN);

    this.isSyncing = true;
    new Thread(new Runnable() {
      @Override public void run() {
        ArrayList<SyncAction> actionsToRemove = new ArrayList<SyncAction>();

        List<SyncAction> actions = getSyncActionService().getUnsuccesfullSyncActions();
        int size = actions == null ? 0 : actions.size();
        for (int index = 0; index < size; index++) {
          SyncAction action = actions.get(index);
          if (action.perform()) {
            actionsToRemove.add(action);
          }
        }

        for (SyncAction action : actionsToRemove) {
          getSyncActionService().updateSyncAction(action);
        }

        isSyncing = false;
        broadcastAction(SyncAction.ACTION_SYNC_END);
      }
    }).start();
  }

  public boolean isSyncing() {
    return isSyncing;
  }

  public void updateSyncAction(SyncAction action) {
    getSyncActionService().updateSyncAction(action);
  }

  void broadcastAction(String action) {
    if (ZupApplication.getContext() == null) return;

    Intent intent = new Intent(action);

    LocalBroadcastManager manager = LocalBroadcastManager.getInstance(ZupApplication.getContext());
    manager.sendBroadcast(intent);
  }

  public void showUsernameInto(TextView textView, String prefix, int userId) {
    UserLoaderTask task = new UserLoaderTask(textView, prefix);
    task.execute(userId);
  }

  public void showInventoryItemInto(TextView textView, String prefix, List<Integer> itemsId) {
    InventoryItemTitleLoaderTask task = new InventoryItemTitleLoaderTask(textView, prefix, itemsId);
    task.execute();
  }

  public void loadInventoryItem(InventoryItemLoaderTask.ItemLoadedListener listener,
      TextView textView, String prefix, int itemId) {
    InventoryItemLoaderTask task = new InventoryItemLoaderTask(listener, textView, prefix, itemId);
    task.execute();
  }

  public int getCaseStatusDrawable(String status) {
    if (status.equals("pending")) {
      return R.drawable.documentos_lista_status_icon_pendente;
    } else if (status.equals("active")) {
      return R.drawable.documentos_lista_status_icon_andamento;
    } else if (status.equals("finished")) {
      return R.drawable.documentos_lista_status_icon_concluido;
    } else {
      return R.drawable.documentos_lista_status_icon_sync;
    }
  }

  public int getCaseStatusColor(Context context, String status) {
    if (status.equals("pending")) {
      return ContextCompat.getColor(context, R.color.pending_action_color);
    } else if (status.equals("active")) {
      return ContextCompat.getColor(context, R.color.running_action_color);
    } else if (status.equals("finished")) {
      return ContextCompat.getColor(context, R.color.completed_action_color);
    } else {
      return ContextCompat.getColor(context, R.color.error_action_color);
    }
  }

  public String getCaseStatusString(Context context, String status) {
    if (status.equals("pending")) {
      return context.getString(R.string.pending_title);
    } else if (status.equals("active")) {
      return context.getString(R.string.in_execution);
    } else if (status.equals("finished")) {
      return context.getString(R.string.done);
    } else {
      return context.getString(R.string.waiting_sync);
    }
  }
}
