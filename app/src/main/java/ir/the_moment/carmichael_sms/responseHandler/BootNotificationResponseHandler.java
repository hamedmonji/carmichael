package ir.the_moment.carmichael_sms.responseHandler;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;

/**
 * Created by vaas on 7/13/17.
 */

public class BootNotificationResponseHandler extends ResponseHandler {
    @Override
    protected void handle() {
        DeviceModel bootedDevice = DeviceInfoDbHelper.getDeviceByNumber(getContext(),getResponse().sender, mR.TYPE_ASSET);

        if (bootedDevice != null) {
            String title = bootedDevice.name + " " + getContext().getString(R.string.boot_notification_received);
            showNotification(null,title,8989,R.drawable.boot_notification);
        }
    }
}
