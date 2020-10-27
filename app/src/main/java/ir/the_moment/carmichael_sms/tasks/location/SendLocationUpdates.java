package ir.the_moment.carmichael_sms.tasks.location;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.responseHandler.GetLocationResponseHandler;
import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.SetAlarm;

/**
 * Created by vaas on 4/10/2017.
 */

public class SendLocationUpdates extends Task {
    private static final String TAG = "getLocation";
    public static final String action = "location.SendLocationUpdates";
    private static final int SEND_LOCATION_ALARM_REQUEST_CODE = 2000;
    private Long sendRespondInterval;


    @Override
    protected void action() {
        parseFlags();
        parseData();
        sendLocationData();
    }

    @Override
    protected void parseData() {
        sendRespondInterval = GetLocationService.sendRespondInterval;
    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isLocationEnabled(context);
    }

    private void sendLocationData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String savedLocations = prefs.getString(GetLocation.DATA_KEY_LOCATION_DATA,"");
        Log.i(TAG,"saved locations " + savedLocations);
        Log.i(TAG,"interval is " + sendRespondInterval/60000 + " minutes");
        if (!savedLocations.equals("")) {
            respond = new Message();
            respond.sender = getMessage().sender;
            respond.action = GetLocationResponseHandler.class.getSimpleName();
            respond.type = Message.Type.response;
            respond.putExtra(GetLocation.DATA_KEY_LOCATION_DATA, savedLocations);
            respond.putExtra(MessageHandler.KEY_SUCCESS, String.valueOf(true));
            if (GetLocationService.isRunning) {
                setLocationUpdateSenderAlarm();
            }else {
                SetAlarm.cancel(context,SEND_LOCATION_ALARM_REQUEST_CODE);
            }
            Log.i(TAG, "sendLocationData: ");
            MessageSender sender = new MessageSender(context, mR.RESPONSE_ENCRYPTION_KEY,respond.sender);
            sender.addMessage(respond)
                    .send();
            prefs.edit().putString(GetLocation.DATA_KEY_LOCATION_DATA,"").commit();
            onActionFinished(true);
        }
    }

    private void setLocationUpdateSenderAlarm() {
        SetAlarm.set(context,getMessage(),SEND_LOCATION_ALARM_REQUEST_CODE,sendRespondInterval);
        Log.i(TAG, "set Location Update Sender Alarm: ");
    }

    @Override
    public int getPermission() {
        return mR.Permissions.ACCESS_LOCATION;
    }

}
