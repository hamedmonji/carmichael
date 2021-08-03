package ir.the_moment.carmichael_sms.fileManager.base;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ir.the_moment.carmichael_sms.fileManager.base.callbacks.FileManagerActivityCallbacks;
import ir.the_moment.carmichael_sms.R;

public abstract class FileManagerActivity extends AppCompatActivity implements FileManagerActivityCallbacks {

    protected FileManagerAdapter adapter;
    protected RecyclerView fileList;

    // list of files to be downloaded
    protected List<File> downloadList = new ArrayList<>();
    // list of files to be deleted
    protected List<File> deleteList = new ArrayList<>();
    // list of files to be secured
    protected List<File> secureList = new ArrayList<>();
    // list of files to be hidden
    protected List<File> hideList = new ArrayList<>();
    // list of messages to be sent

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        enablePermanentMenuKey();
        fileList = (RecyclerView) findViewById(R.id.file_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        fileList.setLayoutManager(layoutManager);
        initAdapter();
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
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_file_manager,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onFinished();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (adapter.isAtRoot()){
            super.onBackPressed();
        }else {
            adapter.onBackPressed();
        }
    }
}
