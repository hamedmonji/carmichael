package ir.the_moment.carmichael_sms.tasks.lock;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.Security;

/**
 * Created by vaas on 5/6/17.
 * disables permanent lock
 */

public class UnLockApp extends UserActivatedTask {
    public static final  String action = "lock.UnLockApp";
    public static final int id = 1000;

    @Override
    protected void action() {
        Security.encryptPreferencesEntry(context,String.valueOf(false),context.getString(R.string.key_pref_permanent_lock_enabled));
        showNotification();
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * show notification indicating that app permanent lock was disabled.
     */
    private void showNotification() {
        Intent signingIntent = new Intent(getContext(), SigningActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),1111,signingIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Notification builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.carmichael_notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(getContext().getString(R.string.app_name))
                .setContentText(context.getString(R.string.app_unlocked))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        NotificationManagerCompat.from(context).notify(id,builder);
    }

    @Override
    public int getPermission() {
        return mR.Permissions.UNLOCK_APP;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_unlock_app);
    }

    @Override
    public priority getPriority() {
        return priority.medium;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.unlock_app);
    }
}
