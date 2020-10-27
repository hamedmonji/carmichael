package ir.the_moment.carmichael_sms.tasks.takePictures;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.SetAlarm;

/**
 * Created by vaas on 3/25/2017.
 * take pictures using the front facing camera
 */
public class TakePictures extends UserActivatedTask {

    public static final String TAG = mR.TAG;
    public static final String action = "takePictures.TakePictures";

    public static final String FLAG_TAKE_PIC_PERIODICALLY = "1";
    public static final String FLAG_AUTO_UPLOAD_PICTURES = "2";
    public static final String FLAG_DISABLE = "3";

    public static final String DATA_KEY_TAKE_PIC_INTERVAL = "take_pic_interval";
    public static final String DATA_KEY_UPLOAD_INTERVAL = "interval_upload";

    private static final int UPLOAD_PIC_REQUEST_CODE = 2000;
    public static final String IS_UPLOAD_SET = "upload_set";

    private long snapIntervalInSeconds;
    private long uploadInterval;
    private boolean isPeriodicallyRequested = false;
    private boolean uploadPictures = false;


    @Override
    protected void action() {
        if (!getMessage().hasFlag(FLAG_DISABLE)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Log.i(TAG, "action: about to execute");
            if (!prefs.getBoolean(FLAG_DISABLE,false)) {
                Log.i(TAG, "action: executed");
                parseFlags();
                parseData();

                if (isPeriodicallyRequested) {
                    int TAKE_PIC_PENDING_INDENT_REQUEST_CODE = 1000;
                    SetAlarm.set(context, getMessage(), TAKE_PIC_PENDING_INDENT_REQUEST_CODE, snapIntervalInSeconds);
                }

                if (uploadPictures) {
                    scheduleUpload();
                }

                takeSnapShots();
            }else {
                prefs.edit().putBoolean(FLAG_DISABLE,false).commit();
                prefs.edit().putBoolean(IS_UPLOAD_SET,false).commit();
            }

        }else {
            Log.i(TAG, "action: disable");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean(FLAG_DISABLE,true).commit();
            prefs.edit().putBoolean(IS_UPLOAD_SET,false).commit();
            respond = new Message();
            respond.action = action;
            respond.type = Message.Type.response;
            respond.addFlag(FLAG_DISABLE);
            onActionFinished(true);
        }
    }

    private void takeSnapShots() {
        Intent takePicturesIntent = new Intent(getContext(),CapturePictureActivity.class);
        takePicturesIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(takePicturesIntent);
    }

    @Override
    protected void parseFlags(){
        if (getMessage().flags != null) {
            for (String flag :
                    getMessage().flags) {
                switch (flag) {
                    case FLAG_TAKE_PIC_PERIODICALLY:
                        isPeriodicallyRequested = true;
                        break;
                    case FLAG_AUTO_UPLOAD_PICTURES:
                        uploadPictures = true;
                        break;
                }
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isTakingPicturesEnabled(context);
    }

    @Override
    protected void parseData(){
        if (getMessage().getData() != null){
            if (isPeriodicallyRequested){
                setSnapIntervalInSeconds(getMessage().getInt(DATA_KEY_TAKE_PIC_INTERVAL));
            }
            setUploadInterval(getMessage().getInt(DATA_KEY_UPLOAD_INTERVAL));
        }
    }

    private void setSnapIntervalInSeconds(int i) {
        long oneMin = 60000;
        long oneHour = oneMin * 60;
        switch (i) {
            case 1:
                snapIntervalInSeconds = oneMin;
                break;
            case 2:
                snapIntervalInSeconds = oneMin * 3;
                break;
            case 3:
                snapIntervalInSeconds = oneMin * 5;
                break;
            case 4:
                snapIntervalInSeconds = oneMin * 10;
                break;
            case 5:
                snapIntervalInSeconds = oneMin * 20;
                break;
            case 6:
                snapIntervalInSeconds = oneMin * 30;
                break;
            case 7:
                snapIntervalInSeconds = oneHour;
                break;
            case 8:
                snapIntervalInSeconds = oneHour * 2;
                break;
            case 9:
                snapIntervalInSeconds = oneMin * 3;
                break;
            case 10:
                snapIntervalInSeconds = oneHour * 5;
                break;
        }
    }

    private void setUploadInterval(int i) {
        long oneMin = 60000;
        long oneHour = oneMin * 60;
        switch (i) {
            case 0:
                uploadInterval = oneMin * 5;
                break;
            case 1:
                uploadInterval = oneMin * 10;
                break;
            case 2:
                uploadInterval = oneMin * 20;
                break;
            case 3:
                uploadInterval = oneMin * 30;
                break;
            case 4:
                uploadInterval = oneHour;
                break;
            case 5:
                uploadInterval = oneHour * 2;
                break;
            case 6:
                uploadInterval = oneMin * 3;
                break;
            case 7:
                uploadInterval = oneHour * 5;
                break;
            case 8:
                uploadInterval = oneHour * 10;
                break;
            case 9:
                uploadInterval = oneHour * 24;
                break;
        }
    }

    private void scheduleUpload() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (!prefs.getBoolean(IS_UPLOAD_SET,false)) {
            Log.i(mR.TAG, "scheduleUpload: ");
            Message uploadMessage = new Message();
            uploadMessage.type = Message.Type.command;
            uploadMessage.action = UploadPictures.action;
            uploadMessage.putExtra(DATA_KEY_UPLOAD_INTERVAL, String.valueOf(uploadInterval));
            SetAlarm.set(context,uploadMessage,UPLOAD_PIC_REQUEST_CODE,uploadInterval);
            prefs.edit().putBoolean(IS_UPLOAD_SET,true).commit();
        }else {
            Log.i(mR.TAG, "don't scheduleUpload: ");
        }
    }

    @Override
    public int getPermission() {
        return mR.Permissions.CAPTURE_PICTURES;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_take_pictures);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.take_pictures);
    }
}
