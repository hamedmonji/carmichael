package ir.the_moment.carmichael_sms.utility;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 4/18/17.
 */

public class Utility {
    private static final int totalColors = 14;
    public static String getSimOperatorName(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getSimOperatorName();
    }

    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    public static void takeSnapShots(@NonNull Context context) {
        SurfaceView surface = new SurfaceView(context);
        int frontFacingCameraId = getFrontFacingId(context);

        if (frontFacingCameraId != -1) {
            Camera camera = Camera.open();

            try {
                camera.setPreviewDisplay(surface.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
            camera.takePicture(null, null, jpegCallback);
        }
    }

    private static int getFrontFacingId(@NonNull Context context) {
        PackageManager packageManager = context.getPackageManager();
        boolean hasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        if (!hasFrontCamera){
            return -1;
        }else {
            int cameraId = 0;
            int cameraCounts = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int cameraInfoId = 0; cameraInfoId < cameraCounts; cameraInfoId++) {
                Camera.getCameraInfo(cameraInfoId, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = cameraInfoId;
                }

            }

            return cameraId;
        }
    }


    /** picture call back */
    static Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera)
        {
            FileOutputStream outStream = null;
            try {
                File dir_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                outStream = new FileOutputStream(dir_path+ File.separator+ System.currentTimeMillis()+"snapshot.jpg");
                outStream.write(data);
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                camera.stopPreview();
                camera.release();
                camera = null;
            }
        }
    };

    public static boolean isWritePermissionsGranted(Context context) {
        return !isPostMarshMellow() || isPostMarshMellow() && (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean isCameraPermissionGranted(Context context) {
        return !isPostMarshMellow() || isPostMarshMellow() &&
                context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return !isPostMarshMellow() || isPostMarshMellow() &&
                context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean deviceHasSimCard(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getSimId(context) != null && telephonyManager.isSmsCapable();
        }else {
            return getSimId(context) != null;
        }
    }


    public static int getCorrectColor(Context context,@ColorRes int id) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            return context.getResources().getColor(id,null);
        }else {
            return context.getResources().getColor(id);
        }
    }

    public static boolean isPowerSavingMode(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean(context.getString(R.string.pref_key_is_power_saving_mode),true);
    }

    public static boolean isDeviceLost(Context context) {
        return Security.getDecryptedBooleanPreferenceEntry(context,context.getString(R.string.key_pref_is_device_lost));
    }

    public static int getMaxMessageLengthForMessage(String... messages) {
        if (messages == null) return 160;
        for (String message :
                messages) {
            for (char c :
                    message.toCharArray()) {
                if (!(c >= 'a' && c <= 'z') && !(c >= 'A' && c <= 'Z') && !(c >= '0' && c <= '9') && c != '.') {
                    return 70;
                }
            }
        }
        return 160;
    }

    public static class DropboxClient {

        public static DbxClientV2 getClient(String ACCESS_TOKEN) {
            // Create Dropbox client
            DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");
            return new DbxClientV2(config, ACCESS_TOKEN);
        }
    }

    public static String getUserID(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.key_pref_user_id),null);
    }

    public static String getSimId(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.getSimCountryIso();
        return telephonyManager.getSubscriberId();
    }

    public static boolean isPostMarshMellow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static String getFormattedNumber(String number) {
        // already formatted
        if (number.length() >= 13 && number.contains(" ") ) return number;
        if (number.length() > 10) {
            String formattedNumber = "";
            if (number.startsWith("+")) { //+989138798609
                String numberWithOutPreFix = number.substring(number.length() - 10);
                String prefix = number.substring(0,number.indexOf(numberWithOutPreFix));

                formattedNumber = numberWithOutPreFix.substring(0, 3);
                formattedNumber += " ";
                formattedNumber += numberWithOutPreFix.substring(3, 6);
                formattedNumber += " ";
                formattedNumber += numberWithOutPreFix.substring(6);

                formattedNumber = prefix + " " + formattedNumber;

            } else { //09138798609
                formattedNumber = number.substring(0, 4);
                formattedNumber += " ";
                formattedNumber += number.substring(4, 7);
                formattedNumber += " ";
                formattedNumber += number.substring(7);
            }
            return formattedNumber;
        }else return number;
    }

    public static boolean hasInternetConnection(@NonNull Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        cm = null;
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public static String getPath(final Context context, final Uri uri) {

        // DocumentProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    private static int getImageResourceIdForPosition(int pos){
        pos -= (int)Math.floor(pos/totalColors) * totalColors;
        switch (pos){
            case 0:
                return R.drawable.light_blue;
            case 1:
                return R.drawable.light_green;
            case 2:
                return R.drawable.light_orange;
            case 3:
                return R.drawable.light_green_3;
            case 4:
                return R.drawable.light_yellow;
            case 5:
                return R.drawable.light_purple;
            case 6:
                return R.drawable.light_blue_2;
            case 7:
                return R.drawable.light_indigo;
            case 8:
                return R.drawable.light_deep_orange;
            case 9:
                return R.drawable.light_amber;
            case 10:
                return R.drawable.light_deep_purple;
            case 11:
                return R.drawable.light_lime;
            case 12:
                return R.drawable.light_red;
            case 13:
                return R.drawable.light_teal;
            default:
                return R.drawable.light_blue;
        }
    }

    public static int getCorrectColorForPosition(Context context, int position) {
        position -= (int)Math.floor(position/totalColors) * totalColors;
        int colorResId = -1;
        switch (position){
            case 0:
                colorResId = R.color.light_blue;
                break;
            case 1:
                colorResId =  R.color.light_green;
                break;
            case 2:
                colorResId =  R.color.light_orange;
                break;
            case 3:
                colorResId =  R.color.light_green_3;
                break;
            case 4:
                colorResId =  R.color.light_yellow;
                break;
            case 5:
                colorResId =  R.color.light_purple;
                break;
            case 6:
                colorResId =  R.color.light_blue_2;
                break;
            case 7:
                colorResId =  R.color.light_indigo;
                break;
            case 8:
                colorResId =  R.color.light_deep_orange;
                break;
            case 9:
                colorResId =  R.color.light_amber;
                break;
            case 10:
                colorResId =  R.color.light_deep_purple;
                break;
            case 11:
                colorResId =  R.color.light_lime;
                break;
            case 12:
                colorResId =  R.color.light_red;
                break;
            case 13:
                colorResId =  R.color.light_teal;
                break;
            default:
                colorResId =  R.color.light_blue;
        }
        return getCorrectColor(context,colorResId);
    }

    public static void setBackgroundDrawable(Context context, ImageView imageView, int position) {
        int drawableId = Utility.getImageResourceIdForPosition(position);
        Drawable backgroundDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundDrawable = context.getResources().getDrawable(drawableId, context.getTheme());
        }else {
            backgroundDrawable = context.getResources().getDrawable(drawableId);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setBackground(backgroundDrawable);
        }else {
            imageView.setBackgroundDrawable(backgroundDrawable);
        }
    }

    public static Drawable getBackgroundDrawable(Context context,int drawableId) {
        Drawable backgroundDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            backgroundDrawable = context.getResources().getDrawable(drawableId, context.getTheme());
        }else {
            backgroundDrawable = context.getResources().getDrawable(drawableId);
        }
        return backgroundDrawable;
    }

}