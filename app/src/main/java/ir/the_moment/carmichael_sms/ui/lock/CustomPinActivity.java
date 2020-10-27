package ir.the_moment.carmichael_sms.ui.lock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Security;

/**
 * lock activity.
 */
public class CustomPinActivity extends AppLockActivity {
    public static final String KEY_FINISH_ON_BACK_PRESSED = "finish";

    private int maxAttempts;
    private boolean maxAttemptEnabled;
    private boolean finishOnBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null) {
            finishOnBackPressed = getIntent().getBooleanExtra(KEY_FINISH_ON_BACK_PRESSED,false);
        }

        maxAttemptEnabled = EnabledServices.isMaxAttemptsEnabled(this);
        if (maxAttemptEnabled) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            try {
                maxAttempts = Integer.parseInt(prefs.getString(getString(R.string.key_pref_lock_max_attempts),"1"));
            }catch (NumberFormatException e) {
                maxAttempts = 5;
            }
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String userProfileImage = prefs.getString(getString(R.string.key_pref_user_photo),null);
        String photoUri = prefs.getString(getString(R.string.key_pref_user_photo_url),null);
        if (userProfileImage != null) {
            Glide.with(this).load(userProfileImage).into(getLockImageView());
        } else if (photoUri != null){
            Glide.with(this).load(photoUri).into(getLockImageView());
        }else {
            getLockImageView().setImageResource(R.mipmap.carmichael);
        }
    }

    @Override
    public void onPinFailure(int attempts) {
        if (maxAttemptEnabled && attempts >= maxAttempts){
                enablePermanentLock();
        }
    }

    private void enablePermanentLock() {
        Security.encryptPreferencesEntry(this, String.valueOf(true),getString(R.string.key_pref_permanent_lock_enabled));
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onPinSuccess(int attempts) {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (!finishOnBackPressed) {
            return;
        }

        if (getType() == AppLock.ENABLE_PINLOCK || finishOnBackPressed){
            setResult(RESULT_CANCELED);
            finish();
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public int getPinLength() {
       return 8;
    }


    public CircleImageView getLockImageView() {
        return (CircleImageView) findViewById(R.id.pin_code_logo_imageview);
    }

    @Override
    public int getContentView() {
        return R.layout.activity_custom_pin_code;
    }
}
