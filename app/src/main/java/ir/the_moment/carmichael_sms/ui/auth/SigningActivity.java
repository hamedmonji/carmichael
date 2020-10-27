package ir.the_moment.carmichael_sms.ui.auth;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.intro.IntroActivity;
import ir.the_moment.carmichael_sms.ui.lock.LockActivity;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;

public class SigningActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int RC_INTRO = 1004;
    private static final int RC_PLAY_SERVICES = 1005;
    private String[] requiredPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signing);
        PreferenceManager.setDefaultValues(this, R.xml.prefs,false);

        if (!isSignedIn()) {
            Intent introIntent = new Intent(this, IntroActivity.class);
            startActivityForResult(introIntent,RC_INTRO);
            return;
        }

        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayServicesAvailable();
    }

    private void showPlayServicesErrorDialog(GoogleApiAvailability googleApiAvailability, int playServicesAvailability) {
        Dialog errorDialog =  googleApiAvailability.getErrorDialog(this,playServicesAvailability,RC_PLAY_SERVICES);
        errorDialog.setCancelable(false);
        errorDialog.show();
    }

    private boolean isSignedIn() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.key_pref_user_name),null) != null;
    }

    private void init() {

        if (!isPlayServicesAvailable()) return;

        Button begin = findViewById(R.id.begin);

        begin.setOnClickListener(this);

        if (isSignedIn()){
            if (!Utility.deviceHasSimCard(this)) {
                showNoSim();
            }else {
                startLockActivity();
            }
        }else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
                requiredPermissions = new String[]{Manifest.permission.READ_SMS
                        ,Manifest.permission.SEND_SMS
                        ,Manifest.permission.RECEIVE_SMS
                        ,Manifest.permission.CAMERA
                        ,Manifest.permission.READ_PHONE_STATE
                        ,Manifest.permission.READ_EXTERNAL_STORAGE
                        ,Manifest.permission.INTERNET};
                getRequiredPermissions();
            }else {
                if (!Utility.deviceHasSimCard(this)) {
                    showNoSim();
                }
            }
        }
    }

    private boolean isPlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int playServicesAvailability = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (playServicesAvailability != ConnectionResult.SUCCESS) {
            showPlayServicesErrorDialog(googleApiAvailability, playServicesAvailability);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_INTRO:
                if (resultCode == RESULT_CANCELED) {
                    onBackPressed();
                }else {
                    init();
                }
                break;
            case RC_PLAY_SERVICES:
                if (resultCode == RESULT_OK){
                    init();
                }
        }
    }

    private void showNoSim() {
        findViewById(R.id.main_content).setVisibility(View.GONE);
        findViewById(R.id.no_sim_card).setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getRequiredPermissions() {
        requestPermissions(requiredPermissions,100);
    }

    private void startLockActivity() {
        Intent lockIntent = new Intent(this, LockActivity.class);
        lockIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lockIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<String> deniedPermission = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                deniedPermission.add(permissions[i]);
            }
        }
        if (deniedPermission.size() > 0) {
            requiredPermissions = deniedPermission.toArray(new String[]{});
            new AlertDialog.Builder(this)
                    .setTitle(R.string.permission_denied)
                    .setMessage(R.string.cant_continue_with_out_permissions)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getRequiredPermissions();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(SigningActivity.this, R.string.cant_continue, Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }).show();
        }else {
            if (!Utility.deviceHasSimCard(this)) {
                showNoSim();
            }
        }
    }

    @Override
    public void onClick(View v) {
        EditText userNameView = findViewById(R.id.user_name);
        EditText passwordView = findViewById(R.id.password);

        String userName = userNameView.getText().toString();
        String password = passwordView.getText().toString();

        if (userName.isEmpty()) userNameView.setError(getString(R.string.cant_be_empty));
        else if (userName.contains("\"")) userNameView.setError(getString(R.string.no_quotation_allowed));
        else if (password.isEmpty()) passwordView.setError(getString(R.string.cant_be_empty));
        else if (password.contains("\"")) passwordView.setError(getString(R.string.no_quotation_allowed));
        else if (Utility.getMaxMessageLengthForMessage(password) != 160) passwordView.setError(getString(R.string.should_be_english));
        else if (password.length() <= 5 ) passwordView.setError(getString(R.string.at_least_six_characters));
        else {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(getString(R.string.key_pref_user_name),userName).commit();
            prefs.edit().putString(getString(R.string.key_pref_user_id),Utility.getSimId(this)).commit();
            prefs.edit().putString(getString(R.string.key_pref_user_password),password).commit();
            Security.encryptPreferencesEntry(this,password,getString(R.string.key_pref_user_password_encrypted));
            startLockActivity();
        }
    }
}