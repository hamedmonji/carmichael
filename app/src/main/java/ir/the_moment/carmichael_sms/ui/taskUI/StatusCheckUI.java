package ir.the_moment.carmichael_sms.ui.taskUI;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.deviceManager.DeviceManagerActivity;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by mac on 6/12/17.
 */

public class StatusCheckUI extends TaskUI {
    private EditText password;
    @Override
    public View constructView( ) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.get_phone_status_advanced_options, null);
        if (mR.CurrentDevice.password != null && !mR.CurrentDevice.password.equals("")) {
            view.findViewById(R.id.password).setVisibility(View.GONE);
        }
        password = view.findViewById(R.id.password);
        return view;
    }

    @Override
    public Message constructMessage(View view) {
        Message message = new Message();
        message.action = getAction();
        return message;
    }

    @Override
    public void onSendClicked(Message message) {
        if (mR.CurrentDevice.password == null || mR.CurrentDevice.password.equals("")){
            if (password.getText().toString().isEmpty()) {
                Toast.makeText(getContext(), R.string.password_cant_be_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.getText().toString().length() < 5) {
                Toast.makeText(getContext(), R.string.at_least_six_characters, Toast.LENGTH_SHORT).show();
                return;
            }
            DeviceManagerActivity.password = password.getText().toString();
            super.onSendClicked(message);
        }else {
            super.onSendClicked(message);
        }
    }
}