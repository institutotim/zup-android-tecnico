package com.particity.zuptecnico.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.particity.zuptecnico.R;
import com.particity.zuptecnico.activities.reports.ReportItemDetailsActivity;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.ReportCategory;
import com.particity.zuptecnico.entities.ReportItem;
import java.util.List;

/**
 * Created by renan on 08/06/16.
 */
public class CaseRelatedReportsAdapter extends BaseExpandableListAdapter {
  Context mContext;
  List<ReportItem> mItems;

  class FilledCustomField {
    public int id;
    public String title;
    public String value;
  }

  public CaseRelatedReportsAdapter(Context context, List<ReportItem> items) {
    mItems = items;
    mContext = context;
  }

  @Override public int getGroupCount() {
    return mItems == null ? 0 : mItems.size();
  }

  @Override public int getChildrenCount(int groupPosition) {
    ReportItem report = getGroup(groupPosition);
    if (report == null) {
      return 0;
    }
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(report.category_id);
    if (category == null || category.custom_fields == null) {
      return 0;
    }
    return category.custom_fields.length + 1;
  }

  @Override public ReportItem getGroup(int groupPosition) {
    return mItems == null || mItems.size() <= groupPosition ? null : mItems.get(groupPosition);
  }

  @Override public ReportCategory.CustomField getChild(int groupPosition, int childPosition) {
    ReportItem item = getGroup(groupPosition);
    if (item == null) {
      return null;
    }
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category == null
        || category.custom_fields == null
        || category.custom_fields.length <= childPosition) {
      return null;
    }
    return category.custom_fields[childPosition];
  }

  @Override public long getGroupId(int groupPosition) {
    ReportItem item = getGroup(groupPosition);
    return item == null ? 0 : item.id;
  }

  @Override public long getChildId(int groupPosition, int childPosition) {
    ReportCategory.CustomField field = getChild(groupPosition, childPosition);
    if (field == null) {
      return 0;
    }
    return field.id;
  }

  @Override public boolean hasStableIds() {
    return false;
  }

  @Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
      ViewGroup parent) {
    ReportItem item = getGroup(groupPosition);
    if (convertView == null) {
      LayoutInflater infalInflater =
          (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = infalInflater.inflate(R.layout.group_list_item, null);
    }
    if (item == null) {
      return convertView;
    }
    TextView titleView = (TextView) convertView.findViewById(R.id.user_name);
    String title = mContext.getString(R.string.activity_title_report_item) + " #" + item.id;
    titleView.setText(title);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) titleView.getLayoutParams();
    params.leftMargin =
        mContext.getResources().getDimensionPixelSize(R.dimen.report_card_padding_x);
    titleView.setLayoutParams(params);
    titleView.setTextColor(ContextCompat.getColor(mContext, R.color.zupblue));
    convertView.findViewById(R.id.user_selected).setVisibility(View.GONE);
    convertView.findViewById(R.id.user_selected_checkbox).setVisibility(View.GONE);

    boolean isLastItem = getGroupCount() == groupPosition + 1;
    convertView.findViewById(R.id.divider_list)
        .setVisibility(isLastItem ? View.GONE : View.VISIBLE);

    return convertView;
  }

  @Override public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
      View convertView, ViewGroup parent) {
    ReportCategory.CustomField field = getChild(groupPosition, childPosition);
    final ReportItem item = getGroup(groupPosition);

    if (convertView == null) {
      LayoutInflater infalInflater =
          (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = infalInflater.inflate(R.layout.related_report_case_item, null);
    }

    if (isLastChild) {
      convertView.findViewById(R.id.access_report).setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          Intent intent = new Intent(mContext, ReportItemDetailsActivity.class);
          intent.putExtra("item_id", item.id);
          mContext.startActivity(intent);
        }
      });
    }

    TextView titleView = (TextView) convertView.findViewById(R.id.user_name);
    TextView valueView = (TextView) convertView.findViewById(R.id.user_email);

    if (field != null) {
      titleView.setText(field.title);
      String value = item.custom_fields.get(field.id);
      valueView.setText(TextUtils.isEmpty(value) ? "-" : value);
    }

    titleView.setVisibility(isLastChild ? View.GONE : View.VISIBLE);
    valueView.setVisibility(isLastChild ? View.GONE : View.VISIBLE);
    convertView.findViewById(R.id.access_report).setVisibility(isLastChild ? View.VISIBLE : View.GONE);

    isLastChild = isLastChild || (getChildrenCount(groupPosition) == childPosition + 2);

    convertView.findViewById(R.id.divider_list)
        .setVisibility(isLastChild ? View.INVISIBLE : View.VISIBLE);
    return convertView;
  }

  @Override public boolean isChildSelectable(int groupPosition, int childPosition) {
    return false;
  }
}
