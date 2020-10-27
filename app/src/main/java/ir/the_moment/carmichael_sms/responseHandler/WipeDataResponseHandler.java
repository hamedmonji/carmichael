package ir.the_moment.carmichael_sms.responseHandler;

import ir.the_moment.carmichael_sms.R;

import static ir.the_moment.carmichael_sms.messageHandler.MessageHandler.KEY_SUCCESS;


/**
 * Created by mac on 6/30/17.
 */

public class WipeDataResponseHandler extends ResponseHandler {
    @Override
    protected void handle() {
        boolean succeed = Boolean.parseBoolean(getResponse().getExtra(KEY_SUCCESS));
        String title;
        if (succeed){
            title = getContext().getString(R.string.phone_wiped);
        }else {
            title = getContext().getString(R.string.wipe_failed);
        }
        int WIPE_NOTIFICATION_CODE = 2252;
        showNotification(null,title, WIPE_NOTIFICATION_CODE,R.drawable.wipe_notification);
    }
}