package com.ntxdev.zuptecnico.adapters;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ntxdev.zuptecnico.R;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.User;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by igorlira on 7/18/15.
 */
public class UsersAdapter extends BaseAdapter {
    private Hashtable<Integer, ViewGroup> viewCache;
    protected boolean areMoreItemsAvailable;
    protected View loadingView;

    protected List<User> items;
    protected int pageId; // next page that will be loaded
    private Tasker pageLoader;

    protected Context context;
    protected UserAdapterListener listener;
    private int groupId;

    protected String query;
    private int selectedUserId;
    private boolean usersGroupList;

    public interface UserAdapterListener {
        void onReportsLoaded();
    }

    public void setListener(UserAdapterListener listener) {
        this.listener = listener;
    }

    public UsersAdapter(Context context) {
        usersGroupList = false;
        init(context);
    }

    public UsersAdapter(Context context, int groupId) {
        usersGroupList = true;
        this.groupId = groupId;
        init(context);
    }

    private void init(Context context) {
        this.context = context;

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

    public void load() {
        pageId = 1;
        this.items.clear();
        notifyDataSetInvalidated();

        if (pageLoader != null)
            pageLoader.cancel(true);

        if(usersGroupList) {
            pageLoader = new Tasker(this.query, pageId, groupId);
        } else {
            pageLoader = new Tasker(this.query, pageId);
        }
        pageLoader.execute();
    }

    public void setSelectedUserId(int selectedUserId) {
        this.selectedUserId = selectedUserId;
        notifyDataSetChanged();
    }

    public void setMoreItemsAvailable(boolean value) {
        boolean oldValue = this.areMoreItemsAvailable;
        this.areMoreItemsAvailable = value;

        if (oldValue != value)
            this.notifyDataSetChanged();
    }

    @Override
    public User getItem(int i) {
        if (areMoreItemsAvailable && i == getCount() - 1)
            return null;

        return (items != null && items.size() > i) ? items.get(i) : null;
    }

    @Override
    public long getItemId(int i) {
        User item = getItem(i);
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

        if(usersGroupList) {
            pageLoader = new Tasker(this.query, pageId, groupId);
        } else {
            pageLoader = new Tasker(this.query, pageId);
        }
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

            User item = getItem(position);
            fillData(root, item);
        }

        root.findViewById(R.id.user_selected).setVisibility(
                selectedUserId == getItem(position).id ? View.VISIBLE : View.INVISIBLE);

        return root;
    }

    void fillData(ViewGroup root, User item) {
        TextView txtName = (TextView) root.findViewById(R.id.user_name);
        TextView txtEmail = (TextView) root.findViewById(R.id.user_email);

        txtName.setText(item.name);
        txtEmail.setText(item.email);
    }

    class Tasker extends AsyncTask<Void, Void, User[]> {
        int pageId;
        String query;
        int groupId;
        boolean interrupted;

        public Tasker(String query, int pageId) {
            this.pageId = pageId;
            this.query = query;
        }

        public Tasker(String query, int pageId, int groupId) {
            this.pageId = pageId;
            this.query = query;
            this.groupId = groupId;
        }

        @Override
        protected User[] doInBackground(Void... voids) {
            try {
                Thread.sleep(500); // Give time for user to input more data
                if (usersGroupList) {
                    return Zup.getInstance().getService().retrieveUsersGroup(this.groupId, this.pageId).users;
                }
                if (query != null && query.length() > 0) {
                    return Zup.getInstance().getService().searchUsers(this.query, this.pageId).users;
                }
                return Zup.getInstance().getService().retrieveUsers(this.pageId).users;
            } catch (RetrofitError ex) {
                Log.e("Retrofit", "Could not load users", ex);
                return null;
            } catch (InterruptedException ex) {
                interrupted = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(User[] users) {
            if (users == null) {
                if (!interrupted)
                    UsersAdapter.this.setMoreItemsAvailable(false);
                return;
            }

            for (int i = 0; i < users.length; i++) {
                items.add(users[i]);
            }

            UsersAdapter.this.pageId = this.pageId + 1;

            if (listener != null)
                listener.onReportsLoaded();

            UsersAdapter.this.setMoreItemsAvailable(users.length > 0);
            if (this.pageId == 1)
                UsersAdapter.this.notifyDataSetInvalidated();
            else
                UsersAdapter.this.notifyDataSetChanged();
            pageLoader = null;
        }
    }
}
