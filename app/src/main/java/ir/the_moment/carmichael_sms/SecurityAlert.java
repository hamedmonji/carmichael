package ir.the_moment.carmichael_sms;

import java.util.ArrayList;

/**
 * Created by vaas on 5/5/17.
 * holds the data for a security alert.
 */

public class SecurityAlert {
    public static final String TASK_UNKNOWN = "unknown";
    public String task;
    public String time;
    public String number;
    public static SecurityAlert createAlert(String alert){
        String[] fields = alert.split(",");
        SecurityAlert securityAlert = new SecurityAlert();
        if (fields.length == 3) {
            securityAlert.task = fields[0];
            securityAlert.time = fields[1];
            securityAlert.number= fields[2];
            return securityAlert;
        }
        return null;
    }

    public static ArrayList<SecurityAlert> createAlerts(String rawAlerts){
        ArrayList<SecurityAlert> alertsList = new ArrayList<>();
        String[] alerts = rawAlerts.split(mR.DATA_SEPARATOR);
        for (String alert:alerts) {
            alertsList.add(createAlert(alert));
        }
        return alertsList;
    }

    @Override
    public String toString() {
        return task + "," + time + "," + number ;
    }
}
