package ir.the_moment.carmichael_sms.tasks.filePreset;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.dataManager.DataManager;

/**
 * Created by vaas on 7/12/17.
 */

public class FilePreset extends UserActivatedTask implements OnActionFinished {
    public static final String action = "filePreset.FilePreset";

    public static final String KEY_PRESET_DOWNLOAD = "key_preset_download";
    public static final String KEY_PRESET_DELETE = "key_preset_delete";
    public static final String KEY_PRESET_SECURE = "key_preset_secure";
    public static final String KEY_PRESET_HIDE = "key_preset_hide";


    @Override
    public int getPermission() {
        return mR.Permissions.ACTIVATE_PRESETS;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_file_preset);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.file_manager_preset);
    }

    @Override
    protected void action() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String download = prefs.getString(KEY_PRESET_DOWNLOAD,"");
        String encrypt = prefs.getString(KEY_PRESET_SECURE,"");
        String hide = prefs.getString(KEY_PRESET_HIDE,"");
        String delete = prefs.getString(KEY_PRESET_DELETE,"");

        Message message = new Message();
        message.action = DataManager.action;
        message.requestRespond = true;
        if (!download.equals("")) {
            message.addFlag(DataManager.FLAG_UPLOAD);
            message.putExtra(DataManager.FLAG_UPLOAD,download);
        }
        if (!encrypt.equals("")) {
            message.addFlag(DataManager.FLAG_ENCRYPT);
            message.putExtra(DataManager.FLAG_ENCRYPT,encrypt);
        }
        if (!hide.equals("")) {
            message.addFlag(DataManager.FLAG_HIDE);
            message.putExtra(DataManager.FLAG_HIDE,hide);
        }
        if (!delete.equals("")) {
            message.addFlag(DataManager.FLAG_DELETE);
            message.putExtra(DataManager.FLAG_DELETE,delete);
        }

        DataManager dataManager = new DataManager();
        dataManager.setContext(getContext())
                .setMessage(message)
                .setOnActionFinished(this)
                .run();

    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(getContext());
    }

    @Override
    public void onActionFinished(boolean succeed, Message respond) {
        Log.i(action,"preset finished " + succeed + " " + respond.toString());
    }
}
