package ir.the_moment.carmichael_sms.ui.customView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.tasks.statusCheck.StatusCheck;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUIFactory;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

import static ir.the_moment.carmichael_sms.ui.customView.TaskView.OnClickType.toggle;

/**
 * Created by vaas on 6/9/17.
 */

public class TaskView extends CardView implements View.OnLongClickListener {
    private TextView textView;
    private boolean enabled = true;
    private String taskName = null;
    private TaskUI.OnSendClicked onSendClicked = null;
    private TaskUI.CurrentStatus currentStatus = null;
    private DeviceModel device = null;
    private Message message;
    private boolean isEnabledInSettings = false;

    public ImageView getImage() {
        return image;
    }

    public void setImage(Drawable image){
        this.image.setImageDrawable(image);
    }

    public void setText(String text){
        textView.setText(text);
    }

    public String getText(){
        return textView.getText().toString();
    }

    private ImageView image;
    private FragmentManager fragmentManager;
    private OnClickType OnClickType = toggle;
    private int permission = 0;

    public void setCurrentStatus(TaskUI.CurrentStatus currentStatus){
        this.currentStatus = currentStatus;
    }

    public void setMessage(Message message){
        this.message = message;
        if (message != null) {
            enableBasedOnMessage(message);
        }
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        if (l == null){
            super.setOnClickListener(listener);
        }else {
            super.setOnClickListener(l);
        }
    }


    public FragmentManager getFragmentManager() {
        return fragmentManager;
    }

    public void setFragmentManager(FragmentManager fragmentManager){
        this.fragmentManager = fragmentManager;
    }

    @Override
    public boolean onLongClick(View v) {
        UserActivatedTask task = TaskFactory.createTask(TaskExecutorService.tasksPackage + getAction());
        task.setContext(getContext());
        new AlertDialog.Builder(getContext())
                .setMessage(task.getDescription())
                .setPositiveButton(R.string.ok,null)
                .show();
        return true;
    }


    /**
     *toggle will add or remove the permission from the device
     * execute will do a check to see if the device has that permission and call the onTaskClicked method
     */
    public enum OnClickType {
        toggle,
        execute
    }

    public void setOnClickType(OnClickType OnClickType) {
        this.OnClickType = OnClickType;
        setOnClickListener(listener);
    }

    public int getPermission() {
        return permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public DeviceModel getDevice() {
        return device;
    }

    public void setDevice(@NonNull DeviceModel device) {
        this.device = device;
        setOnClickListener(listener);
        enableBasedOnPermission(device);
    }

    public void setOnSendClicked(TaskUI.OnSendClicked onSendClicked){
        this.onSendClicked = onSendClicked;
    }

    public TaskView(Context context) {
        super(context);
        init(context, null);
    }

    public TaskView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TaskView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        LayoutInflater.from(getContext()).inflate(R.layout.task_view,this);
        textView = findViewById(R.id.textView);
        image = findViewById(R.id.image);
        if (attributeSet != null) {
            TypedArray attrs = context.obtainStyledAttributes(attributeSet, R.styleable.TaskView);
            permission = attrs.getInt(R.styleable.TaskView_permission,0);
            taskName = attrs.getString(R.styleable.TaskView_taskName);
            String text = attrs.getString(R.styleable.TaskView_text);
            if (text != null) {
                textView.setText(text);
            }
            Drawable drawable = attrs.getDrawable(R.styleable.TaskView_src);
            image.setImageDrawable(drawable);
            attrs.recycle();
        }

        setOnLongClickListener(this);
    }

    View.OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (OnClickType){
                case toggle:
                    togglePermission();
                    break;
                case execute:
                    if (enabled) {
                        executePermission();
                    }else {
                        Toast.makeText(getContext(), R.string.task_not_enabled, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

        private void executePermission() {
            TaskUI taskUI = TaskUIFactory.createTaskUI(taskName);
            taskUI.init(getContext(),getAction(),getFragmentManager());
            taskUI.setOnSendClicked(onSendClicked);
            taskUI.setCurrentStatus(currentStatus);
            taskUI.show();
        }

        private void togglePermission() {
            if ( device != null && isEnabledInSettings) {
                device.permissions = mR.Permissions.togglePermission(device.permissions,getPermission());
                enabled = mR.Permissions.hasPermission(device.permissions,getPermission());
                updateUI(enabled);
            }else {
                showGloballyDisabledToast();
            }
        }
    };

    public void updateUI(boolean enabled) {
        if (enabled){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            }else {
                setCardBackgroundColor(getResources().getColor(R.color.colorPrimary, null));
            }
        }else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                setCardBackgroundColor(getResources().getColor(R.color.red));
            }else {
                setCardBackgroundColor(getResources().getColor(R.color.red, null));
            }
        }
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    private void enableBasedOnPermission(@NonNull DeviceModel device){
        isEnabledInSettings = mR.Permissions.isTaskEnabled(mR.Permissions.getTaskActionForPermission(getPermission()),getContext());
        if (isEnabledInSettings){
            enabled = mR.Permissions.hasPermission(device.permissions,getPermission());
            updateUI(enabled);
        }else {
            enabled = false;
            updateUI(false);
        }
    }

    private void showGloballyDisabledToast() {
        Toast.makeText(getContext(), R.string.task_is_disable_in_settings, Toast.LENGTH_SHORT).show();
    }

    private void enableBasedOnMessage(Message message){
        int availableTasksPermissions = message.getInt(StatusCheck.KEY_AVAILABLE_TASKS);
        enabled = mR.Permissions.hasPermission(availableTasksPermissions,getPermission());
        updateUI(enabled);
    }

    public String getTask(){
        return taskName;
    }

    private String getAction(){
        return mR.Permissions.getTaskActionForPermission(getPermission());
    }
}