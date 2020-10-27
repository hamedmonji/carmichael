package ir.the_moment.carmichael_sms.ui.taskUI.base;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by mac on 6/12/17.
 * default ui for tasks that don't have one
 */

public class NullTaskUI extends TaskUI {
    @Override
    public Message constructMessage(View view ) {
        Message message = new Message();
        message.action = getAction();
        message.type = Message.Type.command;
        return message;
    }

    @Override
    public View constructView() {
        return new View(getContext());
    }

    @Override
    public void show() {
        UserActivatedTask task = TaskFactory.createTask(TaskExecutorService.tasksPackage + getAction());
        task.setContext(getContext());
        new AlertDialog.Builder(getContext())
                .setTitle(task.getTaskName())
                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSendClicked(constructMessage(null));
                    }
                }).setNegativeButton(R.string.cancel,null)
                .show();
    }
}