package ir.the_moment.carmichael_sms;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import ir.the_moment.carmichael_sms.messageHandler.MessageHandler;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;

/**
 * Created by vaas on 3/23/2017.
 * base class for actions.
 *
 */

public class TaskExecutorService extends IntentService implements OnActionFinished {

    public static final String tasksPackage = "ir.the_moment.carmichael_sms.tasks.";

    public static final String TAG = "taskExecutor";
    private Message message = new Message();
    private ResultReceiver respondReceiver;



    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public TaskExecutorService() {
        super("actionExecutorThread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        message = intent.getParcelableExtra(mR.KEY_MESSAGES);
        respondReceiver = intent.getParcelableExtra(MessageHandler.KEY_RESPOND_RECEIVER);
        Log.i(mR.TAG, "pictures: started " + message.toString());
        runTask();
    }

    private void runTask() {
        String taskAction = tasksPackage+message.action;
        Task task = TaskFactory.createTask(taskAction);
        Log.i(TAG,taskAction);
        task.setContext(getApplicationContext())
                .setMessage(message)
                .setOnActionFinished(this)
                .run();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onActionFinished(boolean succeed, Message respond) {
        Log.i(mR.TAG, "onActionFinished: " + respond.action);
        Bundle results = new Bundle();
        results.putBoolean(MessageHandler.KEY_SUCCESS,succeed);
        results.putParcelable(MessageHandler.KEY_RESPOND,respond);
        respondReceiver.send(100,results);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(mR.TAG, "onDestroy: executor : " + message.toString());
    }
}
