package ir.the_moment.carmichael_sms;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.bumptech.glide.Glide;

import java.lang.reflect.Field;

import ir.the_moment.carmichael_sms.database.DeviceInfoContract;
import ir.the_moment.carmichael_sms.tasks.location.GetLocationService;
import ir.the_moment.carmichael_sms.ui.BaseActivity;
import ir.the_moment.carmichael_sms.ui.manageDevices.DevicesFragment;
import ir.the_moment.carmichael_sms.ui.manageDevices.DevicesFragmentStatePagerAdapter;
import ir.the_moment.carmichael_sms.ui.securityReport.SecurityReportActivity;
import ir.the_moment.carmichael_sms.ui.settings.SettingsActivity;
import ir.the_moment.carmichael_sms.utility.Security;
import ir.the_moment.carmichael_sms.utility.Utility;
import uk.co.deanwild.materialshowcaseview.IShowcaseListener;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MainActivity extends BaseActivity {
    private static final String KEY_PREF_IS_APP_UNLOCKED = "key_pref_is_app_unlocked";
    private static final int RC_LOCK = 1001;
    private static final int RC_PERMANENT_LOCK = 1002;

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    private ImageView userPhoto;
    private ImageView userBackground;
    private TextView userName;

    private ViewPager devicesPager;
    private TabLayout tabLayout;
    private DevicesFragmentStatePagerAdapter pagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindDrawer();

        setUpSearchView();

        enablePermanentMenuKey();

        setNavDrawer();
        bindDevicesPager();

    }

    private void setUpSearchView() {
        final FloatingSearchView searchView = findViewById(R.id.floating_search_view);
        searchView.setLeftActionMode(FloatingSearchView.LEFT_ACTION_MODE_SHOW_HAMBURGER);
        searchView.setSearchBarTitle(getString(R.string.search));
        searchView.attachNavigationDrawerToMenuButton(drawerLayout);
        searchView.setCloseSearchOnKeyboardDismiss(true);
        searchView.inflateOverflowMenu(R.menu.menu_main);
        searchView.setDismissOnOutsideClick(true);
        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {

            }

            @Override
            public void onFocusCleared() {
                searchView.setSearchBarTitle(getString(R.string.search));
                String selection = DeviceInfoContract.DeviceInfo.TYPE + "=?";
                String[] selectionArgs = new String[]{Security.encrypt(MainActivity.this, String.valueOf(devicesPager.getCurrentItem()))};
                getCurrentFragment().reQuery(selection,selectionArgs);
            }
        });

        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                int id = item.getItemId();
                switch (id){
                    case R.id.set_as_found:
                        boolean devicesIsLost =
                                Security.getDecryptedBooleanPreferenceEntry(MainActivity.this,getString(R.string.key_pref_is_device_lost));
                        if (devicesIsLost) {
                            Security.encryptPreferencesEntry(MainActivity.this,String.valueOf(false),getString(R.string.key_pref_is_device_lost));
                            devicesPager.setBackgroundColor(Utility.getCorrectColor(MainActivity.this,R.color.white));
                            tabLayout.setBackgroundColor(Utility.getCorrectColor(MainActivity.this,R.color.colorPrimary));
                            tabLayout.setSelectedTabIndicatorColor(Utility.getCorrectColor(MainActivity.this,R.color.red_cherry));
                            FrameLayout searchViewBackground = findViewById(R.id.search_view_background);
                            searchViewBackground.setBackgroundColor(Utility.getCorrectColor(MainActivity.this,R.color.colorPrimary));
                            Toast.makeText(MainActivity.this, R.string.device_was_set_as_found, Toast.LENGTH_SHORT).show();
                            // a sign of my paranoia
                            GetLocationService.isRunning = false;

                        }else {
                            Toast.makeText(MainActivity.this, R.string.device_is_not_lost, Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        });

        searchView.setCloseSearchOnKeyboardDismiss(true);

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                String selection;
                String[] selectionArgs = new String[2];
                if (newQuery.isEmpty()) {
                    selection = DeviceInfoContract.DeviceInfo.TYPE + "=?";
                    selectionArgs = new String[]{Security.encrypt(MainActivity.this, String.valueOf(devicesPager.getCurrentItem()))};
                }else {
                    try {
                        Integer.parseInt(newQuery);
                        selection = DeviceInfoContract.DeviceInfo.NUMBER + " LIKE ?";
                    } catch (NumberFormatException e) {
                        selection = DeviceInfoContract.DeviceInfo.NAME + " LIKE ?";
                    }
                    selectionArgs[0] = "%" + Security.encrypt(MainActivity.this,newQuery) + "%";
                    selectionArgs[1] = Security.encrypt(MainActivity.this,String.valueOf(devicesPager.getCurrentItem()));
                    selection += " AND " + DeviceInfoContract.DeviceInfo.TYPE + "=?";
                }
                getCurrentFragment().reQuery(selection,selectionArgs);
            }
        });
    }


    private void bindDrawer() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigation_drawer);
        View navHeader = navigationView.getHeaderView(0);
        userPhoto = navHeader.findViewById(R.id.userPhoto);
        userBackground = navHeader.findViewById(R.id.userBackground);
        userName = navHeader.findViewById(R.id.name);
    }

    private void bindDevicesPager() {
        devicesPager = findViewById(R.id.devices_pager);
        pagerAdapter = new DevicesFragmentStatePagerAdapter(getSupportFragmentManager(),this);
        devicesPager.setAdapter(pagerAdapter);
        tabLayout = findViewById(R.id.devices_sliding_tabs);

        tabLayout.setupWithViewPager(devicesPager);
        boolean devicesIsLost = Security.getDecryptedBooleanPreferenceEntry(this,getString(R.string.key_pref_is_device_lost));
        if (devicesIsLost) {
            FrameLayout searchViewBackground = findViewById(R.id.search_view_background);
            searchViewBackground.setBackgroundColor(Utility.getCorrectColor(this,R.color.red_cherry));
            devicesPager.setBackgroundColor(Utility.getCorrectColor(this,R.color.dark_grey_color));
            tabLayout.setBackgroundColor(Utility.getCorrectColor(this,R.color.red_cherry));
            tabLayout.setSelectedTabIndicatorColor(Utility.getCorrectColor(this,R.color.colorPrimary));
        }

        new MaterialShowcaseView.Builder(this)
                .setTarget(tabLayout)
                .setDismissText(R.string.ok)
                .setContentText(R.string.handler_and_assets_explain)
                .setDelay(500)
                .singleUse("showcaseId")
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView materialShowcaseView) {

                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView materialShowcaseView) {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.lottery)
                                .setMessage(R.string.lottary_message)
                                .setPositiveButton(R.string.visit_website, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent lotteryIntent = new Intent(Intent.ACTION_VIEW);
                                        lotteryIntent.setData(Uri.parse("http://the-moment.ir/applications/carmichael-support/lottery/"));
                                        startActivity(lotteryIntent);
                                    }
                                }).setNegativeButton(R.string.cancel,null)
                                .show();
                    }
                })
                .show();
    }

    private DevicesFragment getCurrentFragment(){
        return  pagerAdapter.getFragmentAt(devicesPager.getCurrentItem());
    }

    private void enablePermanentMenuKey() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
            try {
                ViewConfiguration config = ViewConfiguration.get(this);
                Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");

                if (menuKeyField != null) {
                    menuKeyField.setAccessible(true);
                    menuKeyField.setBoolean(config, false);
                }
            }
            catch (Exception ignored) {

            }
        }
    }

    private void setNavDrawer() {
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectDrawerItem(item);
                return true;
            }
        });

        setProfileImages();
    }

    /**
     * replaces current fragment or starts another activity base on the item that was selected.
     * @param item that was selected in the navigation drawer.
     */
    public void selectDrawerItem(MenuItem item) {
        drawerLayout.closeDrawers();
        if (!item.isChecked()) {
            switch (item.getItemId()) {
                case R.id.nav_security_alerts:
                    Intent securityAlertsIntent = new Intent(this, SecurityReportActivity.class);
                    startActivity(securityAlertsIntent);
                    break;
                case R.id.nav_settings:
                    startSettingsActivity();
                    break;
                case R.id.about:
                    Intent about = new Intent(this, AboutActivity.class);
                    startActivity(about);
                    break;
            }

            item.setChecked(true);

            setTitle(item.getTitle());
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (navigationView != null) {
            navigationView.getMenu().getItem(0).setChecked(true);
            for (int i = 1; i < navigationView.getMenu().size(); i++) {
                navigationView.getMenu().getItem(i).setChecked(false);
            }
        }
    }

    private void setProfileImages() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString(getString(R.string.key_pref_user_name),"not set yet");
        userName.setText(name);

        setNaveHeaderListeners(prefs);

        String profileImagePath = prefs.getString(getString(R.string.key_pref_user_photo),null);
        String backgroundImagePath = prefs.getString(getString(R.string.key_pref_user_background_image),null);

        if (backgroundImagePath != null){
            Glide.with(this).load(backgroundImagePath).into(userBackground);
        }

        if (profileImagePath != null) {
            userPhoto.setImageURI(Uri.parse(profileImagePath));
        }else {
            TextView userPhotoText = navigationView.getHeaderView(0).findViewById(R.id.user_photo_text);
            userPhotoText.setVisibility(View.VISIBLE);
            userPhotoText.setText(name.substring(0,1).toUpperCase());
        }
    }

    private void setNaveHeaderListeners(final SharedPreferences prefs) {

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptToSelectImage(new OnImageSelected() {
                    @Override
                    public void onSelected(String imagePath) {
                        prefs.edit().putString(getString(R.string.key_pref_user_photo),imagePath).commit();
                        userPhoto.setImageURI(Uri.parse(imagePath));
                        navigationView.getHeaderView(0).findViewById(R.id.user_photo_text).setVisibility(View.GONE);
                    }
                }, true,getString(R.string.change_profile_image_title));
            }
        });

        userBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptToSelectImage(new OnImageSelected() {
                    @Override
                    public void onSelected(String imagePath) {
                        prefs.edit().putString(getString(R.string.key_pref_user_background_image),imagePath).commit();
                        Glide.with(MainActivity.this).load(imagePath).into(userBackground);
                    }
                },false,getString(R.string.change_background_image));
            }
        });

        userBackground.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String backgroundImagePath = prefs.getString(getString(R.string.key_pref_user_background_image),null);
                if (backgroundImagePath != null) {
                    showAlertToSetBackgroundToItsDefault(userBackground);
                }
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode){
            case RC_LOCK:
                if (resultCode == RESULT_OK){
                    disableLock();
                }else {
                    finish();
                }
                break;
            case RC_PERMANENT_LOCK:
                finish();
                break;
        }
    }

    private void disableLock() {
        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putBoolean(KEY_PREF_IS_APP_UNLOCKED,true).commit();
    }

    private void startSettingsActivity() {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)){
            drawerLayout.closeDrawer(Gravity.START);
        }else {
            super.onBackPressed();
        }
    }
}