package ir.the_moment.carmichael_sms.ui.taskUI;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.tasks.wipeData.WipeData;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by mac on 6/14/17.
 */

public class WipeDataUI extends TaskUI {
    @Override
    protected View constructView() {
        return LayoutInflater.from(getContext()).inflate(R.layout.task_ui_wipe_data,null);
    }

    @Override
    protected Message constructMessage(View view) {
        if (view != null){
            CheckBox wipeExternal = view.findViewById(R.id.wipe_external);

            Message message = new Message();
            message.action = getAction();

            if (wipeExternal.isChecked()){
                message.addFlag(WipeData.FLAG_WIPE_EXTERNAL_STORAGE);
            }
            return message;
        }
        return null;
    }
}
