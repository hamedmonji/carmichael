package ir.the_moment.carmichael_sms.ui.lock;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import ir.the_moment.carmichael_sms.R;

public class PermanentLockActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permanent_lock);
    }

    @Override
    public void onBackPressed() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_DEFAULT);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}
