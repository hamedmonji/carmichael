package ir.the_moment.carmichael_sms.tasks.statusCheck;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.NetworkStatus;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.responseHandler.StatusCheckResponseHandler;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.tasks.location.GetLocation;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by vaas on 3/25/2017.
 * checks for available services and tasks ..
 */

public class StatusCheck extends UserActivatedTask implements OnActionFinished {
    public static final String action = "statusCheck.StatusCheck";

    public static final String KEY_AVAILABLE_TASKS = "available_tasks";

    private PhoneInfo info;
    private DeviceModel sendersDevice;
    @Override
    public int getPermission() {
        return mR.Permissions.STATUS_CHECK;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_status_check);
    }

    @Override
    public priority getPriority() {
        return priority.low;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.status_check);
    }

    @Override
    protected void action() {
        sendersDevice = DeviceInfoDbHelper.getDeviceByNumber(context, getMessage().sender, mR.TYPE_HANDLER);
        info = new PhoneInfo();
        info.action = action;
        info.type = Message.Type.response;
        getAvailableService();
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isStatusCheckEnabled(context);
    }

    private void getAvailableService() {
        setNetworkInfo();
        setBatteryInfo();
        setSimInfo();
        setAvailableTasks();
        sendRespond();
    }

    private void setBatteryInfo() {
        Intent batteryIntent = getContext().registerReceiver(null,new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int batteryLevel = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
            int batteryScale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE,0);
            info.batteryLevel = String.valueOf(batteryLevel * 100f / batteryScale);
        }
    }

    // gets all network related info
    private void setNetworkInfo() {
        NetworkStatus status = EnabledServices.getWifiStatus(context);
        if (!status.wifiEnabled){
            EnabledServices.enableWifi(context);
        }
        info.hasInternet = Utility.hasInternetConnection(context);
        if (status.networkInfo != null){
            info.activeConnections = status.networkInfo.getTypeName();
        }
        if (status.wifiEnabled && !status.wifiInfo.getSSID().equals("")) {
            info.wifiConnected = true;
            info.wifiName = status.wifiInfo.getSSID();
        }

        if (EnabledServices.isMobileDataEnabled(context)){
            info.mobileDataEnabled = true;
        }else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            info.mobileDataCanBeEnabled = true;
        }else {
            info.mobileDataEnabled = false;
        }
    }

    // gets all sim card related info
    private void setSimInfo(){
        info.operatorName = Utility.getSimOperatorName(context);
    }

    private void setAvailableTasks(){
        info.putInt(KEY_AVAILABLE_TASKS,sendersDevice.permissions);
    }

    private void sendRespond() {
        info.setData();
        respond = info;
        respond.action = StatusCheckResponseHandler.class.getSimpleName();
        onActionFinished(true);
    }

    @Override
    public void onActionFinished(boolean succeed, Message respond) {
        if (succeed){
            info.putExtra(GetLocation.DATA_KEY_LOCATION_DATA,respond.getExtra(GetLocation.DATA_KEY_LOCATION_DATA));
            sendRespond();
        }
    }

}
