package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryCategoryStatus;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.collections.InventoryItemCollection;
import com.lfdb.zuptecnico.util.ItemsAdapterListener;
import com.lfdb.zuptecnico.util.Utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

public class InventoryItemsAdapter extends BaseAdapter
    implements Callback<InventoryItemCollection>, AbsListView.OnScrollListener {
  public enum Order {
    ModificationDate("updated_at"),
    CreationDate("created_at"),
    Title("id");

    private String mField;

    Order(String field) {
      mField = field;
    }

    @Override public String toString() {
      return mField;
    }
  }

  public List<InventoryItem> mItems;
  private SparseArray<ViewGroup> viewCache;
  public int mPage = 1;
  public boolean mMoreItemsAvailable;
  private View mLoadingView;
  private HashMap<String, Object> filterOptions;

  private String mQuery;
  public int mCategoryId;
  private int mStatusId = -1;
  private Order mOrder = Order.CreationDate;
  private String mSort = "DESC";
  private Context context;
  private HashMap<Integer, InventoryItem> mSelectedItems;
  private ItemsAdapterListener listener;
  private HashMap<Integer, InventoryCategory> categories;

  public void removeSelection() {
    mSelectedItems = new HashMap<>();
    notifyDataSetChanged();
  }

  public void toggleSelection(int position) {
    if (mSelectedItems.containsKey(position)) {
      mSelectedItems.remove(position);
    } else {
      InventoryItem item = getItem(position);
      mSelectedItems.put(position, item);
    }
    notifyDataSetChanged();
  }

  public void setFilterOptions(HashMap<String, Object> filterOptions) {
    this.filterOptions = filterOptions;
  }

  public boolean isFiltered() {
    return mQuery != null || filterOptions != null;
  }

  public ItemsAdapterListener getListener() {
    return listener;
  }

  public void setListener(ItemsAdapterListener listener) {
    this.listener = listener;
  }

  public int getSelectedCount() {
    return mSelectedItems.size();
  }

  public HashMap<Integer, InventoryItem> getSelectedIds() {
    return mSelectedItems;
  }

  public InventoryItemsAdapter(Context context) {
    mCategoryId = -1;
    this.context = context;
    init();
  }

  public InventoryItemsAdapter(Context context, int mCategoryId) {
    this.mCategoryId = mCategoryId;
    this.context = context;
    init();
  }

  private void init() {
    viewCache = new SparseArray<>();
    mItems = new ArrayList<>();
    mSelectedItems = new HashMap<>();
    mMoreItemsAvailable = false;
    categories = new HashMap<>();
    InventoryCategory[] iterator =
        Zup.getInstance().getInventoryCategoryService().getInventoryCategories();
    if (iterator != null && iterator.length > 0) {
      int length = iterator.length;
      for (int i = 0; i < length; i++) {
        InventoryCategory category = iterator[i];
        categories.put(category.id, category);
      }
    }
  }

  public void setCategoryId(int categoryId) {
    mCategoryId = categoryId;
    reset();
  }

  public void setQuery(String query) {
    if (TextUtils.isEmpty(query)) {
      mQuery = null;
    } else {
      mQuery = query;
    }
    reset();
  }

  public void setStatusId(int statusId) {
    mStatusId = statusId;
    reset();
  }

  public void clearStatusId() {
    setStatusId(-1);
  }

  public int getStatusId() {
    return mStatusId;
  }

  public void setOrder(Order order) {
    if (mOrder.equals(order)) {
      if (mSort.equals("ASC")) {
        mSort = "DESC";
      } else {
        mSort = "ASC";
      }
    } else {
      mSort = "ASC";
    }

    mOrder = order;
    reset();
  }

  public void reset() {
    mMoreItemsAvailable = false;
    mItems.clear();
    notifyDataSetInvalidated();
    mPage = 1;

    loadAnotherPage();
  }

  protected void loadAnotherPage() {
    if (!Utilities.isConnected(context)) {
      if (listener != null) {
        listener.onNetworkError();
      }
      return;
    }
    try {
      HashMap<String, Object> options =
          isFiltered() && filterOptions != null ? filterOptions : new HashMap<String, Object>();
      options.put("sort", mOrder);
      options.put("order", mSort);

      if (!TextUtils.isEmpty(mQuery)) {
        Zup.getInstance().getService().searchInventoryItems(mQuery, mPage, options, this);
      } else if (mCategoryId != -1) {
        Zup.getInstance().getService().searchInventoryItems(mCategoryId, mPage, options, this);
      } else {
        Zup.getInstance().getService().searchInventoryItems(mPage, options, this);
      }
    } catch (Exception ex) {
      Log.e("Retrofit", "Could not load inventory items", ex);
    }
  }

  @Override
  public void success(InventoryItemCollection inventoryItemCollection, Response response) {
    if (inventoryItemCollection.items == null) {
      mMoreItemsAvailable = false;
      if (listener != null) listener.onNetworkError();
      this.notifyDataSetChanged();
      return;
    }

    if (mQuery != null) {
      String[] urlParameters = response.getUrl().split("query=");
      String queryUrl = urlParameters.length > 1 ? urlParameters[1].split("&")[0] : mQuery;
      try {
        queryUrl = URLDecoder.decode(queryUrl, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      if (!queryUrl.equals(mQuery)) {
        return;
      }
    }

    int totalList = 0;
    int perPage = 25;

    List<Header> headerList = response.getHeaders();
    for (Header header : headerList) {
      if (header.getName().equals("Total")) {
        totalList = Integer.parseInt(header.getValue());
      }
      if (header.getName().equals("Per-Page")) {
        perPage = Integer.parseInt(header.getValue());
      }
    }

    if (mPage == 1 && inventoryItemCollection.items.length == 0) {
      if (listener != null) {
        listener.onEmptyResultsLoaded();
      }
      return;
    }

    for (InventoryItem item : inventoryItemCollection.items) {
      this.mItems.add(item);
    }

    mPage++;
    if (listener != null) {
      listener.onItemsLoaded();
    }
    mMoreItemsAvailable = totalList >= perPage;
    this.notifyDataSetChanged();
  }

  @Override public void failure(RetrofitError error) {
    if (listener != null) {
      listener.onNetworkError();
    }
  }

  @Override public int getCount() {
    int count = mItems.size();
    if (mMoreItemsAvailable) count++;

    return count;
  }

  @Override public InventoryItem getItem(int i) {
    if (mMoreItemsAvailable && i == getCount() - 1) {
      return null;
    }
    return mItems.get(i);
  }

  @Override public long getItemId(int i) {
    if (getItem(i) != null) {
      return getItem(i).id;
    }
    return 0;
  }

  @Override public void onScrollStateChanged(AbsListView absListView, int i) {

  }

  @Override public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    int position = absListView.getLastVisiblePosition();
    if (mMoreItemsAvailable && position == getCount() - 1) { // loading
      loadAnotherPage();
    }
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    LayoutInflater inflater =
        (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    if (mMoreItemsAvailable && position == getCount() - 1) { // loading
      if (mLoadingView != null) {
        return mLoadingView;
      } else {
        return mLoadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
      }
    }

    InventoryItem item = getItem(position);
    ViewGroup root;
    if (viewCache.get(position, null) != null) {
      root = viewCache.get(position);
      fillData(root, item);
    } else {
      root = (ViewGroup) inflater.inflate(R.layout.inventory_list_item, parent, false);
      viewCache.put(position, root);
      fillData(root, item);
    }

    root.setBackgroundColor(
        mSelectedItems.containsKey(position) ? ContextCompat.getColor(context, R.color.zupblue_half)
            : Color.TRANSPARENT);
    return root;
  }

  private void fillData(ViewGroup view, InventoryItem item) {
    ImageView mCategoryImage = (ImageView) view.findViewById(R.id.inventory_category_image);
    TextView mTitle = (TextView) view.findViewById(R.id.fragment_inventory_item_title);
    TextView mAddress = (TextView) view.findViewById(R.id.inventory_item_address);
    TextView mDescription = (TextView) view.findViewById(R.id.fragment_inventory_item_desc);
    TextView mStatus = (TextView) view.findViewById(R.id.fragment_inventory_item_statedesc);
    View mDownloadIcon = view.findViewById(R.id.fragment_inventory_item_download_icon);
    TextView txtCaseStepResponsible = (TextView) view.findViewById(R.id.related_case_user);

    if (TextUtils.isEmpty(item.title) && Zup.getInstance()
        .getSyncActionService()
        .hasSyncActionRelatedToInventoryItem(item.id)) {
      mTitle.setText(context.getString(R.string.waiting_sync));
      mTitle.setTextColor(ContextCompat.getColor(context, R.color.waiting_sync_action_color));
    } else {
      mTitle.setText(item.title);
      mTitle.setTextColor(ContextCompat.getColor(context, R.color.comment_item_text));
    }
    mAddress.setText(item.address);
    mDescription.setText(context.getString(R.string.inclusion_date_title) + Zup.getInstance()
        .formatIsoDate(item.created_at));
    mDownloadIcon.setVisibility(
        Zup.getInstance().getInventoryItemService().hasInventoryItem(item.id) ? View.VISIBLE
            : View.GONE);

    InventoryCategory category = categories.get(item.inventory_category_id);
    InventoryCategoryStatus status = null;
    if (category != null) {
      category.loadImageInto(mCategoryImage);
      status = category.getStatus(item.inventory_status_id);
    } else {
      mCategoryImage.setVisibility(View.INVISIBLE);
      mStatus.setVisibility(View.GONE);
    }

    if (status != null) {
      mStatus.setText(status.title);
      mStatus.setTextColor(status.getColor());
    } else {
      mStatus.setVisibility(View.GONE);
    }
    boolean hasName = false;
    if (item.relatedEntities != null
        && item.relatedEntities.cases != null
        && item.relatedEntities.cases.length > 0) {
      Case lastCase = item.relatedEntities.cases[0];
      if (lastCase.getStatus() != null && !lastCase.getStatus().equals("finished")) {
        Case.Step currentStep = lastCase.getCurrentStep();
        if (currentStep != null
            && currentStep.responsableUser != null
            && currentStep.responsableUser.name != null) {
          txtCaseStepResponsible.setVisibility(View.VISIBLE);
          txtCaseStepResponsible.setText("Responsável atual: " + currentStep.responsableUser.name);
          hasName = true;
        }
      }
    }

    if (!hasName) {
      txtCaseStepResponsible.setVisibility(View.GONE);
    }
  }
}
