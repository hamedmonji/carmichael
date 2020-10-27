package ir.the_moment.carmichael_sms;

/**
 * Created by vaas on 4/4/2017.
 * representing a device.
 */

public class DeviceModel {
    public static final int STATUS_OK = 0;
    public static final int STATUS_LOST = 1;
    public long id;
    public String pic;
    public String name;
    public String number;
    public String alternateNumber;
    public int type ;
    public int permissions = 0;
    public int status = STATUS_OK;
    public int rank = 0;
    public String userId;

    @Override
    public String toString() {
        return name + "," + number + "," + type;
    }
}
