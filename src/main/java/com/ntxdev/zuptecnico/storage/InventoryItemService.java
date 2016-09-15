package com.ntxdev.zuptecnico.storage;

import com.ntxdev.zuptecnico.entities.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 22/01/2016.
 */
public class InventoryItemService extends BaseService {
  public InventoryItemService(StorageServiceManager manager) {
    super(manager);
  }

  public void clear() {
    deleteObject("inventory_items");
  }

  public int getInventoryItemCount() {
    List<Integer> ids = getObjectList("inventory_items", Integer.class);
    if (ids == null) return 0;

    return ids.size();
  }

  public boolean hasInventoryItem(int id) {
    try {
      List<Integer> ids = getObjectList("inventory_items", Integer.class);
      if (ids == null) return false;

      return ids.contains(id);
    } catch (Exception e) {
      return false;
    }
  }

  public InventoryItem getInventoryItemAtIndex(int index) {
    List<Integer> ids = getObjectList("inventory_items", Integer.class);
    if (ids == null || index >= ids.size()) return null;

    int id = ids.get(index);
    return getInventoryItem(id);
  }

  public InventoryItem getInventoryItem(int id) {
    InventoryItem item = getObject("inventory_item_" + id, InventoryItem.class);
    return item;
  }

  public List<InventoryItem> getInventoryItems() {
    List<InventoryItem> items = new ArrayList<>();
    for (int index = 0; index < getInventoryItemCount(); index++) {
      items.add(getInventoryItemAtIndex(index));
    }
    return items;
  }

  public void addInventoryItem(InventoryItem item) {
    List<Integer> ids = getObjectList("inventory_items", Integer.class);
    if (ids == null) ids = new ArrayList<>();

    if (!ids.contains(item.id)) {
      ids.add(item.id);
      setList("inventory_items", ids);
    }

    saveInventoryItem(item);
  }

  public void addFakeInventoryItem(InventoryItem item) {
    saveInventoryItem(item);
  }

  public void deleteInventoryItem(int id) {
    deleteObject("inventory_item_" + id);
    List<Integer> ids = getObjectList("inventory_items", Integer.class);
    if (ids == null) return;

    if (ids.contains(id)) {
      ids.remove((Integer) id);
      setList("inventory_items", ids);
    }
  }

  private void saveInventoryItem(InventoryItem item) {
    setObject("inventory_item_" + item.id, item);
  }

  public void removeFakeItem(int id) {
    deleteObject("inventory_item_" + id);
  }
}
