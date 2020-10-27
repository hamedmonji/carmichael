package ir.the_moment.carmichael_sms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;

public class LauncherActivity extends AppCompatActivity {

    private static boolean finish = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent signingIntent=  new Intent(this, SigningActivity.class);
        signingIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(signingIntent);
        finish = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (finish) onBackPressed();
    }
}
