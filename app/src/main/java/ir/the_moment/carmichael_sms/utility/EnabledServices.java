package ir.the_moment.carmichael_sms.utility;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import ir.the_moment.carmichael_sms.WipeDataReceiver;
import ir.the_moment.carmichael_sms.ui.settings.SettingsActivity;
import ir.the_moment.carmichael_sms.NetworkStatus;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/1/2017.
 */

public class EnabledServices {
    private static final String TAG = "network";

    public static boolean isGpsEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkProviderEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isMobileDataEnabled(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int dataState = telephonyManager.getDataState();
        return dataState == TelephonyManager.DATA_CONNECTED || dataState == TelephonyManager.DATA_CONNECTING;
    }

    public static NetworkStatus getWifiStatus(Context context) {
        NetworkStatus status = new NetworkStatus();
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        status.networkInfo = connectivityManager.getActiveNetworkInfo();
        status.wifiInfo = wifiManager.getConnectionInfo();
        status.wifiEnabled = !status.wifiInfo.getSSID().contains("unknown");
        return status;
    }

    public static boolean enableMobileData(Context context) {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                final Class conmanClass = Class.forName(conman.getClass().getName());
                final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
                iConnectivityManagerField.setAccessible(true);
                final Object iConnectivityManager = iConnectivityManagerField.get(conman);
                final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
                final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
                setMobileDataEnabledMethod.setAccessible(true);

                setMobileDataEnabledMethod.invoke(iConnectivityManager, true);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean enableWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        return wifiManager.reconnect();
    }

    public static void connectToWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);

        if (!wifiManager.reconnect()) {
            wifiManager.startScan();
            // TODO: 4/2/2017 implement methods to receive on scan completed
            List<ScanResult> results = wifiManager.getScanResults();
            if (results != null && results.size() > 0) {
                List<WifiConfiguration> configurations = wifiManager.getConfiguredNetworks();
                for (int i = 0; i < results.size(); i++) {
                    String ssid = results.get(i).SSID;
                    for (int j = 0; j < configurations.size(); j++) {
                        if (ssid.equals(configurations.get(j).SSID)) {
                            wifiManager.enableNetwork(configurations.get(j).networkId, false);
                            wifiManager.reconnect();
                            break;
                        }

                    }
                }
            }
        }
    }

    public static boolean isAdminEnabled(Context context) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdmin = new ComponentName(context, WipeDataReceiver.class);
        return policyManager.isAdminActive(deviceAdmin);
    }

    public static boolean isFileManagerEnabled(Context context) {
        return Utility.isWritePermissionsGranted(context);
    }

    private static String getDropboxToken(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(SettingsActivity.KEY_PREF_DROP_BOX_ACCESS_TOKEN,null);
    }

    public static boolean isWipeEnabled(Context context) {
        return isAdminEnabled(context) &&
                Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_wipe_encrypted));
    }

    public static boolean isTakingPicturesEnabled(Context context) {
        return getDropboxToken(context) != null && Utility.isCameraPermissionGranted(context) &&
                Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_taking_pictures_encrypted));
    }

    public static boolean isUnauthorizedWipeEnabled(Context context) {
        return Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_unauthorized_wipe));
    }

    public static boolean isLocationEnabled(Context context) {
        return Utility.isLocationPermissionGranted(context) &&
                Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_location_encrypted));
    }


    public static boolean isMaxAttemptsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.key_pref_enable_max_attempts),false);
    }

    public static boolean isLockDeviceEnabled(Context context) {
        return isAdminEnabled(context) &&
                Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_lock_device_encrypted));
    }

    public static boolean isStatusCheckEnabled(Context context) {
        return Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_status_check_encrypted));
    }

    public static boolean isBootNotificationEnabled(Context context) {
        return Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_boot_notification_encrypted));
    }

    public static boolean isControlZonesEnabled(Context context) {
        return Utility.isLocationPermissionGranted(context) &&
                Security.getDecryptedBooleanPreferenceEntry(context, context.getString(R.string.key_pref_allow_control_zones_encrypted));
    }

    @SuppressLint("NewApi")
    public static boolean isDisableSilentEnabled(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean enabled = Security.getDecryptedBooleanPreferenceEntry(context,
                        context.getString(R.string.key_pref_allow_disable_silent_encrypted));

        return (!Utility.isPostMarshMellow()
                && enabled)
                || (Utility.isPostMarshMellow() && enabled && notificationManager.isNotificationPolicyAccessGranted());
    }
}