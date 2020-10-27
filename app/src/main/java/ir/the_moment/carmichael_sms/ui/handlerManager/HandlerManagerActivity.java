package ir.the_moment.carmichael_sms.ui.handlerManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.hbb20.CountryCodePicker;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.lock.CustomPinActivity;
import de.hdodenhof.circleimageview.CircleImageView;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.ui.customView.TaskView;
import ir.the_moment.carmichael_sms.utility.Utility;

public class HandlerManagerActivity extends AppCompatActivity  implements View.OnClickListener, View.OnTouchListener{
    private int lastPermissions;
    public static final String KEY_ID = "id";
    public static final int RC_CHECK_PASS = 10;
    private long id;
    private CardView lock;
    private FrameLayout transparentFilter;
    private DeviceModel device;
    private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler_manager);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        root = findViewById(R.id.permissions_root_view);
        id = getIntent().getLongExtra(KEY_ID,-1);

        device = DeviceInfoDbHelper.getDeviceModelById(this,id);

        if (device != null){
            bindViews(device);
            lastPermissions = device.permissions;
        }
    }

    public DeviceModel getDevice() {
        return device;
    }

    private void bindViews(DeviceModel model) {
        TextView userName = findViewById(R.id.name);
        EditText number = findViewById(R.id.number);

        CircleImageView image = findViewById(R.id.image);
        userName.setText(model.name);
        number.clearFocus();
        number.setEnabled(false);
        number.setOnTouchListener(null);
        CountryCodePicker codePicker = findViewById(R.id.country_code_picker);
        codePicker.registerCarrierNumberEditText(number);
        codePicker.setCcpClickable(false);
        codePicker.setFullNumber(model.number);
        if (model.pic == null) {
            TextView imageText = findViewById(R.id.image_text);
            imageText.setVisibility(View.VISIBLE);
            imageText.setText(model.name.substring(0,1).toUpperCase());
            Utility.setBackgroundDrawable(this,image, (int) id);
        }else {
            Glide.with(this).load(model.pic).crossFade().into(image);
        }
        setDeviceForPermissions();
        enablePermissionsLock();

    }

    /**
     * passes the current device to the permissions TaskView
     * this will cause the view to be updated base on whether that
     * permission is set for the device or not
     */
    private void setDeviceForPermissions() {

        new MaterialShowcaseView.Builder(this)
                .setTarget(((LinearLayout)root.getChildAt(0)).getChildAt(2))
                .setDismissText(R.string.ok)
                .setContentText(R.string.handler_description_how_to)
                .setDelay(500)
                .singleUse("handler_showcase_id")
                .show();

        for (int i = 0; i < root.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) root.getChildAt(i);
            for (int j = 0; j < linearLayout.getChildCount(); j++) {
                TaskView view = (TaskView)linearLayout.getChildAt(j);
                view.setDevice(device);
            }
        }
    }

    // enables the permissions lock
    private void enablePermissionsLock() {
        transparentFilter = findViewById(R.id.transparent_filter);
        transparentFilter.setOnTouchListener(this);
        transparentFilter.setVisibility(View.VISIBLE);

        lock = findViewById(R.id.lock);
        lock.setVisibility(View.VISIBLE);
        lock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lock:
                showUnlockDialog();
                break;
        }
    }

    private void showUnlockDialog() {
        Intent checkPassIntent = new Intent(this, CustomPinActivity.class);
        checkPassIntent.putExtra(AppLock.EXTRA_TYPE,AppLock.UNLOCK_PIN);
        checkPassIntent.putExtra(CustomPinActivity.KEY_FINISH_ON_BACK_PRESSED,true);
        startActivityForResult(checkPassIntent,RC_CHECK_PASS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RC_CHECK_PASS:
                if (resultCode == RESULT_OK) {
                    unlockPermissions();
                }
                break;
        }
    }

    private void unlockPermissions() {
        transparentFilter.setVisibility(View.GONE);
        lock.setVisibility(View.GONE);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return isPermissionsEnabled();
    }

    private boolean isPermissionsEnabled() {
        return transparentFilter.getVisibility() == View.VISIBLE;
    }

    private void saveChanges() {
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(this);
        String[] availableTasks = mR.Permissions.getAvailableTasksForDevice(device, this);
        if (availableTasks != null) {
            device.rank = availableTasks.length;
        }
        dbHelper.updateDeviceById(device,id);
        Toast.makeText(this, R.string.applied_changes, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (device != null && lastPermissions != device.permissions) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_save_changes)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveChanges();
                            goBack();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            goBack();
                        }
                    })
                    .show();
        }else {
            goBack();
        }
    }

    private void goBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            supportFinishAfterTransition();
        }else {
            super.onBackPressed();
        }
    }
}
