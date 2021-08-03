package ir.the_moment.carmichael_sms.ui.taskUI.base;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/23/17.
 */

public class TaskOptionsDialogFragment extends DialogFragment {
    private TaskUI taskUI = null;
    public void setTaskUI(TaskUI taskUI) {
        this.taskUI = taskUI;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.configure_task))
                .setPositiveButton(R.string.menu_file_manager_send, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (taskUI != null){
                            taskUI.onSendClicked(taskUI.getMessage());
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        }).setView(taskUI.getView());
        return builder.create();
    }
}
