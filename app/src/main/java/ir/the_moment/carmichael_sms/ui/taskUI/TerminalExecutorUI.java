package ir.the_moment.carmichael_sms.ui.taskUI;

import androidx.annotation.Nullable;
import android.view.View;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;


/**
 * Created by vaas on 7/29/17.
 */

public class TerminalExecutorUI extends TaskUI {
    @Override
    protected View constructView() {
        return null;
    }

    @Override
    protected Message constructMessage(@Nullable View view) {
        return null;
    }

    @Override
    public void show() {
//        if (getCurrentStatus() != null) {
//            if (getCurrentStatus().getSendMethod() == MessageSender.SendMethods.SIM){
//                Toast.makeText(getContext(), R.string.not_available_with_sim, Toast.LENGTH_LONG).show();
//            }else {
//                Intent terminalIntent = new Intent(getContext(), TerminalActivity.class);
//                terminalIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                Message message = getCurrentStatus().getSenderMessage();
//                terminalIntent.putExtra(mR.KEY_MESSAGES, message);
//                getContext().startActivity(terminalIntent);
//            }
//        }
    }
}