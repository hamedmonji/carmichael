package ir.the_moment.carmichael_sms.tasks.statusCheck;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.utility.MessageCreator;

/**
 * Created by vaas on 4/2/2017.
 * holds all the info about the phone.
 * used for service availability check.
 */

public class PhoneInfo extends Message {

    public static final String KEY_BATTERY_LEVEL = "b";
    public static final String KEY_IS_ADMIN = "a";
    public static final String KEY_OPERATOR_NAME = "o";

    public static final String FLAG_HAS_INTERNET = "3";

    public String activeConnections;
    public boolean wifiConnected = false;
    public boolean mobileDataEnabled = false;
    public boolean mobileDataCanBeEnabled = false;
    public String wifiName;
    public String operatorName;
    public String batteryLevel;
    public boolean hasInternet = false;

    public boolean isAdmin() {
        if (getData().containsKey(KEY_IS_ADMIN)){
            return Boolean.parseBoolean(getData().get(KEY_IS_ADMIN));
        }else return false;
    }

    public String getOperatorName() {
        if (getData().containsKey(KEY_OPERATOR_NAME)){
            return getData().get(KEY_OPERATOR_NAME);
        }else return null;
    }


    @Override
    public String toString() {
        setData();
        MessageCreator creator = new MessageCreator(this);
        type = Type.response;

        return creator.getInJsonFormat();

    }

    public void setData() {
        if (hasInternet){
            addFlag(FLAG_HAS_INTERNET);
        }

        putExtra(KEY_OPERATOR_NAME,operatorName);
        putExtra(KEY_BATTERY_LEVEL,batteryLevel);
    }
}
