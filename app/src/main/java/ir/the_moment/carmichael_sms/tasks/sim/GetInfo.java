package ir.the_moment.carmichael_sms.tasks.sim;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.Task;

/**
 * Created by vaas on 3/23/2017.
 */

public class GetInfo extends Task {

    public static final String action = "sim.GetInfo";

    @Override
    protected void action() {

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

    @Override
    public int getPermission() {
        return mR.Permissions.STATUS_CHECK;
    }

}
