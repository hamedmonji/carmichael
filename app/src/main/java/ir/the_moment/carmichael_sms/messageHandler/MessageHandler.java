package ir.the_moment.carmichael_sms.messageHandler;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.List;

import ir.the_moment.carmichael_sms.SmsReceiver;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.responseHandler.ResponseHandler;
import ir.the_moment.carmichael_sms.responseHandler.ResponseHandlerFactory;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.SecurityAlert;
import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.tasks.requests.addRequest.AddRequest;
import ir.the_moment.carmichael_sms.ui.requests.addDevice.AddRequestHandlerActivity;
import ir.the_moment.carmichael_sms.ui.securityReport.SecurityReportActivity;
import ir.the_moment.carmichael_sms.utility.MessageParser;
import ir.the_moment.carmichael_sms.utility.OnMessageParseFinished;
import ir.the_moment.carmichael_sms.utility.Security;

/**
 * Created by vaas on 7/26/17.
 */

public abstract class MessageHandler implements OnMessageParseFinished {

    private static final int RC_SECURITY_REPORT = 9123;
    private static final int RC_ADD_REQUEST = 24950;
    private static final int RC_VALIDATION = 3002;

    private static final int NOTIFICATION_ID_VALIDATION = 3001;
    private static final int NOTIFICATION_ID_ADD_REQUEST = 3530;

    public static final String KEY_RESPOND_RECEIVER = "respond_receiver";
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_RESPOND = "respond";

    private Context context;
    private String rawMessage;
    private Message.Type type;

    public int getType() {
        switch (type) {
            case command:
                return mR.TYPE_HANDLER;
            default:
                return mR.TYPE_ASSET;
        }
    }

    public Context getContext() {
        return context;
    }


    protected MessageHandler(Context context, String rawMessage) {
        this.context = context;
        this.rawMessage = rawMessage;
    }

    public final void handle(){
        parse();
    }

    private void parse(){
        MessageParser parser = new MessageParser(rawMessage);
        parser.setOnMessageParseFinished(this);
        parser.execute();
    }

    /**
     * @param messages list of messages within the message that was parsed.
     * @param type type of the messages can be one of the two values of {@link MessageParser#COMMAND,MessageParser#RESPONSE}
     */
    @Override
    public final void onParseFinished(List<Message> messages, Message.Type type) {
        this.type = type;
        if (messages != null) {
            for (Message message :
                    messages) {
                setMessage(message);
                switch (type) {
                    case command:
                        if (handlerHasAccess(message)) {
                            changeStatusToLost();
                            handleCommand(message);
                        }else {
                            handleUnauthorizedCommand(message);
                        }
                        break;
                    case response:
                        handleResponse(message);
                        break;
                    case request:
                        if (isPasswordCorrect(message)) {
                            handleRequest(message);
                        }
                        break;
                }
            }
        }
    }



    private void handleRequest(Message message){
        switch (message.action){
            case AddRequest.action:
                Intent addRequestHandlerIntent = new Intent(context, AddRequestHandlerActivity.class);
                addRequestHandlerIntent.putExtra(mR.KEY_MESSAGES,message);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,RC_ADD_REQUEST,addRequestHandlerIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);

                showNotification(pendingIntent,context.getString(R.string.add_request_received),NOTIFICATION_ID_ADD_REQUEST);
                break;
        }
    }

    private void showNotification(PendingIntent pendingIntent, String title, int notificationId) {
        int icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = R.drawable.carmichael_notification;
        }else icon = R.drawable.carmichael_512;

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent)
                .setContentText(title)
                .setContentTitle(getContext().getString(R.string.app_name))
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .build();
        NotificationManagerCompat.from(context).notify(notificationId,notification);
    }

    /**
     * dispatches the received message
     * @param message response message that was received.
     */
    private void handleResponse(Message message){
        String responsePackage = "ir.the_moment.carmichael_sms.responseHandler.";
        ResponseHandler responseHandler = ResponseHandlerFactory.createRespond(responsePackage + message.action);
        responseHandler.setContext(context)
                .setResponse(message)
                .run();
    }
    /**
     * constructs a security alert.
     * this is called when a client with out the
     * proper permissions tries to start a task.
     */
    private void handleUnauthorizedCommand(Message message){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        UserActivatedTask task = TaskFactory.createTask(TaskExecutorService.tasksPackage + message.action);
        String securityAlerts;
        String prefsKey;
        if (task.getPriority() == Task.priority.low) {
            securityAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_LOW, null);
            prefsKey = mR.KEY_PREF_SECURITY_ALERTS_LOW;
        }else if (task.getPriority() == Task.priority.medium) {
            securityAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_MEDIUM, null);
            prefsKey = mR.KEY_PREF_SECURITY_ALERTS_MEDIUM;
        } else {
            securityAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_HIGH, null);
            prefsKey = mR.KEY_PREF_SECURITY_ALERTS_HIGH;
        }

        if (securityAlerts != null){
            securityAlerts += mR.DATA_SEPARATOR;
        }else {
            securityAlerts = "";
        }

        SecurityAlert alert = new SecurityAlert();
        DeviceModel device = getSenderDevice();
        if (device != null) {
            alert.number = device.number;
        }

        alert.time = String.valueOf(System.currentTimeMillis());
        alert.task = message.action;

        securityAlerts += alert.toString();
        prefs.edit().putString(prefsKey,securityAlerts).commit();
        showUnAuthorizedNotification();
    }

    /**
     * notifies the user of the recent unAuthorized command that was received.
     */
    private void showUnAuthorizedNotification() {
        Intent securityReportIntent = new Intent(context,SecurityReportActivity.class);
        PendingIntent securityReport = PendingIntent.getActivity(context,RC_SECURITY_REPORT,securityReportIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        showNotification(securityReport,context.getString(R.string.security_alert), SmsReceiver.SECURITY_ALERT_NOTIFICATION_ID);
    }

    private void handleCommand(Message message){
        Intent intent = new Intent(context,TaskExecutorService.class);
        intent.putExtra(mR.KEY_MESSAGES,message);
        ResultReceiver respondReceiver = new RespondReceiver(null);
        intent.putExtra(KEY_RESPOND_RECEIVER,respondReceiver);
        context.startService(intent);
    }

    private boolean handlerHasAccess(Message message){
        DeviceModel senderDevice = getSenderDevice();
        return senderDevice != null && mR.Permissions.hasPermission(context,senderDevice.permissions, message)
                && isPasswordCorrect(message);
    }

    private boolean isPasswordCorrect(Message message) {
        String ourPassword = Security.getDecryptedPreferenceEntry(context, context.getString(R.string.key_pref_user_password_encrypted));
        String receivedPassword = message.getExtra(Message.DATA_KEY_PASSWORD);
        return receivedPassword != null && receivedPassword.equals(ourPassword);
    }

    private void changeStatusToLost() {
        Security.encryptPreferencesEntry(context,String.valueOf(true),
                context.getString(R.string.key_pref_is_device_lost));
    }

    private class RespondReceiver extends android.os.ResultReceiver {

        RespondReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            boolean succeed = resultData.getBoolean(KEY_SUCCESS);
            Message response = resultData.getParcelable(KEY_RESPOND);
            response.putExtra(KEY_SUCCESS, String.valueOf(succeed));
            onResultReceived(response);

        }
    }

    protected abstract DeviceModel getSenderDevice();
    protected abstract void setMessage(Message message);

    protected abstract void onResultReceived(@Nullable Message response);

}
