package ir.the_moment.carmichael_sms.responseHandler;

import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.requests.addRequest.AddRequest;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 7/22/17.
 * a response handler for add request for devices without a sim card
 */

public class DeviceAddedResponseHandler extends ResponseHandler {

    @Override
    protected void handle() {
        addDevice();
    }

    private void addDevice() {
        DeviceModel device = new DeviceModel();
        device.type = mR.TYPE_ASSET;
        device.number = getResponse().sender;
        device.name = getResponse().getExtra(AddRequest.DATA_KEY_USERNAME);
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
        dbHelper.insertDevice(device);
        String title = device.name + " " + getContext().getString(R.string.was_added_to_your_assets);
        showNotification(null,title,1122,R.drawable.add_user);
    }
}
