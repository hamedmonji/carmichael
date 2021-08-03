package ir.the_moment.carmichael_sms.responseHandler;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;

/**
 * base class to handleResponse the response of a tak.
 * Created by mac on 6/30/17.
 */

public abstract class ResponseHandler {

    private Message response;

    public final Message getResponse() {
        return response;
    }

    public final ResponseHandler setResponse(Message response) {
        this.response = response;
        return this;
    }

    public final Context getContext() {
        return context;
    }

    public final ResponseHandler setContext(Context context) {
        this.context = context;
        return this;
    }

    private Context context;

    public final void run(){
        handle();
    }

    /**
     * the initial method that will be called to start the handling process
     */
    protected abstract void handle();


    protected final String getAction(){
        return getClass().getSimpleName();
    }

    /**
     * show notification for the received message
     * @param pendingIntent to be executed when pressed on the notification
     * @param title for the notification
     * @param id of the notification
     */
     final void showNotification(@Nullable PendingIntent pendingIntent, String title, int id, int later){
        if (pendingIntent == null){
            Intent signingIntent = new Intent(getContext(), SigningActivity.class);
            int RC_SIGNING_ACTIVITY = 9999;
            pendingIntent = PendingIntent.getActivity(getContext(), RC_SIGNING_ACTIVITY,signingIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }

        int icon;
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
             icon = R.drawable.carmichael_notification;
         }else icon = R.drawable.carmichael_512;

        Notification builder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .setContentText(title)
                .setContentTitle(getContext().getString(R.string.app_name))
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .build();
        NotificationManagerCompat.from(context).notify(id,builder);
    }
}