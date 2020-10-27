package ir.the_moment.carmichael_sms;

import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MyNotificationListenerService extends NotificationListenerService {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(mR.TAG, "onNotificationPosted: " + sbn.getPackageName());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean suppressNotifications = prefs.getBoolean(getString(R.string.key_pref_disable_notifications),false);
        if (suppressNotifications) {
            String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());
            boolean isMessageNotification = sbn.getPackageName().contains(defaultSmsPackage);
            Log.i(mR.TAG, "onNotificationPosted: " + SmsReceiver.suppressSms);
            if (isMessageNotification && SmsReceiver.suppressSms) {
                Log.i(mR.TAG, "onNotificationPosted: supress" );
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cancelNotification(sbn.getKey());
                }else {
                    cancelNotification(sbn.getPackageName(),sbn.getTag(),sbn.getId());
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        Log.i(mR.TAG, "onListenerConnected: ");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(mR.TAG, "onCreate: ");
    }
}
