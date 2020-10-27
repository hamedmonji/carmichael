package ir.the_moment.carmichael_sms.tasks.takePictures;

import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.dataManager.UploadData;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.tasks.dataManager.DataManager;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 4/11/2017.
 */

public class UploadPictures extends Task {
    public static final String action = "takePictures.UploadPictures";

    @Override
    protected void action() {
        setUploadTask();
    }


    private void setUploadTask() {
        Log.i(mR.TAG, "setUploadTask: ");
        File picDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Carmichael/");
        if (picDirectory.exists()) {
            List<File> pictures = new ArrayList<>();
            for (File file :
                    picDirectory.listFiles()) {
                if (file.getName().endsWith(".jpg")) {
                    pictures.add(file);
                }
            }

            if (pictures.size() > 0) {
                Log.i(mR.TAG, "setUploadTask: " + pictures.size());
                String picturesPath = FileUtility.getFilePaths(pictures);
                Message upload = new Message();
                upload.type = Message.Type.command;
                upload.action = DataManager.action;
                upload.addFlag(DataManager.FLAG_UPLOAD);
                upload.putExtra(DataManager.FLAG_UPLOAD, picturesPath);
                upload.addFlag(UploadData.FLAG_DELETE_SUCCESSFUL_AFTER_UPLOAD);
                DataManager dataManager = new DataManager();
                dataManager.setMessage(upload);
                dataManager.setContext(context);
                dataManager.run();
            }
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            prefs.edit().putBoolean(TakePictures.IS_UPLOAD_SET, false).commit();
        }
    }

    @Override
    protected void parseData() {

    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isTakingPicturesEnabled(getContext());
    }

    @Override
    public int getPermission() {
        return mR.Permissions.DOWNLOAD_FILE;
    }


}
