package com.ntxdev.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.ReportCategory;
import com.ntxdev.zuptecnico.entities.ReportItem;

/**
 * Created by Renan on 10/02/2016.
 */
public class ReportItemExtraInfoFragment extends Fragment {
  ReportItem getItem() {
    return (ReportItem) getArguments().getParcelable("item");
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    ViewGroup root =
        (ViewGroup) inflater.inflate(R.layout.fragment_report_details_extra_info, container, false);
    fillData(root);
    return root;
  }

  public void refresh() {
    fillData((ViewGroup) getView());
  }

  void fillData(ViewGroup root) {
    ReportItem item = getItem();
    ReportCategory category =
        Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
    if (category.custom_fields == null || category.custom_fields.length == 0) {
      root.setVisibility(View.GONE);
      return;
    }
    ViewGroup container = (ViewGroup) root.findViewById(R.id.container);
    container.removeAllViews();

    TextView sectionTitle =
        new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardTitle));
    sectionTitle.setText(R.string.extra_info);

    container.addView(sectionTitle);
    for (int index = 0; index < category.custom_fields.length; index++) {
      int id = category.custom_fields[index].id;
      String title = category.custom_fields[index].title;
      String value = notInformedIfBlank(item.custom_fields == null ? null : item.custom_fields.get(id));
      TextView mTitleTV =
          new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyTitle));
      LinearLayout.LayoutParams titleParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      titleParams.topMargin = 20;
      mTitleTV.setLayoutParams(titleParams);

      TextView mValueTV =
          new TextView(new ContextThemeWrapper(getActivity(), R.style.ReportCardPropertyValue));
      LinearLayout.LayoutParams valueParams =
          new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
              ViewGroup.LayoutParams.WRAP_CONTENT);
      valueParams.topMargin = 3;
      mValueTV.setLayoutParams(valueParams);

      mTitleTV.setText(title);
      mValueTV.setText(value);

      container.addView(mTitleTV);
      container.addView(mValueTV);
    }
  }

  String notInformedIfBlank(String value) {
    if (TextUtils.isEmpty(value)) {
      return getActivity().getString(R.string.not_informed);
    }
    return value;
  }
}
