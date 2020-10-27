package ir.the_moment.carmichael_sms.tasks;

/**
 * Created by mac on 5/31/17.
 */

public class TaskFactory {
    private TaskFactory(){
    }

    public static <T extends Task> T createTask(String taskAction){
        try {
            return (T) Class.forName(taskAction).newInstance();
        } catch (Exception e){
            //noinspection unchecked
            return (T) new NullUserActivatedTask();
        }
    }
}