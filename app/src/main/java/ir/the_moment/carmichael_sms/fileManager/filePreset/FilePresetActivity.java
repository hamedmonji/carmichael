package ir.the_moment.carmichael_sms.fileManager.filePreset;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.dropbox.core.android.Auth;

import java.io.File;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.ui.settings.SettingsActivity;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.fileManager.base.FileManagerActivity;
import ir.the_moment.carmichael_sms.tasks.filePreset.FilePreset;
import ir.the_moment.carmichael_sms.utility.Utility;

public class FilePresetActivity extends FileManagerActivity{
    private static final int RC_PERMISSION_FILE_MANAGER = 1003;
    private SharedPreferences prefs;
    private boolean isSaved = false;

    @Override
    public void initAdapter() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            if (!Utility.isWritePermissionsGranted(this)) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE}, RC_PERMISSION_FILE_MANAGER);
            }else init();
        }else init();
    }

    private void init() {
        setCurrentPresetData();
        adapter = new FilePresetAdapter(downloadList,this);
        fileList.setAdapter(adapter);
        ((FilePresetAdapter)adapter).getRootDirectoryFiles();
    }

    @Override
    public int getContentView() {
        return R.layout.activity_file_manager;
    }

    public void setCategoryImageForFile(FilePresetAdapter.FilePresetViewHolder holder, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (downloadList.contains(file)) {
                holder.categoryDownload.setImageAlpha(255);
            }else {
                holder.categoryDownload.setImageAlpha(80);
            }
            if (deleteList.contains(file)) {
                holder.categoryDelete.setImageAlpha(255);
            }else {
                holder.categoryDelete.setImageAlpha(80);
            }
            if (secureList.contains(file)) {
                holder.categorySecure.setImageAlpha(255);
            }else {
                holder.categorySecure.setImageAlpha(80);
            }
            if (hideList.contains(file)) {
                holder.categoryHide.setImageAlpha(255);
            }else {
                holder.categoryHide.setImageAlpha(80);
            }
        }else {
            if (downloadList.contains(file)) {
                holder.categoryDownload.setAlpha(255);
            }else {
                holder.categoryDownload.setAlpha(80);
            }
            if (deleteList.contains(file)) {
                holder.categoryDelete.setAlpha(255);
            }else {
                holder.categoryDelete.setAlpha(80);
            }
            if (secureList.contains(file)) {
                holder.categorySecure.setAlpha(255);
            }else {
                holder.categorySecure.setAlpha(80);
            }
            if (hideList.contains(file)) {
                holder.categoryHide.setAlpha(255);
            }else {
                holder.categoryHide.setAlpha(80);
            }
        }

    }

    public void categorySelected(ImageView category, File file) {
        isSaved = false;
        switch (category.getId()) {
            case R.id.category_download:
                if (!isDropboxSetUp()) {
                    startDropBoxAuthentication();
                    return;
                }
                if (downloadList.contains(file)) {
                    downloadList.remove(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(80);
                    }else {
                        category.setAlpha(80);
                    }
                }else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(255);
                    }else category.setAlpha(255);
                    downloadList.add(file);
                }
                break;
            case R.id.category_delete:
                if (deleteList.contains(file)) {
                    deleteList.remove(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(80);
                    }else category.setAlpha(80);
                }else {
                    deleteList.add(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(255);
                    }else category.setAlpha(255);
                }
                break;
            case R.id.category_secure:
                if (secureList.contains(file)) {
                    secureList.remove(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(80);
                    }else category.setAlpha(80);
                }else {
                    secureList.add(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(255);
                    }else category.setAlpha(255);
                }
                break;
            case R.id.category_hide:
                if (hideList.contains(file)) {
                    hideList.remove(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(80);
                    }else category.setAlpha(80);
                }else {
                    hideList.add(file);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        category.setImageAlpha(255);
                    }else category.setAlpha(255);
                }
                break;
        }
    }

    public void setCurrentPresetData() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String download = prefs.getString(FilePreset.KEY_PRESET_DOWNLOAD,"");
        String secure = prefs.getString(FilePreset.KEY_PRESET_SECURE,"");
        String delete = prefs.getString(FilePreset.KEY_PRESET_DELETE,"");
        String hide = prefs.getString(FilePreset.KEY_PRESET_HIDE,"");

        if (!download.equals(""))
            downloadList.addAll(FileUtility.extractFiles(download));
        if (!secure.equals(""))
            secureList.addAll(FileUtility.extractFiles(secure));
        if (!delete.equals(""))
            deleteList.addAll(FileUtility.extractFiles(delete));
        if (!hide.equals(""))
            hideList.addAll(FileUtility.extractFiles(hide));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_preset,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_all){
            clearAll();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void clearAll() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.clear_all)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveChanges("","","","");
                        downloadList.clear();
                        deleteList.clear();
                        secureList.clear();
                        hideList.clear();
                        adapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(R.string.no,null)
                .show();
    }

    @Override
    public void onFinished() {
        String download = FileUtility.getFilePaths(downloadList);
        String delete = FileUtility.getFilePaths(deleteList);
        String secure = FileUtility.getFilePaths(secureList);
        String hide = FileUtility.getFilePaths(hideList);
        saveChanges(download,delete,secure,hide);
        Toast.makeText(this, R.string.applied_changes, Toast.LENGTH_SHORT).show();
        isSaved = true;
    }

    private void saveChanges(String download,String delete,String secure,String hide) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString(FilePreset.KEY_PRESET_DOWNLOAD,download).commit();
        prefs.edit().putString(FilePreset.KEY_PRESET_SECURE,secure).commit();
        prefs.edit().putString(FilePreset.KEY_PRESET_DELETE,delete).commit();
        prefs.edit().putString(FilePreset.KEY_PRESET_HIDE,hide).commit();
    }

    @Override
    public void onBackPressed() {
        if (adapter == null || isSaved) {
            finish();
            return;
        }
        if (adapter.isAtRoot()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_save_changes)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onFinished();
                            FilePresetActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FilePresetActivity.super.onBackPressed();
                        }
                    })
                    .show();
        }else {
            adapter.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_PERMISSION_FILE_MANAGER:
                if (!permissionsGranted(grantResults)){
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    private boolean permissionsGranted(int[] grantResults){
        if (grantResults.length == 0)
            return false;

        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    private boolean isDropboxSetUp() {
        return prefs.getString(SettingsActivity.KEY_PREF_DROP_BOX_ACCESS_TOKEN, null) != null;
    }

    private void startDropBoxAuthentication() {
        if (isDropboxInstalled()) {
            Auth.startOAuth2Authentication(getApplicationContext(), getString(R.string.APP_KEY));
        }else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.please_install_dropbox)
                    .setPositiveButton(R.string.install, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent installDropboxIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.dropbox.android"));
                            if (installDropboxIntent.resolveActivity(getPackageManager()) != null) {
                                startActivity(installDropboxIntent);
                            }else {
                                Toast.makeText(FilePresetActivity.this, R.string.install_dropbox, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
    }

    private boolean isDropboxInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.dropbox.android", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {}

        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveDropBoxAccessToken();
    }

    private void saveDropBoxAccessToken() {
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putString(SettingsActivity.KEY_PREF_DROP_BOX_ACCESS_TOKEN, accessToken).apply();
        }
    }
}