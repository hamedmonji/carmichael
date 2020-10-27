package ir.the_moment.carmichael_sms.tasks.alarm;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 7/8/17.
 */

public class DisableSilent extends UserActivatedTask {

    public static final String action = "alarm.DisableSilent";
    public static final String FLAG_RING = "0";
    private static final String  DISABLE_RING = "disable ring";

    @Override
    public int getPermission() {
        return mR.Permissions.DISABLE_Silent;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_disable_silent);
    }

    @Override
    public priority getPriority() {
        return priority.low;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.disable_silent);
    }

    @Override
    protected void action() {
        AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audioManager.setStreamVolume(AudioManager.STREAM_RING,audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),0);

        if (getMessage().hasFlag(FLAG_RING)) {
            ring();
        }

    }

    private void ring() {
        final Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Settings.System.DEFAULT_RINGTONE_URI);
        if (ringtone != null) {
            ringtone.play();
        }
        final int ringId = 100002;
        BroadcastReceiver disableRingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);
                NotificationManagerCompat.from(context).cancel(ringId);
                if (ringtone != null) {
                    ringtone.stop();
                }
            }
        };

        context.registerReceiver(disableRingReceiver,new IntentFilter(DISABLE_RING));

        Intent disableRingIntent = new Intent(DISABLE_RING);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(),disableRingIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action disableRing =
                new NotificationCompat.Action(R.drawable.ic_media_pause_dark
                        ,context.getString(R.string.disable_ring)
                        ,pendingIntent);

        int icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = R.drawable.carmichael_notification;
        }else icon = R.drawable.carmichael_512;
        final Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .addAction(disableRing)
                .setSmallIcon(icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();

        NotificationManagerCompat.from(context).notify(ringId,notification);
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isDisableSilentEnabled(context);
    }
}