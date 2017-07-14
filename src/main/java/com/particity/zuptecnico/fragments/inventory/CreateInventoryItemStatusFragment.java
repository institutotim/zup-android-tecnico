package com.particity.zuptecnico.fragments.inventory;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.entities.InventoryCategory;
import com.particity.zuptecnico.entities.InventoryCategoryStatus;
import com.particity.zuptecnico.entities.InventoryItem;
import com.particity.zuptecnico.util.Utilities;

/**
 * Created by Renan on 14/01/2016.
 */
public class CreateInventoryItemStatusFragment extends Fragment implements CreateInventoryPublisher {
    InventoryCategory getCategory() {
        return (InventoryCategory) getArguments().getSerializable("category");
    }

    boolean isCreateMode() {
        return getArguments().getBoolean("create_mode");
    }

    InventoryItem getItem() {
        return getArguments().getParcelable("item");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_inventory_details_section, container, false);
        fillData(root);
        return root;
    }

    public void refresh() {
        fillData((ViewGroup) getView());
    }

    void fillData(ViewGroup root) {
        if (getCategory() == null || !isAdded())
            return;

        boolean createMode = isCreateMode();

        TextView txtHeader = (TextView) root.findViewById(R.id.inventory_item_section_title);
        txtHeader.setText(getString(R.string.notification_status_title));

        ViewGroup sectionsContainner = (ViewGroup) root.findViewById(R.id.container);
        createRadiosForCategoryStatuses(sectionsContainner, getActivity(), getActivity().getLayoutInflater(), getCategory(), createMode, getItem());
    }

    public View validateSection(View error) {
        if (!isAdded()) {
            return null;
        }
        View firstFieldError = null;
        ViewGroup container = (ViewGroup) getView().findViewById(R.id.container);

        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_is_status_field) != null && getCategory().require_item_status) {
                if (getStatusFieldValue() == null) {
                    setStatusFieldButtonsColor((RadioGroup) child, R.color.field_label_error);
                    if (error == null) {
                        child.requestFocus();
                        firstFieldError = child;
                    } else {
                        firstFieldError = error;
                    }
                } else {
                    setStatusFieldButtonsColor((RadioGroup) child, R.color.comment_item_text);
                }
            }
        }
        return firstFieldError;
    }

    private void setStatusFieldButtonsColor(RadioGroup group, int color) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (!(child instanceof RadioButton))
                continue;

            RadioButton button = (RadioButton) group.getChildAt(i);
            button.setTextColor(ContextCompat.getColor(getContext(), color));
        }
    }

    private Integer getStatusFieldValue() {
        View root = getView();
        if (!isAdded()) {
            return 0;
        }
        ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getTag(R.id.inventory_item_create_is_status_field) == null)
                continue;

            RadioGroup group = (RadioGroup) child;
            try {
                RadioButton button = (RadioButton) group.findViewById(group.getCheckedRadioButtonId());
                if (button.getTag() instanceof InventoryCategoryStatus)
                    return ((InventoryCategoryStatus) button.getTag()).id;
            } catch (Exception ex) {
                return null;
            }
        }
        return null;
    }

    public InventoryItem createItemFromData(InventoryItem item) {
        if (!isAdded()) {
            return null;
        }
        item.inventory_status_id = getStatusFieldValue();
        return item;
    }

    public void createRadiosForCategoryStatuses(ViewGroup parent, Context context, LayoutInflater layoutInflater, InventoryCategory category, boolean createMode, InventoryItem item) {
        InventoryCategoryStatus[] statuses = category.statuses;
        if (statuses != null && statuses.length > 0) {
            RadioGroup statusesContainer = new RadioGroup(context);
            statusesContainer.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            statusesContainer.setLayoutParams(params);

            int length = statuses.length;
            for (int i = 0; i < length; i++) {
                InventoryCategoryStatus status = statuses[i];

                RadioButton checkBox = new RadioButton(context);
                checkBox.setText(status.title);
                checkBox.setTag(status);
                if (Utilities.isTablet(context))
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 25);
                else
                    checkBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
                statusesContainer.addView(checkBox);

                if (!createMode && item != null && item.inventory_status_id != null && item.inventory_status_id == status.id)
                    statusesContainer.check(checkBox.getId());
            }

            statusesContainer.setTag(R.id.inventory_item_create_is_status_field, true);
            parent.addView(statusesContainer);
        }
    }

}
