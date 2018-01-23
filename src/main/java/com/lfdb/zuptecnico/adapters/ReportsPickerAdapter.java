package com.lfdb.zuptecnico.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lfdb.zuptecnico.R;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.ReportCategory;
import com.lfdb.zuptecnico.entities.ReportItem;
import com.lfdb.zuptecnico.entities.collections.ReportItemCollection;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by Renan on 04/01/2016.
 */
public class ReportsPickerAdapter extends BaseAdapter {
    private Hashtable<Integer, ViewGroup> viewCache;
    protected boolean areMoreItemsAvailable;
    protected View loadingView;

    protected List<ReportItem> items;
    protected int pageId; // next page that will be loaded
    private Tasker pageLoader;

    protected Context context;
    protected ReportsPickerAdapterListener listener;

    protected String query;
    private List<Integer> selectedReportsId;

    public interface ReportsPickerAdapterListener {
        void onReportsLoaded();
    }

    public int getSelectedReportsCount() {
        if(selectedReportsId == null) {
            return 0;
        }
        return selectedReportsId.size();
    }


    public void setListener(ReportsPickerAdapterListener listener) {
        this.listener = listener;
    }

    public ReportsPickerAdapter(Context context) {
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        selectedReportsId = new ArrayList<>();
        items = new ArrayList<>();
        viewCache = new Hashtable<>();
        areMoreItemsAvailable = false;
        pageId = 1;
    }

    public void setQuery(String query) {
        this.query = query;

        areMoreItemsAvailable = true;
        load();
    }

    public void clearSelection(){
        selectedReportsId.clear();
        notifyDataSetInvalidated();
    }

    public void load() {
        pageId = 1;
        this.items.clear();
        notifyDataSetInvalidated();

        if (pageLoader != null) {
            pageLoader.cancel(true);
        }
        pageLoader = new Tasker(this.query, pageId);
        pageLoader.execute();
    }

    public void setSelectedReportsId(List<Integer> reportsId) {
        if(selectedReportsId == null) {
            selectedReportsId = new ArrayList<>();
        }
        selectedReportsId.addAll(reportsId);
        notifyDataSetInvalidated();
    }

    public List<ReportItem> getSelectedReports(){
        List<ReportItem> reports = new ArrayList<>();
        if(selectedReportsId != null) {
            for (int index = 0; index < selectedReportsId.size(); index++) {
                for (int j = 0; j < getCount(); j++) {
                    if (getItemId(j) == selectedReportsId.get(index)) {
                        reports.add(getItem(j));
                        break;
                    }
                }
            }
        }
        return reports;
    }

    public void setSelectedReportId(Integer reportId) {
        if (selectedReportsId.contains(reportId)) {
            selectedReportsId.remove(reportId);
        } else {
            selectedReportsId.add(reportId);
        }
        notifyDataSetInvalidated();
    }

    public void setMoreItemsAvailable(boolean value) {
        boolean oldValue = this.areMoreItemsAvailable;
        this.areMoreItemsAvailable = value;

        if (oldValue != value)
            this.notifyDataSetChanged();
    }

    @Override
    public ReportItem getItem(int i) {
        if (areMoreItemsAvailable && i == getCount() - 1)
            return null;

        return (items != null && items.size() > i) ? items.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        ReportItem item = getItem(i);
        if (item != null)
            return item.id;
        else
            return 0;
    }

    @Override
    public int getCount() {
        int count = items.size();
        if (areMoreItemsAvailable)
            count++; // the loading item

        return count;
    }

    void loadMore() {
        if (pageLoader != null)
            return;

        pageLoader = new Tasker(this.query, pageId);
        pageLoader.execute();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext();
        if (areMoreItemsAvailable && position == getCount() - 1) { // loading
            loadMore();

            if (loadingView != null)
                return loadingView;
            else {
                LayoutInflater inflater = LayoutInflater.from(context);
                return loadingView = inflater.inflate(R.layout.listview_loadingmore, parent, false);
            }
        }

        ViewGroup root;
        if (viewCache.get((int) getItemId(position)) != null)
            root = viewCache.get((int) getItemId(position));
        else {
            LayoutInflater inflater = LayoutInflater.from(context);
            root = (ViewGroup) inflater.inflate(R.layout.user_list_item, parent, false);

            viewCache.put((int) getItemId(position), root);

            ReportItem item = getItem(position);
            fillData(root, item);
        }

        root.findViewById(R.id.user_selected).setVisibility(
                selectedReportsId.contains(getItem(position).id) ? View.VISIBLE : View.INVISIBLE);

        return root;
    }

    void fillData(ViewGroup root, ReportItem item) {
        TextView txtName = (TextView) root.findViewById(R.id.user_name);
        TextView txtEmail = (TextView) root.findViewById(R.id.user_email);

        ReportCategory category = Zup.getInstance().getReportCategoryService().getReportCategory(item.category_id);
        if (category != null) {
            txtName.setText(category.title);
        } else {
            txtName.setText("");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(context.getString(R.string.protocol_title));
        builder.append(" ");
        builder.append(item.id);
        builder.append(" - ");
        builder.append(item.address);
        builder.append(TextUtils.isEmpty(item.number) ? ", " : ", " + item.number + " - ");
        builder.append(TextUtils.isEmpty(item.district) ? item.city : item.district + " - " + item.city);
        txtEmail.setText(builder.toString());
    }

    class Tasker extends AsyncTask<Void, Void, ReportItem[]> {
        int pageId;
        String query;
        boolean interrupted;

        public Tasker(String query, int pageId) {
            this.pageId = pageId;
            this.query = query;
        }


        @Override
        protected ReportItem[] doInBackground(Void... voids) {
            try {
                Thread.sleep(500); // Give time for user to input more data
                ReportItemCollection reports = Zup.getInstance().getService().retrieveReportItemsByAddressOrProtocol(pageId, query);
                return reports.reports;
            } catch (RetrofitError ex) {
                Log.e("Retrofit", "Could not load users", ex);
                return null;
            } catch (InterruptedException ex) {
                interrupted = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(ReportItem[] reports) {
            if (reports == null) {
                if (!interrupted)
                    ReportsPickerAdapter.this.setMoreItemsAvailable(false);
                return;
            }

            for (int i = 0; i < reports.length; i++) {
                items.add(reports[i]);
            }

            ReportsPickerAdapter.this.pageId = this.pageId + 1;

            if (listener != null)
                listener.onReportsLoaded();

            ReportsPickerAdapter.this.setMoreItemsAvailable(reports.length > 0);
            if (this.pageId == 1)
                ReportsPickerAdapter.this.notifyDataSetInvalidated();
            else
                ReportsPickerAdapter.this.notifyDataSetChanged();
            pageLoader = null;
        }
    }
}
