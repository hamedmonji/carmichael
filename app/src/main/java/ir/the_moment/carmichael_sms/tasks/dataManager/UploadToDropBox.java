package ir.the_moment.carmichael_sms.tasks.dataManager;

import androidx.annotation.NonNull;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by vaas on 4/30/17.
 * uploads files to drop box nameForFilesParentFolder
 */

public class UploadToDropBox {
    public static final String TAG = "upload";
    private DbxClientV2 dbxClient;

    /**
     * files to upload.
     */
    private ArrayList<File> files = new ArrayList<>();

    /**
     * the nameForFilesParentFolder name to be used when uploading the files in the {@link #files}.
     * @param folder name of the nameForFilesParentFolder
     */
    public UploadToDropBox setFolderName(String folder) {
        this.nameForFilesParentFolder = folder;
        return this;
    }

    public UploadToDropBox setFiles(ArrayList<File> files) {
        this.files = files;
        return this;
    }

    public UploadToDropBox setFolders(ArrayList<File> folders) {
        this.folders = folders;
        return this;
    }

    /**
     * folders to upload.
     */
    private ArrayList<File> folders = new ArrayList<>();

    /**
     * the nameForFilesParentFolder name to be used when uploading the files in the {@link #files}.
     */
    private String nameForFilesParentFolder;

    /**
     * files that were uploaded.
     */
    private ArrayList<File> succeed = new ArrayList<>();

    /**
     * files that were not uploaded.
     */
    private ArrayList<File> failed = new ArrayList<>();

    /**
     * callback for when upload finished.
     */
    private OnUploadFinished onUploadFinished = null;

    public interface OnUploadFinished {
        /**
         * @param succeed files that were successfully uploaded.
         * @param failed files that were not uploaded.
         */
        void uploadFinished(ArrayList<File> succeed, ArrayList<File> failed);
    }

    public UploadToDropBox setOnUploadFinished(OnUploadFinished onUploadFinished) {
        this.onUploadFinished = onUploadFinished;
        return this;
    }

    public UploadToDropBox(@NonNull DbxClientV2 dbxClient) {
        this.dbxClient = dbxClient;
    }


    public void upload() {
        // Upload to Dropbox
        uploadFiles();
        uploadFolders();

        if (onUploadFinished != null){
            onUploadFinished.uploadFinished(succeed,failed);
        }
    }

    private void uploadFiles() {
        setDefaultFolderNameIfNotSet();
        for (File file :
                files) {
            try {
                uploadFile(file,file.getParentFile().getName());
                succeed.add(file);
            } catch (DbxException | IOException e) {
                e.printStackTrace();
                Log.i(TAG,"failed",e);
                failed.add(file);
            }
        }
    }

    private void uploadFolders() {
        for (File directory :
                folders) {
            if (directory.isDirectory()){
                for (File file :
                        directory.listFiles()) {
                    try {
                        uploadFile(file,file.getParentFile().getName());
                        succeed.add(file);
                    } catch (DbxException | IOException e) {
                        e.printStackTrace();
                        Log.i(TAG,"failed",e);
                        failed.add(file);
                    }
                }
            }
        }
    }

    private void uploadFile(final File file, final String folderName) throws DbxException, IOException {
        final InputStream inputStream = new FileInputStream(file);
        Log.i(TAG, "run:  upload started");
        dbxClient.files().uploadBuilder("/" + folderName + "/" + file.getName() ) //Path in the user's Dropbox to save the file.
                .withMode(WriteMode.OVERWRITE)
                .uploadAndFinish(inputStream);
        Log.i(TAG, "run:  upload finished");
    }

    private void setDefaultFolderNameIfNotSet() {
        if (nameForFilesParentFolder == null || nameForFilesParentFolder.equals("")){
            nameForFilesParentFolder = UploadData.FILES_FOLDER;
        }
    }
}
