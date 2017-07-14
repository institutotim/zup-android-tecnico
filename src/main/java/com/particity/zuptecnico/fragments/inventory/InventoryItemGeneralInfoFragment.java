package com.ntxdev.zuptecnico.fragments.inventory;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryCategoryStatus;
import com.ntxdev.zuptecnico.entities.InventoryItem;
import com.ntxdev.zuptecnico.util.Utilities;

/**
 * Created by igorlira on 7/18/15.
 */
public class InventoryItemGeneralInfoFragment extends Fragment {
    InventoryItem getItem() {
        return getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory_details_general, container, false);
        fillData(root);
        return root;
    }

    public void refresh() {
        fillData((ViewGroup) getView());
    }

    void fillData(ViewGroup root) {
        if (getItem() == null)
            return;

        InventoryItem item = getItem();

        InventoryCategory category = Zup.getInstance().getInventoryCategoryService().getInventoryCategory(item.inventory_category_id);


        TextView txtAddress = (TextView) root.findViewById(R.id.full_address);
        TextView txtCreation = (TextView) root.findViewById(R.id.creation_date);
        TextView txtName = (TextView) root.findViewById(R.id.name);
        TextView txtCategory = (TextView) root.findViewById(R.id.category_name);
        TextView txtStatus = (TextView) root.findViewById(R.id.status);

        if(TextUtils.isEmpty(item.title) && Zup.getInstance().getSyncActionService().hasSyncActionRelatedToInventoryItem(item.id)) {
            txtName.setText(getString(R.string.waiting_sync));
            txtName.setTextColor(ContextCompat.getColor(getContext(), R.color.waiting_sync_action_color));
        } else {
            txtName.setText(item.title);
            txtName.setTextColor(ContextCompat.getColor(getContext(), R.color.comment_item_text));
        }
        txtCreation.setText(Utilities.formatIsoDateAndTime(item.created_at));
        txtAddress.setText(item.address);
        txtCategory.setText(category.title);

        if (item.inventory_status_id != null) {
            InventoryCategoryStatus status = category.getStatus(item.inventory_status_id);
            if(status != null) {
                txtStatus.setText(status.title);
                root.findViewById(R.id.status_title).setVisibility(View.VISIBLE);
                txtStatus.setVisibility(View.VISIBLE);
            } else {
                txtStatus.setVisibility(View.GONE);
                root.findViewById(R.id.status_title).setVisibility(View.GONE);
            }
        } else {
            txtStatus.setVisibility(View.GONE);
            root.findViewById(R.id.status_title).setVisibility(View.GONE);
        }
    }
}
