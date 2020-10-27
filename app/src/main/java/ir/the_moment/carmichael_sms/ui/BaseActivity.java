package ir.the_moment.carmichael_sms.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * Created by vaas on 7/10/17.
 * base class that has the required code to select an image from either camera or galley
 * the path for the selected image will be passed to the caller using the {@link #onImageSelected} callback
 * if provided.
 */

public class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_SELECT_FILE = 110;
    private static final int REQUEST_CAMERA = 111;
    private static final int RC_REQUEST_READ_PERMISSION = 2000;
    private static final int TAKE_PHOTO = 0;
    private static final int CHOOSE_FROM_GALLEY = 1;
    private static final int RC_CROP_IMAGE = 112;

    public ProgressDialog progressDialog ;
    private OnImageSelected onImageSelected = null;

    private File capturedFile = null;
    private boolean crop = false;

    public interface OnImageSelected{
        void onSelected(String imagePath);
    }

    public void setOnImageSelected(OnImageSelected onImageSelected) {
        this.onImageSelected = onImageSelected;
    }


    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getNewWaitDialog();
    }

    protected void showAlertToSetBackgroundToItsDefault(final ImageView backgroundImageView) {
        backgroundImageView.setImageURI(null);
        backgroundImageView.setImageDrawable(null);
        backgroundImageView.setBackgroundColor(Utility.getCorrectColor(BaseActivity.this,R.color.colorPrimary));
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(BaseActivity.this);
        prefs.edit().putString(getString(R.string.key_pref_user_background_image),null).commit();
    }

    private boolean permissionGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void promptToSelectImage(OnImageSelected onImageSelected, boolean crop,String title) {
        this.crop = crop;
        this.onImageSelected = onImageSelected;
        final CharSequence[] items = {getString(R.string.take_photo),
                getString(R.string.choose_from_gallery),
                getString(R.string.cancel)};

        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(title)
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!permissionGranted()) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                                    ,RC_REQUEST_READ_PERMISSION);
                        }else {
                            switch (which) {
                                case TAKE_PHOTO:
                                    cameraIntent();
                                    break;
                                case CHOOSE_FROM_GALLEY:
                                    galleryIntent();
                                    break;
                                default:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    }
                }).show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_SELECT_FILE);
    }

    private void cameraIntent() {
        try {
            capturedFile = File.createTempFile(String.valueOf(System.currentTimeMillis())+"image",".jpg",getFilesDir());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (capturedFile != null){
            Uri photoUri = FileProvider.getUriForFile(this,"ir.the_moment.carmichael_sms.fileprovider",capturedFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            grantUriPermissionsTo(intent,photoUri);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
            startActivityForResult(intent, REQUEST_CAMERA);
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            Uri imageUri = null;
            try {
                imageUri = data.getData();
                String path = Utility.getPath(this,imageUri);
                if (crop) {
                    if (path != null) {
                        File file = new File(path);
                        Uri uri = FileProvider.getUriForFile(this,"ir.the_moment.carmichael_sms.fileprovider",file);
                        cropImage(uri);
                    }
                }else {
                    sendImagePathToCallback(path);
                }
            } catch (ActivityNotFoundException e) {
                String path = Utility.getPath(this,imageUri);
                sendImagePathToCallback(path);
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.set_drawer_image_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void grantUriPermissionsTo(Intent intent,Uri uri) {
        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }

    private void cropImage(Uri imageUri) {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        grantUriPermissionsTo(cropIntent,imageUri);
        cropIntent.setDataAndType(imageUri,"image/*");
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.putExtra("crop","true");
        cropIntent.putExtra("aspectX",1);
        cropIntent.putExtra("aspectY",1);
        cropIntent.putExtra("outputX",256);
        cropIntent.putExtra("outputY",256);
        cropIntent.putExtra("return-data",true);
        try {
            startActivityForResult(cropIntent,RC_CROP_IMAGE);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getImageUriFromBitmap(Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG,100,bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(),inImage,"CarMichealProfile.jpg",null);
        return Uri.parse(path);
    }

    private void addCapturedImageToGallery(File capturedFile) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);;
        Uri contentUri = Uri.fromFile(capturedFile);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void onCaptureImageResult() {
        if (crop && capturedFile != null) {
            Uri uri = FileProvider.getUriForFile(this,"ir.the_moment.carmichael_sms.fileprovider",capturedFile);
            cropImage(uri);
            addCapturedImageToGallery(capturedFile);
        }else if (capturedFile != null){
            sendImagePathToCallback(capturedFile.getAbsolutePath());
            addCapturedImageToGallery(capturedFile);
        }
    }

    private void sendImagePathToCallback(String path) {
        if (onImageSelected != null){
            onImageSelected.onSelected(path);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Utility.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    promptToSelectImage(onImageSelected, false,getString(R.string.change_profile_image_title));
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_CANCELED) {
            switch (requestCode) {
                case REQUEST_SELECT_FILE:
                    if (resultCode == Activity.RESULT_OK)
                        onSelectFromGalleryResult(data);
                    break;
                case REQUEST_CAMERA:
                    if (resultCode == Activity.RESULT_OK)
                        onCaptureImageResult();
                    break;
                case RC_CROP_IMAGE:
                    if (data != null) {
                        Bitmap bitmap = data.getExtras().getParcelable("data");
                        if (bitmap != null) {
                            Uri imageUri = getImageUriFromBitmap(bitmap);
                            sendImagePathToCallback(imageUri.toString());
                        }
                    }
            }
        }
    }

    private void getNewWaitDialog(){
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
    }

    protected void toastNoInternet(){
        Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
    }

    private void changeDialogToSingOut() {
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.signing_out) + "," +getString(R.string.please_wait));
    }
}