package ir.the_moment.carmichael_sms.tasks.dataManager;

import java.io.File;
import java.util.ArrayList;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;

/**
 * Created by vaas on 5/1/17.
 * deletes the requested list of files.
 */

public class DeleteData extends UserActivatedTask {

    public static final String action = "dataManager.DeleteData";

    private ArrayList<File> files = new ArrayList<>();

    /**
     * files that were deleted.
     */
    private ArrayList<File> succeed = new ArrayList<>();

    /**
     * files that were not deleted.
     */
    private ArrayList<File> failed = new ArrayList<>();

    @Override
    public int getPermission() {
        return mR.Permissions.DELETE_FILE;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_delete_data);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.delete_file);
    }

    @Override
    protected void action() {
        parseData();
        remove();
        sendRespond();
    }

    @Override
    protected void parseData() {
        String filePaths = getMessage().getExtra(DataManager.FLAG_DELETE);
        files = FileUtility.extractFiles(filePaths);
    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(context);
    }

    private void remove() {
        if (files == null) return;

        for (File file :
                files) {
            if (file.isDirectory()){
                removeRecursive(file);
            }else {
                if (file.delete()){
                    succeed.add(file);
                }else {
                    failed.add(file);
                }
            }
        }
    }

    /**
     * deletes the content of a directory
     * @param fileOrDirectory file or folder to delete
     */
    private void removeRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                removeRecursive(child);
            }
        }
        if (fileOrDirectory.delete()){
            succeed.add(fileOrDirectory);
        }else {
            failed.add(fileOrDirectory);
        }
    }

    private void sendRespond() {
        respond = new Message();
        respond.requestRespond = true;
        respond.type = Message.Type.request;
        respond.action = DataManager.FLAG_DELETE;
        respond.putExtra(DataManager.DATA_SUCCEED_LIST, FileUtility.getFilesNames(succeed));
        respond.putExtra(DataManager.DATA_FAILED_LIST,FileUtility.getFilesNames(failed));
        onActionFinished(true);
    }

}
