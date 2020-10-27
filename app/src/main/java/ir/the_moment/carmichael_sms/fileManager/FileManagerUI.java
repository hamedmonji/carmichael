package ir.the_moment.carmichael_sms.fileManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.tasks.dataManager.UploadData;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

/**
 * Created by mac on 7/2/17.
 */

public class FileManagerUI extends TaskUI {
    @Override
    protected View constructView() {
        if (getContext() == null){
            throw new IllegalStateException("Context is null");
        }
        return LayoutInflater.from(getContext()).inflate(R.layout.task_ui_file_manager,null,false);
    }

    @Override
    protected Message constructMessage(View view) {
        Message message = new Message();
        message.action = getAction();
        CheckBox deleteAll = (CheckBox) view.findViewById(R.id.delete_all);
        CheckBox deleteSuccessful = (CheckBox) view.findViewById(R.id.delete_successful);

        if (deleteAll.isChecked()) {
            message.addFlag(UploadData.FLAG_DELETE_ALL_AFTER_UPLOAD);
        }
        if (deleteSuccessful.isChecked()){
            message.addFlag(UploadData.FLAG_DELETE_SUCCESSFUL_AFTER_UPLOAD);
        }

        return message;
    }
}
