package ir.the_moment.carmichael_sms.ui.settings;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/1/2017.
 * fragment for settings.
 */

public class SettingsEnabledTasksFragment extends PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prefs);

        checkIfNotificationSouldBeEnabled();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            if (prefs.getString(SettingsActivity.KEY_PREF_DROP_BOX_ACCESS_TOKEN,null) == null) {
                updateCorrespondingCheckbox(false, getString(R.string.key_pref_allow_taking_pictures));
                savePreference(String.valueOf(false),getString(R.string.key_pref_allow_taking_pictures_encrypted));
            }
    }

    private void checkIfNotificationSouldBeEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NotificationManager notificationManager =
                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (!notificationManager.isNotificationPolicyAccessGranted()) {
                updateCorrespondingCheckbox(false,getString(R.string.key_pref_allow_disable_silent));
                savePreference(String.valueOf(false),getString(R.string.key_pref_allow_disable_silent_encrypted));
            }
            notificationManager = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkIfNotificationSouldBeEnabled();
    }

    private void updateCorrespondingCheckbox(boolean checked, String key) {
        ((CheckBoxPreference)findPreference(key)).setChecked(checked);
    }

    private void savePreference(String value, String key) {
        ((SettingsActivity)getActivity()).savePreference(value,key);
    }
}
