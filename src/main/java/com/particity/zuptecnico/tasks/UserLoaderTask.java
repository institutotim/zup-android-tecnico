package com.particity.zuptecnico.tasks;

import android.os.AsyncTask;
import android.widget.TextView;

import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.User;

/**
 * Created by igorlira on 8/8/14.
 */
public class UserLoaderTask extends AsyncTask<Integer, Void, User> {
    TextView textView;
    String prefix;

    int userId;

    public UserLoaderTask(TextView textView, String prefix) {
        this.textView = textView;
        this.prefix = prefix;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        textView.setText(prefix + "Carregando...");
    }

    @Override
    protected User doInBackground(Integer... integers) {
        this.userId = integers[0];
        try {
            return Zup.getInstance().getService().retrieveUser(userId).user;
        } catch (RuntimeException error) {
            error.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(User user) {
        super.onPostExecute(user);
        if (user != null) {
            textView.setText(prefix + user.name);
        } else {
            textView.setText(prefix + "#" + userId);
        }
    }
}
