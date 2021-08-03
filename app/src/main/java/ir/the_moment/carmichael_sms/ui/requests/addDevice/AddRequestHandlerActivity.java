package ir.the_moment.carmichael_sms.ui.requests.addDevice;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import ir.the_moment.carmichael_sms.MainActivity;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.requests.addRequest.AddRequest;
import ir.the_moment.carmichael_sms.utility.Utility;
import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.responseHandler.AddRequestResponseHandler;
import ir.the_moment.carmichael_sms.ui.customView.TaskView;

public class AddRequestHandlerActivity extends AppCompatActivity implements View.OnClickListener {

    protected CircleImageView image;
    protected TextView name;
    protected EditText number;

    protected TextView requestMessage;

    protected CountryCodePicker codePicker;
    protected Message message;
    private DeviceModel temporaryDevice = new DeviceModel();
    private LinearLayout permissionsRoot;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request_handler);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        message = getIntent().getParcelableExtra(mR.KEY_MESSAGES);
        if (message != null) {

            bindViews();

            setName();

            codePicker.registerCarrierNumberEditText(number);
            codePicker.setFullNumber(message.sender);
            codePicker.setClickable(false);

            setMessage();

        }else {
            Toast.makeText(this, R.string.message_corrupted, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void bindViews() {
        permissionsRoot = findViewById(R.id.permissions_root_view);

        image = findViewById(R.id.image);
        TextView imageText = findViewById(R.id.image_text);
        Utility.setBackgroundDrawable(this,image,-1);
        imageText.setText(message.getExtra(AddRequest.DATA_KEY_USERNAME).substring(0,1).toUpperCase());
        imageText.setVisibility(View.VISIBLE);
        name  = findViewById(R.id.contact_name);
        number = findViewById(R.id.number);
        number.setEnabled(false);
        requestMessage = findViewById(R.id.request_message);

        codePicker = findViewById(R.id.country_code_picker);
        codePicker.setEnabled(false);
        codePicker.setCcpClickable(false);

        Button accept = findViewById(R.id.accept);
        Button decline = findViewById(R.id.decline);

        accept.setOnClickListener(this);
        decline.setOnClickListener(this);

        bindPermissions();
    }


    protected void setName() {
        String displayName = message.getExtra(AddRequest.DATA_KEY_USERNAME);
        if (displayName != null){
            name.setText(displayName);
            TextView imageText = findViewById(R.id.image_text);
            imageText.setText(displayName.substring(0,1).toUpperCase());
            imageText.setVisibility(View.VISIBLE);
        }
    }

    /**
     * passes the current device to the permissions TaskView
     * this will cause the view to be updated base on whether that
     * permission is set for the device or not
     */
    protected void bindPermissions() {
        for (int i = 0; i < permissionsRoot.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) permissionsRoot.getChildAt(i);
            for (int j = 0; j < linearLayout.getChildCount(); j++) {
                TaskView view = (TaskView)linearLayout.getChildAt(j);
                view.setOnClickType(TaskView.OnClickType.toggle);
                view.setDevice(temporaryDevice);
            }
        }
    }

    protected void setMessage() {
        String requestMessage = message.getExtra(AddRequest.DATA_KEY_MESSAGE);
        if (requestMessage != null && !requestMessage.equals("")){
            this.requestMessage.setText(requestMessage);
        }else {
            this.requestMessage.setText(getString(R.string.get_coffe_some_time));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.accept:
                accept();
                break;
            case R.id.decline:
                decline();
                break;
        }
    }

    private void accept() {

        if (DeviceInfoDbHelper.getDeviceByNumber(this,message.sender,mR.TYPE_HANDLER) != null) {
            Toast.makeText(this, R.string.device_already_added, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.show();
        final String name = message.getExtra(AddRequest.DATA_KEY_USERNAME);
        DeviceModel device = new DeviceModel();
        device.userId = message.getExtra(AddRequest.DATA_KEY_USER_ID);
        device.name = name;
        device.number = message.sender;
        device.type = mR.TYPE_HANDLER;
        device.permissions = temporaryDevice.permissions;
        device.rank = mR.Permissions.getAvailableTasksForDevice(device,this).length;
        sendRespond(getAcceptMessage(),device,progressDialog);
    }

    private Message getAcceptMessage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Message respond = new Message();
        respond.action = AddRequestResponseHandler.class.getSimpleName();
        respond.putExtra(AddRequest.DATA_KEY_USERNAME,prefs.getString(getString(R.string.key_pref_user_name),""));
        respond.putExtra(AddRequest.DATA_KEY_ACCEPTED, String.valueOf(true));
        respond.putExtra(AddRequest.DATA_KEY_USER_ID, Utility.getSimId(this));
        return respond;
    }

    private void decline() {
        if (DeviceInfoDbHelper.getDeviceByNumber(this,message.sender,mR.TYPE_HANDLER) != null) {
            Toast.makeText(this, R.string.device_already_added, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(R.string.please_wait);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Message respond = new Message();
        respond.action = AddRequestResponseHandler.class.getSimpleName();
        respond.putExtra(AddRequest.DATA_KEY_USERNAME,prefs.getString(getString(R.string.key_pref_user_name),""));
        respond.putExtra(AddRequest.DATA_KEY_ACCEPTED, String.valueOf(false));
        sendRespond(respond, null, progressDialog);
    }

    private void sendRespond(final Message respond, final DeviceModel deviceModel, final ProgressDialog progressDialog) {
        final DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(AddRequestHandlerActivity.this);
        final long deviceId = dbHelper.insertDevice(deviceModel);
        MessageSender sender = new MessageSender(this,mR.RESPONSE_ENCRYPTION_KEY,message.sender);
        respond.type = Message.Type.response;
        sender.setMaxMessageLength(Utility.getMaxMessageLengthForMessage(respond.getExtra(AddRequest.DATA_KEY_USERNAME)));
        sender.addMessage(respond)
                .setRegisterCallbacks(true)
                .setCallback(new MessageSender.MessageCallback() {
                    @Override
                    public void onSent() {
                        if (respond.getBoolean(AddRequest.DATA_KEY_ACCEPTED)) {
                            Toast.makeText(AddRequestHandlerActivity.this, R.string.handler_added, Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(AddRequestHandlerActivity.this, R.string.message_sent, Toast.LENGTH_SHORT).show();
                        }
                        progressDialog.dismiss();
                        startMainActivity();
                    }

                    private void startMainActivity() {
                        Intent mainActivity = new Intent(AddRequestHandlerActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainActivity);
                    }

                    @Override
                    public void onDelivered() {
                    }

                    @Override
                    public void onFailed() {
                        progressDialog.dismiss();
                        dbHelper.deleteDeviceById(deviceId);
                        Toast.makeText(AddRequestHandlerActivity.this, R.string.message_failed, Toast.LENGTH_SHORT).show();
                    }
                })
                .send();
    }
}
