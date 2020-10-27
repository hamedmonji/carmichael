package ir.the_moment.carmichael_sms.tasks.location;

import android.content.Intent;
import android.util.Log;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 3/23/2017.
 * get last knows location and set a location request with a sendRespondInterval
 */

public class GetLocation extends UserActivatedTask {
    private static final String TAG = "getLocation";
    public static final String FLAG_DISABLE = "dis";
    public static final String FLAG_ENABLE_INSTANT_MODE = "43";
    public static final String action = "location.GetLocation";

    /**
     * flag for getting the location periodically rather than just once
     */
    public static final String FLAG_REQUEST_LOCATION_PERIODICALLY = "32";


    /**
     * key for the interval between each time the response is sent back.
     */
    public static final String DATA_KEY_SEND_RESPONSE_INTERVAL = "rli";

    /**
     * key for location data stored in the message
     */
    public static final String DATA_KEY_LOCATION_DATA = "lod";

    /**
     * key for interval between each location request made to google api client.
     */
    public static final String DATA_KEY_REQUEST_LOCATION_INTERVAL = "rei";


    // the interval to request location updates
    private long locationUpdatesInterval = 15000;
    private boolean isPeriodicallyRequested = false;
    // the interval between each respond containing the locations.
    private long sendRespondInterval = 5000;

    @Override
    protected void action() {
        Log.i(TAG,"Message is " + getMessage().toString());
        parseFlags();
        parseData();

        Intent getLocationServiceIntent = new Intent(context,GetLocationService.class);
        getLocationServiceIntent.putExtra(FLAG_REQUEST_LOCATION_PERIODICALLY,isPeriodicallyRequested);
        getLocationServiceIntent.putExtra(DATA_KEY_REQUEST_LOCATION_INTERVAL,locationUpdatesInterval);
        getLocationServiceIntent.putExtra(DATA_KEY_SEND_RESPONSE_INTERVAL,sendRespondInterval);
        getLocationServiceIntent.putExtra(FLAG_DISABLE,getMessage().hasFlag(FLAG_DISABLE));
        getLocationServiceIntent.putExtra(mR.KEY_MESSAGES,getMessage());
        context.startService(getLocationServiceIntent);


    }


    @Override
    protected void parseFlags() {
        if (getMessage().flags != null) {
            for (String flag :
                    getMessage().flags) {
                switch (flag) {
                    case FLAG_REQUEST_LOCATION_PERIODICALLY:
                        isPeriodicallyRequested = true;
                        break;
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isLocationEnabled(context);
    }

    @Override
    protected void parseData() {
        if (getMessage().getData() != null) {
            setLocationRequestInterval(getMessage().getInt(DATA_KEY_REQUEST_LOCATION_INTERVAL));
            setSendRespondInterval(getMessage().getInt(DATA_KEY_SEND_RESPONSE_INTERVAL));
        }
    }

    /**
     * sets respond interval based on the index value in the message
     * @param i index of the option item that was selected
     */
    private void setLocationRequestInterval(int i) {
        long oneSec = 1000;
        long oneMin = 60000;
        long oneHour = oneMin * 60;
        switch (i) {
            case 0:
                locationUpdatesInterval = oneSec * 15;
                break;
            case 1:
                locationUpdatesInterval = oneSec * 30;
                break;
            case 2:
                locationUpdatesInterval = oneMin;
                break;
            case 3:
                locationUpdatesInterval = oneMin * 2;
                break;
            case 4:
                locationUpdatesInterval = oneMin * 3;
                break;
            case 5:
                locationUpdatesInterval = oneMin * 5;
                break;
            case 6:
                locationUpdatesInterval = oneMin * 10;
                break;
            case 7:
                locationUpdatesInterval = oneMin * 15;
                break;
            case 8:
                locationUpdatesInterval = oneMin * 20;
                break;
            case 9:
                locationUpdatesInterval = oneHour;
                break;
            case 10:
                locationUpdatesInterval = oneHour * 2;
                break;
            case 11:
                locationUpdatesInterval = oneHour * 3;
                break;
            case 12:
                locationUpdatesInterval = oneHour * 5;
                break;
            case 13:
                locationUpdatesInterval = oneHour * 10;
                break;
        }
    }

    /**
     * sets respond location request interval based on the index value in the message
     * @param i index of the option item that was selected
     */
    private void setSendRespondInterval(int i) {
        long oneMin = 60000;
        long oneHour = oneMin * 60;
        switch (i) {
            case 1:
                sendRespondInterval = oneMin;
                break;
            case 2:
                sendRespondInterval = oneMin * 3;
                break;
            case 3:
                sendRespondInterval = oneMin * 5;
                break;
            case 4:
                sendRespondInterval = oneMin * 10;
                break;
            case 5:
                sendRespondInterval = oneMin * 20;
                break;
            case 6:
                sendRespondInterval = oneMin * 30;
                break;
            case 7:
                sendRespondInterval = oneMin * 60;
                break;
            case 8:
                sendRespondInterval = oneHour * 3;
                break;
            case 9:
                sendRespondInterval = oneHour * 5;
                break;
            case 10:
                sendRespondInterval = oneHour * 10;
                break;
            case 11:
                sendRespondInterval = oneHour * 24;
                break;
        }
    }


    @Override
    public int getPermission() {
        return mR.Permissions.ACCESS_LOCATION;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_location);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.get_location);
    }
}
