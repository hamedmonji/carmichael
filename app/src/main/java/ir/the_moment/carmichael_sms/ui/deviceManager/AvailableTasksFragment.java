package ir.the_moment.carmichael_sms.ui.deviceManager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.customView.TaskView;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/28/17.
 * shows a list of the tasks that can be used to control the device.
 * which tasks are available is based on the message that was received.
 */

public class AvailableTasksFragment extends Fragment implements TaskUI.OnSendClicked,TaskUI.CurrentStatus {

    private Message message;
    private LinearLayout permissionsRoot;
    public static AvailableTasksFragment newInstance(Message message) {
        Bundle args = new Bundle();
        args.putParcelable(mR.KEY_MESSAGES,message);
        AvailableTasksFragment fragment = new AvailableTasksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.message = getArguments().getParcelable(mR.KEY_MESSAGES);
        return inflater.inflate(R.layout.fragment_available_tasks,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        permissionsRoot = (LinearLayout) view.findViewById(R.id.permissions_root_view);
        setDeviceForPermissions();
    }

    private void setDeviceForPermissions() {
        for (int i = 0; i < permissionsRoot.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) permissionsRoot.getChildAt(i);
            for (int j = 0; j < linearLayout.getChildCount(); j++) {
                TaskView view = (TaskView)linearLayout.getChildAt(j);
                view.setFragmentManager(getFragmentManager());
                view.setOnSendClicked(AvailableTasksFragment.this);
                view.setOnClickType(TaskView.OnClickType.execute);
                view.setCurrentStatus(this);
                view.setMessage(message);
            }
        }
        // these tasks are activated automatically and should'nt be activated remotely
        permissionsRoot.findViewById(R.id.none_invoke).setVisibility(View.GONE);
        permissionsRoot.findViewById(R.id.sim_changed).setVisibility(View.GONE);
    }


    @Override
    public String getNumber() {
        return ((DeviceManagerActivity)getActivity()).getNumber();
    }

    @Override
    public Message getSenderMessage() {
        return message;
    }

    @Override
    public void onSendClicked(Message message) {
        sendMessage(message);
    }

    private void sendMessage(Message message){
        ((DeviceManagerActivity)getActivity()).sendMessage(message);
    }
}