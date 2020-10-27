package ir.the_moment.carmichael_sms.fileManager.filePreset;

import android.app.ProgressDialog;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.fileManager.GetSubFilesAtDirectory;
import ir.the_moment.carmichael_sms.fileManager.OnGetFilesFinished;
import ir.the_moment.carmichael_sms.fileManager.base.FileManagerAdapter;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 8/16/17.
 * adapter for file preset.
 */

class FilePresetAdapter extends FileManagerAdapter<FilePresetAdapter.FilePresetViewHolder> implements OnGetFilesFinished {
    private File currentDirectory = Environment.getExternalStorageDirectory();
    private FilePresetActivity presetActivity;
    private ProgressDialog waitDialog;
    FilePresetAdapter(List<File> currentTaskFileList, FilePresetActivity filePresetActivity) {
        super(currentTaskFileList);
        this.presetActivity = filePresetActivity;
        waitDialog = getWaitDialog();
    }

    @Override
    public FilePresetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file_preset,parent,false);
        return new FilePresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FilePresetViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        presetActivity.setCategoryImageForFile(holder,getFileAtPosition(position));
    }

    public class FilePresetViewHolder extends FileManagerAdapter.ViewHolder {
        ImageView categoryDownload;
        ImageView categoryDelete;
        ImageView categorySecure;
        ImageView categoryHide;
        FilePresetViewHolder(View itemView) {
            super(itemView);
            categoryDownload = (ImageView) itemView.findViewById(R.id.category_download);
            categoryDelete = (ImageView) itemView.findViewById(R.id.category_delete);
            categorySecure = (ImageView) itemView.findViewById(R.id.category_secure);
            categoryHide = (ImageView) itemView.findViewById(R.id.category_hide);
            categoryDownload.setOnClickListener(this);
            categoryDelete.setOnClickListener(this);
            categorySecure.setOnClickListener(this);
            categoryHide.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            File selectedFile = getFileAtPosition(getAdapterPosition());
            switch (view.getId()) {
                case R.id.file:
                case R.id.icon:
                    super.onClick(view);
                    break;
                default:
                    presetActivity.categorySelected((ImageView)view,selectedFile);
            }
        }
    }

    @Override
    public void onGetFilesFinished(ArrayList<File> allFiles) {
        files.clear();
        folders.clear();
        FileUtility.separateFilesAndFolders(allFiles,files,folders);
        notifyDataSetChanged();
        waitDialog.dismiss();
    }

    void getRootDirectoryFiles() {
        getSubFilesFor(currentDirectory);
    }

    @Override
    public void onFileClicked(File clickedFile) {
        getSubFilesFor(clickedFile);
    }

    private void getSubFilesFor(File parent){
        waitDialog.show();
        currentDirectory = parent;
        GetSubFilesAtDirectory getSubFilesAtDirectory = new GetSubFilesAtDirectory();
        getSubFilesAtDirectory.setIncludeEmptyDirectories(true);
        getSubFilesAtDirectory.setOnGetFilesFinished(this);
        getSubFilesAtDirectory.execute(parent);
    }

    @Override
    public boolean isAtRoot() {
        return currentDirectory.equals(Environment.getExternalStorageDirectory());
    }

    @Override
    public void onBackPressed() {
        getSubFilesFor(currentDirectory.getParentFile());
    }


    private ProgressDialog getWaitDialog() {
        ProgressDialog progressDialog = new ProgressDialog(presetActivity);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(presetActivity.getString(R.string.please_wait));
        return progressDialog;
    }
}