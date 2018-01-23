package com.particity.zuptecnico.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.Group;
import com.particity.zuptecnico.entities.ReportCategory;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by Renan on 20/10/2015.
 */
public class GroupAdapter extends BaseAdapter {
  private final int categoryId;
  private Hashtable<Integer, ViewGroup> viewCache;
  protected View loadingView;

  protected List<Group> items;
  protected Context context;
  protected GroupAdapterListener listener;
  protected int stepId;
  protected int flowId;
  private boolean alreadyLoaded = false;

  private int selectedGroupId;

  public interface GroupAdapterListener {
    void onGroupLoaded();
  }

  public void setListener(GroupAdapterListener listener) {
    this.listener = listener;
  }

  public GroupAdapter(Context context, int category) {
    this.context = context;
    this.categoryId = category;
    items = new ArrayList<>();
    viewCache = new Hashtable<>();
  }

  public GroupAdapter(Context context, int stepId, int flowId) {
    this.context = context;
    this.categoryId = -1;
    this.stepId = stepId;
    this.flowId = flowId;
    items = new ArrayList<>();
    viewCache = new Hashtable<>();
  }

  public GroupAdapter(Context context, Flow.Step.FlowPermissions permissions) {
    this.context = context;
    alreadyLoaded = true;
    this.categoryId = -1;
    items = new ArrayList<>();
    List<Group> namespaceGroups = Zup.getInstance().getGroupService().getNamespaceGroups();
    if (permissions == null || permissions.canExecuteStep == null || (namespaceGroups == null
        || namespaceGroups.isEmpty())) {
      return;
    }
    for (Flow.Step.FlowPermissions.Permission permission : permissions.canExecuteStep) {
      for (Group group : namespaceGroups) {
        if (group.getId() == permission.id) {
          items.add(group);
        }
      }
    }
    viewCache = new Hashtable<>();
  }

  public void load() {
    if (alreadyLoaded) {
      return;
    }
    this.items.clear();
    notifyDataSetInvalidated();

    if (categoryId == -1) {
      List<Group> groups = Zup.getInstance().getGroupService().getGroups();
      for (Group group : groups) {
        if (group.canEditStep(stepId, flowId)) {
          items.add(group);
        }
      }
    } else {
      ReportCategory category =
          Zup.getInstance().getReportCategoryService().getReportCategory(categoryId);
      if (category != null) {
        int[] ids = category.solver_groups_ids;
        if (ids != null) {
          for (int index = 0; index < ids.length; index++) {
            Group group = Zup.getInstance().getGroupService().getGroup(ids[index]);
            if (group == null) {
              continue;
            }
            items.add(group);
          }
        }
      }
    }
    if (listener != null) {
      listener.onGroupLoaded();
    }
  }

  public void setSelectedGroupId(int selectedGroupId) {
    this.selectedGroupId = selectedGroupId;
    notifyDataSetInvalidated();
  }

  @Override public Group getItem(int i) {
    return (items != null && items.size() > i) ? items.get(i) : null;
  }

  @Override public long getItemId(int i) {
    Group item = getItem(i);
    if (item != null) {
      return item.getId();
    } else {
      return 0;
    }
  }

  @Override public int getCount() {
    return items.size();
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    context = parent.getContext();

    ViewGroup root;
    if (viewCache.get((int) getItemId(position)) != null) {
      root = viewCache.get((int) getItemId(position));
    } else {
      LayoutInflater inflater = LayoutInflater.from(context);
      root = (ViewGroup) inflater.inflate(R.layout.group_list_item, parent, false);

      viewCache.put((int) getItemId(position), root);

      Group item = getItem(position);
      if (item == null) {
        return null;
      }
      fillData(root, item);
    }

    root.findViewById(R.id.user_selected)
        .setVisibility(
            selectedGroupId == getItem(position).getId() ? View.VISIBLE : View.INVISIBLE);

    return root;
  }

  void fillData(ViewGroup root, Group item) {
    TextView txtName = (TextView) root.findViewById(R.id.user_name);
    txtName.setText(item.getName());
  }
}
