package com.particity.zuptecnico.fragments.reports;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.TextView;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.entities.ReportCategory;
import com.particity.zuptecnico.entities.User;
import com.particity.zuptecnico.fragments.UserMultiSelectPickerDialog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Renan on 22/08/2015.
 */
public class FilterReportsFragment extends Fragment {
    private FilterOptions options;
    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter, dateFormatterForTextView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter_reports, container, false);
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
                options.getQueryMap().put("begin_date", fromDate);
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
                cancelDate((TextView) view.findViewById(R.id.created_from_date_selected));
            }
        });

        toDatePickerDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker datePickerView, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                String toDate = dateFormatter.format(newDate.getTime());

                options.getQueryMap().put("end_date", toDate);
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

        view.findViewById(R.id.layout_requested_by).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestedByUsers(v);
            }
        });
        view.findViewById(R.id.layout_related_to_me).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.related_to_me_switch).performClick();
            }
        });

        view.findViewById(R.id.layout_category).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterCategories();
            }
        });

        view.findViewById(R.id.layout_notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterByNotifications();
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

        view.findViewById(R.id.layout_related_to_my_group).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.related_to_my_group_switch).performClick();
            }
        });
        CompoundButton.OnCheckedChangeListener relatedToMeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                options.setRelatedToMe(isChecked);
            }
        };
        CompoundButton.OnCheckedChangeListener relatedToMyGroupListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                options.setRelatedToMyGroup(isChecked);
            }
        };
        ((SwitchCompat) view.findViewById(R.id.related_to_me_switch)).setOnCheckedChangeListener(relatedToMeListener);
        ((SwitchCompat) view.findViewById(R.id.related_to_my_group_switch)).setOnCheckedChangeListener(relatedToMyGroupListener);
        updateView();
    }

    private void filterByNotifications() {
        FilterReportsByNotificationsDialog dialog = new FilterReportsByNotificationsDialog();
        dialog.setSelectedFilters(options.getMinimumNotificationNumber(), options.getDaysSinceLastNotification(),
                options.getDaysForLastNotificationDeadline(), options.getDaysForOverdueNotification());
        dialog.show(getActivity().getSupportFragmentManager(), "notifications_filter");
        dialog.setListener(new FilterReportsByNotificationsDialog.OnNotificationsFilterSettingsSetListener() {
            @Override
            public void onNotificationsFilterSettingsSetListener(int minimumNotificationNumber,
                                                                 FilterOptions.Range daysSinceLastNotification,
                                                                 FilterOptions.Range daysForLastNotificationDeadline,
                                                                 FilterOptions.Range daysForOverdueNotification) {
                options.setDaysSinceLastNotification(daysSinceLastNotification);
                options.setMinimumNotificationNumber(minimumNotificationNumber);
                options.setDaysForLastNotificationDeadline(daysForLastNotificationDeadline);
                options.setDaysForOverdueNotification(daysForOverdueNotification);
                updateNotificationTextView((TextView) getView().findViewById(R.id.notifications_filter_text));
            }
        });
    }

    private void updateNotificationTextView(TextView notificationTextView) {
        if (options.getDaysSinceLastNotification() != null) {
            String notificationText = getActivity().getString(R.string.with_minimum_of) + " " + options.getMinimumNotificationNumber()
                    + " " + getActivity().getString(R.string.notifications_title).toLowerCase()
                    + "\n" + options.getDaysSinceLastNotification().begin + " a " + options.getDaysSinceLastNotification().end
                    + " " + getActivity().getString(R.string.days_since_last_notification_title).toLowerCase()
                    + "\n" + options.getDaysForLastNotificationDeadline().begin +
                    " a " + options.getDaysForLastNotificationDeadline().end + " " +
                    getActivity().getString(R.string.days_for_last_notification_title).toLowerCase()
                    + "\n" + options.getDaysForOverdueNotification().end + " " + getActivity().getString(R.string.days_for_overdue_notification_title).toLowerCase();
            notificationTextView.setTextColor(getResources().getColor(R.color.report_item_text_default));
            notificationTextView.setText(notificationText);
        } else {
            notificationTextView.setText(getActivity().getString(R.string.define_filter_hint));
            notificationTextView.setTextColor(getActivity().getResources().getColor(R.color.document_list_pending_selecting));
        }
    }

    private void cancelDate(TextView dateTextView) {
        dateTextView.setText(getActivity().getString(R.string.define_date_filter));
        dateTextView.setTextColor(getResources().getColor(R.color.document_list_pending_selecting));
    }

    private void updateDate(TextView dateTextView, String toDate) {
        dateTextView.setText(toDate);
        dateTextView.setTextColor(getResources().getColor(R.color.report_item_text_default));
    }

    private void filterStatuses() {
        ReportStatusesMultiSelectorDialog dialog;
        if (options.getQueryMap().containsKey("statuses_ids")) {
            String statusesIds = (String) options.getQueryMap().get("statuses_ids");
            dialog = ReportStatusesMultiSelectorDialog.newInstance(statusesIds.split(", "));
        }else{
            dialog = new ReportStatusesMultiSelectorDialog();
        }
        dialog.show(getActivity().getSupportFragmentManager(), "status_list");
        dialog.setListener(new ReportStatusesMultiSelectorDialog.OnReportStatusesSetListener() {
            @Override
            public void onReportStatusSet(List<ReportCategory.Status> selectedStatuses) {
                options.setStatuses(selectedStatuses);
                updateStatusesTextView((TextView) getView().findViewById(R.id.status_selected), options.getStatuses());

            }
        });
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
        ReportCategoryMultiSelectorDialog dialog = new ReportCategoryMultiSelectorDialog();
        if (options.getQueryMap().containsKey("reports_categories_ids")) {
            String categoryIds = (String) options.getQueryMap().get("reports_categories_ids");
            dialog.setSelectedCategories(categoryIds.split(", "));
        }
        dialog.show(getActivity().getSupportFragmentManager(), "category_list");
        dialog.setListener(new ReportCategoryMultiSelectorDialog.OnReportCategoryMultiSelectPickedListener() {
            @Override
            public void onReportCategoriesSet(List<ReportCategory> categories) {
                options.setCategories(categories);
                updateCategoriesTextView((TextView) getView().findViewById(R.id.cateogies_selected), options.getCategories());
            }
        });
    }

    private void updateCategoriesTextView(TextView textView, String categories) {
        if (categories == null || categories.isEmpty()) {
            textView.setText(getActivity().getString(R.string.all_categories_filter));
        } else {
            textView.setText(categories);
        }
    }

    public void requestedByUsers(View sender) {
        UserMultiSelectPickerDialog dialog = new UserMultiSelectPickerDialog();
        if (options.getQueryMap().containsKey("reporters_ids")) {
            String userIds = (String) options.getQueryMap().get("reporters_ids");
            dialog.setSelectedUsers(userIds.split(", "));
        }
        dialog.show(getActivity().getSupportFragmentManager(), "user_picker");
        dialog.setListener(new UserMultiSelectPickerDialog.OnUserMultiSelectPickedListener() {
            @Override
            public void onUsersPicked(List<User> users) {
                options.setRequestedByUsersList(users);
                updateUsersTextView((TextView) getView().findViewById(R.id.users_requested_selected), options.getRequestedByUsersList());
            }
        });
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
        updateNotificationTextView((TextView) getView().findViewById(R.id.notifications_filter_text));
        updateStatusesTextView((TextView) getView().findViewById(R.id.status_selected), options.getStatuses());
        updateUsersTextView((TextView) getView().findViewById(R.id.users_created_selected), options.getCreatedByUsersList());
        updateCategoriesTextView((TextView) getView().findViewById(R.id.cateogies_selected), options.getCategories());
        updateUsersTextView((TextView) getView().findViewById(R.id.users_requested_selected), options.getRequestedByUsersList());
        ((SwitchCompat) getView().findViewById(R.id.related_to_me_switch)).setChecked(options.isRelatedToMe() != null && options.isRelatedToMe());
        ((SwitchCompat) getView().findViewById(R.id.related_to_my_group_switch)).setChecked(options.isRelatedToMyGroup() != null && options.isRelatedToMyGroup());

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
        private Boolean relatedToMe;
        private Boolean relatedToMyGroup;
        private String initialDate;
        private String finalDate;
        private String createdByUsersList;
        private String requestedByUsersList;
        private String categories;
        private String statuses;
        private int minimumNotificationNumber;
        private Range daysSinceLastNotification;
        private Range daysForLastNotificationDeadline;
        private Range daysForOverdueNotification;
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
            dest.writeValue(relatedToMe);
            dest.writeValue(relatedToMyGroup);
            dest.writeString(initialDate);
            dest.writeString(finalDate);
            dest.writeString(createdByUsersList);
            dest.writeString(requestedByUsersList);
            dest.writeString(categories);
            dest.writeString(statuses);
            dest.writeInt(minimumNotificationNumber);
            dest.writeSerializable(daysSinceLastNotification);
            dest.writeSerializable(daysForLastNotificationDeadline);
            dest.writeSerializable(daysForOverdueNotification);
            dest.writeSerializable(query);
        }

        public FilterOptions(Parcel source) {
            relatedToMe = (Boolean) source.readValue(null);
            relatedToMyGroup = (Boolean) source.readValue(null);
            initialDate = source.readString();
            finalDate = source.readString();
            createdByUsersList = source.readString();
            requestedByUsersList = source.readString();
            categories = source.readString();
            statuses = source.readString();
            minimumNotificationNumber = source.readInt();
            daysSinceLastNotification = (Range) source.readSerializable();
            daysForLastNotificationDeadline = (Range) source.readSerializable();
            daysForOverdueNotification = (Range) source.readSerializable();
            query = (HashMap<String, Object>) source.readSerializable();
        }

        public static class Range implements Serializable {
            public int begin;
            public int end;

            public Range() {
                begin = 0;
                end = 90;
            }
        }

        public FilterOptions() {
            query = new HashMap<String, Object>();
        }

        public Integer getMinimumNotificationNumber() {
            return minimumNotificationNumber;
        }

        public void setMinimumNotificationNumber(int minimumNotificationNumber) {
            this.minimumNotificationNumber = minimumNotificationNumber;
            query.put("minimum_notification_number", minimumNotificationNumber);
        }

        public Range getDaysSinceLastNotification() {
            return daysSinceLastNotification;
        }

        public void setDaysSinceLastNotification(Range daysSinceLastNotification) {
            this.daysSinceLastNotification = daysSinceLastNotification;
            query.put("days_since_last_notification[begin]", daysSinceLastNotification.begin);
            query.put("days_since_last_notification[end]", daysSinceLastNotification.end);
        }

        public Range getDaysForLastNotificationDeadline() {
            return daysForLastNotificationDeadline;
        }

        public void setDaysForLastNotificationDeadline(Range daysForLastNotificationDeadline) {
            this.daysForLastNotificationDeadline = daysForLastNotificationDeadline;
            query.put("days_for_last_notification_deadline[begin]", daysForLastNotificationDeadline.begin);
            query.put("days_for_last_notification_deadline[end]", daysForLastNotificationDeadline.end);
        }

        public Range getDaysForOverdueNotification() {
            return daysForOverdueNotification;
        }

        public void setDaysForOverdueNotification(Range daysForOverdueNotification) {
            this.daysForOverdueNotification = daysForOverdueNotification;
            query.put("days_for_overdue_notification[begin]", daysForOverdueNotification.begin);
            query.put("days_for_overdue_notification[end]", daysForOverdueNotification.end);
        }

        public void setCategories(List<ReportCategory> categories) {
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
            query.put("reports_categories_ids", categoriesId);
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

        public void setRequestedByUsersList(List<User> requestedByUsersList) {
            String usersId = "";
            String usersName = "";
            if (requestedByUsersList != null) {
                for (int index = 0; index < requestedByUsersList.size(); index++) {
                    if (index == requestedByUsersList.size() - 1) {
                        usersId = usersId.concat(String.valueOf(requestedByUsersList.get(index).id));
                        usersName = usersName.concat(String.valueOf(requestedByUsersList.get(index).name));
                    } else {
                        usersId = usersId.concat(String.valueOf(requestedByUsersList.get(index).id) + ", ");
                        usersName = usersName.concat(String.valueOf(requestedByUsersList.get(index).name) + ", ");
                    }
                }
            }
            this.requestedByUsersList = usersName;
            query.put("reporters_ids", usersId);
        }

        public String getStatuses() {
            return statuses;
        }

        public void setStatuses(List<ReportCategory.Status> statuses) {
            String statusesId = "";
            String statusesName = "";
            if (statuses != null) {
                for (int index = 0; index < statuses.size(); index++) {
                    if (index == statuses.size() - 1) {
                        statusesId = statusesId.concat(String.valueOf(statuses.get(index).getId()));
                        statusesName = statusesName.concat(String.valueOf(statuses.get(index).getTitle()));
                    } else {
                        statusesId = statusesId.concat(String.valueOf(statuses.get(index).getId()) + ", ");
                        statusesName = statusesName.concat(String.valueOf(statuses.get(index).getTitle()) + ", ");
                    }
                }
            }
            this.statuses = statusesName;
            query.put("statuses_ids", statusesId);
        }

        public String getCategories() {
            return categories;
        }

        public String getCreatedByUsersList() {
            return createdByUsersList;
        }

        public String getRequestedByUsersList() {
            return requestedByUsersList;
        }

        public Boolean isRelatedToMe() {
            return relatedToMe;
        }

        public void setRelatedToMe(boolean relatedToMe) {
            this.relatedToMe = relatedToMe;
            query.put("assigned_to_me", relatedToMe);
        }

        public Boolean isRelatedToMyGroup() {
            return relatedToMyGroup;
        }

        public void setRelatedToMyGroup(boolean relatedToMyGroup) {
            this.relatedToMyGroup = relatedToMyGroup;
            query.put("assigned_to_my_group", relatedToMyGroup);
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
