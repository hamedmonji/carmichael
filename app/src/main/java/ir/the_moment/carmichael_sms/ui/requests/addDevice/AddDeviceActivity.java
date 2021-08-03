package ir.the_moment.carmichael_sms.ui.requests.addDevice;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.database.DeviceInfoDbHelper;

import static android.provider.ContactsContract.CommonDataKinds.Phone;

public class AddDeviceActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, FloatingSearchView.OnMenuItemClickListener {
    public static final int RP_READ_CONTACTS = 2001;
    private static final int LOADER_MANAGER_ID = 2002;
    public static final String CONTACT_URI = "contact_uri";

    private ListView contacts;
    private ContactsCursorAdapter adapter;
    private FloatingSearchView searchView;
    private static int CONTACT_ID_INDEX;
    private String selection;
    private String[] selectionArgs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        searchView = (FloatingSearchView) findViewById(R.id.floating_search_view);

        searchView.inflateOverflowMenu(R.menu.contacts);
        searchView.setLeftActionMode(FloatingSearchView.LEFT_ACTION_MODE_SHOW_HOME);
        searchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                onBackPressed();
            }
        });

        searchView.setOnMenuItemClickListener(this);
        setFloatingSearchListeners();
        contacts = (ListView) findViewById(R.id.contacts_list);
        contacts.setOnItemClickListener(this);

        adapter = new ContactsCursorAdapter(this,null);
        contacts.setAdapter(adapter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED ) {
                getLoaderManager().initLoader(LOADER_MANAGER_ID,null,this);
            }else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},RP_READ_CONTACTS);
            }
        }else {
            getLoaderManager().initLoader(LOADER_MANAGER_ID,null,this);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        searchView.clearQuery();
    }

    private void setFloatingSearchListeners() {
        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (newQuery.isEmpty()){
                    selection = null;
                    selectionArgs = null;
                }else {
                    try {
                        Integer.parseInt(newQuery);
                        selection = Phone.NUMBER + " LIKE ?";
                        selectionArgs = new String[]{"%" + newQuery + "%"};
                    }catch (NumberFormatException e){
                        selection = Phone.DISPLAY_NAME + " LIKE ?";
                        selectionArgs = new String[]{"%" + newQuery + "%"};
                    }
                }
                String[] projection = new String[]{Phone._ID,Phone.DISPLAY_NAME,Phone.PHOTO_URI,Phone.NUMBER};
                Cursor cursor = getContentResolver().query(Phone.CONTENT_URI,projection,selection,selectionArgs,Phone.DISPLAY_NAME + " ASC");
                adapter.swapCursor(cursor);
            }
        });
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{Phone._ID,Phone.DISPLAY_NAME,Phone.PHOTO_URI,Phone.NUMBER};
        return new CursorLoader(this, Phone.CONTENT_URI,projection ,selection,selectionArgs,Phone.DISPLAY_NAME + " ASC");
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        CONTACT_ID_INDEX = data.getColumnIndex(ContactsContract.Contacts._ID);
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

        if (DeviceInfoDbHelper.getDeviceByNumber(this,phoneNumber, mR.TYPE_ASSET) != null) {
            Toast.makeText(this, R.string.device_already_added, Toast.LENGTH_SHORT).show();
        }else {

            long contactID = cursor.getLong(CONTACT_ID_INDEX);

            Intent requestAddIntent = new Intent(this, RequestAddingDeviceActivity.class);
            requestAddIntent.putExtra(CONTACT_URI, contactID);
            requestAddIntent.putExtra(mR.KEY_POSITION,position);
            startActivity(requestAddIntent);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_device,menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RP_READ_CONTACTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLoaderManager().initLoader(LOADER_MANAGER_ID, null, this);
                }else {
                    Toast.makeText(this, R.string.cant_continue_with_out_permissions, Toast.LENGTH_SHORT).show();
                    requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},RP_READ_CONTACTS);
                }
                break;
        }
    }

    @Override
    public void onActionMenuItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.add:
                Intent requestAddIntent = new Intent(this,RequestAddingDeviceActivity.class);
                startActivity(requestAddIntent);
                break;
        }
    }
}
