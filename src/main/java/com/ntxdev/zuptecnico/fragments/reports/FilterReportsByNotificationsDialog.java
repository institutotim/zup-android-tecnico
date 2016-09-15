package com.ntxdev.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.appyvet.rangebar.RangeBar;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.fragments.reports.FilterReportsFragment.FilterOptions.Range;

/**
 * Created by Renan on 25/08/2015.
 */
public class FilterReportsByNotificationsDialog extends DialogFragment {
    View confirmButton;
    View cancelButton;
    int minimumNotificationNumber;
    Range daysSinceLastNotification;
    Range daysForLastNotificationDeadline;
    Range daysForOverdueNotification;

    RangeBar daysSinceLastNotificationSeekBar;
    RangeBar daysForLastNotificationDeadlineSeekBar;
    RangeBar daysForOverdueNotificationSeekBar;

    EditText editMaxDaysForLastNotificationDeadline;
    EditText editMaxDaysSinceLastNotification;
    EditText editMaxDaysForOverdueNotification;
    EditText editMinDaysForLastNotificationDeadline;
    EditText editMinDaysSinceLastNotification;

    public interface OnNotificationsFilterSettingsSetListener {
        void onNotificationsFilterSettingsSetListener(int minimumNotificationNumber,
                                                      Range daysSinceLastNotification,
                                                      Range daysForLastNotificationDeadline,
                                                      Range daysForOverdueNotification);
    }

    OnNotificationsFilterSettingsSetListener listener;

    public void setSelectedFilters(int minimumNotificationNumber,
                                   Range daysSinceLastNotification,
                                   Range daysForLastNotificationDeadline,
                                   Range daysForOverdueNotification) {
        this.minimumNotificationNumber = minimumNotificationNumber;
        this.daysForLastNotificationDeadline = daysForLastNotificationDeadline == null ? new Range() : daysForLastNotificationDeadline;
        this.daysForOverdueNotification = daysForOverdueNotification == null ? new Range() : daysForOverdueNotification;
        this.daysSinceLastNotification = daysSinceLastNotification == null ? new Range() : daysSinceLastNotification;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(DialogFragment.STYLE_NO_TITLE);
        return inflater.inflate(R.layout.dialog_filter_by_notification, container, false);
    }

    boolean hasUpdatedEditDaysForOverdueNotification = false;
    boolean hasUpdatedDaysForOverdueNotificationSeekBar = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Range daysSinceLastNotification = new Range();
        Range daysForLastNotificationDeadline = new Range();
        final Range daysForOverdueNotification = new Range();


        daysSinceLastNotificationSeekBar = (RangeBar) view.findViewById(R.id.days_since_last_notification_seekbar);
        daysForLastNotificationDeadlineSeekBar = (RangeBar) view.findViewById(R.id.days_for_last_notification_deadline_seekbar);
        daysForOverdueNotificationSeekBar = (RangeBar) view.findViewById(R.id.days_for_overdue_notification_seekbar);

        //TODO criar comportamentos para cada botão dessa tela (seekbars e edittexts)
        //TODO SeekBar com dois thumbs não existe no nativo do Android!


        editMaxDaysForLastNotificationDeadline = (EditText) view.findViewById(R.id.edit_max_days_for_last_notification_deadline_filter);
        editMaxDaysSinceLastNotification = (EditText) view.findViewById(R.id.edit_max_since_last_notification_filter);
        editMaxDaysForOverdueNotification = (EditText) view.findViewById(R.id.edit_max_days_for_overdue_notification_filter);
        editMinDaysForLastNotificationDeadline = (EditText) view.findViewById(R.id.edit_min_days_for_last_notification_deadline_filter);
        editMinDaysSinceLastNotification = (EditText) view.findViewById(R.id.edit_min_since_last_notification_filter);

        updateDaysSinceLastNotification();
        updateDaysForOverdueNotification();
        updateDaysForLastNotification();


        ((EditText) view.findViewById(R.id.edit_minimum_notifications_filter)).setText(String.valueOf(minimumNotificationNumber));

        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirm();
            }
        });

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

    }

    private void updateDaysForLastNotification() {
        daysForLastNotificationDeadlineSeekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int left, int right, String s, String s1) {
                editMinDaysForLastNotificationDeadline.setText(s);
                editMaxDaysForLastNotificationDeadline.setText(s1);
            }
        });

        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    int currentLeftValue = Integer.parseInt(editMinDaysForLastNotificationDeadline.getText().toString());
                    int rightValue = editMaxDaysForLastNotificationDeadline.getText().toString().isEmpty() ? 0 : Integer.parseInt(editMaxDaysForLastNotificationDeadline.getText().toString());
                    if (rightValue <= currentLeftValue) {
                        if (rightValue == 0) {
                            currentLeftValue = 0;
                            rightValue = 1;
                            editMinDaysForLastNotificationDeadline.setText(String.valueOf(currentLeftValue));
                            editMaxDaysForLastNotificationDeadline.setText(String.valueOf(rightValue));
                        } else if (rightValue < currentLeftValue) {
                            int aux = currentLeftValue;
                            currentLeftValue = rightValue;
                            rightValue = aux;
                            editMinDaysForLastNotificationDeadline.setText(String.valueOf(currentLeftValue));
                            editMaxDaysForLastNotificationDeadline.setText(String.valueOf(rightValue));
                        } else {
                            currentLeftValue = rightValue - 1;
                        }
                    }
                    rightValue = rightValue > 90 ? 90 : rightValue;
                    daysForLastNotificationDeadlineSeekBar.setRangePinsByValue(currentLeftValue, rightValue);
                }

            }
        };
        editMinDaysForLastNotificationDeadline.setOnFocusChangeListener(focusListener);
        editMaxDaysForLastNotificationDeadline.setOnFocusChangeListener(focusListener);
        if (daysForLastNotificationDeadline != null) {
           daysForLastNotificationDeadlineSeekBar.setRangePinsByValue(daysForLastNotificationDeadline.begin, daysForLastNotificationDeadline.end);
        }
    }

    void updateDaysForOverdueNotification() {
        daysForOverdueNotificationSeekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int left, int right, String s, String s1) {
                editMaxDaysForOverdueNotification.setText(s1);
            }
        });
        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int rightValue = editMaxDaysForOverdueNotification.getText().toString().isEmpty() ? 0 : Integer.parseInt(editMaxDaysForOverdueNotification.getText().toString());
                    rightValue = rightValue > 90 ? 90 : rightValue;
                    daysForLastNotificationDeadlineSeekBar.setRangePinsByValue(0, rightValue);
                }

            }
        };
        editMaxDaysForOverdueNotification.setOnFocusChangeListener(focusListener);
        if (daysForOverdueNotification != null) {
            daysForOverdueNotificationSeekBar.setRangePinsByValue(daysForOverdueNotification.begin, daysForOverdueNotification.end);    
        }
        
    }

    void updateDaysSinceLastNotification() {
        daysSinceLastNotificationSeekBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
            @Override
            public void onRangeChangeListener(RangeBar rangeBar, int left, int right, String s, String s1) {
                editMinDaysSinceLastNotification.setText(s);
                editMaxDaysSinceLastNotification.setText(s1);
            }
        });

        View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    int currentLeftValue = Integer.parseInt(editMinDaysSinceLastNotification.getText().toString());
                    int rightValue = editMaxDaysSinceLastNotification.getText().toString().isEmpty() ? 0 : Integer.parseInt(editMaxDaysSinceLastNotification.getText().toString());
                    if (rightValue <= currentLeftValue) {
                        if (rightValue == 0) {
                            currentLeftValue = 0;
                            rightValue = 1;
                            editMinDaysSinceLastNotification.setText(String.valueOf(currentLeftValue));
                            editMaxDaysSinceLastNotification.setText(String.valueOf(rightValue));
                        } else if (rightValue < currentLeftValue) {
                            int aux = currentLeftValue;
                            currentLeftValue = rightValue;
                            rightValue = aux;
                            editMinDaysSinceLastNotification.setText(String.valueOf(currentLeftValue));
                            editMaxDaysSinceLastNotification.setText(String.valueOf(rightValue));
                        } else {
                            currentLeftValue = rightValue - 1;
                        }
                    }
                    rightValue = rightValue > 90 ? 90 : rightValue;
                    daysSinceLastNotificationSeekBar.setRangePinsByValue(currentLeftValue, rightValue);
                }

            }
        };
        editMinDaysSinceLastNotification.setOnFocusChangeListener(focusListener);
        editMaxDaysSinceLastNotification.setOnFocusChangeListener(focusListener);
        if (daysSinceLastNotification != null) {
            daysSinceLastNotificationSeekBar.setRangePinsByValue(daysSinceLastNotification.begin, daysSinceLastNotification.end);
        }
    }

    void confirm() {
        updateData();
        if (this.listener != null) {
            this.listener.onNotificationsFilterSettingsSetListener(minimumNotificationNumber,
                    daysSinceLastNotification, daysForLastNotificationDeadline, daysForOverdueNotification);
        }

        this.dismiss();
    }

    private void updateData() {
        minimumNotificationNumber = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_minimum_notifications_filter)).getText().toString());
        daysForLastNotificationDeadline.begin = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_min_days_for_last_notification_deadline_filter)).getText().toString());
        daysForLastNotificationDeadline.end = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_max_days_for_last_notification_deadline_filter)).getText().toString());
        daysSinceLastNotification.begin = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_min_since_last_notification_filter)).getText().toString());
        daysSinceLastNotification.end = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_max_since_last_notification_filter)).getText().toString());
        daysForOverdueNotification.begin = 0;
        daysForOverdueNotification.end = Integer.parseInt(((EditText) getView().findViewById(R.id.edit_max_days_for_overdue_notification_filter)).getText().toString());

    }

    public void setListener(OnNotificationsFilterSettingsSetListener listener) {
        this.listener = listener;
    }
}
