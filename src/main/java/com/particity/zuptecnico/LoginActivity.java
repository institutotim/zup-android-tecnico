package com.particity.zuptecnico;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.particity.zuptecnico.api.Zup;
import com.particity.zuptecnico.entities.Group;
import com.particity.zuptecnico.entities.Session;
import com.particity.zuptecnico.entities.User;
import com.particity.zuptecnico.util.ViewUtils;

import io.fabric.sdk.android.Fabric;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity implements Callback<Session> {
    public static final String EXPIRED_TOKEN = "expiredToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        setContentView(R.layout.activity_login);
        Zup.getInstance().initStorage(this.getApplicationContext());
        boolean isExpired = getIntent().getBooleanExtra(EXPIRED_TOKEN, false);
        getPermissions();
        if (isExpired) {
			Zup.getInstance().clearSessionToken();
            EditText txtLogin = (EditText) findViewById(R.id.txt_login);
            User currentUser = Zup.getInstance().getSessionUser();
            if (currentUser != null) {
                txtLogin.setText(currentUser.email);
            }
            return;
        }
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            ((TextView) findViewById(R.id.version)).setText(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Zup.getInstance().hasSessionToken()) {
            goToLoadingDataActivity();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items_list to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void forgotPassword(View view) {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
    }

    public void login(View view) {
        EditText txtLogin = (EditText) findViewById(R.id.txt_login);
        EditText txtSenha = (EditText) findViewById(R.id.txt_senha);

        String username = txtLogin.getText().toString();
        String password = txtSenha.getText().toString();

        Zup.getInstance().getService().authenticate(username, password, this);
        //Zup.getInstance().tryLogin(username, password, this);

        findViewById(R.id.login_button).setVisibility(View.GONE);
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        txtLogin.setEnabled(false);
        txtSenha.setEnabled(false);

    }

    public void onLoginSuccess() {
        TextView txtLogin = (TextView) findViewById(R.id.txt_login);
        TextView txtSenha = (TextView) findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);

        ViewUtils.hideKeyboard(this, txtLogin.getWindowToken());

        goToLoadingDataActivity();

    }

    private void goToLoadingDataActivity() {
        User user = Zup.getInstance().getSessionUser();
        if (user != null) {
            Crashlytics.setUserIdentifier(String.valueOf(user.id));
            Crashlytics.setUserEmail(user.email);
            Crashlytics.setUserName(user.name);
        }

        Intent intent = new Intent(this.getApplicationContext(), LoadingDataActivity.class);
        this.startActivity(intent);
        finish();
    }

    public void onLoginError(String errorDescription) {
        if (isFinishing()) {
            return;
        }
        TextView txtLogin = (TextView) findViewById(R.id.txt_login);
        TextView txtSenha = (TextView) findViewById(R.id.txt_senha);
        findViewById(R.id.login_button).setVisibility(View.VISIBLE);
        findViewById(R.id.login_progress).setVisibility(View.GONE);
        txtLogin.setEnabled(true);
        txtSenha.setEnabled(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.error_title));
        builder.setMessage(errorDescription);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.lab_ok), null);
        builder.show();
    }

    @Override
    public void success(Session session, Response response) {
        User user = session.user;
        Group[] groups = user.groups;
        if (groups != null) {
            for (int index = 0; index < groups.length; index++) {
                Group group = groups[index];
                if (group.getPermissions().panel_access) {
                    Zup.getInstance().getUserService().addUser(session.user);
                    Zup.getInstance().setSession(this, session);
                    this.onLoginSuccess();
                    return;
                }
            }
        }
        this.onLoginError(getString(R.string.error_user_not_allowed_message));
    }

    @Override
    public void failure(RetrofitError retrofitError) {
        try {
            Session session = (Session) retrofitError.getBodyAs(Session.class);
            if (session != null) {
                Log.e("Error", "Could not login", retrofitError.getCause());
                this.onLoginError(session.error);
            } else {
                this.onLoginError(getString(R.string.error_network));
            }
        } catch (Exception e) {
            this.onLoginError(getString(R.string.error_network));
        }
    }

    private void getPermissions() {
        requestPermissions(new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION},
                                261);
    }
}
