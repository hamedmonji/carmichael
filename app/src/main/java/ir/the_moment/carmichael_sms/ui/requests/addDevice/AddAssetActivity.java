package ir.the_moment.carmichael_sms.ui.requests.addDevice;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import ir.the_moment.carmichael_sms.MainActivity;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.requests.addRequest.AddRequest;
import ir.the_moment.carmichael_sms.utility.Utility;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;

public class AddAssetActivity extends AppCompatActivity implements View.OnClickListener {
    private Message message;
    private DeviceModel requestedAssetDevice;
    private Button addAsset;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_asset);

        message = getIntent().getParcelableExtra(mR.KEY_MESSAGES);

        if (message != null) {
            addAsset = findViewById(R.id.add_asset);
            setDeviceInfo();

            if (message.getBoolean(AddRequest.DATA_KEY_ACCEPTED)) {
                addAsset.setOnClickListener(this);
            }else {
                findViewById(R.id.device_info).setVisibility(View.GONE);
                TextView rejectedMessage = findViewById(R.id.rejected_message);
                String rejectedMessageString = getString(R.string.request_rejected_message);
                rejectedMessageString  = rejectedMessageString.replace("***", " " + message.sender +  " ");
                rejectedMessage.setVisibility(View.VISIBLE);
                rejectedMessage.setText(rejectedMessageString);
                addAsset.setVisibility(View.GONE);
                addAsset.setEnabled(false);
            }
        }
    }

    private void setDeviceInfo() {
        String deviceName = message.getExtra(AddRequest.DATA_KEY_USERNAME);
        TextView name = findViewById(R.id.name);
        EditText number = findViewById(R.id.number);
        number.setClickable(false);
        CountryCodePicker codePicker = findViewById(R.id.country_code_picker);
        codePicker.registerCarrierNumberEditText(number);
        if (deviceName != null) {
            name.setText(deviceName);
            CircleImageView image = findViewById(R.id.image);
            Utility.setBackgroundDrawable(this,image,0);
            TextView imageText = findViewById(R.id.image_text);
            imageText.setVisibility(View.VISIBLE);
            imageText.setText(deviceName.substring(0,1).toUpperCase());
        }

        codePicker.setFullNumber(message.sender);
        codePicker.setCcpClickable(false);
        requestedAssetDevice = getDeviceModel(message);
    }


    @Override
    public void onClick(View v) {
        addDevice(message);
    }

    /**
     * checks to see if this devices sim id was added to the list of authorized numbers of the other device
     * with a value of true,in which case this user is permitted to be the commander for that device.
     * @param message that was received after as a response for the add request.
     */
    private void addDevice(final Message message) {
        if (DeviceInfoDbHelper.getDeviceByNumber(this,message.sender,mR.TYPE_ASSET) != null) {
            Toast.makeText(this, R.string.device_already_added, Toast.LENGTH_SHORT).show();
            return;
        }

        insertDevice();
        startMainActivity();
    }

    private void insertDevice() {
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(this);
        dbHelper.insertDevice(requestedAssetDevice);
    }

    private void startMainActivity() {
        Intent mainActivity = new Intent(AddAssetActivity.this, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainActivity);
    }

    @NonNull
    private DeviceModel getDeviceModel(Message message) {
        DeviceModel model = new DeviceModel();
        model.number = message.sender;
        model.name = message.getExtra(AddRequest.DATA_KEY_USERNAME);
        model.type = mR.TYPE_ASSET;
        model.userId = message.getExtra(AddRequest.DATA_KEY_USER_ID);
        return model;
    }
}