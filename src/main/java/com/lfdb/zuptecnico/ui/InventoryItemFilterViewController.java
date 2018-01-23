package com.lfdb.zuptecnico.ui;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.entities.InventoryCategory;

/**
 * Created by igorlira on 3/15/15.
 */
public class InventoryItemFilterViewController
{
    public enum FilterType
    {
        Equals,
        GreaterThan,
        LessThan,
        Different,
        Like,
        Between,
        Includes,
        Excludes
    }

    private boolean isSelect;
    private FilterType type;
    private Activity activity;
    private ViewGroup viewGroup;
    private InventoryCategory.Section.Field field;
    private boolean isFilterEnabled;

    public InventoryItemFilterViewController(ViewGroup viewGroup, InventoryCategory.Section.Field field, Activity activity)
    {
        this.activity = activity;
        this.field = field;
        this.viewGroup = viewGroup;
        this.removeFilter();

        this.init();
        this.setMultipleValues(false);

        if(this.field.kind.equals("select") || this.field.kind.equals("radio") || this.field.kind.equals("checkbox"))
        {
            this.setSelect();
        }

        this.setType(this.getAvailableTypes()[0]);
    }

    public void setValues(Object firstV, Object secondV)
    {
        if(isSelect)
        {
            /*Integer[] val = null;

            if(firstV instanceof Integer[])
                val = (Integer[]) firstV;

            int opt = 0;
            if(val != null && val.length > 0)
                opt = val[0];*/

            String opt = "";
            if(firstV != null && firstV instanceof String)
                opt = (String)firstV;

            InventoryCategory.Section.Field.Option option = field.getOptionWithValue(opt);

            TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);
            if(option != null)
            {
                between.setText(option.value);
                between.setTag(option.value);
            }
            else
            {
                between.setText("Selecione");
                between.setTag(null);
            }
        }
        else
        {
            EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
            EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

            first.setTag(firstV);
            second.setTag(secondV);

            if (firstV == null)
                firstV = "";

            if (secondV == null)
                secondV = "";

            first.setText(firstV.toString());
            second.setText(secondV.toString());
        }
    }

    void setSelect()
    {
        isSelect = true;

        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);
        View extra = findViewById(R.id.inventory_item_filter_extra);
        View pointer = findViewById(R.id.inventory_item_filter_select_pointer);
        TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);


        first.setVisibility(View.GONE);
        second.setVisibility(View.GONE);
        extra.setVisibility(View.GONE);
        pointer.setVisibility(View.VISIBLE);
        between.setVisibility(View.VISIBLE);

        between.setText("Selecione");
        between.setTag(null);

        between.setClickable(true);
        between.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectDialog();
            }
        });
    }

    void showSelectDialog()
    {
        TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);

        android.support.v7.widget.PopupMenu menu = new android.support.v7.widget.PopupMenu(this.activity, between);
        int i = 0;
        for(InventoryCategory.Section.Field.Option option : this.field.field_options)
        {
            menu.getMenu().add(0, option.id, i++, option.value);
        }

        menu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                setSelectedValue(menuItem.getItemId());
                return false;
            }
        });

        menu.show();
    }

    void setSelectedValue(int id)
    {
        InventoryCategory.Section.Field.Option option = field.getOption(id);
        if(option == null)
            return;

        //Integer[] ret = new Integer[] { id };

        TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);
        between.setText(option.value);
        between.setTag(option.value);
        //between.setTag(id);
    }

    public Object[] getValues()
    {
        if(isSelect)
        {
            TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);
            if(between.getTag() instanceof Integer)
                return new Integer[] { (Integer)between.getTag(), null };
            else
                return new Object[] { between.getTag(), null };
        }

        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

        Object firstV, secondV;

        if(isDecimal())
        {
            if(first.getText().length() > 0)
                firstV = Double.parseDouble(first.getText().toString());
            else
                firstV = null;

            if(second.getText().length() > 0)
                secondV = Double.parseDouble(second.getText().toString());
            else
                secondV = null;
        }
        else if(isNumeric())
        {
            if(first.getText().length() > 0)
                firstV = Integer.parseInt(first.getText().toString());
            else
                firstV = null;

            if(second.getText().length() > 0)
                secondV = Integer.parseInt(second.getText().toString());
            else
                secondV = null;
        }
        else
        {
            firstV = first.getText().toString();
            secondV = second.getText().toString();
        }

        return new Object[] { firstV, secondV };
    }

    public void setEnabled(boolean enabled)
    {
        this.isFilterEnabled = enabled;

        if(enabled)
            this.addFilter();
        else
            this.removeFilter();
    }

    public boolean isEnabled()
    {
        return this.isFilterEnabled;
    }

    void init()
    {
        this.type = FilterType.Equals;

        TextView textView = (TextView) findViewById(R.id.inventory_item_text_name);
        if (field.label != null) {
            textView.setText(this.field.label.toUpperCase());
        }
        else {
            textView.setText(this.field.title.toUpperCase());
        }

        findViewById(R.id.inventory_item_filter_add_button).setClickable(true);
        findViewById(R.id.inventory_item_filter_add_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFilter();
            }
        });

        findViewById(R.id.inventory_item_filter_remove).setClickable(true);
        findViewById(R.id.inventory_item_filter_remove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFilter();
            }
        });

        findViewById(R.id.inventory_item_filter_type_button).setClickable(true);
        findViewById(R.id.inventory_item_filter_type_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTypeDialog();
            }
        });

        this.setExtra();
        this.setNumeric(isNumeric(), isDecimal());
    }

    void setExtra()
    {
        TextView textView = (TextView) findViewById(R.id.inventory_item_filter_extra);

        String pkgName = this.getClass().getPackage().getName();
        int resId = activity.getResources().getIdentifier("inventory_item_extra_" + field.kind, "string", pkgName);
        if(resId != 0)
        {
            textView.setVisibility(View.VISIBLE);
            textView.setText(activity.getResources().getText(resId));
        }
        else
            textView.setVisibility(View.GONE);
    }

    String getTypeName(FilterType type)
    {
        switch (type)
        {
            case Equals:
                return "Igual a";

            case Different:
                return "Diferente de";

            case Between:
                return "Entre";

            case GreaterThan:
                return "Maior que";

            case LessThan:
                return "Menor que";

            case Like:
                return "Parecido com";

            case Includes:
                return "Inclui";

            case Excludes:
                return "Não inclui";
        }

        return null;
    }

    boolean isNumeric()
    {
        if(field.kind.equals("integer") || field.kind.equals("decimal") || field.kind.equals("meters")
                || field.kind.equals("centimeters") || field.kind.equals("kilometers")
                || field.kind.equals("years") || field.kind.equals("months")
                || field.kind.equals("days") || field.kind.equals("hours")
                || field.kind.equals("seconds") || field.kind.equals("angle"))
            return true;

        return false;
    }

    boolean isDecimal()
    {
        if(field.kind.equals("decimal"))
            return true;

        return false;
    }

    public boolean isArray()
    {
        if(field.kind.equals("checkbox") || field.kind.equals("select"))
            return true;

        return false;
    }

    FilterType[] getAvailableTypes()
    {
        if(field.kind.equals("text"))
        {
            return new FilterType[] { FilterType.Equals, FilterType.Different };
        }
        else if(field.kind.equals("integer") || field.kind.equals("decimal") || field.kind.equals("meters")
                || field.kind.equals("centimeters") || field.kind.equals("kilometers")
                || field.kind.equals("years") || field.kind.equals("months")
                || field.kind.equals("days") || field.kind.equals("hours")
                || field.kind.equals("seconds") || field.kind.equals("angle"))
        {
            return new FilterType[]
                    { FilterType.Equals, FilterType.Different, FilterType.GreaterThan,
                    FilterType.LessThan };
        }
        else if(field.kind.equals("select") || field.kind.equals("checkbox"))
        {
            return new FilterType[]
                    { FilterType.Includes, FilterType.Excludes };
        }
        else if(field.kind.equals("radio"))
        {
            return new FilterType[] { FilterType.Equals, FilterType.Different };
        }

        return new FilterType[] { FilterType.Equals, FilterType.Different };
    }

    public InventoryCategory.Section.Field getField()
    {
        return field;
    }

    public FilterType getType()
    {
        return type;
    }

    public String getTypeString()
    {
        switch (getType())
        {
            case Equals:
                return "equal_to";

            case Like:
                return "like";

            case Between:
                return "between";

            case Different:
                return "different";

            case GreaterThan:
                return "greater_than";

            case Includes:
                return "includes";

            case LessThan:
                return "lesser_than";

            case Excludes:
                return "excludes";
        }

        return "";
    }

    public void setType(FilterType type)
    {
        this.type = type;

        TextView tv = (TextView) findViewById(R.id.inventory_item_filter_type_button);
        tv.setText(getTypeName(type));

        if(this.isSelect)
            return;

        switch (type)
        {
            case Between:
                setMultipleValues(true);
                break;

            default:
                setMultipleValues(false);
                break;
        }
    }

    void createTypeDialog()
    {
        FilterType[] types = getAvailableTypes();

        AlertDialog.Builder builder = new AlertDialog.Builder(this.viewGroup.getContext());
        builder.setTitle("Escolher tipo de filtro");

        View dialogView = this.activity.getLayoutInflater().inflate(R.layout.dialog_select_items, null);
        EditText input = (EditText) dialogView.findViewById(R.id.dialog_select_items_search);

        input.setVisibility(View.GONE);

        builder.setView(dialogView);
        final AlertDialog dialog = builder.show();

        ViewGroup container = (ViewGroup) dialogView.findViewById(R.id.dialog_select_items_container);

        for(FilterType type : types)
        {
            View separator = new View(this.viewGroup.getContext());
            separator.setBackgroundColor(0xffcccccc);
            separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));


            TextView itemView = new TextView(this.viewGroup.getContext());
            itemView.setClickable(true);
            itemView.setText(getTypeName(type));
            itemView.setBackgroundResource(R.drawable.sidebar_cell);
            itemView.setPadding(20, 20, 20, 20);
            itemView.setTag(type);

            container.addView(separator);
            container.addView(itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setType((FilterType)view.getTag());
                    dialog.dismiss();
                }
            });
        }
    }

    void setNumeric(boolean numeric, boolean decimal)
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);

        int type = 0;
        if(numeric)
            type |= InputType.TYPE_CLASS_NUMBER;
        else
            type |= InputType.TYPE_CLASS_TEXT;

        if(decimal)
            type |= InputType.TYPE_NUMBER_FLAG_DECIMAL;

        first.setInputType(type);
        second.setInputType(type);
    }

    void setMultipleValues(boolean multipleValues)
    {
        EditText first = (EditText) findViewById(R.id.inventory_item_filter_first);
        EditText second = (EditText) findViewById(R.id.inventory_item_filter_second);
        TextView between = (TextView) findViewById(R.id.inventory_item_filter_between);

        if(multipleValues)
        {
            first.setVisibility(View.VISIBLE);
            second.setVisibility(View.VISIBLE);
            between.setVisibility(View.VISIBLE);

            first.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
            second.setLayoutParams(new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        else
        {
            first.setVisibility(View.VISIBLE);
            second.setVisibility(View.GONE);
            between.setVisibility(View.GONE);

            first.setLayoutParams(new LinearLayout.LayoutParams(230, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
    }

    void addFilter()
    {
        isFilterEnabled = true;

        if(isSelect)
        {
            findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.GONE);
            findViewById(R.id.inventory_item_filter_fields).setVisibility(View.VISIBLE);
        }
        else
        {
            findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.GONE);
            findViewById(R.id.inventory_item_filter_fields).setVisibility(View.VISIBLE);
        }
    }

    void removeFilter()
    {
        isFilterEnabled = false;

        if(isSelect)
        {
            findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.VISIBLE);
            findViewById(R.id.inventory_item_filter_fields).setVisibility(View.GONE);
        }
        else
        {
            findViewById(R.id.inventory_item_filter_add_button).setVisibility(View.VISIBLE);
            findViewById(R.id.inventory_item_filter_fields).setVisibility(View.GONE);
        }
    }

    View findViewById(int id)
    {
        return viewGroup.findViewById(id);
    }
}
