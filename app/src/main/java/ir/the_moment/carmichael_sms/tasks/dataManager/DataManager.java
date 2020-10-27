package ir.the_moment.carmichael_sms.tasks.dataManager;

import java.io.File;
import java.util.ArrayList;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.fileManager.GetSubFilesAtDirectory;
import ir.the_moment.carmichael_sms.fileManager.OnGetFilesFinished;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/6/2017.
 * handles all data related actions such as delete,secure,hide and download
 */

public class DataManager extends UserActivatedTask implements OnActionFinished {

    private static final String TAG = "server";
    public static final String RESPOND_SEPARATOR = "||";

    /**
     * key for list of the files that were successfully uploaded.
     */
    public static final String DATA_SUCCEED_LIST = "succeed";

    /**
     * key for list of the files that were not uploaded.
     */
    public static final String DATA_FAILED_LIST = "failed";


    public static final String FLAG_GET_DIRECTORY_LIST = "0";
    public static final String FLAG_ENCRYPT = "1";
    public static final String FLAG_UPLOAD = "2";
    public static final String FLAG_DELETE = "3";
    public static final String FLAG_HIDE = "4";

    public static final String DATA_KEY_RESPOND_FOLDER_LIST = "folders";
    public static final String DATA_KEY_RESPOND_FILE_LIST = "files";

    public static final String DATA_KEY_FILE_LIST_FOR_DIRECTORY_REQUEST = "files";
    public static final String action = "dataManager.DataManager";
    public static final String action_succeed_and_failed = "succeed_and_failed";
    private ArrayList<File> files = new ArrayList<>();

    private String filesPaths;

    @Override
    protected void action() {
        parseData();
        executeAction();
    }

    @Override
    protected void parseFlags(){

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(context);
    }

    @Override
    protected void parseData(){
        filesPaths = getMessage().getExtra(DATA_KEY_FILE_LIST_FOR_DIRECTORY_REQUEST);
    }

    private void executeAction() {
        if (getMessage().hasFlag(FLAG_GET_DIRECTORY_LIST)) {
            getFileList();
        } else {
            if (getMessage().hasFlag(FLAG_UPLOAD)) {
                upload();
            }
            if (getMessage().hasFlag(FLAG_HIDE)) {
                hide();
            }
            if (getMessage().hasFlag(FLAG_ENCRYPT)) {
                secure();
            }
            if (getMessage().hasFlag(FLAG_DELETE)) {
                delete();
            }
            sendResponse();
        }
    }

    private void upload() {
        UploadData uploadData = new UploadData();
        uploadData.setMessage(getMessage());
        uploadData.setContext(context)
                .setOnActionFinished(this)
                .run();
    }

    private void secure() {
        SecureData secureData = new SecureData();
        secureData.setMessage(getMessage())
                .setContext(context)
                .setOnActionFinished(this)
                .run();
    }

    private void delete() {
        DeleteData deleteData = new DeleteData();
        deleteData.setMessage(getMessage());
        deleteData.setContext(context)
                .setOnActionFinished(this)
                .run();
    }

    private void hide() {
        HideFiles hideFiles = new HideFiles();
        hideFiles.setMessage(getMessage())
                .setContext(context)
                .setOnActionFinished(this)
                .run();
    }

    private void getFileList() {
        GetSubFilesAtDirectory getSubFilesAtDirectory = new GetSubFilesAtDirectory();
        getSubFilesAtDirectory.setOnGetFilesFinished(new OnGetFilesFinished() {
            @Override
            public void onGetFilesFinished(ArrayList<File> files) {
                sendGetFilesRespond(files);
            }
        });
        if (filesPaths == null){
            getSubFilesAtDirectory.execute();
        }else {
            getSubFilesAtDirectory.execute(new File(filesPaths));
        }
    }

    private void sendGetFilesRespond(ArrayList<File> allFiles){
        ArrayList<File> files =  new ArrayList<>();
        ArrayList<File> folders =  new ArrayList<>();
        FileUtility.separateFilesAndFolders(allFiles,files,folders);
        String requestedFiles = FileUtility.getFilePaths(files);
        String requestedFolders = FileUtility.getFilePaths(folders);
        respond = new Message();
        respond.action = action;
        respond.type = Message.Type.response;
        if (!requestedFiles.isEmpty()) {
            respond.putExtra(DATA_KEY_RESPOND_FILE_LIST, requestedFiles);
        }
        if (!requestedFolders.isEmpty()) {
            respond.putExtra(DATA_KEY_RESPOND_FOLDER_LIST, requestedFolders);
        }
        onActionFinished(true);
    }

    private void sendResponse() {
        if (respond != null){
            onActionFinished(true);
        }
    }

    @Override
    public void onActionFinished(boolean succeed, Message respond) {
        if (this.respond == null){
            this.respond = new Message();
            this.respond.action = action_succeed_and_failed;
            this.respond.type = Message.Type.response;
            this.respond.requestRespond = true;
        }
        String failedList = respond.getExtra(DATA_FAILED_LIST);
        if (!failedList.equals("")) {
            this.respond.putExtra(respond.action, failedList);
        }
    }

    @Override
    public int getPermission() {
        int permission = 0;
        if (getMessage() == null) {
            permission = mR.Permissions.DOWNLOAD_FILE +
                    mR.Permissions.DELETE_FILE +
                    mR.Permissions.ENCRYPT_FILE +
                    mR.Permissions.HIDE_FILE ;
            return permission;
        }

        if (getMessage().hasFlag(FLAG_GET_DIRECTORY_LIST))
            return -1;
        if (getMessage().flags != null) {
            if (getMessage().hasFlag(FLAG_UPLOAD)) {
                permission = permission ^ mR.Permissions.DOWNLOAD_FILE;
            }
            if (getMessage().hasFlag(FLAG_HIDE)) {
                permission = permission ^ mR.Permissions.DOWNLOAD_FILE;
            }
            if (getMessage().hasFlag(FLAG_ENCRYPT)) {
                permission = permission ^ mR.Permissions.DOWNLOAD_FILE;
            }
            if (getMessage().hasFlag(FLAG_DELETE)) {
                permission = permission ^ mR.Permissions.DOWNLOAD_FILE;
            }
        }
        return permission;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_data_manager);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.file_manager);
    }
}