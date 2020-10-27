package ir.the_moment.carmichael_sms.ui.deviceManager;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.customView.TaskView;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 5/2/17.
 * fragment controls the device permissions and allows to send commands to the device or get the
 * phone status and also handles control zones.
 */

public class ManageDeviceFragment extends Fragment implements TaskUI.OnSendClicked, TaskUI.CurrentStatus {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manage_device,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpStatusCheck(view);

        setDeviceFoundListener(view);

        setShowAvailableTaskListener(view);

    }

    private void setUpStatusCheck(View view) {
        TaskView statusCheck = view.findViewById(R.id.status_check);
        statusCheck.setOnClickType(TaskView.OnClickType.execute);
        statusCheck.setCurrentStatus(this);
        statusCheck.setOnSendClicked(this);
        statusCheck.setFragmentManager(getFragmentManager());
    }

    private void setDeviceFoundListener(View view) {
        view.findViewById(R.id.set_as_found).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DeviceManagerActivity)getActivity()).setAsFound();
            }
        });
    }

    private void setShowAvailableTaskListener(View view) {
        mR.CurrentDevice.password = null;
        view.findViewById(R.id.show_available_tasks).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View passwordContainer = LayoutInflater.from(getContext()).inflate(R.layout.pasword_edittext,null,false);
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.please_enter_password)
                        .setView(passwordContainer)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText password = passwordContainer.findViewById(R.id.password);
                                String pass = password.getText().toString();
                                if (pass.isEmpty()) {
                                    Toast.makeText(getContext(), R.string.password_cant_be_empty, Toast.LENGTH_SHORT).show();
                                }else {
                                    mR.CurrentDevice.password = password.getText().toString();
                                    DeviceManagerActivity.password = mR.CurrentDevice.password;
                                    AvailableTasksFragment statusFragment = AvailableTasksFragment.newInstance(null);
                                    getFragmentManager().beginTransaction().replace(R.id.device_manager_container,statusFragment).commit();
                                }

                            }
                        }).show();
            }
        });
    }

    private void sendMessage(Message message){
        ((DeviceManagerActivity)getActivity()).sendMessage(message);
    }

    @Override
    public Message getSenderMessage() {
        return null;
    }

    @Override
    public String getNumber() {
        return ((DeviceManagerActivity)getActivity()).getNumber();
    }

    @Override
    public void onSendClicked(Message message) {
        sendMessage(message);
    }
}