package com.lfdb.zuptecnico.fragments.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.adapters.ReportItemCommentsAdapter;
import com.lfdb.zuptecnico.api.sync.PublishReportCommentSyncAction;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.ui.ScrollLessListView;

/**
 * Created by igorlira on 7/18/15.
 */
public class ReportItemCommentsFragment extends Fragment
    implements ReportItemCommentDialog.OnCommentListener {
  ReportItemCommentsAdapter adapter;

  ReportItem getItem() {
    return (ReportItem) getArguments().getParcelable("item");
  }

  int getFilterType() {
    return getArguments().getInt("filter_type", ReportItemCommentsAdapter.FILTER_COMMENTS);
  }

  public void refresh(ReportItem item) {
    if (!isAdded()) {
      return;
    }
    if (adapter == null && getView() != null) {
      this.adapter = new ReportItemCommentsAdapter(getActivity(), getItem(), this.getFilterType());
      ScrollLessListView listView =
          (ScrollLessListView) getView().findViewById(R.id.report_comments_listview);
      listView.setAdapter(this.adapter);
    } else {
      this.adapter.updateComments(item);
    }
    hideCreatingProgress();
    updateVisibility(getView());
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_report_details_comments, container, false);
    return root;
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    TextView txtTitle = (TextView) view.findViewById(R.id.comments_title);
    View createBtn = view.findViewById(R.id.comment_create);
    if (getFilterType() == ReportItemCommentsAdapter.FILTER_INTERNAL) {
      txtTitle.setText(getActivity().getString(R.string.internal_comments_title));
      if (!Zup.getInstance()
          .getAccess()
          .canCreateInternalCommentOnReportItem(getItem().category_id)) {
        createBtn.setVisibility(View.GONE);
      }
    } else {
      if (!Zup.getInstance().getAccess().canCreateCommentOnReportItem(getItem().category_id)) {
        createBtn.setVisibility(View.GONE);
      }
    }

    createBtn.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        showCreateDialog();
      }
    });
    fillData(view);
    this.hideCreatingProgress();
  }

  private boolean canCreate() {
    boolean canCreate;
    switch (getFilterType()) {
      case ReportItemCommentsAdapter.FILTER_COMMENTS:
      default:
        canCreate =
            Zup.getInstance().getAccess().canCreateCommentOnReportItem(getItem().category_id);
        break;

      case ReportItemCommentsAdapter.FILTER_INTERNAL:
        canCreate = Zup.getInstance()
            .getAccess()
            .canCreateInternalCommentOnReportItem(getItem().category_id);
        break;
    }

    return canCreate;
  }

  void showCreateDialog() {
    ReportItemCommentDialog dialog = new ReportItemCommentDialog();
    dialog.setListener(this);
    dialog.setHasType(this.getFilterType() != ReportItemCommentsAdapter.FILTER_INTERNAL);
    dialog.show(getChildFragmentManager(), "create_dialog");
  }

  void fillData(View root) {
    if (getItem() == null) return;
    ScrollLessListView listView =
        (ScrollLessListView) root.findViewById(R.id.report_comments_listview);
    this.adapter = new ReportItemCommentsAdapter(getActivity(), getItem(), this.getFilterType());
    listView.setAdapter(this.adapter);
    updateVisibility(root);
  }

  private void updateVisibility(View root) {
    ScrollLessListView listView =
        (ScrollLessListView) root.findViewById(R.id.report_comments_listview);

    root.findViewById(R.id.empty_list)
        .setVisibility(!(canCreate()) && adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    listView.setVisibility(adapter.getCount() == 0 ? View.GONE : View.VISIBLE);
  }

  void showCreatingProgress() {
    if (getView() == null) return;

    View progress = getView().findViewById(R.id.comment_create_progress);
    View button = getView().findViewById(R.id.comment_create);

    progress.setVisibility(View.VISIBLE);
    button.setVisibility(View.GONE);
  }

  void hideCreatingProgress() {
    if (getView() == null) return;

    View progress = getView().findViewById(R.id.comment_create_progress);
    View button = getView().findViewById(R.id.comment_create);

    progress.setVisibility(View.GONE);
    button.setVisibility(canCreate() ? View.VISIBLE : View.GONE);
  }

  @Override public void onComment(int type, String text) {
    if (getItem() == null) return;

    showCreatingProgress();

    PublishReportCommentSyncAction syncAction =
        new PublishReportCommentSyncAction(getItem().id, type, text);
    Zup.getInstance().getSyncActionService().addSyncAction(syncAction);
    Zup.getInstance().sync();
  }
}
