package ir.the_moment.carmichael_sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.SmsMessage;
import android.util.Log;

import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.messageHandler.SmsHandler;
import ir.the_moment.carmichael_sms.utility.MessageParser;
import ir.the_moment.carmichael_sms.utility.Security;

/**
 * Created by vaas on 3/26/2017.
 * receiver for sms.if the sms was sent from the authorized numbers and the message starts with a certain sequence
 * then the requested action or response will be executed.
 *
 */
public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "smsReceiver";
    public static boolean suppressSms = false;
    public static final int SECURITY_ALERT_NOTIFICATION_ID = 12475;


    /**
     * the type of the message that was received.
     * can be on of the three values{@link MessageParser#REQUEST }{@link MessageParser#RESPONSE}{@link MessageParser#COMMAND}
     */
    private String type;
    public static final String SMS_BUNDLE = "pdus";
    private Context context;

    /**
     * the message itself  in json format without the type and message count
     */
    private String message = "";
    private String senderNumber;
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle intentExtras = intent.getExtras();
        this.context = context;
        if (isUserSignedIn()) {
            if (intentExtras != null) {
                Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
                if (sms != null) {
                    extractSmsInfo(sms);
                    type = getTypeIfValid(message);
                    if (isMessageValid()) {
                        suppressSms = true;
                        abortBroadcast();
                        Log.i(TAG,message);
                        message = separateMessageAndType();
                        switch (type) {
                            case MessageParser.REQUEST:
                                handleRequest();
                                break;
                            case MessageParser.RESPONSE:
                                handleResponse();
                                break;
                            case MessageParser.COMMAND:
                                if (isSenderAuthorized(context)) {
                                    handleCommand();
                                }
                                break;
                        }
                    } else {
                        suppressSms = false;
                        reset();
                    }
                }
            }
        }else {
            suppressSms = false;
            reset();
        }
    }

    private boolean isSenderAuthorized(Context context) {
        return Security.isSenderAuthorized(context,senderNumber,mR.TYPE_HANDLER);
    }

    private void extractSmsInfo(Object[] sms) {
        SmsMessage smsMessage;
        for (Object sm : sms) {
            smsMessage = SmsMessage.createFromPdu((byte[]) sm);
            message += smsMessage.getMessageBody();
            senderNumber = smsMessage.getOriginatingAddress();
        }
    }

    private boolean isUserSignedIn() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.key_pref_user_name),null) != null;
    }

    /**
     * check to see if the sms that was received is indeed
     * meant for this app and is not just an text message.
     * @return true if is not a regular text message
     */
    private boolean isMessageValid() {
        return type != null ;
    }

    private void handleRequest() {
//        message = getDecryptedMessage(smsBody);
        if (message != null) {
            delegateSmsToMessageHandler();
        }
        reset();
    }

    private void handleResponse() {
//        message = getDecryptedResponse();
        if (message != null) {
            Log.i(TAG,message);
        }
        delegateSmsToMessageHandler();
    }

    private void handleCommand() {
//        message = getDecryptedCommand();
        delegateSmsToMessageHandler();
    }

    @Nullable
    private String getTypeIfValid(String message){
        if (type != null){
            return type;
        }else if (message.startsWith(mR.response)){
            return MessageParser.RESPONSE;
        }else if (message.startsWith(mR.command)){
            return MessageParser.COMMAND;
        }else if (message.startsWith(mR.request)){
            return MessageParser.REQUEST;
        }else return null;
    }

    /**
     * sets the static variables used to handleResponse the message back to there default value
     */
    private void reset() {
        type = null;
    }

    @NonNull
    private String separateMessageAndType() {
        return message.substring(4);
    }

    private String getDecryptedMessage(String messagebody) {
        String message = Security.decrypt(context, messagebody,mR.RESPONSE_ENCRYPTION_KEY);
         return message != null ? message : Security.decrypt(context, messagebody);
    }

    private String getDecryptedCommand() {
        return Security.decrypt(context, message);
    }

    private String getDecryptedResponse() {
        return Security.decrypt(context, message, mR.RESPONSE_ENCRYPTION_KEY);
    }

    private void delegateSmsToMessageHandler() {
        if (message != null) {
            abortBroadcast();
            MessageHandler messageHandler = new SmsHandler(context, message, senderNumber);
            messageHandler.handle();
        }
        reset();
    }
}