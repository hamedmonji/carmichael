package ir.the_moment.carmichael_sms.fileManager;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by vaas on 3/26/2017.
 */

/**
 * asyncTask used to get the list of all child files for a given file
 */
public class GetSubFilesAtDirectory extends AsyncTask<File,Void,Void>{

    private boolean includeEmptyDirectories = false;

    public void setIncludeEmptyDirectories(boolean includeEmptyDirectories) {
        this.includeEmptyDirectories = includeEmptyDirectories;
    }

    // ArrayList containing all of the child files and directories for the given file
    private ArrayList<File> files;

    //callback notifying the listener that the list is ready passing the files list to that listener
    private OnGetFilesFinished onGetFilesFinished = null;

    public void setOnGetFilesFinished(OnGetFilesFinished onGetFilesFinished) {
        this.onGetFilesFinished = onGetFilesFinished;
    }



    @Override
    protected Void doInBackground(File... paths) {
        File root;
        files = new ArrayList<>();
        if (paths != null && paths.length != 0) {
            root = paths[0];
        }else {
            root = Environment.getExternalStorageDirectory();
        }
        for (File file :
                root.listFiles()) {
            if (includeEmptyDirectories || (file.isDirectory() && file.listFiles().length > 0)){
                files.add(file);
            }else if (file.isFile()){
                files.add(file);
            }
        }


        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (onGetFilesFinished != null ) {
            onGetFilesFinished.onGetFilesFinished(files);
        }
    }

}
