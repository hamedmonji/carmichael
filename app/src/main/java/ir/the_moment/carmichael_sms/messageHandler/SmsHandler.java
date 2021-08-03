package ir.the_moment.carmichael_sms.messageHandler;

import android.content.Context;
import androidx.annotation.Nullable;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;

/**
 * Created by vaas on 7/26/17.
 * message handler for messages received using sms.
 */

public class SmsHandler extends MessageHandler {
    private String senderNumber;
    protected SmsHandler(Context context, String rawMessage) {
        super(context, rawMessage);
    }

    @Override
    protected DeviceModel getSenderDevice() {
        return DeviceInfoDbHelper.getDeviceByNumber(getContext(),senderNumber,getType());
    }

    @Override
    protected void setMessage(Message message) {
        message.sender = senderNumber;
    }

    @Override
    protected void onResultReceived(@Nullable Message response) {
        if (response != null) {
            MessageSender sender = new MessageSender(getContext(), mR.RESPONSE_ENCRYPTION_KEY,senderNumber);
            sender.setMessage(response)
                    .send();
        }
    }

    public SmsHandler(Context context, String rawMessage, String senderNumber) {
        super(context, rawMessage);
        this.senderNumber = senderNumber;
    }
}
