package ir.the_moment.carmichael_sms.ui.taskUI;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import ir.the_moment.carmichael_sms.tasks.lock.Lock;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

/**
 * Created by vaas on 8/11/17.
 * ui for lock.
 * allows to get a password to use for locking the device with.
 */

public class LockUI extends TaskUI {
    private CheckBox setPassword;
    private EditText password;
    private EditText passwordConfirmation;
    @Override
    protected View constructView() {
        View view =  LayoutInflater.from(getContext()).inflate(R.layout.task_ui_lock,null,false);
        setPassword = view.findViewById(R.id.set_password);
        password = view.findViewById(R.id.password);
        passwordConfirmation = view.findViewById(R.id.password_confirm);
        setPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                password.setEnabled(isChecked);
                passwordConfirmation.setEnabled(isChecked);
            }
        });

        return view;
    }

    @Override
    protected Message constructMessage(@Nullable View view) {
        Message message = new Message();
        message.type = Message.Type.command;
        message.action = getAction();
        return message;
    }

    @Override
    public void onSendClicked(Message message) {
        if (setPassword.isChecked()) {
            String firstPassword = this.password.getText().toString();
            String passwordConfirmation = this.passwordConfirmation.getText().toString();
            if (firstPassword.length() <=5) {
                Toast.makeText(getContext(), getContext().getString(R.string.at_least_six_characters), Toast.LENGTH_SHORT).show();
                return;
            }
            if (firstPassword.equals(passwordConfirmation)) {
                message.putExtra(Lock.DATA_KEY_PASSWORD,firstPassword);
                super.onSendClicked(message);
            }else {
                Toast.makeText(getContext(), getContext().getString(R.string.password_dont_match), Toast.LENGTH_SHORT).show();
            }

        }else {
            super.onSendClicked(message);
        }
    }
}
