package com.ntxdev.zuptecnico.fragments.cases;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.activities.inventory.InventoryItemDetailsActivity;
import com.ntxdev.zuptecnico.adapters.InventoriesPickerAdapter;
import com.ntxdev.zuptecnico.entities.InventoryCategory;
import com.ntxdev.zuptecnico.entities.InventoryItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Renan on 27/01/2016.
 */
public class InventoryPickerDialog extends DialogFragment implements AdapterView.OnItemClickListener, InventoriesPickerAdapter.InventoriesPickerAdapterListener, AdapterView.OnItemLongClickListener {
    InventoriesPickerAdapter adapter;
    private EditText searchText;
    private OnInventoriesMultiSelectPickedListener multiSelectPickedListener;
    List<InventoryItem> selectedItems;
    List<Integer> selectedInventoriesId;
    InventoryCategory[] categories;
    boolean isMultiple = false;
    TextView headerTextView;

    public void onInventoriesLoaded() {
        if (selectedInventoriesId != null) {
            adapter.setSelectedInventoriesId(selectedInventoriesId);
            selectedItems = adapter.getSelectedInventories();
            showHeaderView(selectedInventoriesId.size());
        }
    }

    public void setSelectedItems(Integer[] inventoriesId) {
        if (inventoriesId == null || inventoriesId.length == 0) {
            return;
        }
        List<Integer> inventoriesList = new ArrayList<>();
        for (int index = 0; index < inventoriesId.length; index++) {
            inventoriesList.add(inventoriesId[index]);
        }
        selectedInventoriesId = inventoriesList;
    }

    public void setCategories(InventoryCategory[] categories) {
        this.categories = categories;
    }

    public void setMultiple(boolean multiple) {
        isMultiple = multiple;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!isAdded()) {
            return true;
        }
        InventoryItem selectedItem = adapter.getItem(position);
        Intent intent = new Intent(getActivity(), InventoryItemDetailsActivity.class);
        intent.putExtra("item_id", selectedItem.id);
        intent.putExtra("categoryId", selectedItem.inventory_category_id);
        startActivity(intent);
        return true;
    }

    public interface OnInventoriesMultiSelectPickedListener {
        void onInventoriesPicked(List<InventoryItem> items);
    }

    public void setListener(OnInventoriesMultiSelectPickedListener listener) {
        multiSelectPickedListener = listener;
    }

    private void updateAdapter() {
        if (selectedItems != null && selectedItems.size() > 0) {
            selectedInventoriesId = new ArrayList<>();
            for (int index = 0; index < selectedItems.size(); index++) {
                selectedInventoriesId.add(selectedItems.get(index).id);
            }
        } else if (selectedInventoriesId == null) {
            selectedInventoriesId = new ArrayList<>();
        }
        if (adapter != null) {
            adapter.setSelectedInventoriesId(selectedInventoriesId);
        }
        if (selectedInventoriesId.size() > 0) {
            showHeaderView(selectedInventoriesId.size());
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_userpicker, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        headerTextView = (TextView) view.findViewById(R.id.header_multiselection_clear);
        ((TextView) view.findViewById(R.id.textView31)).setText(getContext().getString(R.string.select_inventory));
        ((EditText) view.findViewById(R.id.search_edit)).setHint(getContext().getString(R.string.search_inventory));
        hideHeaderView();
        headerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.clearSelection();
                selectedItems.clear();
                hideHeaderView();
            }
        });
        super.onViewCreated(view, savedInstanceState);

        hideConfirmButton();
        ListView listView = (ListView) view.findViewById(R.id.listView);

        listView.setDividerHeight(0);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);

        searchText = (EditText) view.findViewById(R.id.search_edit);
        searchText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchTextChanged(charSequence.toString());
            }

            public void afterTextChanged(Editable editable) {

            }
        });

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });
        loadAdapter(listView);
        if (selectedItems == null)
            selectedItems = new ArrayList<>();
        showConfirmButton();
    }

    void loadAdapter(ListView listView) {
        if (adapter == null) {
            adapter = new InventoriesPickerAdapter(this.getActivity());
            adapter.setListener(this);
        }
        adapter.setMultiple(isMultiple);
        adapter.setCategories(categories);
        listView.setAdapter(adapter);
        adapter.load();
        updateAdapter();
    }

    private void showHeaderView(int selectedUsersCount) {
        if (!isAdded()) {
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
            headerTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.report_item_selecting));
            headerTextView.setClickable(false);
        }
    }

    void showConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.VISIBLE);
    }

    void hideConfirmButton() {
        getView().findViewById(R.id.confirm).setVisibility(View.INVISIBLE);
    }

    void searchTextChanged(String newQuery) {
        adapter.setQuery(newQuery);
    }

    void confirm() {
        if (multiSelectPickedListener != null)
            multiSelectPickedListener.onInventoriesPicked(selectedItems);
        this.dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (searchText != null && isAdded()) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        }
        super.onDismiss(dialog);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        InventoryItem selectedItem = adapter.getItem(i);
        toggleSelectedItem(selectedItem);
        adapter.setSelectedInventoryId((Integer) selectedItem.id);
        if (adapter.getSelectedInventoriesCount() > 0) {
            showHeaderView(adapter.getSelectedInventoriesCount());
        } else {
            hideHeaderView();
        }

    }

    private void toggleSelectedItem(InventoryItem inventoryItem) {
        if (selectedItems.contains(inventoryItem)) {
            selectedItems.remove(inventoryItem);
        } else {
            if (!isMultiple) {
                selectedItems.clear();
            }
            selectedItems.add(inventoryItem);
        }
    }
}
