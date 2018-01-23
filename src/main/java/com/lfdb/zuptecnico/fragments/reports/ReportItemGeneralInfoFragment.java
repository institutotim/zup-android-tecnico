package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.ZupApplication;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.api.sync.ChangeReportResponsableGroupSyncAction;
import com.lfdb.zuptecnico.api.sync.ChangeReportResponsableUserSyncAction;
import com.lfdb.zuptecnico.entities.Group;
import com.lfdb.zuptecnico.entities.ReportCategory;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.entities.User;
import com.lfdb.zuptecnico.fragments.GroupPickerDialog;
import com.lfdb.zuptecnico.fragments.UserPickerDialog;
import com.lfdb.zuptecnico.util.Utilities;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemGeneralInfoFragment extends Fragment implements OnClickListener, ReportItemCommentDialog.OnCommentListener {
    Group group;

    ReportItem getItem() {
        return (ReportItem) getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_report_details_general, container, false);
        fillData(root);
        return root;
    }

    public void refresh() {
        fillData((ViewGroup) getView());
    }

    void fillData(ViewGroup root) {
        if (getItem() == null)
            return;

        ReportItem item = getItem();

        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
        ReportCategory.Status status = null;
        if (item.status_id != -1 && category != null) {
            status = category.getStatus(item.status_id);
        }

        TextView txtProtocol = (TextView) root.findViewById(R.id.protocol);
        TextView txtAddress = (TextView) root.findViewById(R.id.full_address);
        TextView txtReference = (TextView) root.findViewById(R.id.reference);
        TextView txtDescription = (TextView) root.findViewById(R.id.description);
        TextView txtCategory = (TextView) root.findViewById(R.id.category_name);
        TextView txtCreation = (TextView) root.findViewById(R.id.creation_date);
        TextView txtStatus = (TextView) root.findViewById(R.id.status);
        TextView txtGroup = (TextView) root.findViewById(R.id.responsible_group_name);
        TextView txtUser = (TextView) root.findViewById(R.id.responsible_user_name);

        txtProtocol.setText(item.protocol);
        txtAddress.setText(item.getFullAddress());
        txtReference.setText(notInformedIfBlank(item.reference));
        txtDescription.setText(notInformedIfBlank(item.description));
        txtCategory.setText(category != null ? category.title : "");
        txtCreation.setText(Utilities.formatIsoDateAndTime(item.created_at));
        if (status != null)
            txtStatus.setText(status.getTitle());
        else
            txtStatus.setText(R.string.no_status);

        if (item.assignedUser != null)
            txtUser.setText(item.assignedUser.name);
        else
            txtUser.setText(R.string.no_responsable_user_text);

        if (item.assignedGroup != null)
            txtGroup.setText(item.assignedGroup.getName());
        else
            txtGroup.setText(R.string.no_responsable_group_text);

        updateResponsabilityBehavior(root);
    }

    private void updateResponsabilityBehavior(ViewGroup root) {
        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(getItem().category_id);
        //boolean hasSolverGroups = category != null && category.solver_groups_ids != null && category.solver_groups_ids.length > 0;
        if(Zup.getInstance().getAccess().canForwardReportItems(category.id)) {
            root.findViewById(R.id.responsable_group_container).setOnClickListener(this);
            root.findViewById(R.id.responsable_user_container).setOnClickListener(this);
            root.findViewById(R.id.change_responsable_group).setVisibility(View.VISIBLE);
            root.findViewById(R.id.change_responsable_user).setVisibility(View.VISIBLE);
        } else {
            root.findViewById(R.id.change_responsable_group).setVisibility(View.GONE);
            root.findViewById(R.id.change_responsable_user).setVisibility(View.GONE);
        }
    }

    void showCommentDialog() {
        ReportItemCommentDialog dialog = new ReportItemCommentDialog();
        dialog.setListener(this);
        dialog.setHasType(false);
        dialog.show(getChildFragmentManager(), "create_dialog");
    }

    String notInformedIfBlank(String value) {
        if (TextUtils.isEmpty(value)) {
            return getActivity().getString(R.string.not_informed);
        }
        return value;
    }

    @Override
    public void onClick(View view) {
        if (!Utilities.isConnected(getActivity())) {
            ZupApplication.toast(getView(), R.string.only_online_option_error).show();
            return;
        }
        switch (view.getId()) {
            case R.id.responsable_group_container:
                selectGroup();
                break;
            case R.id.responsable_user_container:
                selectUser();
                break;
        }
    }

    private void assignResponsableUser(User user) {
        if (user == null) {
            return;
        }
        ReportItem item = getItem();
        if(item == null) {
            return;
        }
        ChangeReportResponsableUserSyncAction action = new ChangeReportResponsableUserSyncAction(item.id, item.category_id, user.id);
        Zup.getInstance().getSyncActionService().addSyncAction(action);
        Zup.getInstance().sync();
    }

    public void selectGroup() {
        ReportItem item = getItem();
        if(item == null) {
            return;
        }
        GroupPickerDialog dialog;
        if(group == null) {
            if (item.assignedGroup != null) {
                dialog = GroupPickerDialog.newInstance(item.category_id, item.assignedGroup);
            } else {
                dialog = GroupPickerDialog.newInstance(item.category_id);
            }
        } else {
            dialog = GroupPickerDialog.newInstance(item.category_id, group);
        }
        dialog.show(getActivity().getSupportFragmentManager(), "group_picker");
        dialog.setListener(new GroupPickerDialog.OnGroupPickedListener() {
            @Override
            public void onGroupPicked(Group group) {
                ReportItemGeneralInfoFragment.this.group = group;
                showCommentDialog();
            }
        });
    }

    private void assignResponsableGroup(String comment) {
        if (group == null) {
            return;
        }
        ReportItem item = getItem();
        if(item == null) {
            return;
        }
        ChangeReportResponsableGroupSyncAction action = new ChangeReportResponsableGroupSyncAction(item.id, item.category_id, group.getId(), comment);
        Zup.getInstance().getSyncActionService().addSyncAction(action);
        Zup.getInstance().sync();
    }

    private void selectUser() {
        if(getItem().assignedGroup == null) {
            ZupApplication.toast(getView(), R.string.please_assign_responsable_group_message).show();
            return;
        }
        UserPickerDialog dialog = new UserPickerDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("groupId", getItem().assignedGroup.getId());
        dialog.setArguments(bundle);
        dialog.show(getActivity().getSupportFragmentManager(), "user_picker");
        dialog.setListener(new UserPickerDialog.OnUserPickedListener() {
            @Override
            public void onUserPicked(User user) {
                assignResponsableUser(user);
            }
        });
    }

    @Override
    public void onComment(int type, String text) {
        assignResponsableGroup(text);
    }
}
