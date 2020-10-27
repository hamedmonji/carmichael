package ir.the_moment.carmichael_sms.responseHandler;

import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;

/**
 * Created by vaas on 8/11/17.
 */

public class GeneralResponseHandler extends ResponseHandler {
    public static final String DATA_KEY_TASK_NAME = "99";
    @Override
    protected void handle() {
        String taskClassName = getResponse().getExtra(DATA_KEY_TASK_NAME);
        UserActivatedTask task = TaskFactory.createTask(TaskExecutorService.tasksPackage + taskClassName);
        task.setContext(getContext());
        String title = task.getTaskName() + " ";
        if (getResponse().getBoolean(MessageHandler.KEY_SUCCESS)) {
            title += getContext().getString(R.string.succeed);
        }else {
            title += getContext().getString(R.string.failed);
        }
        showNotification(null,title,1010,R.drawable.app_logo);
    }
}