package ir.the_moment.carmichael_sms.tasks.dataManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import ir.the_moment.carmichael_sms.fileManager.FileUtility;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.OnActionFinished;
import ir.the_moment.carmichael_sms.tasks.UserActivatedTask;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Security;

/**
 * Created by vaas on 4/9/2017.
 * class to encrypt data
 */

public class SecureData extends UserActivatedTask implements OnActionFinished {

    public static final String action = "dataManager.SecureData";
    private boolean deleteAfterEncryption = false;

    private String filesToEncryptPaths;

    private ArrayList<File> successfullyEncryptedFiles = new ArrayList<>();

    private ArrayList<File> failedToEncryptFiles = new ArrayList<>();

    private ArrayList<File> files = new ArrayList<>();
    private ArrayList<File> folders = new ArrayList<>();

    @Override
    public int getPermission() {
        return mR.Permissions.ENCRYPT_FILE;
    }

    @Override
    public String getDescription() {
        return context.getString(R.string.description_encrypt_data);
    }

    @Override
    public priority getPriority() {
        return priority.high;
    }

    @Override
    public String getTaskName() {
        return context.getString(R.string.encrypt_file);
    }

    @Override
    protected void action() {
        parseData();
        parseFlags();
        separateFilesAndFolders();
        encryptFiles();
        encryptFolders();
        DeleteData deleteData = new DeleteData();
        Message deleteMessage = new Message();
        deleteMessage.putExtra(DataManager.FLAG_DELETE, FileUtility.getFilePaths(files));
        deleteData.setMessage(deleteMessage)
                .setContext(context)
                .setOnActionFinished(this)
                .run();
    }
    private void encryptFiles(){
        String password = Security.getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        if (password != null) {
            for (File file :
                    files) {
                encryptFileAndAddToList(password,file);
            }
        }
    }

    private void encryptFolders() {
        String password = Security.getDecryptedPreferenceEntry(context,context.getString(R.string.key_pref_user_password_encrypted));
        if (password != null) {
            for (File file :
                    folders) {
                if (file.isDirectory()) {
                    encryptFolder(password, file);
                }else {
                    encryptFileAndAddToList(password, file);
                }
            }
        }
    }

    private void encryptFileAndAddToList(String password, File file) {
        try {
            encryptFile(file, password.toCharArray());
            successfullyEncryptedFiles.add(file);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
            successfullyEncryptedFiles.add(file);
        }
    }

    private void encryptFolder(String password, File folder) {
        for (File file :
                folder.listFiles()) {
            try {
                encryptFile(file, password.toCharArray());
                successfullyEncryptedFiles.add(file);
            } catch (IOException | GeneralSecurityException e) {
                e.printStackTrace();
                failedToEncryptFiles.add(file);
            }
        }
    }

    @Override
    protected void parseData() {
        filesToEncryptPaths = getMessage().getExtra(DataManager.FLAG_ENCRYPT);
    }

    @Override
    protected void parseFlags() {

    }

    @Override
    public boolean isEnabled() {
        return EnabledServices.isFileManagerEnabled(context);
    }

    private void separateFilesAndFolders() {
        ArrayList<File> allFiles = FileUtility.extractFiles(filesToEncryptPaths);
        if (allFiles != null){
            for (File file :
                    allFiles) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        folders.add(file);
                    } else {
                        files.add(file);
                    }
                }
            }
        }
    }

    public static void encryptFile(File fileToEncrypt, char[] pass) throws IOException, GeneralSecurityException {
        Cipher cipher = makeCipher(pass, true);
        CipherOutputStream cipherOutputStream = null;
        BufferedInputStream bis = null;
        try {
            cipherOutputStream =
                    new CipherOutputStream(new FileOutputStream(fileToEncrypt.getAbsoluteFile() + String.valueOf(System.currentTimeMillis()) +  ".encrypted"), cipher);
            bis = new BufferedInputStream(new FileInputStream(fileToEncrypt));
            int i;
            while ((i = bis.read()) != -1) {
                cipherOutputStream.write(i);
            }
        } finally {
            if (cipherOutputStream != null) {
                cipherOutputStream.close();
            }
            if (bis != null) {
                bis.close();
            }
        }
    }

    public static void decryptFile(String inFileName, String outFileName, char[] pass) throws GeneralSecurityException, IOException {
        Cipher cipher = makeCipher(pass, false);
        CipherInputStream cipherInputStream = null;
        BufferedOutputStream bos = null;
        try {
            cipherInputStream = new CipherInputStream(new FileInputStream(inFileName), cipher);
            bos = new BufferedOutputStream(new FileOutputStream(outFileName));
            int i;
            while ((i = cipherInputStream.read()) != -1) {
                bos.write(i);
            }
        }finally {
            if (cipherInputStream != null) {
                cipherInputStream.close();
            }
            if (bos != null) {
                bos.close();
            }
        }
    }

    private static Cipher makeCipher(char[] pass, Boolean decryptMode) throws GeneralSecurityException {

        // Use a KeyFactory to derive the corresponding key from the passphrase:
        PBEKeySpec keySpec = new PBEKeySpec(pass);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        // Create parameters from the salt and an arbitrary number of iterations:
        byte[] salt = new byte[1024];
        // TODO: 6/23/17 see if this salt is correct
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 43);

        // Set up the cipher:
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        // TODO: 6/23/17 check if this algorithm is secure
        // Set the cipher mode to decryption or encryption:
        if (decryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }

    @Override
    public void onActionFinished(boolean succeed, Message r) {
        sendRespond();
    }

    private void sendRespond() {
        respond = new Message();
        respond.requestRespond = true;
        respond.type = Message.Type.response;
        respond.action = DataManager.FLAG_ENCRYPT;
        respond.putExtra(DataManager.DATA_SUCCEED_LIST, FileUtility.getFilesNames(successfullyEncryptedFiles));
        respond.putExtra(DataManager.DATA_FAILED_LIST,FileUtility.getFilesNames(failedToEncryptFiles));
        onActionFinished(true);
    }

}
