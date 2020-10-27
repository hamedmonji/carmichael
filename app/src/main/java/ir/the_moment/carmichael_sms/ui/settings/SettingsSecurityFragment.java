package ir.the_moment.carmichael_sms.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.github.orangegangsters.lollipin.lib.managers.AppLock;

import ir.the_moment.carmichael_sms.ui.lock.CustomPinActivity;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by mac on 6/24/17.
 */

public class SettingsSecurityFragment extends PreferenceFragment {

    private static final int RC_CHANGE_PIN = 12321;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_securirty);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        EditTextPreference numberPreference = (EditTextPreference) findPreference(getString(R.string.key_pref_hide_app_icon_number));
        String phoneNumber = prefs.getString(getString(R.string.key_pref_hide_app_icon_number),null);
        boolean hideAppIcon = prefs.getBoolean(getString(R.string.key_pref_hide_app_icon),false);

        if (hideAppIcon && phoneNumber != null && !phoneNumber.isEmpty()) {
            numberPreference.setSummary(phoneNumber);
        }else if (phoneNumber != null && !phoneNumber.isEmpty()){
            numberPreference.setSummary(phoneNumber);
        }else {
            numberPreference.setSummary(R.string.one_to_four);
        }

        boolean maxAttemptEnable = prefs.getBoolean(getString(R.string.key_pref_enable_max_attempts),false);
        String maxAttempt = prefs.getString(getString(R.string.key_pref_lock_max_attempts),null);

        EditTextPreference maxAttemptPref = (EditTextPreference) findPreference(getString(R.string.key_pref_lock_max_attempts));
        if (maxAttemptEnable && maxAttempt != null && !maxAttempt.isEmpty()) {
            maxAttemptPref.setSummary(maxAttempt);
        }else if (maxAttemptEnable){
            maxAttemptPref.setSummary("0");
        }else maxAttemptPref.setSummary("");

        Preference changeAppPassCode = findPreference(getString(R.string.key_pref_app_password));
        changeAppPassCode.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startChangePinActivity();
                return true;
            }
        });

        String notificationListenerString = Settings.Secure.getString(getActivity().getContentResolver(), "enabled_notification_listeners");
        if (notificationListenerString == null || !notificationListenerString.contains(getActivity().getPackageName())) {
            updateCorrespondingCheckbox(false,getString(R.string.key_pref_disable_notifications));
        }


    }

    private void updateCorrespondingCheckbox(boolean checked, String key) {
        ((CheckBoxPreference)findPreference(key)).setChecked(checked);
    }

    private void startChangePinActivity() {
        Intent intent = new Intent(getActivity(), CustomPinActivity.class);
        intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
        startActivityForResult(intent,RC_CHANGE_PIN);
    }
}