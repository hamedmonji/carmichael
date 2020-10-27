package ir.the_moment.carmichael_sms.tasks.dataManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.util.ArrayList;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.ui.settings.SettingsActivity;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Utility;

import static ir.the_moment.carmichael_sms.tasks.dataManager.DataManager.DATA_FAILED_LIST;

/**
 * Created by vaas on 4/10/2017.
 * task to upload the requested files to drop box.
 * this task will be called indirectly and by the data manger class.
 */

public class UploadData extends UserActivatedTask implements UploadToDropBox.OnUploadFinished, OnActionFinished {

    public static final String action = "dataManager.UploadData";

    /**
     * default name for {@link #files} parent directory name.
     */
    public static final String FILES_FOLDER = "uploaded files";

    /**
     * whether to delete the files that were successfully uploaded after upload.
     */
    public static final String FLAG_DELETE_SUCCESSFUL_AFTER_UPLOAD = "8";

    private boolean deleteSuccessfulFileAfterUpload = false;

    /**
     * whether to delete all of the files after upload regardless of whether it was successful.
     */
    public static final String FLAG_DELETE_ALL_AFTER_UPLOAD = "9";
    private boolean deleteAllFilesAfterUpload = false;

    private ArrayList<File> files = new ArrayList<>();
    private ArrayList<File> folders = new ArrayList<>();

    private String paths;
    private PowerManager.WakeLock wakeLock;
    @Override
    protected void action() {
        parseFlags();
        parseData();
        if (paths != null){
            PowerManager powerManager = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,action);
            wakeLock.acquire();
            separateFilesAndFolders();
            upload();
        }
    }

    @Override
    protected void parseFlags() {
        if (getMessage().flags != null) {
            for (String flag :
                    getMessage().flags) {
                switch (flag){
                    case FLAG_DELETE_SUCCESSFUL_AFTER_UPLOAD:
                        deleteSuccessfulFileAfterUpload = true;
                        break;
                    case FLAG_DELETE_ALL_AFTER_UPLOAD:
                        deleteAllFilesAfterUpload = true;
                        break;
                }
            }
        }
    }

    @Override
    protected void parseData() {
        paths = getMessage().getExtra(DataManager.FLAG_UPLOAD);
    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(context);
    }

    private void separateFilesAndFolders() {
        ArrayList<File> allFiles = FileUtility.extractFiles(paths);
        if (allFiles != null){
            for (File file :
                    allFiles) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        folders.add(file);
                    } else {
                        files.add(file);
                    }
                }
            }
        }
    }

    private void upload() {
        Log.i(mR.TAG, "upload: ");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String token = prefs.getString(SettingsActivity.KEY_PREF_DROP_BOX_ACCESS_TOKEN,null);
        if (token != null){
            Log.i(mR.TAG, "upload: started");
            DbxClientV2 dbxClientV2 = Utility.DropboxClient.getClient(token);
            UploadToDropBox uploadToDropBox = new UploadToDropBox(dbxClientV2);
            uploadToDropBox.setFiles(files)
                    .setFolders(folders)
                    .setOnUploadFinished(this)
                    .upload();
        }
    }

    @Override
    public void uploadFinished(ArrayList<File> succeed, ArrayList<File> failed) {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        respond = new Message();
        respond.requestRespond = true;
        respond.action = DataManager.FLAG_UPLOAD;
        respond.type  = Message.Type.response;
        respond.putExtra(DataManager.DATA_SUCCEED_LIST, FileUtility.getFilesNames(succeed));
        respond.putExtra(DATA_FAILED_LIST,FileUtility.getFilesNames(failed));

        DeleteData deleteData = new DeleteData();
        deleteData.setContext(context);
        deleteData.setOnActionFinished(this);
        Message deleteMessage = new Message();
        deleteMessage.requestRespond = true;
        deleteMessage.action = DeleteData.action;
        if (!deleteAllFilesAfterUpload && !deleteSuccessfulFileAfterUpload) {
            onActionFinished(true);
        }else {
            if (deleteAllFilesAfterUpload) {
                succeed.addAll(failed);
                deleteMessage.putExtra(DataManager.FLAG_DELETE, FileUtility.getFilePaths(succeed));
            } else {
                deleteMessage.putExtra(DataManager.FLAG_DELETE, FileUtility.getFilePaths(succeed));
            }
            deleteData.setMessage(deleteMessage);
            deleteData.run();
        }

    }

    @Override
    public int getPermission() {
        return mR.Permissions.DOWNLOAD_FILE;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_upload_data);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.download_file);
    }

    @Override
    public void onActionFinished(boolean succeed, Message respond) {
        if (wakeLock != null) {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
        String failedList = respond.getExtra(DATA_FAILED_LIST);
        if (!failedList.equals("")) {
            this.respond.putExtra(respond.action, failedList);
        }
        onActionFinished(true);
    }
}
