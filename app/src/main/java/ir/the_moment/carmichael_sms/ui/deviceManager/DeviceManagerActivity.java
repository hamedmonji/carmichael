package ir.the_moment.carmichael_sms.ui.deviceManager;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.statusCheck.PhoneInfo;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * a container activity for managing a device.
 * the actual handling will be done by replacing the framelayout with the appropriate fragment.
 */
public class DeviceManagerActivity extends AppCompatActivity {

    private DeviceModel device;
    private String number;
    public static String password;

    private TextView primaryNumber;
    private TextView alternateNumber;
    private CheckBox internet;
    private CheckBox requestRespond;
    private Message message;
    private long id = -1;
    private ImageView batteryLevelImage;
    private TextView batteryLevelText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_device);
        message = getIntent().getParcelableExtra(mR.KEY_MESSAGES);
        if (getIntent() != null){
            id = getIntent().getLongExtra(mR.KEY_ID,-1);
        }

        setDevice();
        bindViews();
        mR.CurrentDevice.number = number;
        mR.CurrentDevice.password = password;

        setBatteryLevel();

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (message != null) {
            mR.CurrentDevice.password =
                    Security.getDecryptedPreferenceEntry(this,device.number);
            updateStatus(message);
            AvailableTasksFragment availableTasksFragment;
            availableTasksFragment = AvailableTasksFragment.newInstance(message);
            fragmentManager.beginTransaction().replace(R.id.device_manager_container,availableTasksFragment).commit();
        }else {
            ManageDeviceFragment deviceFragment = new ManageDeviceFragment();
            fragmentManager.beginTransaction().replace(R.id.device_manager_container, deviceFragment).commit();
        }
    }

    private void bindViews() {
        batteryLevelImage = findViewById(R.id.battery_level_image);
        batteryLevelText = findViewById(R.id.battery_level_text);

        internet = findViewById(R.id.hasInternet);
        internet.setEnabled(false);
        requestRespond = findViewById(R.id.request_respond);

        CircleImageView userPhoto = findViewById(R.id.device_image);
        if (device.pic == null) {
            if (id == -1) id = 1;
            TextView deviceImageText = findViewById(R.id.device_image_text);
            deviceImageText.setVisibility(View.VISIBLE);
            deviceImageText.setText(device.name.substring(0,1).toUpperCase());
            Utility.setBackgroundDrawable(this,userPhoto, (int) id);
        }else {
            Glide.with(this).load(Uri.parse(device.pic)).into(userPhoto);
        }
        primaryNumber = findViewById(R.id.device_number);

        alternateNumber = findViewById(R.id.device_alternate_number);
        number = device.number;
        primaryNumber.setText(device.number);
        if (device.alternateNumber != null) {
            number = device.alternateNumber;
            mR.CurrentDevice.number = device.alternateNumber;

            primaryNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                        primaryNumber.setBackgroundColor(Utility.getCorrectColor(DeviceManagerActivity.this,R.color.colorAccent));
                        alternateNumber.setBackgroundColor(Utility.getCorrectColor(DeviceManagerActivity.this,R.color.white));
                        number = primaryNumber.getText().toString();
                }
            });
            alternateNumber.setVisibility(View.VISIBLE);
            alternateNumber.setText(device.alternateNumber);
            alternateNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alternateNumber.setBackgroundColor(Utility.getCorrectColor(DeviceManagerActivity.this,R.color.colorAccent));
                    primaryNumber.setBackgroundColor(Utility.getCorrectColor(DeviceManagerActivity.this,R.color.white));
                    number = alternateNumber.getText().toString();
                }
            });
            alternateNumber.setBackgroundColor(Utility.getCorrectColor(this,R.color.colorAccent));
        }
    }

    private void setBatteryLevel() {
        if (message != null) {
            float batteryLevel = message.getFloat(PhoneInfo.KEY_BATTERY_LEVEL);
            batteryLevelText.setText(String.valueOf(batteryLevel));
            if (batteryLevel > 0 && batteryLevel <= 25){
                this.batteryLevelImage.setImageResource(R.drawable.battery_1);
            }else if (batteryLevel > 25 && batteryLevel <= 50){
                this.batteryLevelImage.setImageResource(R.drawable.battery_2);
            }else if (batteryLevel > 50 && batteryLevel <= 75){
                this.batteryLevelImage.setImageResource(R.drawable.battery_3);
            }else {
                this.batteryLevelImage.setImageResource(R.drawable.battery_4);
            }
        }
    }

    private void setDevice() {
        if (id != -1) {
            device = DeviceInfoDbHelper.getDeviceModelById(this,id);
        }else if (message.sender != null) {
            device = DeviceInfoDbHelper.getDeviceByNumber(this,message.sender,mR.TYPE_ASSET);
        }
    }

    public void sendMessage(Message message) {
        message.requestRespond = requestRespond.isChecked();
        message.type = Message.Type.command;
        savePassword(this,password);
        setDeviceAsLost();
        MessageSender sender = new MessageSender(DeviceManagerActivity.this, password ,number);
        Toast.makeText(this,R.string.message_being_sent,Toast.LENGTH_SHORT).show();
        sender.setMessage(message)
                .setRegisterCallbacks(true)
                .send();
    }

    public void updateStatus(@NonNull Message message){
        internet.setEnabled(false);
        boolean hasInternet = message.hasFlag(PhoneInfo.FLAG_HAS_INTERNET);
        internet.setChecked(hasInternet);
    }

    public String getNumber(){
        return number;
    }

    private void savePassword(Context context, String password) {
        Security.encryptPreferencesEntry(context,password,device.number);
    }

    public void setDeviceAsLost() {
        if (device.status == DeviceModel.STATUS_LOST)
            return;
        device.status = DeviceModel.STATUS_LOST;
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(this);
        dbHelper.updateDeviceByNumber(this,device,device.number,mR.TYPE_ASSET);
    }

    public void setAsFound() {
        mR.CurrentDevice.password = null;
        if (device.status == DeviceModel.STATUS_LOST){
            device.status = DeviceModel.STATUS_OK;
            DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(this);
            dbHelper.updateDeviceByNumber(this,device,device.number,mR.TYPE_ASSET);
            Security.encryptPreferencesEntry(this, (String) null,device.number);
            Toast.makeText(this, R.string.mark_device_as_found, Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, R.string.device_is_not_lost, Toast.LENGTH_SHORT).show();
        }
    }
}
