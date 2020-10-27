package ir.the_moment.carmichael_sms.responseHandler;

import android.app.PendingIntent;
import android.content.Intent;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.requests.addDevice.AddAssetActivity;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 6/30/17.
 */

public class AddRequestResponseHandler extends ResponseHandler {
    public static final String TAG = "requestDebug";
    @Override
    protected void handle() {
        Intent addAssetIntent = new Intent(getContext(), AddAssetActivity.class);
        addAssetIntent.putExtra(mR.KEY_MESSAGES, getResponse());
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 12342, addAssetIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        showNotification(pendingIntent, getContext().getString(R.string.add_request_response_received), 8888, R.drawable.add_user);
    }


}
