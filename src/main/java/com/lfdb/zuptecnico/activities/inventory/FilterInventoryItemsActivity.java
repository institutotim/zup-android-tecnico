package com.particity.zuptecnico.activities.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.particity.zuptecnico.R;
import com.particity.zuptecnico.fragments.inventory.FilterInventoryFragment;
import com.particity.zuptecnico.ui.UIHelper;

/**
 * Created by Renan on 21/08/2015.
 */
public class FilterInventoryItemsActivity extends AppCompatActivity {
    public static final String FILTER_OPTIONS = "filter_options";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_filter_inventory);
        if(getIntent().getParcelableExtra(FILTER_OPTIONS) != null){
            FilterInventoryFragment fragment = (FilterInventoryFragment) getSupportFragmentManager().findFragmentById(R.id.filter_inventory_fragment);
            fragment.setFilterOptions((FilterInventoryFragment.FilterOptions) getIntent().getParcelableExtra(FILTER_OPTIONS));
        }

        UIHelper.initActivity(this);
        UIHelper.setTitle(this, getString(R.string.filter_inventory));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_reports, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_submit) {
            Intent intent = new Intent();
            FilterInventoryFragment fragment = (FilterInventoryFragment) getSupportFragmentManager().findFragmentById(R.id.filter_inventory_fragment);
            intent.putExtra(FILTER_OPTIONS, fragment.getFilterOptions());
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
