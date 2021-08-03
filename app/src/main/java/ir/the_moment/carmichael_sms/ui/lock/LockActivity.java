package ir.the_moment.carmichael_sms.ui.lock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;


import com.github.omadahealth.lollipin.lib.managers.AppLock;
import com.github.omadahealth.lollipin.lib.managers.LockManager;

import ir.the_moment.carmichael_sms.MainActivity;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.R;

public class LockActivity extends AppCompatActivity {
    private static final int RC_PERMANENT_LOCK = 1002;
    private static final int RC_ENTER_PASS = 1003;
    private static final int RC_SET_PASS = 11;

    private SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableLock();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!isPassSet()){
            setLockPass();
            return;
        }


        if (checkForPermanentLock()) return;

        showLock();

    }

    private boolean checkForPermanentLock() {
        boolean isPermanentLockEnabled =
                Security.getDecryptedBooleanPreferenceEntry(this,getString(R.string.key_pref_permanent_lock_enabled));
        if (isPermanentLockEnabled){
            startPermanentLockActivity();
            return true;
        }
        return false;
    }


    private void setLockPass() {
        Intent customPinIntent = new Intent(this,CustomPinActivity.class);
        customPinIntent.putExtra(AppLock.EXTRA_TYPE,AppLock.ENABLE_PINLOCK);
        startActivityForResult(customPinIntent, RC_SET_PASS);
    }

    private void showLock() {
        Intent customPinIntent = new Intent(this,CustomPinActivity.class);
        customPinIntent.putExtra(AppLock.EXTRA_TYPE,AppLock.UNLOCK_PIN);
        startActivityForResult(customPinIntent,RC_ENTER_PASS);
    }

    private void enableLock() {
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);
        lockManager.getAppLock().setTimeout(2000);
    }

    private boolean isPassSet() {
        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        return lockManager.getAppLock().isPasscodeSet();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case RC_SET_PASS:
                if (resultCode == RESULT_OK) {
                    prefs.edit().putBoolean(getString(R.string.key_pref_is_first_time),false).commit();
                    startMainActivity();
                }
                break;
            case RC_ENTER_PASS:
                if (resultCode == RESULT_OK)
                    startMainActivity();
                else {
                    Toast.makeText(this, R.string.too_many_tries_activating_lock, Toast.LENGTH_SHORT).show();
                    boolean isPermanentLockEnabled =
                            Security.getDecryptedBooleanPreferenceEntry(this,getString(R.string.key_pref_permanent_lock_enabled));
                    if (isPermanentLockEnabled) {
                        startPermanentLockActivity();
                    }
                }
                break;
        }
    }

    private void startMainActivity(){
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(mainIntent);
    }

    private void startPermanentLockActivity() {
        Intent permanentLockIntent = new Intent(this,PermanentLockActivity.class);
        startActivity(permanentLockIntent);
    }
}
