package ir.the_moment.carmichael_sms.tasks;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 3/24/2017.
 * base class for all tasks.
 */

public abstract class Task {

    private Message message;
    protected Message respond;
    public Context context;
    private OnActionFinished onActionFinished;

    protected void onActionFinished(boolean succeed){
        if (onActionFinished != null && message.requestRespond){
            respond.type = Message.Type.response;
            onActionFinished.onActionFinished(succeed,respond);
        }
    }

    public Task setOnActionFinished(OnActionFinished onActionFinished) {
        this.onActionFinished = onActionFinished;
        return this;
    }

    public Task setContext(Context context) {
        this.context = context;
        return this;
    }

    public Context getContext() {
        return context;
    }




    public Task setMessage(Message message) {
        this.message = message;
        return this;
    }



    public final void run(){
        action();
    }

    public Message getMessage() {
        return message;
    }


    public abstract boolean isEnabled();

    /**
     * show notification for the received message
     * @param pendingIntent to be executed when pressed on the notification
     * @param title for the notification
     * @param id for the notification
     */
    public final void showNotification(PendingIntent pendingIntent, String title, int id,int icon){
        if (pendingIntent == null){
            Intent signingIntent = new Intent(getContext(), SigningActivity.class);
            int RC_SIGNING_ACTIVITY = 9999;
            pendingIntent = PendingIntent.getActivity(getContext(), RC_SIGNING_ACTIVITY,signingIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }
        Notification builder = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentTitle(getContext().getString(R.string.app_name))
                .setContentText(title)
                .build();

        NotificationManagerCompat.from(context).notify(id,builder);
    }

    protected abstract void action();
    protected abstract void parseData();
    protected abstract void parseFlags();
    public abstract int getPermission();

    public enum priority {
        low,
        medium,
        high
    }
}