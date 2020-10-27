package ir.the_moment.carmichael_sms.tasks.lock;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.WipeDataReceiver;
import ir.the_moment.carmichael_sms.responseHandler.GeneralResponseHandler;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 4/25/17.
 * locks the device with a password
 */

public class Lock extends UserActivatedTask {
    public static final String action = "lock.Lock";
    public static final String DATA_KEY_PASSWORD = "0";

    private String password;

    @Override
    protected void action() {
        parseData();
        lock();
    }

    @Override
    protected void parseData() {
        password = getMessage().getExtra(DATA_KEY_PASSWORD);
    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isLockDeviceEnabled(context);
    }

    private void lock() {
        if (isEnabled()) {
            DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            boolean succeed = false;
            if (password != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                ComponentName admin = new ComponentName(getContext(), WipeDataReceiver.class);
                policyManager.setPasswordMinimumLength(admin,6);
                policyManager.setPasswordQuality(admin,DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
                succeed = policyManager.resetPassword(password,DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            }else if (password == null){
                succeed = true;
            }
            policyManager.lockNow();
            respond = new Message();
            respond.action = GeneralResponseHandler.class.getSimpleName();
            respond.type = Message.Type.response;
            respond.putExtra(GeneralResponseHandler.DATA_KEY_TASK_NAME,action);
            onActionFinished(succeed);
        }
    }

    @Override
    public int getPermission() {
        return mR.Permissions.LOCK_DEVICE;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_lock);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.lock_device);
    }
}
