package com.lfdb.zuptecnico.fragments.inventory;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.InventoryCategory;
import com.lfdb.zuptecnico.entities.InventoryCategoryStatus;
import com.lfdb.zuptecnico.entities.User;
import com.lfdb.zuptecnico.fragments.UserMultiSelectPickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Renan on 06/01/2016.
 */
public class FilterInventoryFragment extends Fragment {
    private FilterOptions options;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter, dateFormatterForTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_inventory, container, false);
        return view;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dateFormatterForTextView = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePickerView, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String fromDate = dateFormatter.format(newDate.getTime());
                options.getQueryMap().put("created_at[begin]", fromDate);
                options.setInitialDate(dateFormatterForTextView.format(newDate.getTime()));
                updateDate((TextView) view.findViewById(R.id.created_from_date_selected), options.getInitialDate());
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        fromDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.cancel();
                }
            }
        });

        fromDatePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                options.setInitialDate(null);

                options.getQueryMap().put("created_at[begin]", null);
                cancelDate((TextView) view.findViewById(R.id.created_from_date_selected));
            }
        });

        toDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePickerView, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String toDate = dateFormatter.format(newDate.getTime());
                options.getQueryMap().put("created_at[end]", toDate);
                options.setFinalDate(dateFormatterForTextView.format(newDate.getTime()));
                updateDate((TextView) view.findViewById(R.id.created_to_date_selected), options.getFinalDate());
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEGATIVE) {
                    dialog.cancel();
                }
            }
        });

        toDatePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                options.setFinalDate(null);
                options.getQueryMap().put("created_at[end]", null);
                cancelDate((TextView) view.findViewById(R.id.created_to_date_selected));
            }
        });

        view.findViewById(R.id.layout_created_by).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createdByUsers(v);
            }
        });

        view.findViewById(R.id.layout_status).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterStatuses();
            }
        });

        view.findViewById(R.id.layout_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterCategories();
            }
        });

        view.findViewById(R.id.layout_created_from_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.show();
            }
        });

        view.findViewById(R.id.layout_created_to_date).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDatePickerDialog.show();
            }
        });
        updateView();
    }

    private void cancelDate(TextView dateTextView) {
        dateTextView.setText(getActivity().getString(R.string.define_date_filter));
        dateTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.document_list_pending_selecting));
    }

    private void updateDate(TextView dateTextView, String toDate) {
        dateTextView.setText(toDate);
        dateTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.report_item_text_default));
    }

    private void filterStatuses() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.filter_inventory_by_status_title);
        final ArrayList<InventoryCategoryStatus> statuses = new ArrayList<>();
        final ArrayList<InventoryCategoryStatus> selectedStatuses = new ArrayList<>();
        InventoryCategory[] categoryIterator = Zup.getInstance().getInventoryCategoryService().getInventoryCategories();
        if (categoryIterator != null && categoryIterator.length > 0) {
            int length = categoryIterator.length;
            for (int i = 0; i < length; i++) {
                InventoryCategory category = categoryIterator[i];
                if (!Zup.getInstance().getAccess().canViewInventoryCategory(category.id)) {
                    continue;
                }
                InventoryCategoryStatus[] statusIterator = category.statuses;
                if (statusIterator != null && statusIterator.length > 0) {
                    int statusLength = statusIterator.length;
                    for (int j = 0; j < statusLength; j++) {
                        InventoryCategoryStatus status = statusIterator[j];
                        statuses.add(status);
                    }
                }
            }
        }
        String[] items = new String[statuses.size()];
        String statusesIds = (String) options.getQueryMap().get("inventory_statuses_ids");
        final ArrayList<String> statusIds = new ArrayList<>(Arrays.asList(statusesIds == null ? new String[0] : statusesIds.split(", ")));
        boolean[] checkedItems = new boolean[statuses.size()];
        for (int i = 0; i < statuses.size(); i++) {
            InventoryCategoryStatus item = statuses.get(i);
            items[i] = item.title;
            if (statusIds.contains(String.valueOf(statuses.get(i).id))) {
                selectedStatuses.add(item);
                checkedItems[i] = true;
            } else {
                checkedItems[i] = false;
            }
        }

        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                InventoryCategoryStatus status = statuses.get(i);
                if (checked) {
                    if (!statusIds.contains(String.valueOf(status.id))) {
                        statusIds.add(String.valueOf(status.id));
                        selectedStatuses.add(status);
                    }
                } else {
                    if (statusIds.contains(String.valueOf(status.id))) {
                        statusIds.remove(String.valueOf(status.id));
                        selectedStatuses.remove(status);
                    }
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                options.setStatuses(selectedStatuses);
                updateStatusesTextView((TextView) getView().findViewById(R.id.status_selected), options.getStatuses());
            }
        });
        builder.show();
    }

    private void updateStatusesTextView(TextView textView, String statuses) {
        if (statuses == null || statuses.isEmpty()) {
            textView.setText(getActivity().getString(R.string.all_status_filter));
        } else {
            textView.setText(statuses);
        }
    }

    public void createdByUsers(View sender) {
        UserMultiSelectPickerDialog dialog = new UserMultiSelectPickerDialog();
        if (options.getQueryMap().containsKey("users_ids")) {
            String userIds = (String) options.getQueryMap().get("users_ids");
            dialog.setSelectedUsers(userIds.split(", "));
        }
        dialog.show(getActivity().getSupportFragmentManager(), "user_picker");
        dialog.setListener(new UserMultiSelectPickerDialog.OnUserMultiSelectPickedListener() {
            @Override
            public void onUsersPicked(List<User> users) {
                options.setCreatedByUsersList(users);
                updateUsersTextView((TextView) getView().findViewById(R.id.users_created_selected), options.getCreatedByUsersList());
            }
        });
    }

    public void filterCategories() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.filter_inventory_by_category_title);
        final ArrayList<InventoryCategory> categories = new ArrayList<>();
        final ArrayList<InventoryCategory> selectedCategories = new ArrayList<>();
        InventoryCategory[] categoryIterator = Zup.getInstance().getInventoryCategoryService().getInventoryCategories();
        if (categoryIterator != null && categoryIterator.length > 0) {
            int length = categoryIterator.length;
            for (int i = 0; i < length; i++) {
                InventoryCategory category = categoryIterator[i];
                if (Zup.getInstance().getAccess().canViewInventoryCategory(category.id)) {
                    categories.add(category);
                }
            }
        }

        String[] items = new String[categories.size()];
        String categoriesIds = (String) options.getQueryMap().get("inventory_categories_ids");
        final ArrayList<String> categoryIds = new ArrayList<>(Arrays.asList(categoriesIds == null ? new String[0] : categoriesIds.split(", ")));
        boolean[] checkedItems = new boolean[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            InventoryCategory item = categories.get(i);
            items[i] = item.title;
            if (categoryIds.contains(String.valueOf(categories.get(i).id))) {
                selectedCategories.add(item);
                checkedItems[i] = true;
            } else {
                checkedItems[i] = false;
            }
        }

        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean checked) {
                InventoryCategory category = categories.get(i);
                if (checked) {
                    if (!categoryIds.contains(String.valueOf(category.id))) {
                        categoryIds.add(String.valueOf(category.id));
                        selectedCategories.add(category);
                    }
                } else {
                    if (categoryIds.contains(String.valueOf(category.id))) {
                        categoryIds.remove(String.valueOf(category.id));
                        selectedCategories.remove(category);
                    }
                }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                options.setCategories(selectedCategories);
                updateCategoriesTextView((TextView) getView().findViewById(R.id.cateogies_selected), options.getCategories());
            }
        });
        builder.show();
    }

    private void updateCategoriesTextView(TextView textView, String categories) {
        if (categories == null || categories.isEmpty()) {
            textView.setText(getActivity().getString(R.string.all_categories_filter));
        } else {
            textView.setText(categories);
        }
    }

    private void updateUsersTextView(TextView userTextView, String users) {
        if (users == null || users.isEmpty()) {
            userTextView.setText(getActivity().getString(R.string.all_users));
        } else {
            userTextView.setText(users);
        }
    }

    public FilterOptions getFilterOptions() {
        return options;
    }

    public void setFilterOptions(FilterOptions options) {
        this.options = options;
        updateView();
    }

    private void updateView() {
        if (options == null || getView() == null) {
            options = new FilterOptions();
            return;
        }
        updateStatusesTextView((TextView) getView().findViewById(R.id.status_selected), options.getStatuses());
        updateUsersTextView((TextView) getView().findViewById(R.id.users_created_selected), options.getCreatedByUsersList());
        updateCategoriesTextView((TextView) getView().findViewById(R.id.cateogies_selected), options.getCategories());
        if (options.getInitialDate() != null && !options.getInitialDate().isEmpty()) {
            updateDate((TextView) getView().findViewById(R.id.created_from_date_selected),
                    options.getInitialDate());
        }
        if (options.getFinalDate() != null && !options.getFinalDate().isEmpty()) {
            updateDate((TextView) getView().findViewById(R.id.created_to_date_selected),
                    options.getFinalDate());
        }
    }

    public static class FilterOptions implements Parcelable {
        private String initialDate;
        private String finalDate;
        private String createdByUsersList;
        private String categories;
        private String statuses;
        private HashMap<String, Object> query;
        public static final Parcelable.Creator<FilterOptions> CREATOR = new Parcelable.Creator<FilterOptions>() {

            @Override
            public FilterOptions createFromParcel(Parcel source) {
                return new FilterOptions(source);
            }

            @Override
            public FilterOptions[] newArray(int size) {
                return new FilterOptions[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(initialDate);
            dest.writeString(finalDate);
            dest.writeString(createdByUsersList);
            dest.writeString(categories);
            dest.writeString(statuses);
            dest.writeSerializable(query);
        }

        public FilterOptions(Parcel source) {
            initialDate = source.readString();
            finalDate = source.readString();
            createdByUsersList = source.readString();
            categories = source.readString();
            statuses = source.readString();
            query = (HashMap<String, Object>) source.readSerializable();
        }

        public FilterOptions() {
            query = new HashMap<String, Object>();
        }

        public void setCategories(List<InventoryCategory> categories) {
            String categoriesId = "";
            String categoriesName = "";
            if (categories != null) {
                for (int index = 0; index < categories.size(); index++) {
                    if (index == categories.size() - 1) {
                        categoriesId = categoriesId.concat(String.valueOf(categories.get(index).id));
                        categoriesName = categoriesName.concat(String.valueOf(categories.get(index).title));
                    } else {
                        categoriesId = categoriesId.concat(String.valueOf(categories.get(index).id) + ", ");
                        categoriesName = categoriesName.concat(String.valueOf(categories.get(index).title) + ", ");
                    }
                }
            }
            this.categories = categoriesName;
            query.put("inventory_categories_ids", categoriesId);
        }

        public void setCreatedByUsersList(List<User> createdByUsersList) {
            String usersId = "";
            String usersName = "";
            if (createdByUsersList != null) {
                for (int index = 0; index < createdByUsersList.size(); index++) {
                    if (index == createdByUsersList.size() - 1) {
                        usersId = usersId.concat(String.valueOf(createdByUsersList.get(index).id));
                        usersName = usersName.concat(String.valueOf(createdByUsersList.get(index).name));
                    } else {
                        usersId = usersId.concat(String.valueOf(createdByUsersList.get(index).id) + ", ");
                        usersName = usersName.concat(String.valueOf(createdByUsersList.get(index).name) + ", ");
                    }
                }
            }
            this.createdByUsersList = usersName;
            query.put("users_ids", usersId);
        }

        public String getStatuses() {
            return statuses;
        }

        public void setStatuses(List<InventoryCategoryStatus> statuses) {
            String statusesId = "";
            String statusesName = "";
            if (statuses != null) {
                for (int index = 0; index < statuses.size(); index++) {
                    if (index == statuses.size() - 1) {
                        statusesId = statusesId.concat(String.valueOf(statuses.get(index).id));
                        statusesName = statusesName.concat(String.valueOf(statuses.get(index).title));
                    } else {
                        statusesId = statusesId.concat(String.valueOf(statuses.get(index).id) + ", ");
                        statusesName = statusesName.concat(String.valueOf(statuses.get(index).title) + ", ");
                    }
                }
            }
            this.statuses = statusesName;
            query.put("inventory_statuses_ids", statusesId);
        }

        public String getCategories() {
            return categories;
        }

        public String getCreatedByUsersList() {
            return createdByUsersList;
        }

        public String getInitialDate() {
            return initialDate == null ? "" : initialDate;
        }

        public void setInitialDate(String initialDate) {
            this.initialDate = initialDate;
        }

        public String getFinalDate() {
            return finalDate == null ? "" : finalDate;
        }

        public void setFinalDate(String finalDate) {
            this.finalDate = finalDate;
        }

        public HashMap<String, Object> getQueryMap() {
            return query;
        }
    }
}

