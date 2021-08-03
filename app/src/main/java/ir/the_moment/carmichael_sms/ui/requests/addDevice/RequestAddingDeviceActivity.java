package ir.the_moment.carmichael_sms.ui.requests.addDevice;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.MainActivity;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.MessageSender;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.requests.addRequest.AddRequest;
import ir.the_moment.carmichael_sms.utility.Utility;

public class RequestAddingDeviceActivity extends AppCompatActivity implements View.OnClickListener {

    private CircleImageView image;
    private TextView name;
    private EditText number;

    private EditText requestMessage;

    private EditText password;
    private Button sendRequest;
    private CountryCodePicker codePicker;
    private long contactUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_adding_device);

        contactUri = getIntent().getLongExtra(AddDeviceActivity.CONTACT_URI,-1);
        bindViews();

        if (contactUri != -1){
            Cursor contact = getContact(contactUri);
            if (contact != null) {
                setContactData(contact);
            }

        }
    }

    private void setContactData(Cursor contact) {
        contact.moveToFirst();
        String displayName = contact.getString(contact.getColumnIndexOrThrow(CommonDataKinds.Phone.DISPLAY_NAME));
        name.setText(displayName);

        String phoneNumber = contact.getString(contact.getColumnIndexOrThrow(CommonDataKinds.Phone.NUMBER));
        if (phoneNumber != null ){
            // if number can't be converted to an int then it provably has unsupported characters in it
            try {
                //removing all possible spaces
                phoneNumber = phoneNumber.replaceAll("\\s+","");
                if (phoneNumber.startsWith("+"))
                    Long.parseLong(phoneNumber.substring(1));
                else Long.parseLong(phoneNumber);

                if (phoneNumber.startsWith("+")) {
                    codePicker.setFullNumber(phoneNumber);
                }else if (phoneNumber.length() == 11 && phoneNumber.charAt(0) == '0'){
                    number.setText(phoneNumber.substring(1));
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    String simIso = telephonyManager.getSimCountryIso();
                    codePicker.setCountryForNameCode(simIso);
                }else {
                    number.setText(phoneNumber);
                }
            }catch (NumberFormatException e) {
                Toast.makeText(this, R.string.wrong_number_format, Toast.LENGTH_LONG).show();
            }

            requestMessage.requestFocus();
        }
        String photoUri = contact.getString(contact.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

        if (photoUri != null){
            image.setImageURI(Uri.parse(photoUri));
        }else {
            Utility.setBackgroundDrawable(this,image, (int) contactUri);
            TextView imageText = findViewById(R.id.image_text);
            imageText.setVisibility(View.VISIBLE);
            imageText.setText(displayName.substring(0,1).toUpperCase());
        }
        contact.close();
    }

    private Cursor getContact(long contactUri) {
        String[] projection = new String[]{CommonDataKinds.Phone.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER, CommonDataKinds.Phone.PHOTO_URI};
        String selection = CommonDataKinds.Phone._ID + " =?";
        String[] selectionArgs = new String[]{String.valueOf(contactUri)};

        return getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    private void bindViews() {
        image = findViewById(R.id.image);
        name  = findViewById(R.id.contact_name);
        number = findViewById(R.id.number);
        sendRequest = findViewById(R.id.send_request);
        CardView sendRequestContainer = findViewById(R.id.sendRequestContainer);
        sendRequestContainer.setCardBackgroundColor(Utility.getCorrectColorForPosition(this, (int) contactUri));

        requestMessage = findViewById(R.id.request_message);
        password = findViewById(R.id.password);

        sendRequest.setOnClickListener(this);

        codePicker = findViewById(R.id.country_code_picker);
        codePicker.registerCarrierNumberEditText(number);
    }

    @Override
    public void onClick(View v) {
        String phoneNumber = codePicker.getFullNumberWithPlus();

        if (TextUtils.isEmpty(number.getText().toString())){
            number.setError(getString(R.string.empty_number));
            return;
        }

        String pass = password.getText().toString();
        if (TextUtils.isEmpty(pass)){
            password.setError(getString(R.string.empty_password));
            return;
        }

        final MessageSender sender = new MessageSender(this,pass,phoneNumber);
        final Message message = new Message();
        message.putExtra(AddRequest.DATA_KEY_USER_ID, Utility.getSimId(this));
        message.action = AddRequest.action;
        message.type = Message.Type.request;
        message.putExtra(AddRequest.DATA_KEY_MESSAGE,requestMessage.getText().toString().trim());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        message.putExtra(AddRequest.DATA_KEY_USERNAME,prefs.getString(getString(R.string.key_pref_user_name),""));
        final ProgressDialog progressDialog = new ProgressDialog(RequestAddingDeviceActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.show();
        sender.setMaxMessageLength(Utility.getMaxMessageLengthForMessage(message.getExtra(AddRequest.DATA_KEY_USERNAME),
                requestMessage.getText().toString().trim()));
        sender.addMessage(message)
                .setRegisterCallbacks(true)
                .setCallback(new MessageSender.MessageCallback() {
                    @Override
                    public void onSent() {
                        Toast.makeText(RequestAddingDeviceActivity.this, R.string.message_sent, Toast.LENGTH_SHORT).show();
                        sender.unregisterReceivers();
                        progressDialog.dismiss();
                        Intent mainActivity = new Intent(RequestAddingDeviceActivity.this, MainActivity.class);
                        mainActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainActivity);
                    }

                    @Override
                    public void onDelivered() {

                    }

                    @Override
                    public void onFailed() {
                        Toast.makeText(RequestAddingDeviceActivity.this, R.string.message_failed, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                })
                .send();
    }
}