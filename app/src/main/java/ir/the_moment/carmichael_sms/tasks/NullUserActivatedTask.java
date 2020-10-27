package ir.the_moment.carmichael_sms.tasks;

/**
 * Created by mac on 5/31/17.
 */

class NullUserActivatedTask extends UserActivatedTask {

    public static final String action = "NullUserActivatedTask";

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
        return false;
    }

    @Override
    public int getPermission() {
        return 0;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public priority getPriority() {
        return priority.low;
    }

    @Override
    public String getTaskName() {
        return null;
    }
}
