package com.lfdb.zuptecnico;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lfdb.zuptecnico.activities.RootActivity;
import com.lfdb.zuptecnico.adapters.NamespaceAdapter;
import com.lfdb.zuptecnico.api.Zup;
import com.lfdb.zuptecnico.entities.Namespace;
import com.lfdb.zuptecnico.entities.User;
import com.lfdb.zuptecnico.ui.UIHelper;

public class ProfileActivity extends RootActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_profile);

    Zup.getInstance().initStorage(getApplicationContext());

    User user = Zup.getInstance().getSessionUser();

    TextView txtName = (TextView) findViewById(R.id.profile_name);
    final TextView txtNamespace = (TextView) findViewById(R.id.txt_profile_namespace);
    TextView txtEmail = (TextView) findViewById(R.id.txt_profile_email);

    txtName.setText(user.name);
    txtEmail.setText(user.email);
    String namespace = Zup.getInstance().getNamespaceName(this);
    if (!TextUtils.isEmpty(namespace)) {
      txtNamespace.setText(namespace);
    } else {
      findViewById(R.id.namespace_layout).setVisibility(View.GONE);
    }

    findViewById(R.id.change_namespace).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setCancelable(true);
        builder.setTitle("Escoha a localidade");
        Namespace[] namespaces = Zup.getInstance().getNamespaceService().getNamespaces();
        if (namespaces == null) {
          return;
        }
        final NamespaceAdapter adapter = new NamespaceAdapter(ProfileActivity.this, namespaces);
        builder.setSingleChoiceItems(adapter, -1, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            Namespace namespace = adapter.getItem(which);
            Zup.getInstance().setNamespace(namespace);
            txtNamespace.setText(namespace.getName());
            dialog.dismiss();
          }
        });
        builder.show();
      }
    });

    UIHelper.initActivity(this);
    UIHelper.setTitle(this, user.name);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.profile, menu);
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  public void logout(View view) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(true);
    if (Zup.getInstance().getSyncActionService().hasPendingSyncActions()) {
      builder.setTitle(R.string.error_title);
      builder.setMessage(getString(R.string.error_pending_sync_logout));
      builder.setPositiveButton(R.string.lab_ok, null);
    } else {
      builder.setTitle(R.string.usure);
      builder.setMessage(getString(R.string.logout_alert_message));

      builder.setPositiveButton(R.string.logout_button, new DialogInterface.OnClickListener() {
        @Override public void onClick(DialogInterface dialogInterface, int i) {
          confirmlogout();
        }
      });
      builder.setNegativeButton(R.string.cancel, null);
    }

    builder.show();
  }

  void confirmlogout() {
    Zup.getInstance().clearStorage(this);

    Intent intent = new Intent(this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    this.startActivity(intent);
  }

  @Override public void updateDrawerStatus() {

  }
}
