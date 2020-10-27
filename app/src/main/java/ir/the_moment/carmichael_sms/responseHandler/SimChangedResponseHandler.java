package ir.the_moment.carmichael_sms.responseHandler;

import android.util.Log;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.tasks.sim.SimChanged;

/**
 * Created by vaas on 7/3/17.
 */

public class SimChangedResponseHandler extends ResponseHandler {
    @Override
    protected void handle() {
        String senderId = getResponse().getExtra(SimChanged.KEY_DATA_USER_ID);
        Log.i(mR.TAG, "handle: " + senderId);
        DeviceModel device = DeviceInfoDbHelper.getDeviceByUserId(getContext(),senderId, mR.TYPE_ASSET);
        device.alternateNumber = getResponse().sender;
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
        dbHelper.updateDeviceByNumber(getContext(),device,device.number,mR.TYPE_ASSET);
        String deviceSimChanged =
                getContext().getString(R.string.sim_changed_for_device).replace("***",device.number);

        showNotification(null,deviceSimChanged,1298,R.drawable.sim_card_notification);
    }
}
