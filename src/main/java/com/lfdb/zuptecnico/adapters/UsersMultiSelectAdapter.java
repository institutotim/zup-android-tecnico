package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 24/08/2015.
 */
public class UsersMultiSelectAdapter extends UsersAdapter {
    private List<Integer> selectedUsersId;

    public UsersMultiSelectAdapter(Context context) {
        super(context);
        selectedUsersId = new ArrayList<Integer>();
    }

    public List<Integer> getSelectedUsersId(){
        return selectedUsersId;
    }

    public void clearSelection(){
        selectedUsersId.clear();
        notifyDataSetInvalidated();
    }

    public void setSelectedUsersId(List<Integer> usersId){
        selectedUsersId.clear();
        selectedUsersId.addAll(usersId);
        notifyDataSetInvalidated();
    }

    public void setSelectedUserId(Integer selectedUserId) {
        if(selectedUsersId.contains(selectedUserId)){
            selectedUsersId.remove(selectedUserId);
        }else{
            selectedUsersId.add(selectedUserId);
        }
        notifyDataSetInvalidated();
    }

    public List<User> getSelectedUsers(){
        List<User> usersList = new ArrayList<User>();
        if(selectedUsersId != null) {
            for (int index = 0; index < selectedUsersId.size(); index++) {
                for (int j = 0; j < getCount(); j++) {
                    if (getItemId(j) == selectedUsersId.get(index)) {
                        usersList.add(getItem(j));
                        break;
                    }
                }
            }
        }
        return usersList;
    }

    public int getSelectedUsersCount(){
        return selectedUsersId.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View root = super.getView(position, convertView, parent);
        if(root.findViewById(R.id.user_selected) != null) {
            root.findViewById(R.id.user_selected).setVisibility(View.INVISIBLE);
            CheckBox checkBox = (CheckBox) root.findViewById(R.id.user_selected_checkbox);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(selectedUsersId.contains(getItem(position).id) ? true : false);

        }
        return root;
    }

}
