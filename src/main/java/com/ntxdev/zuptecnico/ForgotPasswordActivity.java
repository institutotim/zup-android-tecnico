package com.ntxdev.zuptecnico;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.widget.TextView;
import com.ntxdev.zuptecnico.api.Zup;
import com.ntxdev.zuptecnico.entities.Message;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ForgotPasswordActivity extends AppCompatActivity implements Callback<Message> {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_forgot_password);
    try {
      PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
      String version = pInfo.versionName;
      ((TextView) findViewById(R.id.version)).setText(version);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void back(View view) {
    onBackPressed();
  }

  public void submit(View view) {
    String email = ((EditText) findViewById(R.id.txt_email)).getText().toString().trim();
    if (TextUtils.isEmpty(email)) {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.error_type_email).show();
    } else {
      Zup.getInstance().getService().recoverPassword(email, this);
    }
  }

  @Override public void success(Message s, Response response) {
    if (response.getStatus() == 200) {
      ZupApplication.toast(findViewById(android.R.id.content), R.string.recover_password_success)
          .show();
      if (isFinishing()) {
        return;
      }
      onBackPressed();
    }
  }

  @Override public void failure(RetrofitError error) {
    ZupApplication.toast(findViewById(android.R.id.content), R.string.error_processing_call).show();
    Log.e("Error", "Recovering password " + error.getMessage(), error);
  }
}

