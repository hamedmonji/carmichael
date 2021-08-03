package ir.the_moment.carmichael_sms.database;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.utility.Security;

import static ir.the_moment.carmichael_sms.utility.Security.getDecryptedPreferenceEntry;

/**
 * Created by vaas on 4/5/2017.
 * database that will save all the info about each device both assets and handlers
 *
 */

public class DeviceInfoDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "deviceInfo2.db";
    private static final int DATABASE_VERSION = 2;
    private Context context;

    public DeviceInfoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_DEVICE_INFO_TABLE = "CREATE TABLE " + DeviceInfoContract.DeviceInfo.TABLE_NAME + "("
                + DeviceInfoContract.DeviceInfo._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DeviceInfoContract.DeviceInfo.NAME + " TEXT, "
                + DeviceInfoContract.DeviceInfo.NUMBER + " TEXT NOT NULL, "
                + DeviceInfoContract.DeviceInfo.PIC + " TEXT, "
                + DeviceInfoContract.DeviceInfo.USER_ID + " TEXT NOT NULL, "
                + DeviceInfoContract.DeviceInfo.ALTERNATE_NUMBER + " TEXT, "
                + DeviceInfoContract.DeviceInfo.PERMISSIONS + " TEXT NOT NULL, "
                + DeviceInfoContract.DeviceInfo.TYPE + " TEXT NOT NULL, "
                + DeviceInfoContract.DeviceInfo.STATUS + " TEXT NOT NULL, "
                + DeviceInfoContract.DeviceInfo.RANK + " TEXT NOT NULL);";
        db.execSQL(SQL_CREATE_DEVICE_INFO_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long insertDevice(DeviceModel device){
        return getWritableDatabase().insert(DeviceInfoContract.DeviceInfo.TABLE_NAME, null, getContentValues(device));
    }

    public int updateDeviceById(DeviceModel device , long id){
        String whereClause = DeviceInfoContract.DeviceInfo._ID + "=?";
        String[] whereArgs = new String[]{String.valueOf(id)};
        return getWritableDatabase().update(DeviceInfoContract.DeviceInfo.TABLE_NAME,getContentValues(device),whereClause,whereArgs);
    }

    public int updateDeviceByNumber(Context context ,DeviceModel device , String number ,int type){
        String whereClause = DeviceInfoContract.DeviceInfo.NUMBER + "=?"
                + " AND " + DeviceInfoContract.DeviceInfo.TYPE + "=?";
        String[] whereArgs = new String[]{Security.encrypt(context , number) , Security.encrypt(context, String.valueOf(type))};
        return getWritableDatabase().update(DeviceInfoContract.DeviceInfo.TABLE_NAME,getContentValues(device),whereClause,whereArgs);
    }

    public int deleteDeviceById(long id){
        String where = "_id=?";
        return getWritableDatabase().delete(DeviceInfoContract.DeviceInfo.TABLE_NAME,where, new String[]{String.valueOf(id)});
    }

    public static DeviceModel getDeviceByUserId(Context context,String userId, int type) {
        String password = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        if (password != null) {
            String selection = DeviceInfoContract.DeviceInfo.USER_ID + "=? AND "
                    + DeviceInfoContract.DeviceInfo.TYPE + "=?";
            String[] whereArgs = new String[]{Security.encrypt(context,userId),Security.encrypt(context, String.valueOf(type))};
            Cursor cursor = getCursor(context,getProjection(),selection,whereArgs);

            if ((cursor != null && cursor.getCount() > 0)) {
                cursor.moveToFirst();
                return Security.getDecryptedDeviceModel(cursor,password);
            }else {
                return null;
            }
        }else {
            return null;
        }
    }

    public void deleteDeviceByNumber(Context context,String number,int type) {
        String whereClause = DeviceInfoContract.DeviceInfo.NUMBER + "=?" +
                " AND " + DeviceInfoContract.DeviceInfo.TYPE + "=?";
        String[] whereArgs = new String[]{Security.encrypt(context,number),Security.encrypt(context, String.valueOf(type))};
        getWritableDatabase().delete(DeviceInfoContract.DeviceInfo.TABLE_NAME,whereClause,whereArgs);
    }

    private ContentValues getContentValues(DeviceModel model){
        if (model != null) {
            String name = Security.encrypt(context, model.name);
            String number = Security.encrypt(context, model.number);
            String alternateNumber = Security.encrypt(context, model.alternateNumber);
            String type = Security.encrypt(context, String.valueOf(model.type));
            String pic = Security.encrypt(context,model.pic);
            String userId = Security.encrypt(context,model.userId);
            String permissions = Security.encrypt(context, String.valueOf(model.permissions));
            String status = Security.encrypt(context, String.valueOf(model.status));
            String rank = Security.encrypt(context, String.valueOf(model.rank));

            ContentValues values = new ContentValues();
            values.put(DeviceInfoContract.DeviceInfo.NAME, name);
            values.put(DeviceInfoContract.DeviceInfo.USER_ID,userId);
            values.put(DeviceInfoContract.DeviceInfo.NUMBER, number);
            values.put(DeviceInfoContract.DeviceInfo.ALTERNATE_NUMBER, alternateNumber);
            values.put(DeviceInfoContract.DeviceInfo.TYPE, type);
            values.put(DeviceInfoContract.DeviceInfo.PIC, pic);
            values.put(DeviceInfoContract.DeviceInfo.PERMISSIONS,permissions);
            values.put(DeviceInfoContract.DeviceInfo.STATUS,status);
            values.put(DeviceInfoContract.DeviceInfo.RANK,rank);

            return values;
        }else return null;
    }

    public static class EncryptDatabaseAndPrefsWithNewPassword extends AsyncTask<Void,Void,Void>{
        private Context context;
        private AlertDialog dialog;
        private String oldPassword;
        private String newPassword;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.show();
        }

        public EncryptDatabaseAndPrefsWithNewPassword(@NonNull Context context,
                                                      @NonNull String oldPassword, @NonNull String newPassword) {
            this.context = context;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
            dialog = new ProgressDialog.Builder(context)
                    .setMessage(context.getString(R.string.encrypting_database))
                    .setCancelable(false)
                    .create();
        }

        @Override
        protected Void doInBackground(Void... params) {
            startEncryption(oldPassword,newPassword);
            return null;
        }



        private void startEncryption(@NonNull final String oldPassword, @NonNull final String newPassword) {
            encryptDatabase(oldPassword, newPassword);
        }

        private void encryptDatabase(@NonNull String oldPassword, @NonNull String newPassword) {
            DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(context);
            Cursor cursor =
                    dbHelper.getWritableDatabase().query(DeviceInfoContract.DeviceInfo.TABLE_NAME,null,null,null,null,null,null);
            List<DeviceModel> models = new ArrayList<>();
            while (cursor.moveToNext()){
                DeviceModel model = Security.getDecryptedDeviceModel(cursor,oldPassword);
                if (model != null){
                    models.add(model);
                }
            }
            cursor.close();
            Security.encryptPreferencesEntry(context,newPassword,context.getString(R.string.key_pref_user_password_encrypted));

            for (DeviceModel model :
                    models) {
                dbHelper.deleteDeviceByNumber(context,model.number,model.type);
                dbHelper.insertDevice(model);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            dialog.dismiss();
        }
    }


    /**
     *
     * @param context context for decrypting the values
     * @param number number of the requested device
     * @param type can be one of two value {@link mR#TYPE_ASSET} or {@link mR#TYPE_HANDLER}
     * @return the cursor containing the values for a device with the given number
     */
    public static DeviceModel getDeviceByNumber(Context context, String number, int type){
        String password = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        if (password != null) {
            String selection = "( " +DeviceInfoContract.DeviceInfo.NUMBER + "=? OR "
                    + DeviceInfoContract.DeviceInfo.ALTERNATE_NUMBER + "=? ) AND "
                    + DeviceInfoContract.DeviceInfo.TYPE + "=?";
            String[] whereArgs = new String[]{Security.encrypt(context,number),Security.encrypt(context,number),Security.encrypt(context, String.valueOf(type))};

            Cursor cursor = getCursor(context,getProjection(),selection,whereArgs);


            if ((cursor != null && cursor.getCount() > 0)) {
                cursor.moveToFirst();
                return Security.getDecryptedDeviceModel(cursor,password);
            }else {
                return null;
            }

        }else {
            return null;
        }
    }

    /**
     *
     * @param context context for decrypting the values
     * @param id if of the device to return
     * @return model requested for that id
     */
    @Nullable
    public static DeviceModel getDeviceModelById(Context context, long id){
        if (id != -1) {
            Cursor cursor = getDeviceCursorById(id, context);
            cursor.moveToFirst();
            String password = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
            if (password != null) {
                return Security.getDecryptedDeviceModel(cursor,password);
            }
        }
        return null;
    }

    @Nullable
    public static Cursor getDevicesByType(@NonNull Context context,int type){
        if (isTypeValid(type)){
            String selection = DeviceInfoContract.DeviceInfo.TYPE + "=?";
            // we need to encrypt the args since the details for a device are first encrypted
            // and then written to the database.
            String args = Security.encrypt(context, String.valueOf(type));
            String[] selectionArgs = new String[]{args};
            return getCursor(context,getProjection(),selection,selectionArgs);

        }else return null;
    }

    private static Cursor getDeviceCursorById(long id, @NonNull Context context){
        String selection = DeviceInfoContract.DeviceInfo._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(id)};

        return getCursor(context,getProjection(),selection,selectionArgs);

    }

    private static boolean isTypeValid(int type) {
        return type == mR.TYPE_HANDLER || type == mR.TYPE_ASSET ;
    }

    public static Cursor getCursor(@NonNull Context context,@Nullable String[] projection,
                                    @Nullable String selection,@Nullable String[] selectionArgs){
        DeviceInfoDbHelper dbHelper = new DeviceInfoDbHelper(context);
        return dbHelper.getReadableDatabase().query(DeviceInfoContract.DeviceInfo.TABLE_NAME,projection,selection,selectionArgs,null,null,null);

    }

    @NonNull
    public static String[] getProjection() {
        return new String[]{
                DeviceInfoContract.DeviceInfo._ID,
                DeviceInfoContract.DeviceInfo.NAME,
                DeviceInfoContract.DeviceInfo.NUMBER,
                DeviceInfoContract.DeviceInfo.ALTERNATE_NUMBER,
                DeviceInfoContract.DeviceInfo.PERMISSIONS,
                DeviceInfoContract.DeviceInfo.TYPE,
                DeviceInfoContract.DeviceInfo.STATUS,
                DeviceInfoContract.DeviceInfo.RANK,
                DeviceInfoContract.DeviceInfo.USER_ID,
                DeviceInfoContract.DeviceInfo.PIC
        };
    }
}
