package ir.the_moment.carmichael_sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.utility.MessageCreator;

/**
 * Created by vaas on 3/24/2017.
 * sends a message to a number of devices.
 */

public class MessageSender {
    public static final String TAG = "messageSender";
    private int totalMessagesSize = 0;
    private int currentMessagePartSent = 0;
    private MessageDeliveredReceiver deliveredReceiver;
    private MessageSentDeliver sentDeliver;

    private static final String MESSAGE_SENT_INTENT_FILTER = "SMS_SENT";
    private static final String MESSAGE_DELIVERED_INTENT_FILTER = "SMS_DELIVERED";
    private String[] phoneNumber;
    private int maxMessageLength = 160;

    private ArrayList<Message> messages = new ArrayList<>();
    private Context context;
    private MessageCallback callback = null;
    private String password;
    private MessageCallback defaultCallback;
    private boolean registerCallbacks = false;

    private boolean isSentFailed = false;


    public void setMaxMessageLength(int max) {
        this.maxMessageLength = max;
    }

    public MessageSender setRegisterCallbacks(boolean registerCallbacks) {
        this.registerCallbacks = registerCallbacks;
        return this;
    }

    public MessageSender setCallback(MessageCallback callback){
        this.callback = callback;
        return this;
    }
    public MessageSender setPassword(@NonNull String password){
        this.password = password;
        return this;
    }

    public MessageSender addMessage(@NonNull Message message){
        messages.add(message);
        return this;
    }

    public MessageSender addMessage(@NonNull List<Message> messages) {
        this.messages.addAll(messages);
        return this;
    }


    public MessageSender setMessage(Message message) {
        this.messages.clear();
        this.messages.add(message);
        return this;
    }

    public MessageSender(Context context,String password ,@Nullable String... phoneNumber) {
        this.context = context;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public void send(){
        if (messages.size() != 0) {
            if (registerCallbacks) {
                registerMessageCallbackReceivers();
            }
            String message = getEncryptedMessage();
            if (message == null) return;
            SmsManager smsManager = SmsManager.getDefault();
            if (message.length() > maxMessageLength) {
                ArrayList<String> parts = smsManager.divideMessage(message);
                totalMessagesSize = parts.size();
                ArrayList<PendingIntent> sentListeners = new ArrayList<>();
                ArrayList<PendingIntent> deliverListeners = new ArrayList<>();
                for (int i = 0; i < parts.size(); i++) {
                    sentListeners.add(getMessageSentPendingIntent());
                    deliverListeners.add(getMessageDeliveredPendingIntent());
                }
                for (String number :
                        phoneNumber) {
                    Log.i(TAG, "send: multipart " + parts.toString());
                    smsManager.sendMultipartTextMessage(number,null,parts,sentListeners,deliverListeners);
                }
            }else {
                totalMessagesSize = 1;
                for (String number :
                        phoneNumber) {
                    Log.i(TAG, "send: single part" + message);
                    smsManager.sendTextMessage(number,null,message,getMessageSentPendingIntent(),getMessageDeliveredPendingIntent());
                }
            }
            messages.clear();
        }
    }

    private String getEncryptedMessage() {
        if (messages.size() > 0) {
            if (messages.get(0).type != Message.Type.response) {
                messages.get(0).putExtra(Message.DATA_KEY_PASSWORD, password);
            }
            MessageCreator messageCreator = new MessageCreator(messages);
            String encryptedMessage = messageCreator.getInJsonFormat();
            if (encryptedMessage == null) return null;
            switch (messages.get(0).type) {
                case command:
                    return mR.command + encryptedMessage;
                case response:
                    return mR.response + encryptedMessage;
                case request:
                    return mR.request + encryptedMessage;
            }
        }
        return null;
    }

    private PendingIntent getMessageSentPendingIntent(){
        return PendingIntent.getBroadcast(context,1000,new Intent(MESSAGE_SENT_INTENT_FILTER),0);
    }

    private PendingIntent getMessageDeliveredPendingIntent(){
        return PendingIntent.getBroadcast(context,1000,new Intent(MESSAGE_DELIVERED_INTENT_FILTER),0);
    }

    private void registerMessageCallbackReceivers(){
        if (context != null) {
            if (callback == null) {
                callback = getDefaultMessageCallback();
            }
            sentDeliver = new MessageSentDeliver();
            deliveredReceiver = new MessageDeliveredReceiver();
            context.registerReceiver(sentDeliver, new IntentFilter(MESSAGE_SENT_INTENT_FILTER));

            context.registerReceiver(deliveredReceiver, new IntentFilter(MESSAGE_DELIVERED_INTENT_FILTER));
        }
    }

    public void unregisterReceivers() {
        context.unregisterReceiver(sentDeliver);
        context.unregisterReceiver(deliveredReceiver);
    }

    @NonNull
    public  MessageCallback getDefaultMessageCallback() {
        if (defaultCallback != null){
            return defaultCallback;
        }
        defaultCallback = new MessageCallback() {
            @Override
            public void onSent() {
                Toast.makeText(context, R.string.message_sent, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDelivered() {
                Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailed() {
                Toast.makeText(context, R.string.message_failed, Toast.LENGTH_SHORT).show();
            }
        };
        return defaultCallback;
    }


    private class MessageDeliveredReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentMessagePartSent == totalMessagesSize) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        callback.onDelivered();
                        break;
                    default:
                        callback.onFailed();
                        break;
                }
            }
        }
    }
    private class MessageSentDeliver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            currentMessagePartSent++;
            if (currentMessagePartSent == totalMessagesSize) {
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        callback.onSent();
                        break;
                    default:
                        callback.onFailed();
                        break;
                }
            }
        }
    }

    // sent and delivery callbacks
    public interface MessageCallback{
        void onSent();
        void onDelivered();
        void onFailed();
    }
}