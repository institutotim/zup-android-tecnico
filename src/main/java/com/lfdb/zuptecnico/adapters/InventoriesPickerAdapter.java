package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryItem;
import com.lfdb.zuptecnico.entities.collections.InventoryItemCollection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Renan on 27/01/2016.
 */
public class InventoriesPickerAdapter extends BaseAdapter {
  private Hashtable<Integer, ViewGroup> viewCache;
  protected boolean areMoreItemsAvailable;
  protected View loadingView;

  protected List<InventoryItem> items;
  protected int pageId; // next page that will be loaded
  private Tasker pageLoader;
  private int[] categories = null;
  private boolean isMultiple = false;
  protected Context context;
  protected InventoriesPickerAdapterListener listener;

  protected String query;
  private List<Integer> selectedInventoriesId;

  public interface InventoriesPickerAdapterListener {
    void onInventoriesLoaded();
  }

  public int[] getCategories() {
    return categories;
  }

  public void setCategories(InventoryCategory[] categories) {
    if (categories == null) {
      this.categories = null;
    } else {
      this.categories = new int[categories.length];
      for (int index = 0; index < categories.length; index++) {
        this.categories[index] = categories[index].id;
      }
    }

    areMoreItemsAvailable = true;
    load();
  }

  public void setMultiple(boolean isMultiple) {
    this.isMultiple = isMultiple;
  }

  public int getSelectedInventoriesCount() {
    if (selectedInventoriesId == null) {
      return 0;
    }
    return selectedInventoriesId.size();
  }

  public void setListener(InventoriesPickerAdapterListener listener) {
    this.listener = listener;
  }

  public InventoriesPickerAdapter(Context context) {
    init(context);
  }

  private void init(Context context) {
    this.context = context;
    selectedInventoriesId = new ArrayList<>();
    items = new ArrayList<>();
    viewCache = new Hashtable<>();
    areMoreItemsAvailable = false;
    pageId = 1;
  }

  public void setQuery(String query) {
    this.query = query;

    areMoreItemsAvailable = true;
    load();
  }

  public void clearSelection() {
    selectedInventoriesId.clear();
    notifyDataSetInvalidated();
  }

  public void load() {
    pageId = 1;
    this.items.clear();
    notifyDataSetInvalidated();

    if (pageLoader != null) {
      pageLoader.cancel(true);
    }
    pageLoader = new Tasker(this.query, pageId);
    pageLoader.execute();
  }

  public void setSelectedInventoriesId(List<Integer> inventoriesId) {
    if (selectedInventoriesId == null) {
      selectedInventoriesId = new ArrayList<>();
    }
    selectedInventoriesId.addAll(inventoriesId);
    notifyDataSetInvalidated();
  }

  public List<InventoryItem> getSelectedInventories() {
    List<InventoryItem> inventories = new ArrayList<>();
    if (selectedInventoriesId != null) {
      for (int index = 0; index < selectedInventoriesId.size(); index++) {
        for (int j = 0; j < getCount(); j++) {
          if (getItemId(j) == selectedInventoriesId.get(index)) {
            inventories.add(getItem(j));
            break;
          }
        }
      }
    }
    return inventories;
  }

  public void setSelectedInventoryId(Integer inventoryId) {
    if (selectedInventoriesId.contains(inventoryId)) {
      selectedInventoriesId.remove(inventoryId);
    } else {
      if (!isMultiple) {
        selectedInventoriesId.clear();
      }
      selectedInventoriesId.add(inventoryId);
    }
    notifyDataSetInvalidated();
  }

  public void setMoreItemsAvailable(boolean value) {
    boolean oldValue = this.areMoreItemsAvailable;
    this.areMoreItemsAvailable = value;

    if (oldValue != value) this.notifyDataSetChanged();
  }

  @Override public InventoryItem getItem(int i) {
    if (areMoreItemsAvailable && i == getCount() - 1) return null;

    return (items != null && items.size() > i) ? items.get(i) : null;
  }

  @Override public long getItemId(int i) {
    InventoryItem item = getItem(i);
      if (item != null) {
        return item.id;
      } else {
        return 0;
      }
  }

  @Override public int getCount() {
    int count = items.size();
    if (areMoreItemsAvailable) count++; // the loading item

    return count;
  }

  void loadMore() {
    if (pageLoader != null) return;

    pageLoader = new Tasker(this.query, pageId);
    pageLoader.execute();
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    context = parent.getContext();
    if (areMoreItemsAvailable && position == getCount() - 1) { // loading
      loadMore();

        if (loadingView != null) {
          return loadingView;
        } else {
          LayoutInflater inflater = LayoutInflater.from(context);
          return loadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
        }
    }

    ViewGroup root;
      if (viewCache.get((int) getItemId(position)) != null) {
        root = viewCache.get((int) getItemId(position));
      } else {
          LayoutInflater inflater = LayoutInflater.from(context);
          root = (ViewGroup) inflater.inflate(R.layout.user_list_item, parent, false);

          viewCache.put((int) getItemId(position), root);

          InventoryItem item = getItem(position);
          fillData(root, item);
      }

    root.findViewById(R.id.user_selected)
        .setVisibility(
            selectedInventoriesId.contains(getItem(position).id) ? View.VISIBLE : View.INVISIBLE);

    return root;
  }

  void fillData(ViewGroup root, InventoryItem item) {
    TextView txtName = (TextView) root.findViewById(R.id.user_name);
    TextView txtEmail = (TextView) root.findViewById(R.id.user_email);

    InventoryCategory category = Zup.getInstance()
        .getInventoryCategoryService()
        .getInventoryCategory(item.inventory_category_id);
    if (category != null) {
      txtName.setText(category.title);
    } else {
      txtName.setText(item.title);
    }
    StringBuilder builder = new StringBuilder();
    builder.append(item.title);
    if (!TextUtils.isEmpty(item.address)) {
      builder.append(" - ");
      builder.append(item.address);
    }
    txtEmail.setText(builder.toString());
  }

  class Tasker extends AsyncTask<Void, Void, InventoryItem[]> {
    int pageId;
    String query;
    boolean interrupted;

    public Tasker(String query, int pageId) {
      this.pageId = pageId;
      this.query = query;
    }

    @Override protected InventoryItem[] doInBackground(Void... voids) {
      try {
        Thread.sleep(500); // Give time for user to input more data
        InventoryItemCollection inventories;
        if (categories == null || categories.length == 0) {
          inventories = Zup.getInstance().getService().searchInventoryItems(pageId, query);
        } else {
          inventories =
              Zup.getInstance().getService().searchInventoryItems(pageId, query, categories);
        }

        return inventories.items;
      } catch (RetrofitError ex) {
        Log.e("Retrofit", "Could not load inventories", ex);
        return null;
      } catch (InterruptedException ex) {
        interrupted = true;
        return null;
      }
    }

    @Override protected void onPostExecute(InventoryItem[] items) {
      if (items == null) {
        if (!interrupted) InventoriesPickerAdapter.this.setMoreItemsAvailable(false);
        return;
      }

      for (int i = 0; i < items.length; i++) {
        InventoriesPickerAdapter.this.items.add(items[i]);
      }

      InventoriesPickerAdapter.this.pageId = this.pageId + 1;

      if (listener != null) listener.onInventoriesLoaded();

      InventoriesPickerAdapter.this.setMoreItemsAvailable(items.length > 0);
        if (this.pageId == 1) {
          InventoriesPickerAdapter.this.notifyDataSetInvalidated();
        } else {
          InventoriesPickerAdapter.this.notifyDataSetChanged();
        }
      pageLoader = null;
    }
  }
}
