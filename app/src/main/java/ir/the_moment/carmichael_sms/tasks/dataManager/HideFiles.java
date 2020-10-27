package ir.the_moment.carmichael_sms.tasks.dataManager;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;

/**
 * Created by vaas on 8/12/17.
 * task to hide files and folders.
 */

public class HideFiles extends UserActivatedTask {
    public static final String action = "dataManager.HideFiles";
    private ArrayList<File> filesAndFolders = new ArrayList<>();
    private ArrayList<File> failedList = new ArrayList<>();
    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(context);
    }

    @Override
    protected void action() {
        parseData();
        if (filesAndFolders == null) sendRespond();
        for (File file :
                filesAndFolders) {

            Log.i("testHide", "action: path " + file.getAbsolutePath());
            File newFile = new File(file.getParentFile().getAbsolutePath() + "/." + file.getName());
            Log.i("testHide", "hide: new file name " + newFile.getName() + " path is " + newFile.getAbsolutePath());
            boolean success = file.renameTo(newFile);
            if (!success) {
                failedList.add(file);
            }
            Log.i("testHide", "hide: " + file.getName() + success);
        }
        sendRespond();
    }

    private void sendRespond() {
        respond = new Message();
        respond.requestRespond = true;
        respond.type = Message.Type.response;
        respond.putExtra(DataManager.DATA_FAILED_LIST, FileUtility.getFilesNames(failedList));
        onActionFinished(true);
    }

    @Override
    protected void parseData() {
        filesAndFolders = FileUtility.extractFiles(getMessage().getExtra(DataManager.FLAG_HIDE));
    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public int getPermission() {
        return mR.Permissions.HIDE_FILE;
    }

    @Override
    public String getDescription() {
        return getContext().getString(R.string.description_hide);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return getContext().getString(R.string.hide_file);
    }
}
