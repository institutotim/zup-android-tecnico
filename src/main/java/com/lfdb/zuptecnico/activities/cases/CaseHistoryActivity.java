package com.particity.zuptecnico.activities.cases;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.ZupApplication;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.collections.CaseHistoryCollection;
import com.particity.zuptecnico.entities.collections.CaseHistoryItem;
import com.particity.zuptecnico.fragments.reports.ReportItemHistoryFragment;
import com.particity.zuptecnico.ui.UIHelper;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CaseHistoryActivity extends AppCompatActivity implements Callback<CaseHistoryCollection> {
    ReportItemHistoryFragment history;
    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_item_details);
        UIHelper.initActivity(this);

        int caseId = getIntent().getIntExtra("case_id", -1);
        loadItem(caseId);
    }

    void loadItem(int id) {
        if (id == -1) {
            ZupApplication.toast(findViewById(android.R.id.content),  R.string.not_found_item_error).show();
            finish();
            return;
        } else {
            Zup.getInstance().getService().retrieveCaseItemHistory(id, this);
        }
        showLoading();
    }

    void showLoading() {
        findViewById(R.id.wait_sync_standard_message).setVisibility(View.VISIBLE);
        findViewById(R.id.report_loading).setVisibility(View.VISIBLE);
    }

    void hideLoading() {
        findViewById(R.id.wait_sync_standard_message).setVisibility(View.GONE);
        findViewById(R.id.report_loading).setVisibility(View.GONE);
    }

    void itemLoaded() {
        hideLoading();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        history = new ReportItemHistoryFragment();
        history.setArguments(this.bundle);
        transaction.add(R.id.listView, history, "history");

        try {
            transaction.commit();
        } catch (Exception ex) {
            Log.e("Error", ex.getMessage(), ex);
        }
    }

    @Override
    public void success(CaseHistoryCollection caseHistoryCollection, Response response) {
        CaseHistoryItem[] histories = caseHistoryCollection.histories;
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putParcelableArray("history", histories);
        itemLoaded();
    }

    @Override
    public void failure(RetrofitError error) {

    }
}