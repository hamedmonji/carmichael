package ir.the_moment.carmichael_sms.tasks.wipeData;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import ir.the_moment.carmichael_sms.WipeDataReceiver;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.responseHandler.WipeDataResponseHandler;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 3/25/2017.
 * perform a factory reset
 */

public class WipeData  extends UserActivatedTask {
    public static final String action = "wipeData.WipeData";

    /**
     *only wipe data if a shutdown was requested
     */
    public static final String FLAG_WIPE_DATA_AT_SHUTDOWN = "0";

    /**
     * wipe external storage such as sdcard too.
     */
    public static final String FLAG_WIPE_EXTERNAL_STORAGE = "1";



    private boolean wipeAtShutdown = false;
    private boolean wipeExternalStorage = false;
    private DevicePolicyManager policyManager;
    private boolean succeed = false;

    @Override
    protected void action() {
        parseFlags();
        if (!wipeAtShutdown) {
            Log.i("wifiReceiver", "action: wipe");
            wipeData();
        }
    }
    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {
        if (getMessage().flags != null){
            for (String flag :
                    getMessage().flags) {
                switch (flag){
                    case FLAG_WIPE_DATA_AT_SHUTDOWN:
                        wipeAtShutdown = true;
                        break;
                    case FLAG_WIPE_EXTERNAL_STORAGE:
                        wipeExternalStorage = true;
                        break;
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isWipeEnabled(context);
    }

    private void wipeData(){
        policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName deviceAdmin = new ComponentName(getContext(), WipeDataReceiver.class);
        if (policyManager.isAdminActive(deviceAdmin)) {
            succeed = true;
            sendRespond();
            policyManager = (DevicePolicyManager) getContext().getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (wipeExternalStorage) {
                policyManager.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
            }else {
                policyManager.wipeData(0);
            }

        }
    }

    private void sendRespond() {
        respond = new Message();
        respond.type = Message.Type.response;
        respond.action  = WipeDataResponseHandler.class.getSimpleName();
        onActionFinished(succeed);
    }

    @Override
    public int getPermission() {
        return mR.Permissions.WIPE;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_wipe);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.wipe_data);
    }
}
