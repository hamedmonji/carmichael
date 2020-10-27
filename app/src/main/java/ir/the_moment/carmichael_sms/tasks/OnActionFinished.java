package ir.the_moment.carmichael_sms.tasks;

import ir.the_moment.carmichael_sms.Message;

/**
 * Created by vaas on 4/2/2017.
 */

public interface OnActionFinished {
    void onActionFinished(boolean succeed,Message respond);
}
