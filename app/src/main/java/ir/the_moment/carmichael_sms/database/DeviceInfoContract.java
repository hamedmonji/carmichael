package ir.the_moment.carmichael_sms.database;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by vaas on 4/5/2017.
 * contract for deviceInfo database
 */

public class DeviceInfoContract {
    private DeviceInfoContract(){};
    public static final String AUTHORITY = "the_moment.carmichael_sms";
    public static final Uri BASE_DEVICE_INFO_URI = Uri.parse("content://the_moment.carmichael_sms/");

    public static final class DeviceInfo implements BaseColumns {

        private DeviceInfo() {
        }

        public static final String TABLE_NAME = "deviceInfo";
        public static final String PATH_DEVICE_INFO = "deviceInfo";
        public static final Uri DEVICE_INFO_URI = Uri.withAppendedPath(BASE_DEVICE_INFO_URI , PATH_DEVICE_INFO);

        public static final String _ID = BaseColumns._ID;

        public static final String NAME = "name";
        public static final String PERMISSIONS = "permissions";
        public static final String STATUS = "status";
        public static final String RANK = "rank";
        public static final String NUMBER = "number";
        public static final String PIC = "pic";
        public static final String USER_ID = "user_id";
        //phone number to use if the sim card was changed
        public static final String ALTERNATE_NUMBER = "alternate_number";
        /**
        *can only be one of two values
         * {@link the_moment.carmichael_sms.mR#TYPE_HANDLER}
         * {@link the_moment.carmichael_sms.mR#TYPE_ASSET}
         */
        public static final String TYPE = "type";

    }
}
