package ir.the_moment.carmichael_sms.ui.taskUI;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.alarm.DisableSilent;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

/**
 * Created by vaas on 7/28/17.
 */

public class DisableSilentUI extends TaskUI {
    @Override
    protected View constructView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.task_ui_disable_silent,null,false);
    }

    @Override
    protected Message constructMessage(View view) {
        Message message = new Message();
        message.action = getAction();
        message.type = Message.Type.command;
        CheckBox ring = (CheckBox) view.findViewById(R.id.ring);
        if (ring.isChecked()){
            message.addFlag(DisableSilent.FLAG_RING);
        }

        return message;
    }
}
