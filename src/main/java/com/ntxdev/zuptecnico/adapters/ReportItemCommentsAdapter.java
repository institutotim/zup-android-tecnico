package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.entities.ReportItem;
import com.ntxdev.zuptecnico.util.Utilities;
import in.uncod.android.bypass.Bypass;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by igorlira on 7/24/15.
 */
public class ReportItemCommentsAdapter extends BaseAdapter {
  public static final int FILTER_COMMENTS = 1;
  public static final int FILTER_INTERNAL = 2;

  Context context;
  static ReportItem.Comment[] items;
  SparseArray<View> viewCache;
  int filter;
  Bypass mBypass;

  public ReportItemCommentsAdapter(Context context, ReportItem item, int filter) {
    this.context = context;
    this.viewCache = new SparseArray<>();
    this.filter = filter;
    mBypass = new Bypass();
    this.updateComments(item);
  }

  public void updateComments(ReportItem item) {
    this.items = filterComments(item.comments);
    this.notifyDataSetChanged();
  }

  ReportItem.Comment[] filterComments(ReportItem.Comment[] comments) {
    Set<ReportItem.Comment> result = new HashSet<>();
    if (comments == null) {
      return new ReportItem.Comment[0];
    }
    for (int i = 0; i < comments.length; i++) {
      if (comments[i].visibility == ReportItem.Comment.TYPE_INTERNAL) {
        if (this.filter == FILTER_INTERNAL) result.add(comments[i]);
      } else if (comments[i].visibility == ReportItem.Comment.TYPE_PUBLIC
          || comments[i].visibility == ReportItem.Comment.TYPE_PRIVATE) {
        if (this.filter == FILTER_COMMENTS) result.add(comments[i]);
      }
    }

    ReportItem.Comment[] resultArray = new ReportItem.Comment[result.size()];
    result.toArray(resultArray);

    return resultArray;
  }

  @Override public boolean isEnabled(int position) {
    return false;
  }

  @Override public int getCount() {
    Log.d("COUNT", "" + items.length);
    return items.length;
  }

  @Override public ReportItem.Comment getItem(int i) {
    return items[i];
  }

  @Override public long getItemId(int i) {
    return getItem(i).id;
  }

  @Override public View getView(int i, View v, ViewGroup viewGroup) {
    ReportItem.Comment comment = getItem(i);
    if (v != null) {
      fillData(v, comment);
      return v;
    }
    if (viewCache.get(comment.id) != null) {
      return viewCache.get(comment.id);
    } else {
      LayoutInflater inflater = LayoutInflater.from(context);
      View view = inflater.inflate(R.layout.report_comment_item, viewGroup, false);
      fillData(view, comment);

      return view;
    }
  }

  void fillData(View view, ReportItem.Comment comment) {
    view.findViewById(R.id.fake_comment_indicator)
        .setVisibility(comment.isFake ? View.VISIBLE : View.INVISIBLE);

    TextView txtUsername = (TextView) view.findViewById(R.id.comment_username);
    TextView txtDate = (TextView) view.findViewById(R.id.comment_date);
    TextView txtText = (TextView) view.findViewById(R.id.comment_text);

    String extraDate = "";
    if (comment.visibility == ReportItem.Comment.TYPE_PRIVATE) {
      extraDate = " - " + context.getString(R.string.comment_private);
    } else if (comment.visibility == ReportItem.Comment.TYPE_INTERNAL) {
      if (Build.VERSION.SDK_INT >= 21) {
        txtText.setBackground(context.getDrawable(R.drawable.report_internal_comment_bg));
      } else {
        txtText.setBackgroundDrawable(
            ContextCompat.getDrawable(context, R.drawable.report_internal_comment_bg));
      }
    }

    txtUsername.setText(comment.author.name);
    txtDate.setText(Utilities.formatIsoDateAndTime(comment.created_at) + extraDate);

    String message = transformLinksIntoMarkdownLinks(comment.message);

    CharSequence commentText = mBypass.markdownToSpannable(message);
    txtText.setText(commentText);
    txtText.setMovementMethod(LinkMovementMethod.getInstance());
  }

  private String transformLinksIntoMarkdownLinks(String message) {
    String formattedMessage = message;
    Pattern urlPattern = Pattern.compile(
        "(https?:\\/\\/|)(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)($|\\s)",
        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    Matcher matcher = urlPattern.matcher(message);
    while (matcher.find()) {
      int matchStart = matcher.start(1);
      int matchEnd = matcher.end();
      String url = message.substring(matchStart, matchEnd).trim();
      String formattedUrl = "[" + url + "](" + url + ")";
      formattedMessage = formattedMessage.replace(url, formattedUrl);
    }

    return formattedMessage;
  }
}
