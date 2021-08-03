package ir.the_moment.carmichael_sms.fileManager;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.mR;

/**
 * Created by vaas on 4/6/2017.
 * utility for common file operations.
 */

public class FileUtility {
    /**
     * gets the paths for a list of files.each path is separated by ";,".
     * @param files to get the paths for
     * @return an string containing the paths for all of the files in the list.
     */
    public static String getFilePaths(List<File> files) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            File f = files.get(i);
            if (!f.getAbsolutePath().equals("")) {
                builder.append(f.getAbsolutePath());
                if (i < files.size() -1) {
                    builder.append(";,");
                }
            }
        }
        return builder.toString();
    }

    public static String getFilesNames(@NonNull List<File> files){
        int lastFilePosition = files.size() - 1;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            builder.append(file.getName());
            if (i < lastFilePosition){
                builder.append(mR.DATA_SEPARATOR);
            }
        }
        return builder.toString();
    }

    /**
     * extracts files from a string with each file separated by ;,
     * @param fileList string containing all of the files
     * @return extracted files from the string return as a list.
     */
    public static ArrayList<File> extractFiles(String fileList){
        ArrayList<File> files = null;
        if (fileList != null && !fileList.equals("")) {
            files = new ArrayList<>();
            for (String path :
                    fileList.split(mR.DATA_SEPARATOR)) {
                File file = new File(path);
                files.add(file);
            }
            return files;
        }
        return files;
    }

    /**
     * separates files and folders form a list of files and puts them in the lists received as parameter;
     * @param allFiles containing both files and directories.
     * @param files list to put the files in.
     * @param folders list to put folders in.
     */
    public static void separateFilesAndFolders(@NonNull ArrayList<File> allFiles, @NonNull ArrayList<File> files, @NonNull ArrayList<File> folders){
        if (allFiles.size() == 0){
            return;
        }

        for (File file :
                allFiles) {
            if (file.isDirectory()) {
                folders.add(file);
            }else {
                files.add(file);
            }
        }
    }
}
