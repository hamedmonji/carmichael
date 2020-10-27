package ir.the_moment.carmichael_sms;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;

/**
 * Created by vaas on 3/25/2017.
 */
public class WipeDataReceiver  extends DeviceAdminReceiver {

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        DevicePolicyManager policyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        policyManager.lockNow();
        return context.getString(R.string.disable_admin);
    }
}