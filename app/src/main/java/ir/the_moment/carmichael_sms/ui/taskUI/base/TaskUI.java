package ir.the_moment.carmichael_sms.ui.taskUI.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.View;

import ir.the_moment.carmichael_sms.Message;

public abstract class TaskUI{
    private Context context;
    private String action;
    private FragmentManager fragmentManager;
    private OnSendClicked onSendClicked;
    private CurrentStatus currentStatus = null;

    private View optionsView = null;

    public final CurrentStatus getCurrentStatus() {
        return currentStatus;
    }

    @Nullable
    public final String getNumber(){
        if (currentStatus != null){
            return currentStatus.getNumber();
        }else {
            return null;
        }
    }

    public final void setOnSendClicked(OnSendClicked onSendClicked) {
        this.onSendClicked = onSendClicked;
    }

    public final FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void init(Context context, String action, FragmentManager fragmentManager){
        this.context = context;
        this.action = action;
        this.fragmentManager = fragmentManager;

    }

    protected final Context getContext() {
        return context;
    }

    protected abstract View constructView();
    protected abstract Message constructMessage(@Nullable View view);

    public final View getView(){
        if (optionsView != null) {
            return optionsView;
        }
        optionsView = constructView();
        return optionsView;
    }
    @Nullable
    public final Message getMessage(){
        return constructMessage(optionsView);
    }

    public void setAction(String action){
        this.action = action;
    }

    protected final String getAction() {
        return action;
    }

    public void onSendClicked(Message message){
        if (onSendClicked != null){
            onSendClicked.onSendClicked(message);
        }
    }

    public final void setCurrentStatus(CurrentStatus currentStatus) {
        this.currentStatus = currentStatus;
    }

    public interface OnSendClicked {
        void onSendClicked(Message message);
    }

    public interface CurrentStatus{
        String getNumber();
        Message getSenderMessage();
    }

    public void show(){
        TaskOptionsDialogFragment optionsDialogFragment = new TaskOptionsDialogFragment();
        optionsDialogFragment.setTaskUI(this);
        optionsDialogFragment.show(getFragmentManager(), getAction());
    }

    public final void dismiss(){
        if (getFragmentManager() != null){
            ((TaskOptionsDialogFragment) getFragmentManager().findFragmentByTag(getAction())).dismiss();
        }
    }
}
