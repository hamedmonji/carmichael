package ir.the_moment.carmichael_sms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.ArrayList;

import ir.the_moment.carmichael_sms.tasks.Task;
import ir.the_moment.carmichael_sms.tasks.TaskFactory;
import ir.the_moment.carmichael_sms.tasks.alarm.DisableSilent;
import ir.the_moment.carmichael_sms.tasks.boot.BootNotification;
import ir.the_moment.carmichael_sms.tasks.dataManager.DataManager;
import ir.the_moment.carmichael_sms.tasks.dataManager.DeleteData;
import ir.the_moment.carmichael_sms.tasks.dataManager.SecureData;
import ir.the_moment.carmichael_sms.tasks.dataManager.UploadData;
import ir.the_moment.carmichael_sms.tasks.filePreset.FilePreset;
import ir.the_moment.carmichael_sms.tasks.location.GetLocation;
import ir.the_moment.carmichael_sms.tasks.lock.Lock;
import ir.the_moment.carmichael_sms.tasks.lock.UnLockApp;
import ir.the_moment.carmichael_sms.tasks.sim.SimChanged;
import ir.the_moment.carmichael_sms.tasks.statusCheck.StatusCheck;
import ir.the_moment.carmichael_sms.tasks.takePictures.TakePictures;
import ir.the_moment.carmichael_sms.tasks.wipeData.WipeData;

/**
 * Created by vaas on 3/24/2017.
 * class containing all constants.
 */

public final class mR {
    public static final String KEY_PREF_SECURITY_ALERTS_LOW = "key_pref_security_alerts_low";
    public static final String KEY_PREF_SECURITY_ALERTS_MEDIUM = "key_pref_security_alerts_medium";
    public static final String KEY_PREF_SECURITY_ALERTS_HIGH = "key_pref_security_alerts_high";
    public static final String KEY_ID = "id";
    public static final String TAG = "getLocation";
    public static final String KEY_POSITION = "position";

    private mR() {
    }

    public static class CurrentDevice{
        public static String number;
        public static String password;
    }

    public static final String response = "res ";
    public static final String command = "com ";
    public static final String request = "req ";

    public static final int TYPE_HANDLER = 0;
    public static final int TYPE_ASSET = 1;

    public static final String KEY_MESSAGES = "messages";
    public static final String RESPONSE_ENCRYPTION_KEY = "response_encryption";

    public static final String DATA_SEPARATOR = ";,";
    public static final String PASSWORD_ENCRYPTOION_KEY = "werealyneedtochangethis";

    public static final String KEY_SMS_BODY = "key_sms_body";

    public static final String PREFS_KEY_SIM_SUBSCRIBER_ID = "subscriberId";


    /**
     * permission that can be given to a handler.
     * permissions are added using the logically or '|' operator.
     */
    public static final class Permissions{

        public static final int CAPTURE_PICTURES = 1;
        public static final int DELETE_FILE = 2;
        public static final int DOWNLOAD_FILE = 4;
        public static final int ENCRYPT_FILE = 8;
        public static final int HIDE_FILE = 16;
        public static final int LOCK_DEVICE = 32;
        public static final int UNLOCK_DEVICE = 64;
        public static final int UNLOCK_APP = 128;
        public static final int WIPE = 256;
        public static final int STATUS_CHECK = 512;
        public static final int ACCESS_LOCATION = 1024;
        public static final int ACTIVATE_PRESETS = 2048;
        public static final int TERMINAL = 4096;
        public static final int SIM_CHANGED = 8192;
        public static final int DISABLE_Silent = 16384;
        public static final int BOOT_NOTIFICATION = 32768;
        public static final int CONTROL_ZONE = 65536;

        public static boolean hasPermission(int permissionsSet,int permission) {
            return permission == -1 || (permissionsSet != 0 && (permissionsSet | permission) == permissionsSet);
        }

        public static boolean hasPermission(@NonNull Context context,int permissionsSet,Message message){
            if (permissionsSet == 0){
                return false;
            }
            Task task = TaskFactory.createTask(TaskExecutorService.tasksPackage+message.action);
            task.setContext(context);
            task.setMessage(message);
            return task.isEnabled() &&  hasPermission(permissionsSet,task.getPermission());
        }

        public static int togglePermission(int permissionsSet,int permission){
            return permissionsSet ^ permission;
        }

        public static String[] getAvailableTasksForDevice(@NonNull DeviceModel device,Context context){
            if (device.type == mR.TYPE_ASSET){
                return null;
            }
            SparseArray<String> tasksForPermissions = getTasksForPermissionsArray();
            ArrayList<String> availableTasks = new ArrayList<>();
            for (int i = 0; i < tasksForPermissions.size(); i++) {
                if (isTaskEnabled(tasksForPermissions.valueAt(i),context) &&
                        hasPermission(device.permissions,tasksForPermissions.keyAt(i))) {
                    availableTasks.add(String.valueOf(tasksForPermissions.keyAt(i)));
                }
            }

            return availableTasks.toArray(new String[]{});
        }

        public static boolean isTaskEnabled(String taskAction,Context context){
            Task task = TaskFactory.createTask(TaskExecutorService.tasksPackage + taskAction);
            task.setContext(context);
            return task.isEnabled();
        }

        public static String getTaskActionForPermission(int permission){
            return getTasksForPermissionsArray().get(permission);
        }

        private static SparseArray<String> getTasksForPermissionsArray(){
            SparseArray<String> tasksForPermission = new SparseArray<>();
            tasksForPermission.put(CAPTURE_PICTURES, TakePictures.action);
            tasksForPermission.put(DELETE_FILE, DeleteData.action);
            tasksForPermission.put(DOWNLOAD_FILE, UploadData.action);
            tasksForPermission.put(ENCRYPT_FILE, SecureData.action);
            tasksForPermission.put(HIDE_FILE, DataManager.action);
            tasksForPermission.put(LOCK_DEVICE, Lock.action);
            tasksForPermission.put(UNLOCK_APP, UnLockApp.action);
            tasksForPermission.put(WIPE, WipeData.action);
            tasksForPermission.put(STATUS_CHECK, StatusCheck.action);
            tasksForPermission.put(ACCESS_LOCATION, GetLocation.action);
            tasksForPermission.put(ACTIVATE_PRESETS, FilePreset.action);
            tasksForPermission.put(DISABLE_Silent, DisableSilent.action);
            tasksForPermission.put(SIM_CHANGED, SimChanged.action);
            tasksForPermission.put(BOOT_NOTIFICATION, BootNotification.action);
            return tasksForPermission;
        }
    }
}
