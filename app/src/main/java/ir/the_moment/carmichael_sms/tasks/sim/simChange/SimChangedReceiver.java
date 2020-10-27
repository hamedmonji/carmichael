package ir.the_moment.carmichael_sms.tasks.sim.simChange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.boot.BootNotification;
import ir.the_moment.carmichael_sms.tasks.sim.SimChanged;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by vaas on 3/24/2017.
 * checks sim id on start up and compares it to the one store before shutdown.
 * if the new id is different a message will be send to the first device stored in the database.
 */

public class SimChangedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!isUserSignedIn(context)) return;

        boolean deviceIsLost  = Security.getDecryptedBooleanPreferenceEntry(context,context.getString(R.string.key_pref_is_device_lost));
        if (deviceIsLost){
            String newSubscriberId = Utility.getSimId(context);
            String subscriberId = Utility.getUserID(context);
            if (subscriberId != null && newSubscriberId != null && !newSubscriberId.equals(subscriberId)){
                Message message = new Message();
                message.action = SimChanged.action;
                SimChanged simChanged = new SimChanged();
                simChanged.setMessage(message);
                simChanged.setContext(context);
                simChanged.run();
            }

            Message bootMessage = new Message();
            bootMessage.action = BootNotification.action;
            BootNotification bootNotification = new BootNotification();
            bootNotification.setContext(context);
            bootNotification.setMessage(bootMessage);
            bootNotification.run();

        }
    }

    private boolean isUserSignedIn(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.key_pref_user_name),null) != null;
    }
}
