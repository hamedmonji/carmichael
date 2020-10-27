package ir.the_moment.carmichael_sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;

public class OutgoingCallReceiver extends BroadcastReceiver {

    public static final String TAG = "callReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAppIconHidden = prefs.getBoolean(context.getString(R.string.key_pref_hide_app_icon),false);
        if (!isAppIconHidden) return;
        String calledNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String registeredNumber =  prefs.getString(context.getString(R.string.key_pref_hide_app_icon_number),null);
        if (calledNumber != null && registeredNumber != null && calledNumber.equals(registeredNumber)){
            Intent appIntent = new Intent(context, SigningActivity.class);
            appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(appIntent);
            setResultData(null);
        }
    }
}