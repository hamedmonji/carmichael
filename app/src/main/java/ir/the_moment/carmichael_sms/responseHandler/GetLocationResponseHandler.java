package ir.the_moment.carmichael_sms.responseHandler;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.location.GetLocation;
import ir.the_moment.carmichael_sms.ui.location.MapActivity;

/**
 * Created by mac on 6/30/17.
 */

public class GetLocationResponseHandler extends ResponseHandler {
    private static final int LOCATION_NOTIFICATION_CODE = 1111;
    private static final int RC_MAP_ACTIVITY = 3333;
    String TAG = "locationResponse";
    @Override
    protected void handle() {

        if (getResponse().getBoolean(ir.the_moment.carmichael_sms.messageHandler.MessageHandler.KEY_SUCCESS)) {
            String rawData = getResponse().getExtra(GetLocation.DATA_KEY_LOCATION_DATA);
            Log.i(TAG,rawData);
            saveLocationData(rawData);
            Intent mapIntent = new Intent(getContext(), MapActivity.class);
            mapIntent.putExtra(mR.KEY_MESSAGES, getResponse());
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            mapIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), RC_MAP_ACTIVITY, mapIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
            showNotification(pendingIntent, getContext().getString(R.string.location_available), LOCATION_NOTIFICATION_CODE,R.drawable.location_notification);
        }else{
            showNotification(null,getContext().getString(R.string.location_connection_failed),1313,R.drawable.location_notification);
        }
    }

    private void saveLocationData(String locationData){
        String ALL_COORDINATES = "all_coordinates";
        SharedPreferences prefs = getContext().getSharedPreferences(getResponse().sender, Context.MODE_PRIVATE);
        String allCoordinates = prefs.getString(ALL_COORDINATES,"");
        if (!allCoordinates.equals("")) allCoordinates += mR.DATA_SEPARATOR;
        allCoordinates += locationData;
        prefs.edit().putString(ALL_COORDINATES,allCoordinates).commit();
    }
}