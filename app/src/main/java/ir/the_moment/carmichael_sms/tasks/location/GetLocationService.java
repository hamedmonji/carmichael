package ir.the_moment.carmichael_sms.tasks.location;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.responseHandler.GetLocationResponseHandler;
import ir.the_moment.carmichael_sms.utility.SetAlarm;

import static ir.the_moment.carmichael_sms.tasks.location.GetLocation.DATA_KEY_LOCATION_DATA;
import static ir.the_moment.carmichael_sms.tasks.location.GetLocation.DATA_KEY_SEND_RESPONSE_INTERVAL;
import static ir.the_moment.carmichael_sms.tasks.location.GetLocation.FLAG_ENABLE_INSTANT_MODE;
import static ir.the_moment.carmichael_sms.tasks.location.GetLocation.action;

/**
 * Created by vaas on 10/29/17.
 */

public class GetLocationService extends Service implements com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    String TAG = "getLocationService";
    public static boolean isRunning = false;
    private GoogleApiClient apiClient;
    public static long sendRespondInterval = 1000;

    // the interval to request location updates
    private long locationUpdatesInterval = 15000;
    private Location location;
    private boolean isPeriodicallyRequested = false;
    private static final int SEND_LOCATION_ALARM_REQUEST_CODE = 2000;
    private Message respond = null;
    private Message message = null;
    private PowerManager.WakeLock wakeLock;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        boolean disable = intent.getBooleanExtra(GetLocation.FLAG_DISABLE,true);
        Log.i(TAG,"disable " + disable);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (disable) {
            disconnectGoogleApi();
            isRunning = false;
            prefs.edit().putString(GetLocation.DATA_KEY_LOCATION_DATA,"").commit();
            SetAlarm.cancel(getApplicationContext(),SEND_LOCATION_ALARM_REQUEST_CODE);
            stopSelf();
            return Service.START_NOT_STICKY;
        }
        PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,action);
        wakeLock.acquire();
        message = intent.getParcelableExtra(mR.KEY_MESSAGES);
        Log.i(TAG,"Message is " + message.toString());
        setLocationAttributesFromIntent(intent);
        Log.i(TAG,"response interval is " + sendRespondInterval + " and location update interval is " + locationUpdatesInterval);
        respond = null;
        if (!isRunning) {
            Log.i(TAG,"is first time");
            setLocationRequests();
        }else {
            Log.i(TAG,"is not first time");
            disconnectGoogleApi();
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,action);
            wakeLock.acquire();
            setLocationRequests();
        }
        return Service.START_REDELIVER_INTENT;
    }

    private void setLocationAttributesFromIntent(Intent intent) {
        locationUpdatesInterval = intent.getLongExtra(GetLocation.DATA_KEY_REQUEST_LOCATION_INTERVAL,150000);
        sendRespondInterval = intent.getLongExtra(GetLocation.DATA_KEY_SEND_RESPONSE_INTERVAL,60000 * 5);
        isPeriodicallyRequested = intent.getBooleanExtra(GetLocation.FLAG_REQUEST_LOCATION_PERIODICALLY,false);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setLocationRequests() {
        apiClient = new GoogleApiClient
                .Builder(getApplicationContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        apiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected: ");
        isRunning = true;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(locationUpdatesInterval);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(apiClient, locationRequest, this);

        if (isPeriodicallyRequested){
            setLocationUpdateSenderAlarm();
            Log.i(TAG, "onConnected: ");
        }
    }

    /**
     * sets an alarm to sendWithSms the saved location back to the client that started this task.
     */
    private void setLocationUpdateSenderAlarm() {
        Message message = new Message();
        message.action = SendLocationUpdates.action;
        message.type = Message.Type.command;
        message.sender = this.message.sender;
        message.putExtra(DATA_KEY_SEND_RESPONSE_INTERVAL, String.valueOf(sendRespondInterval));
        SetAlarm.set(getApplicationContext(),message,SEND_LOCATION_ALARM_REQUEST_CODE,sendRespondInterval,true);
        Log.i(TAG, "setLocationUpdateSenderAlarm: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged: " + location);
        if (isRunning) {
            this.location = location;
            if (!isPeriodicallyRequested) {
                disconnectGoogleApi();
                isRunning = false;
            }else {
                saveLocationData();
            }
            // if response is not null then we have  already sent the locations
            if (respond == null) {
                sendResponse();
            }
        }else {
            Log.i(TAG, "onLocationChanged: disconnected ");
            disconnectGoogleApi();
        }
    }

    private void disconnectGoogleApi() {
        if (apiClient != null && apiClient.isConnected()) {
            apiClient.disconnect();
            apiClient.unregisterConnectionCallbacks(this);
            apiClient.unregisterConnectionFailedListener(this);
            isRunning = false;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
             wakeLock.release();
        }
    }

    /**
     * saves current location in preferences.
     */
    private void saveLocationData() {
        Log.i(TAG, "saveLocationData: ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String savedLocations = prefs.getString(DATA_KEY_LOCATION_DATA,"");
        if (!savedLocations.contains(location.getLatitude() + "," + location.getLongitude())) {
            Log.i(TAG,"Location is new");
            if (!savedLocations.equals("")) {
                savedLocations += mR.DATA_SEPARATOR;
            }
            savedLocations += getLocationData();
            prefs.edit().putString(DATA_KEY_LOCATION_DATA, savedLocations).commit();
        }
    }

    /**
     * constructs a string containing the latlng and the time location was received.
     * @return returns the current location data plus current time
     */
    private String getLocationData(){
        return location.getLatitude() + "," + location.getLongitude() + "," + System.currentTimeMillis();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectGoogleApi();
    }

    /**
     * send the respond back to the client that started this task.
     */
    private void sendResponse() {
        Log.i(TAG, "sendResponse: ");
        respond = new Message();
        respond.action = GetLocationResponseHandler.class.getSimpleName();
        respond.type = Message.Type.response;
        respond.addFlag(FLAG_ENABLE_INSTANT_MODE);
        respond.putExtra(MessageHandler.KEY_SUCCESS, String.valueOf(true));
        if (location != null) {
            respond.putExtra(DATA_KEY_LOCATION_DATA, getLocationData());
        }
        MessageSender sender = new MessageSender(getApplicationContext(),mR.RESPONSE_ENCRYPTION_KEY,message.sender);
        sender.setMessage(respond)
                .send();
    }
}