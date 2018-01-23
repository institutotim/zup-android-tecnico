package com.particity.zuptecnico.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.GroupAdapter;
import com.particity.zuptecnico.entities.Flow;
import com.particity.zuptecnico.entities.Group;

/**
 * Created by Renan on 20/10/2015.
 */
public class GroupPickerDialog extends DialogFragment
    implements AdapterView.OnItemClickListener, GroupAdapter.GroupAdapterListener {
  GroupAdapter adapter;
  Group selectedGroup;
  int category;
  int stepId;
  int flowId;
  Flow.Step.FlowPermissions permissions;
  boolean shouldFinish = false;

  @Override public void onGroupLoaded() {
    if (!isAdded()) {
      return;
    }
    if (adapter.getCount() == 0) {
      Toast.makeText(getContext(), R.string.empty_groups_loaded, Toast.LENGTH_LONG).show();
      shouldFinish = true;
    }
  }

  public interface OnGroupPickedListener {
    void onGroupPicked(Group group);
  }

  private OnGroupPickedListener listener;

  public void setListener(OnGroupPickedListener listener) {
    this.listener = listener;
  }

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
    return inflater.inflate(R.layout.dialog_group_picker, container, false);
  }

  public static GroupPickerDialog newInstance(int category, Group group) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable("group", group);
    bundle.putInt("category", category);
    dialog.setArguments(bundle);
    return dialog;
  }

  public static GroupPickerDialog newInstance(int stepId, int flowId, Group group) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable("group", group);
    bundle.putInt("step", stepId);
    bundle.putInt("flow", flowId);
    dialog.setArguments(bundle);
    return dialog;
  }

  public static GroupPickerDialog newInstance(Flow.Step.FlowPermissions permission, Group group) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable("group", group);
    bundle.putSerializable("flow_permissions", permission);
    dialog.setArguments(bundle);
    return dialog;
  }

  public static GroupPickerDialog newInstance(Flow.Step.FlowPermissions permission) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putSerializable("flow_permissions", permission);
    dialog.setArguments(bundle);
    return dialog;
  }

  public static GroupPickerDialog newInstance(int stepId, int flowId) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putInt("step", stepId);
    bundle.putInt("flow", flowId);
    dialog.setArguments(bundle);
    return dialog;
  }

  public static GroupPickerDialog newInstance(int category) {
    GroupPickerDialog dialog = new GroupPickerDialog();
    Bundle bundle = new Bundle();
    bundle.putInt("category", category);
    dialog.setArguments(bundle);
    return dialog;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    hideConfirmButton();
    view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        confirm();
      }
    });

    if (getArguments() == null) {
      return;
    }
    if (getArguments().containsKey("group")) {
      selectedGroup = (Group) getArguments().getSerializable("group");
      adapter.setSelectedGroupId(selectedGroup.getId());
      showConfirmButton();
    } else {
      hideConfirmButton();
    }
    category = getArguments().getInt("category", -1);
    stepId = getArguments().getInt("step", -1);
    flowId = getArguments().getInt("flow", -1);
    if (getArguments().containsKey("flow_permissions")) {
      permissions = (Flow.Step.FlowPermissions) getArguments().getSerializable("flow_permissions");
    }
    loadAdapter();
  }

  void loadAdapter() {
    adapter = category != -1 ? new GroupAdapter(this.getActivity(), category)
        : new GroupAdapter(this.getActivity(), permissions);
    adapter.setListener(this);
    ListView listView = (ListView) getView().findViewById(R.id.listView);

    listView.setDividerHeight(0);
    listView.setOnItemClickListener(this);
    listView.setAdapter(adapter);
    if (selectedGroup != null) {
      adapter.setSelectedGroupId(selectedGroup.getId());
    }
    adapter.load();
  }


  @Override public void onResume() {
    super.onResume();
    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
    lp.copyFrom(getDialog().getWindow().getAttributes());
    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
    lp.height = WindowManager.LayoutParams.MATCH_PARENT;
    getDialog().getWindow().setAttributes(lp);

    if (shouldFinish) dismiss();
  }


  void showConfirmButton() {
    getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
  }

  void hideConfirmButton() {
    getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
  }

  void confirm() {
    if (this.selectedGroup == null) return;

    if (this.listener != null) this.listener.onGroupPicked(this.selectedGroup);

    this.dismiss();
  }

  @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    this.selectedGroup = adapter.getItem(i);
    adapter.setSelectedGroupId(this.selectedGroup.getId());
    showConfirmButton();
  }
}
