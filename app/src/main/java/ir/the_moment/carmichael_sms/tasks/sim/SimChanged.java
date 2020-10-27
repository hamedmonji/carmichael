package ir.the_moment.carmichael_sms.tasks.sim;

import android.database.Cursor;

import java.util.ArrayList;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.responseHandler.SimChangedResponseHandler;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by mac on 6/28/17.
 */

public class SimChanged extends UserActivatedTask {

    public static final String action = "sim.SimChanged";

    public static final String KEY_DATA_USER_ID = "key_user_id";

    @Override
    public int getPermission() {
        return mR.Permissions.SIM_CHANGED;
    }

    @Override
    public String getDescription() {
        return  context.getString(R.string.description_sim_changed);
    }

    @Override
    public priority getPriority() {
        return priority.low;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.sim_changed);
    }

    @Override
    protected void action() {
        sendSimChangedMessage();
    }

    private void sendSimChangedMessage() {
        Message message = new Message();
        message.action = SimChangedResponseHandler.class.getSimpleName();
        message.type = Message.Type.response;
        String simID = Utility.getUserID(getContext());
        if (simID != null) {
            message.putExtra(KEY_DATA_USER_ID, simID);
            Cursor cursor = DeviceInfoDbHelper.getDevicesByType(context, mR.TYPE_HANDLER);
            ArrayList<String> phoneNumber = new ArrayList<>();
            String password = Security.getDecryptedPreferenceEntry(context, context.getString(R.string.key_pref_user_password_encrypted));
            if (password != null) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        DeviceModel device = Security.getDecryptedDeviceModel(cursor, password);
                        if (device != null) {
                            if (mR.Permissions.hasPermission(device.permissions, mR.Permissions.SIM_CHANGED)) {
                                phoneNumber.add(device.number);
                            }
                        }
                    }
                }
            }
            if (!phoneNumber.isEmpty()) {
                MessageSender sender =
                        new MessageSender(getContext(),mR.RESPONSE_ENCRYPTION_KEY,phoneNumber.toArray(new String[]{}));
                sender.addMessage(message)
                        .send();
            }
        }
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
