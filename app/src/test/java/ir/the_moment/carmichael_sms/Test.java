package ir.the_moment.carmichael_sms;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;

import org.junit.Assert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

import ir.the_moment.carmichael_sms.responseHandler.ResponseHandler;
import ir.the_moment.carmichael_sms.responseHandler.ResponseHandlerFactory;
import ir.the_moment.carmichael_sms.responseHandler.StatusCheckResponseHandler;

import static android.os.Build.VERSION_CODES.M;
import static ir.the_moment.carmichael_sms.mR.Permissions.ACCESS_LOCATION;
import static ir.the_moment.carmichael_sms.mR.Permissions.LOCK_DEVICE;
import static ir.the_moment.carmichael_sms.mR.Permissions.WIPE;

/**
 * Created by vaas on 4/22/17.
 */

public class Test {

    public static final String TAG = "test";
    @org.junit.Test
    public void permissionsTest(){
        int permissions = ACCESS_LOCATION;
        Assert.assertEquals(true, mR.Permissions.hasPermission(permissions,WIPE));
        Assert.assertEquals(true,mR.Permissions.hasPermission(permissions,LOCK_DEVICE));
        Assert.assertEquals(true,mR.Permissions.hasPermission(permissions,ACCESS_LOCATION));
    }


    @org.junit.Test
    public void classNameTest(){
        Assert.assertEquals("String",String.class.getSimpleName());
    }


    @org.junit.Test
    public void formatterTest(){
        String number = "09138798609";
        Assert.assertEquals("0913 879 8609",getFormattedNumber(number));
    }

    @org.junit.Test
    public void formatterWithPrefixTest(){
        String numberWIthPrefix = "+989138798609";
        Assert.assertEquals("+98 913 879 8609",getFormattedNumber(numberWIthPrefix));

        String numberWIthPrefix_2 = "+19138798609";
        Assert.assertEquals("+1 913 879 8609",getFormattedNumber(numberWIthPrefix_2));
    }

    public String getFormattedNumber(String number){
        String formattedNumber = "";
        if (number.startsWith("+")) {
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
    }

    @org.junit.Test
    public void responseHandlerFactoryTest(){
        String responsePackage = "the_moment.carmichael_sms.responseHandler.";
        ResponseHandler statusCheckResponse =
                ResponseHandlerFactory.createRespond(responsePackage + StatusCheckResponseHandler.class.getSimpleName());
        Assert.assertEquals(StatusCheckResponseHandler.class.getSimpleName(),statusCheckResponse.getClass().getSimpleName());

    }

    @RequiresApi(api = M)
    @org.junit.Test
    public void encryptPasswordTest(){
        String password = "pass";
        String encryptedPassword = encryptPassword(null,password);
        Assert.assertEquals(password,getDecryptedPassword(encryptedPassword));
    }


    @RequiresApi(api = M)
    private String encryptPassword(Context context, String password){
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

            return new String(cipher.doFinal(password.getBytes("UTF-8")),"UTF-8");
//            PreferenceManager.getDefaultSharedPreferences(context).edit().
//                    putString(context.getString(R.string.key_pref_user_password_encrypted),encryptedData).commit();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidAlgorithmParameterException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | UnsupportedEncodingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getDecryptedPassword(String encryptedPassword){
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

    @org.junit.Test
    public void decryptMessage() {

    }


}
