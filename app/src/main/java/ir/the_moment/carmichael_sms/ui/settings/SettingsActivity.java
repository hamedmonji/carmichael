package ir.the_moment.carmichael_sms.ui.settings;

import android.Manifest;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.dropbox.core.android.Auth;

import de.hdodenhof.circleimageview.CircleImageView;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.WipeDataReceiver;
import ir.the_moment.carmichael_sms.ui.BaseActivity;
import ir.the_moment.carmichael_sms.ui.LauncherActivity;
import ir.the_moment.carmichael_sms.utility.EnabledServices;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;

import static ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper.EncryptDatabaseAndPrefsWithNewPassword;

public class SettingsActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener,View.OnClickListener {
    public static final String KEY_PREF_DROP_BOX_ACCESS_TOKEN = "db_access_token";
    public static final int DEVICE_ADMIN_REQUEST_CODE = 1000;
    private static final int RC_PERMISSION_LOCATION = 1002;

    private static final int RC_PERMISSION_CAMERA = 1004;
    private static final int RC_PERMISSION_CONTROL_ZONES = 1005;
    public static final String TAG_TASKS_FRAGMENT = "enabled_tasks_fragment";
    private static final String TAG_SECURITY_FRAGMENT = "security_fragment";

    private boolean adminRequestIsForWipe = false;


    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private SharedPreferences prefs;

    private CircleImageView profileImage;
    private ImageView backgroundImage;

    private LinearLayout userProfileRoot;
    private FloatingActionButton changeImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        listener = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();

        setUpUserImages();

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    userProfileRoot.setVisibility(View.VISIBLE);
                    changeImages.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void bindViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setTitleTextColor(Utility.getCorrectColor(this,R.color.white));
        toolbar.setBackgroundColor(Utility.getCorrectColor(this,R.color.transparent));
        setTitle(R.string.menu_navigation_title_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        userProfileRoot = findViewById(R.id.user_profile_root);

        profileImage = findViewById(R.id.user_profile_image);
        backgroundImage = findViewById(R.id.user_background_image);

        TextView security = findViewById(R.id.security);
        TextView enabledTasks = findViewById(R.id.enabled_tasks);

        security.setOnClickListener(this);
        enabledTasks.setOnClickListener(this);

        TextView userName = findViewById(R.id.name);

        String name = prefs.getString(getString(R.string.key_pref_user_name),getString(R.string.not_yet_set));
        userName.setText(name);

        TextView faq = findViewById(R.id.faq);
        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent faqIntent = new Intent(Intent.ACTION_VIEW);
                CharSequence[] items = {getString(R.string.carmichael_faq),getString(R.string.ask_a_question),getString(R.string.document)};
                   new AlertDialog.Builder(SettingsActivity.this)
                           .setTitle(R.string.faq_help)
                           .setItems(items, new DialogInterface.OnClickListener() {
                               @Override
                               public void onClick(DialogInterface dialogInterface, int i) {
                                   switch (i) {
                                       case 0 :
                                           faqIntent.setData(Uri.parse("http://the-moment.ir/applications/carmichael-support/faq/"));
                                           break;
                                       case 1:
                                           faqIntent.setData(Uri.parse("http://the-moment.ir/applications/carmichael-support/ask-question/"));
                                           break;
                                       case 2:
                                           faqIntent.setData(Uri.parse("http://the-moment.ir/applications/carmichael-support/documentation/"));
                                           break;
                                   }
                                   startActivity(faqIntent);
                               }
                           }).show();
            }
        });

        TextView bugReport = findViewById(R.id.bug_report);
        bugReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bugReportIntent = new Intent(Intent.ACTION_VIEW);
                bugReportIntent.setData(Uri.parse("http://the-moment.ir/applications/carmichael-support/bug-report/"));
                startActivity(bugReportIntent);
            }
        });
    }

    private void setUpUserImages() {

        final String backgroundImagePath = prefs.getString(getString(R.string.key_pref_user_background_image),null);

        if (backgroundImagePath != null) {
            Glide.with(this).load(backgroundImagePath).into(backgroundImage);
        }


        String profileImagePath = prefs.getString(getString(R.string.key_pref_user_photo),null);

        if (profileImagePath != null) {
            Glide.with(this).load(profileImagePath).into(profileImage);
        }else {
            TextView imageText = findViewById(R.id.user_profile_image_text);
            imageText.setVisibility(View.VISIBLE);
            String name = prefs.getString(getString(R.string.key_pref_user_name),getString(R.string.not_yet_set));
            imageText.setText(name.substring(0,1).toUpperCase());
            profileImage.setImageResource(R.drawable.blue_electric);
        }

        changeImages = findViewById(R.id.change_image);
        changeImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence[] items = {getString(R.string.change_profile_image_title),
                        getString(R.string.change_background_image),
                        getString(R.string.remove_image)};
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(getString(R.string.change_image))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        updateUserProfileImage();
                                        break;
                                    case 1:
                                        updateUserBackgroundImage();
                                        break;
                                    case 2 :
                                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                                        String backgroundImagePath = prefs.getString(getString(R.string.key_pref_user_background_image),null);
                                        if (backgroundImagePath != null) {
                                            showAlertToSetBackgroundToItsDefault(backgroundImage);
                                        }
                                        break;
                                }
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.security:
                userProfileRoot.setVisibility(View.GONE);
                changeImages.setVisibility(View.GONE);
                getFragmentManager().beginTransaction()
                        .addToBackStack("security_fragment")
                        .replace(R.id.settings_fragment_container,new SettingsSecurityFragment(),TAG_SECURITY_FRAGMENT)
                        .commit();
                break;
            case R.id.enabled_tasks:
                userProfileRoot.setVisibility(View.GONE);
                changeImages.setVisibility(View.GONE);
                getFragmentManager().beginTransaction()
                        .addToBackStack("enabled_tasks_fragment")
                        .replace(R.id.settings_fragment_container,new SettingsEnabledTasksFragment(), TAG_TASKS_FRAGMENT)
                        .commit();
                break;
        }
    }


    private void updateUserProfileImage() {
        promptToSelectImage(new OnImageSelected() {
            @Override
            public void onSelected(String imagePath) {
                prefs.edit().putString(getString(R.string.key_pref_user_photo),imagePath).commit();
                profileImage.setImageURI(Uri.parse(imagePath));
                findViewById(R.id.user_profile_image_text).setVisibility(View.GONE);

            }
        }, true,getString(R.string.change_profile_image_title));
    }

    private void updateUserBackgroundImage(){
        promptToSelectImage(new OnImageSelected() {
            @Override
            public void onSelected(String imagePath) {
                prefs.edit().putString(getString(R.string.key_pref_user_background_image),imagePath).commit();
                Glide.with(SettingsActivity.this).load(imagePath).into(backgroundImage);
            }
        }, false,getString(R.string.change_background_image));
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(listener);
        saveDropBoxAccessToken();
    }

    private void saveDropBoxAccessToken() {
        String accessToken = Auth.getOAuth2Token();
        if (accessToken != null) {
            prefs.edit().putString(KEY_PREF_DROP_BOX_ACCESS_TOKEN, accessToken).apply();
        }else {
            savePreference(String.valueOf(false),String.valueOf(R.string.key_pref_allow_file_manager_encrypted));
            savePreference(String.valueOf(false),String.valueOf(R.string.key_pref_allow_taking_pictures_encrypted));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs,final String key) {
        if (key.equals(getString(R.string.key_pref_allow_status_check))){
            boolean statusCheck = prefs.getBoolean(key,false);
            savePreference(String.valueOf(statusCheck),getString(R.string.key_pref_allow_status_check_encrypted));
        } else if (key.equals(getString(R.string.key_pref_allow_wipe))) {
            boolean wipeEnabled = prefs.getBoolean(getString(R.string.key_pref_allow_wipe), false);
            if (wipeEnabled) {
                if (!EnabledServices.isAdminEnabled(this)) {
                    adminRequestIsForWipe = true;
                    showEnableAdminDialog();
                } else {
                    savePreference(String.valueOf(true), getString(R.string.key_pref_allow_wipe_encrypted));
                }
            }

        }else if (key.equals(getString(R.string.key_pref_lock_max_attempts))) {
            PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_SECURITY_FRAGMENT);
            if (fragment == null) return;
            EditTextPreference maxAttemptPref = (EditTextPreference) fragment.findPreference(getString(R.string.key_pref_lock_max_attempts));
            if (maxAttemptPref == null) return;
            String maxAttempt = prefs.getString(key,null);
            boolean maxAttemptEnabled = prefs.getBoolean(getString(R.string.key_pref_enable_max_attempts),false);
            if (maxAttemptEnabled && maxAttempt != null && !maxAttempt.isEmpty()) {
                maxAttemptPref.setSummary(maxAttempt);
            }else {
                maxAttemptPref.setSummary(R.string.not_yet_set);
            }

        } else if (key.equals(getString(R.string.key_pref_user_password))){
            String newPassword = prefs.getString(key,null);
            String oldPassword = Security.getDecryptedPreferenceEntry(this,getString(R.string.key_pref_user_password_encrypted));
            if (oldPassword == null && newPassword != null){
                savePreference(newPassword,getString(R.string.key_pref_user_password_encrypted));
            }else if (newPassword != null && !newPassword.isEmpty()){
                EncryptDatabaseAndPrefsWithNewPassword encryptDatabase =
                        new EncryptDatabaseAndPrefsWithNewPassword(this,oldPassword,newPassword);
                encryptDatabase.execute();
            }else {
                savePreference(oldPassword,getString(R.string.key_pref_user_password_encrypted));
                Toast.makeText(this, R.string.password_cant_be_empty, Toast.LENGTH_SHORT).show();
            }
        } else if (key.equals(getString(R.string.key_pref_allow_location))) {
            boolean locationEnabled = prefs.getBoolean(key, false);
            if (locationEnabled){
                if (isPostMarshMellow() && !Utility.isLocationPermissionGranted(this)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION_LOCATION);
                }else {
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_location_encrypted));
                }
            }else {
                savePreference(String.valueOf(false),getString(R.string.key_pref_allow_location_encrypted));
            }
        }else if (key.equals(getString(R.string.key_pref_allow_taking_pictures))){
            boolean capturePictures = prefs.getBoolean(key,false);

            if (capturePictures){
                if (isPostMarshMellow()){
                    if (!Utility.isCameraPermissionGranted(this)){
                        requestPermissions(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},RC_PERMISSION_CAMERA);
                    }else {
                        savePreference(String.valueOf(true),getString(R.string.key_pref_allow_taking_pictures_encrypted));
                        startDropBoxAuthentication();
                    }
                }else {
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_taking_pictures_encrypted));
                    startDropBoxAuthentication();
                }
            }else {
                savePreference(String.valueOf(false),getString(R.string.key_pref_allow_taking_pictures_encrypted));
            }

        }else if (key.equals(getString(R.string.key_pref_allow_lock_device))){
            boolean lock = prefs.getBoolean(key,false);
            savePreference(String.valueOf(lock), getString(R.string.key_pref_allow_lock_device_encrypted));
            if (lock){
                if (!EnabledServices.isAdminEnabled(this)){
                    showEnableAdminDialog();
                    adminRequestIsForWipe = false;
                }else {
                    savePreference(String.valueOf(true), getString(R.string.key_pref_allow_lock_device_encrypted));
                }
            }
        }else if (key.equals(getString(R.string.key_pref_hide_app_icon))){
            boolean hideAppIcon = prefs.getBoolean(key,false);
            PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_SECURITY_FRAGMENT);
            if (fragment != null) {
                EditTextPreference numberPreference = (EditTextPreference) fragment.findPreference(getString(R.string.key_pref_hide_app_icon_number));
                String phoneNumber = prefs.getString(getString(R.string.key_pref_hide_app_icon_number),null);
                if (phoneNumber != null && numberPreference != null) {
                    numberPreference.setSummary(phoneNumber);
                }else if (numberPreference != null){
                    numberPreference.setSummary(getString(R.string.one_to_four));
                }
            }
            if (hideAppIcon) {
                disableAppIcon();
            }else {
                enableAppIcon();
            }
        } else if (key.equals(getString(R.string.key_pref_hide_app_icon_number))) {
            String phoneNumber = prefs.getString(key,null);
            PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_SECURITY_FRAGMENT);
            if (fragment != null && phoneNumber != null && !phoneNumber.isEmpty()) {
                Preference hideAppNumber = fragment.findPreference(key);
                if (hideAppNumber != null) {
                    hideAppNumber.setSummary(phoneNumber);
                }
            }else if ( fragment != null){
                fragment.findPreference(key).setSummary(R.string.number_not_set);
            }
        } else if (key.equals(getString(R.string.key_pref_allow_boot_notification))) {
            boolean bootNotification = prefs.getBoolean(key,false);
            savePreference(String.valueOf(bootNotification),
                    getString(R.string.key_pref_allow_boot_notification_encrypted));
        }else if (key.equals(getString(R.string.key_pref_allow_disable_silent))) {
            boolean disableSilent = prefs.getBoolean(key,false);
            if (disableSilent){
                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !notificationManager.isNotificationPolicyAccessGranted()) {
                    if (Build.MANUFACTURER.contains("LG") && Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
                        String notificationListenerString = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
                        if (notificationListenerString == null || !notificationListenerString.contains(getPackageName())) {
                            updateCorrespondingCheckbox(false,getString(R.string.key_pref_allow_disable_silent));
                            savePreference(String.valueOf(false),getString(R.string.key_pref_allow_disable_silent_encrypted));
                            new AlertDialog.Builder(this)
                                    .setTitle(R.string.needs_bind_notification_access)
                                    .setMessage(R.string.needs_bind_notification_access_message)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent requestIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                            startActivity(requestIntent);
                                        }
                                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                        }
                    }else {
                        Intent intent = new Intent(
                                android.provider.Settings
                                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivityForResult(intent, 100);
                    }
                }else {
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_disable_silent_encrypted));
                }
            }else {
                savePreference(String.valueOf(false),getString(R.string.key_pref_allow_disable_silent_encrypted));
            }
        }else if (key.equals(getString(R.string.key_pref_disable_notifications))) {
            boolean suppressNotifications = prefs.getBoolean(key,false);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT && suppressNotifications) {
                Toast.makeText(this, R.string.no_need_on_this_version_of_android, Toast.LENGTH_SHORT).show();
                PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_SECURITY_FRAGMENT);
                CheckBoxPreference disableNotificationPref = (CheckBoxPreference) fragment.findPreference(key);
                if (disableNotificationPref != null) {
                   disableNotificationPref.setChecked(false);
                }
                return;
            }
            if (suppressNotifications) {
                String notificationListenerString = Settings.Secure.getString(this.getContentResolver(), "enabled_notification_listeners");
                if (notificationListenerString == null || !notificationListenerString.contains(getPackageName())) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.needs_bind_notification_access)
                            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent requestIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                    startActivity(requestIntent);
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_SECURITY_FRAGMENT);
                                    ((CheckBoxPreference)fragment.findPreference(key)).setChecked(false);
                                }
                    }).show();
                }
            }
        }
    }

    private void enableAppIcon() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(getPackageName(), LauncherActivity.class.getName());
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void disableAppIcon() {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(getPackageName(), LauncherActivity.class.getName());
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

    private boolean isPostMarshMellow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
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
                                Toast.makeText(SettingsActivity.this, R.string.install_dropbox, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_taking_pictures_encrypted));
                    updateCorrespondingCheckbox(false,getString(R.string.key_pref_allow_taking_pictures));
                }
            }).show();
        }
    }

    private boolean isDropboxInstalled() {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo("com.dropbox.android", PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return false;
    }


    public void savePreference(String value, String key) {
        if (prefs != null) {
            prefs.unregisterOnSharedPreferenceChangeListener(listener);
        }
        Security.encryptPreferencesEntry(this,value , key);
        if (prefs != null) {
            prefs.registerOnSharedPreferenceChangeListener(listener);
        }
    }

    private void showEnableAdminDialog() {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.enable_device_admin)
                    .setMessage(R.string.enable_device_admin_explanation)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ComponentName deviceAdmin = new ComponentName(SettingsActivity.this, WipeDataReceiver.class);
                            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdmin);
                            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Process will remove user installed applications," +
                                    " settings, wallpaper and sound settings. Are you sure you want to wipe device?");
                            startActivityForResult(intent, DEVICE_ADMIN_REQUEST_CODE);


                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            savePreference(String.valueOf(false),getString(R.string.key_pref_allow_wipe_encrypted));
                            updateCorrespondingCheckbox(false, getString(R.string.key_pref_allow_wipe));
                            updateCorrespondingCheckbox(false, getString(R.string.key_pref_allow_lock_device));
                        }
                    })
                    .create();
            dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0 ){
            super.onBackPressed();
        } else {
            setResult(RESULT_OK);
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case DEVICE_ADMIN_REQUEST_CODE:
                String key = getString(R.string.key_pref_allow_lock_device);
                String keyEncrypted = getString(R.string.key_pref_allow_lock_device_encrypted);
                if (adminRequestIsForWipe){
                    key = getString(R.string.key_pref_allow_wipe);
                    keyEncrypted = getString(R.string.key_pref_allow_wipe_encrypted);
                }
                if (resultCode == RESULT_OK) {
                    savePreference(String.valueOf(true),keyEncrypted);
                    Toast.makeText(this, R.string.wipe_enabled, Toast.LENGTH_SHORT).show();
                    updateCorrespondingCheckbox(true, key);
                }else {
                    savePreference(String.valueOf(false),keyEncrypted);
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                    updateCorrespondingCheckbox(false, key);
                }
                break;
        }
    }

    private void updateCorrespondingCheckbox(boolean checked, String key) {
        PreferenceFragment fragment = (PreferenceFragment) getFragmentManager().findFragmentByTag(TAG_TASKS_FRAGMENT);
        ((CheckBoxPreference)fragment.findPreference(key)).setChecked(checked);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RC_PERMISSION_LOCATION:
                if (permissionsGranted(grantResults)){
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_location_encrypted));
                    updateCorrespondingCheckbox(true, getString(R.string.key_pref_allow_location));
                }else {
                    savePreference(String.valueOf(false),getString(R.string.key_pref_allow_location_encrypted));
                    updateCorrespondingCheckbox(false,getString(R.string.key_pref_allow_location));
                }
                break;
            case RC_PERMISSION_CAMERA:
                if (permissionsGranted(grantResults)) {
                    savePreference(String.valueOf(true),getString(R.string.key_pref_allow_taking_pictures_encrypted));
                    startDropBoxAuthentication();
                }else {
                    savePreference(String.valueOf(false),getString(R.string.key_pref_allow_taking_pictures_encrypted));
                    updateCorrespondingCheckbox(false,getString(R.string.key_pref_allow_taking_pictures));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String pass = prefs.getString(getString(R.string.key_pref_user_password),null);
        if (pass == null){
            prefs.edit().putBoolean(getString(R.string.key_pref_is_first_time),true).commit();
        }
    }
}