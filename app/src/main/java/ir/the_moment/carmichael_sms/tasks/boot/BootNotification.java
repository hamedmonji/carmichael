package ir.the_moment.carmichael_sms.tasks.boot;

import android.database.Cursor;

import java.util.ArrayList;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.responseHandler.BootNotificationResponseHandler;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Security;

import static ir.the_moment.carmichael_sms.utility.Security.getDecryptedPreferenceEntry;

/**
 * Created by vaas on 7/13/17.
 */

public class BootNotification extends UserActivatedTask {
    public static final String action = "boot.BootNotification";
    @Override
    public int getPermission() {
        return mR.Permissions.BOOT_NOTIFICATION;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_boot_notification);
    }

    @Override
    public priority getPriority() {
        return priority.low;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.boot_notification);
    }

    @Override
    protected void action() {
        sendDeviceBootedMessage();
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isBootNotificationEnabled(getContext());
    }

    private void sendDeviceBootedMessage() {
        Message message = new Message();
        message.type = Message.Type.response;
        message.action = BootNotificationResponseHandler.class.getSimpleName();
        Cursor cursor = DeviceInfoDbHelper.getDevicesByType(context, mR.TYPE_HANDLER);
        ArrayList<String> phoneNumber = new ArrayList<>();
        String password = getDecryptedPreferenceEntry(context, context.getString(R.string.key_pref_user_password_encrypted));
        if (password != null && cursor != null) {
            while (cursor.moveToNext()) {
                DeviceModel device = Security.getDecryptedDeviceModel(cursor, password);
                if (device != null) {
                    if (mR.Permissions.hasPermission(device.permissions, mR.Permissions.BOOT_NOTIFICATION)) {
                        if (device.number != null) {
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