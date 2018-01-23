package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.Case;
import com.lfdb.zuptecnico.entities.ReportCategory;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.entities.collections.ReportItemCollection;
import com.lfdb.zuptecnico.ui.WebImageView;
import com.lfdb.zuptecnico.util.ItemsAdapterListener;
import com.lfdb.zuptecnico.util.Utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Header;
import retrofit.client.Response;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportsAdapter extends BaseAdapter
    implements Callback<ReportItemCollection>, AbsListView.OnScrollListener {
  public static final String ORDER = "order";
  public static final String SORT = "sort";

  public enum Order {
    CreationDate("created_at"),
    Address("address");

    private String mField;

    Order(String field) {
      mField = field;
    }

    @Override public String toString() {
      return mField;
    }
  }

  private SparseArray<ViewGroup> viewCache;
  private boolean moreItemsAvailable;
  private View loadingView;
  private Map<String, Object> filterOptions;
  private int sizeOfRealList;

  private List<ReportItem> items;
  private int pageId; // next page that will be loaded
  private Order mOrder = Order.CreationDate;
  private String mSort = "DESC";

  private Context context;
  private ItemsAdapterListener listener;
  private String query;

  public String getSort() {
    return mSort;
  }

  public void setSort(String sort) {
    if (sort == null) {
      return;
    }
    mSort = sort;
    reset();
  }

  public String getOrder() {
    return mOrder.mField;
  }

  public void setQuery(String query) {
    if (query == null || query.trim().isEmpty()) {
      this.query = null;
    } else {
      this.query = query;
    }
    reset();
  }

  public void setOrder(Order order) {
    if (order == null) {
      return;
    }
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

  public void setOrder(String order) {
    if (order == null) {
      return;
    }
    if (order.equals(Order.Address.toString())) {
      mOrder = Order.Address;
    } else if (order.equals(Order.CreationDate.toString())) {
      mOrder = Order.CreationDate;
    }
    reset();
  }

  public String getQuery() {
    return query;
  }

  public int getSizeOfRealList() {
    if (isFiltered()) {
      return sizeOfRealList;
    }
    return 0;
  }

  @Override public void success(ReportItemCollection reportItemCollection, Response response) {
    sizeOfRealList = 0;
    int totalList = 0;
    int perPage = 25;
    if (query != null) {
      String[] urlParameters = response.getUrl().split("query=");
      String queryUrl = urlParameters.length > 1 ? urlParameters[1].split("&")[0] : query;
      try {
        queryUrl = URLDecoder.decode(queryUrl, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      if (!queryUrl.equals(query)) {
        return;
      }
    }

    List<Header> headerList = response.getHeaders();
    for (Header header : headerList) {
      if (header.getName().equals("Total")) {
        totalList = Integer.parseInt(header.getValue());
        if ((query != null && !query.isEmpty()) || (filterOptions != null)) {
          sizeOfRealList = Integer.parseInt(header.getValue());
        }
      }
      if (header.getName().equals("Per-Page")) {
        perPage = Integer.parseInt(header.getValue());
      }
    }
    ReportItem[] reportItems = reportItemCollection.reports;
    if (reportItems == null) {
      ReportsAdapter.this.setMoreItemsAvailable(false);
      if (listener != null) listener.onNetworkError();
      return;
    }

    if (reportItems.length == 0) {
      if (pageId == 1 && (filterOptions != null || (query != null && !query.isEmpty()))) {
        listener.onEmptyResultsLoaded();
        return;
      }
    }

    for (int i = 0; i < reportItems.length; i++) {
      items.add(reportItems[i]);
    }

    pageId = pageId + 1;

    if (listener != null) {
      listener.onItemsLoaded();
    }

    ReportsAdapter.this.setMoreItemsAvailable(totalList >= perPage);
    ReportsAdapter.this.notifyDataSetChanged();
  }

  @Override public void failure(RetrofitError error) {
    Log.e("RETROFIT", "Could not load reports list", error);
    listener.onNetworkError();
  }

  public void setListener(ItemsAdapterListener listener) {
    this.listener = listener;
  }

  protected ItemsAdapterListener getListener() {
    return this.listener;
  }

  public ReportsAdapter(Context context) {
    this.context = context;

    items = new ArrayList<>();
    viewCache = new SparseArray<>();
    moreItemsAvailable = false;
    pageId = 1;
  }

  public boolean isFiltered() {
    return query != null || filterOptions != null;
  }

  public void setFilterOptions(HashMap<String, Object> filterOptions) {
    this.filterOptions = filterOptions;
  }

  public void load() {
    if (!Utilities.isConnected(getContext())) {
      if (listener != null) {
        listener.onNetworkError();
      }
      return;
    }
    pageId = 1;
    loadMore();
  }

  public void reset() {
    items.clear();
    notifyDataSetInvalidated();
    moreItemsAvailable = false;
    load();
  }

  public void clear() {
    items.clear();
    notifyDataSetInvalidated();
    moreItemsAvailable = false;
  }

  public void setMoreItemsAvailable(boolean value) {
    boolean oldValue = this.moreItemsAvailable;
    this.moreItemsAvailable = value;
    if (oldValue != value) this.notifyDataSetChanged();
  }

  @Override public ReportItem getItem(int i) {
    if (moreItemsAvailable && i == getCount() - 1) return null;

    return items.get(i);
  }

  @Override public long getItemId(int i) {
    ReportItem item = getItem(i);
    if (item != null) {
      return item.id;
    }
    return 0;
  }

  @Override public int getCount() {
    int count = items.size();
    if (moreItemsAvailable) count++; // the loading item

    return count;
  }

  void loadMore() {

    try {
      Map<String, Object> options =
          isFiltered() && filterOptions != null ? filterOptions : new HashMap<String, Object>();
      options.put("sort", mOrder);
      options.put("order", mSort);

      if (filterOptions != null && query != null) {
        Zup.getInstance()
            .getService()
            .retrieveFilteredReportItemsByAddressOrProtocol(pageId, query, options, this);
      } else if (filterOptions != null) {
        Zup.getInstance().getService().retrieveFilteredReportItems(pageId, filterOptions, this);
      } else if (query != null) {
        Zup.getInstance()
            .getService()
            .retrieveFilteredReportItemsByAddressOrProtocol(pageId, query, options, this);
      } else {
        Zup.getInstance().getService().retrieveReportItemsListing(pageId, options, this);
      }
    } catch (Exception ex) {
      Log.e("Retrofit", "Could not load report items", ex);
    }
  }

  public Context getContext() {
    return context;
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    if (moreItemsAvailable && position == getCount() - 1) { // loading
      loadMore();

      if (loadingView != null) {
        return loadingView;
      } else {
        LayoutInflater inflater = LayoutInflater.from(context);
        return loadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
      }
    }

    ReportItem item = getItem(position);
    ViewGroup root;
    if (viewCache.get(position, null) != null) {
      root = viewCache.get(position);
      fillData(root, item);
    } else {
      LayoutInflater inflater = LayoutInflater.from(context);
      root = (ViewGroup) inflater.inflate(R.layout.report_list_item, parent, false);

      viewCache.put(position, root);

      fillData(root, item);
    }

    return root;
  }

  void fillData(ViewGroup root, ReportItem item) {
    TextView txtTitle = (TextView) root.findViewById(R.id.user_name);
    TextView txtAddress = (TextView) root.findViewById(R.id.user_email);
    TextView txtDate = (TextView) root.findViewById(R.id.report_date);
    TextView txtStatus = (TextView) root.findViewById(R.id.report_status);
    TextView txtCaseStepResponsible = (TextView) root.findViewById(R.id.related_case_user);

    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    root.findViewById(R.id.report_saved)
        .setVisibility(
            Zup.getInstance().getReportItemService().hasReportItem(item.id) ? View.VISIBLE
                : View.GONE);
    if (category != null) {
      ReportCategory.Status status = null;
      if (item.status_id != 0) status = category.getStatus(item.status_id);

      txtTitle.setText(category.title);
      if (status != null) {
        txtStatus.setText(status.getTitle());
        txtStatus.setTextColor(status.getUiColor());
      } else {
        txtStatus.setVisibility(View.GONE);
      }
    } else {
      txtStatus.setVisibility(View.GONE);
      txtTitle.setText(context.getString(R.string.error_category_not_found).toUpperCase()
          + item.category_id
          + " ####");
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
          hasName = true;
          txtCaseStepResponsible.setText("Responsável atual: " + currentStep.responsableUser.name);
        }
      }
    }

    if (!hasName) {
      txtCaseStepResponsible.setVisibility(View.GONE);
    }

    String address =
        TextUtils.isEmpty(item.number) ? item.address : item.address + ", " + item.number;
    if (item.protocol != null) {
      txtAddress.setText(
          context.getString(R.string.protocol_title) + " " + item.protocol + " - " + address);
    } else {
      txtAddress.setText(address);
    }
    txtDate.setText(
        context.getString(R.string.report_inclusion_date_title) + " " + Zup.getInstance()
            .formatIsoDate(item.created_at));
    WebImageView imageView = (WebImageView) root.findViewById(R.id.report_category_image);
    if (category != null) {
      category.loadImageInto(imageView);
    }
  }

  @Override public void onScrollStateChanged(AbsListView absListView, int i) {

  }

  @Override public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    int position = absListView.getLastVisiblePosition();
    if (moreItemsAvailable && position == getCount() - 1) { // loading
      loadMore();
    }
  }
}
