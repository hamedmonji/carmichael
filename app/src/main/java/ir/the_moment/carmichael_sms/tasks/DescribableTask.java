package ir.the_moment.carmichael_sms.tasks;

/**
 * Created by vaas on 8/14/17.
 */

public interface DescribableTask {
    String getDescription();
    String getTaskName();
    Task.priority getPriority();
}
