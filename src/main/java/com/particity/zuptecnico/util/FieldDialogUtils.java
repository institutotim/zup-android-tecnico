package com.particity.zuptecnico.util;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.entities.InventoryCategory;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Renan on 28/10/2015.
 */
public class FieldDialogUtils {
    public static DatePickerDialog createDatePickerDialog(Context context, final InventoryCategory.Section.Field field, final View fieldView) {
        String label;
        if (field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        Calendar cal = Calendar.getInstance();

        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        final String oldstring = fieldValue.getText().toString();
        final String oldvalue = (String) fieldValue.getTag();
        if (oldvalue != null) {
            try {
                String[] chunks = oldvalue.split("/");

                cal.set(Integer.parseInt(chunks[2]), Integer.parseInt(chunks[1]) - 1, Integer.parseInt(chunks[0]));
            } catch (Exception ex) {
            }
        }

        final DatePickerDialog dialog;
        dialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;

                String daystr = Integer.toString(day);
                if (daystr.length() < 2)
                    daystr = "0" + daystr;

                String monthstr = Integer.toString(month);
                if (monthstr.length() < 2)
                    monthstr = "0" + monthstr;

                String text = daystr + "/" + monthstr + "/" + year;

                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(text);
                fieldValue.setTag(text);

            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(oldstring);
                fieldValue.setTag(oldvalue);
            }
        });

        dialog.setTitle(label);

        dialog.setCancelable(true);
        return dialog;
    }

    public static TimePickerDialog createTimePickerDialog(Context context, final InventoryCategory.Section.Field field, final View fieldView) {
        String label;
        if (field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
        final String oldstring = fieldValue.getText().toString();
        final String oldvalue = (String) fieldValue.getTag();
        if (oldvalue != null) {
            try {
                String[] chunks = oldvalue.split(":");

                hour = Integer.parseInt(chunks[0]);
                minute = Integer.parseInt(chunks[1]);
            } catch (Exception ex) {
            }
        }

        final TimePickerDialog dialog = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                String hourstr = Integer.toString(hour);
                if (hourstr.length() < 2)
                    hourstr = "0" + hourstr;

                String minutestr = Integer.toString(minute);
                if (minutestr.length() < 2)
                    minutestr = "0" + minutestr;

                String text = hourstr + ":" + minutestr;

                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(text);
                fieldValue.setTag(text);
            }
        }, hour, minute, true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(oldstring);
                fieldValue.setTag(oldvalue);
            }
        });

        dialog.setTitle(label);

        dialog.setCancelable(true);
        return dialog;
    }

    public static AlertDialog createSelectDialog(Context activity, final InventoryCategory.Section.Field field, final View fieldView, LayoutInflater inflater) {
        String label;
        if (field.label != null) {
            label = field.label;
        } else {
            label = field.title;
        }

        final View view = inflater.inflate(R.layout.dialog_select_items, null);
        final EditText input = (EditText) view.findViewById(R.id.dialog_select_items_search);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(label);
        builder.setView(view);

        builder.setCancelable(true);
        builder.setNegativeButton(R.string.cancel, null);

        ArrayList<InventoryCategory.Section.Field.Option> items = new ArrayList<>();
        if (field.field_options != null) {
            for (int i = 0; i < field.field_options.length; i++) {
                InventoryCategory.Section.Field.Option option = field.field_options[i];
                if (option.disabled) {
                    continue;
                }
                items.add(option);
            }
        }
        final AlertDialog dialog = builder.create();
        final ArrayAdapter<InventoryCategory.Section.Field.Option> adapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_list_item_1, items);
        final ListView listView = (ListView) view.findViewById(R.id.dialog_select_items_container);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                InventoryCategory.Section.Field.Option option = adapter.getItem(position);
                TextView fieldValue = (TextView) fieldView.findViewById(R.id.inventory_item_text_value);
                fieldValue.setText(option.value);
                fieldValue.setTag(option.id);
                dialog.dismiss();
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                adapter.getFilter().filter(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        return dialog;
    }
}
