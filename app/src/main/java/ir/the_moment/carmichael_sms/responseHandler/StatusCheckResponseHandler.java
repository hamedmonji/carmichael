package ir.the_moment.carmichael_sms.responseHandler;

import android.app.PendingIntent;
import android.content.Intent;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.deviceManager.DeviceManagerActivity;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by mac on 6/30/17.
 */

public class StatusCheckResponseHandler extends ResponseHandler {
    @Override
    protected void handle() {
        Intent availableTaskIntent = new Intent(getContext(), DeviceManagerActivity.class);
        availableTaskIntent.putExtra(mR.KEY_MESSAGES,getResponse());
        availableTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent statusCheckPendingIntent = PendingIntent.getActivity(getContext(),87872,availableTaskIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        showNotification(statusCheckPendingIntent,getContext().getString(R.string.received_status_check),87829,R.drawable.status_notification);
    }
}