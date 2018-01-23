package com.particity.zuptecnico.fragments;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.adapters.UsersAdapter;
import com.particity.zuptecnico.adapters.UsersMultiSelectAdapter;
import com.particity.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 24/08/2015.
 */
public class UserMultiSelectPickerDialog extends UserPickerDialog implements UsersAdapter.UserAdapterListener {
    private OnUserMultiSelectPickedListener multiSelectPickedListener;
    List<User> selectedUsers;
    List<Integer> selectedUsersId;
    TextView headerTextView;

    @Override
    public void onReportsLoaded() {
        if(selectedUsersId != null) {
            ((UsersMultiSelectAdapter) adapter).setSelectedUsersId(selectedUsersId);
            selectedUsers = ((UsersMultiSelectAdapter) adapter).getSelectedUsers();
            showHeaderView(selectedUsersId.size());
        }
    }

    public interface OnUserMultiSelectPickedListener {
        void onUsersPicked(List<User> users);
    }

    public void setListener(OnUserMultiSelectPickedListener listener) {
        multiSelectPickedListener = listener;
    }

    public void setSelectedUsers(String[] usersId) {
        if (usersId == null || usersId.length == 0 || usersId[0].isEmpty()) {
            return;
        }
        List<Integer> usersList = new ArrayList<>();
        for (int index = 0; index < usersId.length; index++) {
            usersList.add(Integer.parseInt(usersId[index]));
        }
        selectedUsersId = usersList;
    }



    public void setSelectedUsers(List<User> users) {
        if (users == null) {
            return;
        }
        if (selectedUsers == null) {
            selectedUsers = new ArrayList<User>();
        } else {
            selectedUsers.clear();
        }
        selectedUsers.addAll(users);
        updateAdapter();
    }

    private void updateAdapter() {
        if (selectedUsers != null) {
            ArrayList<Integer> usersId = new ArrayList<Integer>();
            for (int index = 0; index < selectedUsers.size(); index++) {
                usersId.add(selectedUsers.get(index).id);
            }
            if (adapter != null) {
                ((UsersMultiSelectAdapter) adapter).setSelectedUsersId(usersId);
            }
            if (usersId.size() > 0) {
                showHeaderView(usersId.size());
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        hideHeaderView();
        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((UsersMultiSelectAdapter) adapter).clearSelection();
                selectedUsers.clear();
                hideHeaderView();
            }
        });
        super.onViewCreated(view, savedInstanceState);
        if (selectedUsers == null)
            selectedUsers = new ArrayList<User>();
        showConfirmButton();
    }

    private void showHeaderView(int selectedUsersCount) {
        if(!isAdded()) {
            return;
        }
        if (headerTextView != null) {
            try {
                ColorStateList colors = ContextCompat.getColorStateList(getActivity(), R.drawable.button_dialog_title);
                headerTextView.setTextColor(colors);
            } catch (Exception e) {
                headerTextView.setTextColor(getResources().getColor(R.color.zupblue));
            }
            headerTextView.setClickable(true);
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + selectedUsersCount + ")");
        }
    }

    private void hideHeaderView() {
        if (headerTextView != null) {
            headerTextView.setVisibility(View.VISIBLE);
            headerTextView.setText(getActivity().getString(R.string.clear_selected_items) + " (" + 0 + ")");
            headerTextView.setTextColor(getResources().getColor(R.color.report_item_selecting));
            headerTextView.setClickable(false);
        }
    }

    @Override
    void loadAdapter(ListView listView) {
        if (adapter == null) {
            adapter = new UsersMultiSelectAdapter(getActivity());
            adapter.setListener(this);
        }
        listView.setAdapter(adapter);
        adapter.load();
        updateAdapter();
    }

    @Override
    void confirm() {
        if (multiSelectPickedListener != null)
            multiSelectPickedListener.onUsersPicked(selectedUsers);
        this.dismiss();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        User selectedUser = adapter.getItem(i);
        toggleSelectedUser(selectedUser);
        ((UsersMultiSelectAdapter) adapter).setSelectedUserId((Integer) selectedUser.id);
        if (((UsersMultiSelectAdapter) adapter).getSelectedUsersCount() > 0) {
            showHeaderView(((UsersMultiSelectAdapter) adapter).getSelectedUsersCount());
        } else {
            hideHeaderView();
        }

    }

    private void toggleSelectedUser(User user) {
        if (selectedUsers.contains(user)) {
            selectedUsers.remove(user);
        } else {
            selectedUsers.add(user);
        }
    }
}
