package ir.the_moment.carmichael_sms.ui.manageDevices;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.BaseActivity;
import ir.the_moment.carmichael_sms.ui.DevicesCursorAdapter;
import ir.the_moment.carmichael_sms.ui.deviceManager.DeviceManagerActivity;
import ir.the_moment.carmichael_sms.ui.handlerManager.HandlerManagerActivity;
import ir.the_moment.carmichael_sms.ui.requests.addDevice.AddDeviceActivity;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.MainActivity;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoContract;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.utility.Security;

import static ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper.getDevicesByType;

/**
 * Created by vaas on 4/17/17.
 * fragment for showing the added devices
 */

public class DevicesFragment extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    private DevicesCursorAdapter adapter;
    public static final String KEY_TYPE = "type";
    private int type;
    private ProgressDialog dialog;
    public static DevicesFragment newInstance(int type) {

        Bundle args = new Bundle();
        args.putInt(KEY_TYPE,type);
        DevicesFragment fragment = new DevicesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        type = getArguments().getInt(KEY_TYPE);
        return inflater.inflate(R.layout.fragment_devices,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        if (type == 1){
            setAddDeviceListener(view);
        }

        dialog = new ProgressDialog(getContext());
        ListView devices = view.findViewById(R.id.devicesList);
        adapter = new DevicesCursorAdapter(getContext(), getDevicesByType(getContext(), type),this);
        devices.setAdapter(adapter);
        devices.setOnItemLongClickListener(this);
        devices.setOnItemClickListener(this);
    }

    private void setAddDeviceListener(View view) {
        FloatingActionButton addDevice = view.findViewById(R.id.main_addDevice);
        addDevice.setVisibility(View.VISIBLE);

        addDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addDeviceIntent = new Intent(getContext(), AddDeviceActivity.class);
                startActivity(addDeviceIntent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        String selection = DeviceInfoContract.DeviceInfo.TYPE + "=?";
        String[] selectionArgs = new String[]{Security.encrypt(getContext(), String.valueOf(type))};
        reQuery(selection,selectionArgs);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, final long id) {
        final DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
        final DeviceModel device = DeviceInfoDbHelper.getDeviceModelById(getContext(),id);
        new AlertDialog.Builder(getContext())
                .setTitle(getTitle(device.name))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dbHelper.deleteDeviceById(id);
                        swapCursor();
                        showUndoSnackBar(device);
                    }
                }).setNegativeButton(R.string.cancel,null)
                .show();
        return true;
    }

    private void showUndoSnackBar(final DeviceModel deleteDevice) {
        String title = getTitle(deleteDevice.name);
        Snackbar.make(getView().findViewById(R.id.devices_root),title, BaseTransientBottomBar.LENGTH_SHORT)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restoreDevice(deleteDevice);
                    }
                }).show();
    }

    private void restoreDevice(DeviceModel deleteDevice) {
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
        dbHelper.insertDevice(deleteDevice);
        swapCursor();
    }

    @NonNull
    private String getTitle(String deviceName) {
        String message;
        if (type == 0){
            message = getContext().getString(R.string.retire_handler);
        }else {
            message = getContext().getString(R.string.delete_asset);
        }
        return message + ": " + deviceName;
    }

    public void swapCursor() {
        adapter.swapCursor(DeviceInfoDbHelper.getDevicesByType(getContext(), type));
    }

    public void reQuery(String selection,String[] selectionArgs){
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
        Cursor cursor = dbHelper.getReadableDatabase().query(DeviceInfoContract.DeviceInfo.TABLE_NAME,
                DeviceInfoDbHelper.getProjection(),selection,selectionArgs,null,null,null);
        adapter.swapCursor(cursor);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (type == mR.TYPE_HANDLER) {
            showHandlerManager(id,getContext(),view.findViewById(R.id.user_image_text_container));
        }else {
            showAssetManager(id,getContext(),view.findViewById(R.id.user_image_text_container));
        }
    }

    private void showHandlerManager(long id,Context context,View sharedView) {
        Intent handlerMangerIntent = new Intent(context, HandlerManagerActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),sharedView,"user");
        handlerMangerIntent.putExtra(HandlerManagerActivity.KEY_ID,id);
        startActivity(handlerMangerIntent,optionsCompat.toBundle());
    }

    private void showAssetManager(long id,Context context,View sharedView) {
        Intent deviceManagerIntent = new Intent(context, DeviceManagerActivity.class);
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),sharedView,"user");

        deviceManagerIntent.putExtra(mR.KEY_ID,id);
        startActivity(deviceManagerIntent,optionsCompat.toBundle());
    }

    public void changeDeviceImage(final DeviceModel selectedDevice) {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.promptToSelectImage(new BaseActivity.OnImageSelected() {
            @Override
            public void onSelected(String imagePath) {
                if (!imagePath.equals("")) {
                    selectedDevice.pic = imagePath;
                    DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(getContext());
                    dbHelper.updateDeviceByNumber(getContext(),selectedDevice,selectedDevice.number,type);
                    adapter.notifyDataSetChanged();
                }
            }
        },true,getString(R.string.change_profile_image_title));
    }

}