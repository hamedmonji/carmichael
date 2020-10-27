package ir.the_moment.carmichael_sms.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import com.scottyab.aescrypt.AESCrypt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import ir.the_moment.carmichael_sms.database.DeviceInfoContract;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.DeviceModel;
import ir.the_moment.carmichael_sms.R;

/**
 * Created by vaas on 3/28/2017.
 * handles authentication and encryption.
 */

public class Security {
    public static byte[] FIXED_IV = new byte[1024];

    /**
     * @param context context used to get an instance of preferences
     * @param password password that was entered by the user.
     * @return true if password was correct.
     */
    public static boolean isAuthorized(Context context,String password){
        String actualPassword = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        return actualPassword != null && actualPassword.equals(password);
    }

    /**
     *check to see if the sender of the message is authorized.
     * @param context context used to get an instance of preferences
     * @param phoneNumber devices number that sendWithSms the request.
     * @return true if the number is authorized by the user.
     */
    public static boolean isSenderAuthorized(@NonNull Context context,@NonNull String phoneNumber,int type) {

        DeviceModel deviceModel = DeviceInfoDbHelper.getDeviceByNumber(context, phoneNumber,type);
        return deviceModel != null && deviceModel.type == mR.TYPE_HANDLER;
    }

    /**
     * encrypts a message with users password
     * @param context context used to get an instance of preferences
     * @param message message to be encrypted.
     * @return encrypted message
     */
    public static String encrypt(Context context,String message,String password){
        if (password != null){
            try {
                return AESCrypt.encrypt(password,message);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else throw new IllegalStateException("password can't be null");
    }

    public static String encrypt(Context context,String message){
        String password = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        return encrypt(context,message,password);
    }

    /**
     * decrypts a message with users password
     * @param context context used to get an instance of preferences
     * @param message message to be decrypted.
     * @return decrypted message
     */
    public static String decrypt(Context context,String message){
        String password = getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        return decrypt(context,message,password);
    }

    /**
     * decrypts a message with a given password
     * @param context context used to get an instance of preferences
     * @param message message to be decrypted.
     * @param password password to decrypt the message with
     * @return decrypted message
     */
    public static String decrypt(Context context,String message,String password){
        if (password != null){
            try {
                return AESCrypt.decrypt(password,message);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }else throw new IllegalStateException("password can't be null");
    }

    @Nullable
    public static DeviceModel getDecryptedDeviceModel(@NonNull Cursor cursor ,@NonNull String password){
            DeviceModel model = new DeviceModel();
            try {
                model.name =
                        AESCrypt.decrypt(password, cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.NAME)));

                String number = cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.NUMBER));

                if (number != null) {
                    model.number =
                            AESCrypt.decrypt(password, number);
                }

                String alternateNumber = cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.ALTERNATE_NUMBER));

                if (alternateNumber != null) {
                    model.alternateNumber =
                            AESCrypt.decrypt(password, alternateNumber);
                }

                model.permissions =
                        Integer.parseInt(AESCrypt.decrypt(password, cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.PERMISSIONS))));


                String pic = cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.PIC));
                if (pic != null) {
                    model.pic =
                            AESCrypt.decrypt(password, pic);
                }
                model.userId =
                        AESCrypt.decrypt(password,cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.USER_ID)));

                model.type =
                        Integer.parseInt(AESCrypt.decrypt(password, cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.TYPE))));

                model.status =
                        Integer.parseInt(AESCrypt.decrypt(password, cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.STATUS))));

                model.rank=
                        Integer.parseInt(AESCrypt.decrypt(password, cursor.getString(cursor.getColumnIndexOrThrow(DeviceInfoContract.DeviceInfo.RANK))));

                return model;

            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        return null;
    }

    /**
     * decrypts a preference entry
     * @param context context used to get an instance of preferences
     * @param key key for the desired preference entry.
     * @return decrypted entry for the requested key
     */
    public static String getDecryptedPreferenceEntry(Context context,String key){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String encryptedEntry = sharedPreferences.getString(key,null);
        if (encryptedEntry  != null){
            try {
                return AESCrypt.decrypt(getPreferencesEncryptionKey(),encryptedEntry );
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static boolean getDecryptedBooleanPreferenceEntry(Context context,String key){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String encryptedEntry = prefs.getString(key,null);
        if (encryptedEntry != null){
            try {
                return Boolean.parseBoolean(AESCrypt.decrypt(getPreferencesEncryptionKey(),encryptedEntry));
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    /**
     * encrypts a preference value
     * @param context context used to get an instance of preferences
     * @param key key for the desired preference value.
     * @param value value to be encrypted and saved in the preferences.
     */
    public static void encryptPreferencesEntry(Context context, String value, String key){
        if (value != null ){
            String preferenceEncryptionKey = getPreferencesEncryptionKey();
            try {
                String encryptedEntry = AESCrypt.encrypt(preferenceEncryptionKey,value);
                saveEncryptedPreferenceEntry(context, key, encryptedEntry);
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveEncryptedPreferenceEntry(Context context, String key, String encryptedEntry) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(key,encryptedEntry).commit();
    }


    /**
     * encrypts an array of values .
     * preserves the values that already existed before for that key .
     * @param context context used to get an instance of preferences
     * @param key key for the desired preference entry.
     * @param values values to be encrypted and saved in the preferences.
     */
    public static void encryptPreferencesEntry(Context context,String[] values,String key){
        if (values != null ){
            String preferenceEncryptionKey = getPreferencesEncryptionKey();
            try {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String devices = getDecryptedPreferenceEntry(context,key);

                StringBuilder builder = new StringBuilder();
                if (devices != null){
                    builder.append(devices);
                    builder.append(mR.DATA_SEPARATOR);
                }

                for (int i = 0; i < values.length; i++) {
                    String device = values[i];
                    builder.append(device);
                    if (i < values.length -1){
                        builder.append(mR.DATA_SEPARATOR);
                    }
                }

                prefs.edit().putString(key,AESCrypt.encrypt(preferenceEncryptionKey,builder.toString())).commit();

            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * delete an entry form preferences
     * @param context context used to get an instance of preferences
     * @param key key for the desired preference entry.
     * @param position position for the value to be deleted.
     */
    public static void deletePreferencesEntry(Context context,int position,String key){

        String preferenceEncryptionKey = getPreferencesEncryptionKey();
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            String devicesAsString = getDecryptedPreferenceEntry(context,key);

            if (devicesAsString != null){
                String[] devices = devicesAsString.split(mR.DATA_SEPARATOR);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < devices.length; i++) {
                    if (i != position){
                        builder.append(devices[i]);
                        if (i < devices.length -1) {
                            builder.append(mR.DATA_SEPARATOR);
                        }
                    }
                }
                prefs.edit().putString(key,AESCrypt.encrypt(preferenceEncryptionKey,builder.toString())).commit();
            }


        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }

    }

    // TODO: 7/18/17 store password in keystore
    private static String getPreferencesEncryptionKey(){
        return mR.PASSWORD_ENCRYPTOION_KEY;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public static String encryptPassword(Context context, String password){
        final String KEY_ALIAS = "password";

        try {
            final KeyGenerator keyGenerator = KeyGenerator
                    .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build();

            keyGenerator.init(keyGenParameterSpec);
            final SecretKey secretKey = keyGenerator.generateKey();

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();

            return  new String(cipher.doFinal(password.getBytes("UTF-8")),"UTF-8");


        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getDecryptedPassword(String encryptedPassword){
        try {
            final String KEY_ALIAS = "password";
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore
                    .getEntry(KEY_ALIAS, null);

            final SecretKey secretKey = secretKeyEntry.getSecretKey();
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, cipher.getIV());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

            final byte[] decodedData = cipher.doFinal(encryptedPassword.getBytes("UTF-8"));

            return new String(decodedData, "UTF-8");
        } catch (KeyStoreException | NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidAlgorithmParameterException | InvalidKeyException
                | CertificateException | IOException | UnrecoverableEntryException
                | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void encryptPassword(Context context){
//        final String AES_MODE = "AES/GCM/NoPadding";
//        Cipher c = null;
//        try {
//            c = Cipher.getInstance(AES_MODE);
//            c.init(Cipher.ENCRYPT_MODE, getSecretKey(), new GCMParameterSpec(128, FIXED_IV.getBytes()));
//            byte[] encodedBytes = c.doFinal(input);
//            String encryptedBase64Encoded = Base64.encodeToString(encodedBytes, Base64.DEFAULT);
//            return encryptedBase64Encoded;
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (NoSuchPaddingException e) {
//            e.printStackTrace();
//        }
//    }


}
