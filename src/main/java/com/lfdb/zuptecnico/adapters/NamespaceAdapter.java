package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.lfdb.zuptecnico.entities.Namespace;

/**
 * Created by Renan on 31/08/2016.
 */
public class NamespaceAdapter extends BaseAdapter {
  private Namespace[] namespaces;
  private Context context;

  public NamespaceAdapter(Context context, Namespace[] namespaces) {
    this.namespaces = namespaces;
    this.context = context;
  }

  @Override public int getCount() {
    return namespaces.length;
  }

  @Override public Namespace getItem(int position) {
    return namespaces[position];
  }

  @Override public long getItemId(int position) {
    return getItem(position).getId();
  }

  @Override public View getView(int position, View convertView, ViewGroup parent) {
    if (convertView == null) {
      convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
    }
    TextView title = (TextView) convertView.findViewById(android.R.id.text1);
    title.setText(getItem(position).getName());
    return convertView;
  }
}
