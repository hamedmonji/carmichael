package ir.the_moment.carmichael_sms.ui.securityReport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.SecurityAlert;
import ir.the_moment.carmichael_sms.utility.Utility;

/**
 * shows the list of the alerts.
 */
public class SecurityReportActivity extends AppCompatActivity {

    private SecurityAlertsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_report);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.menu_title_security_alerts);
        toolbar.setTitleTextColor(Utility.getCorrectColor(this,R.color.white));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        RecyclerView alerts = findViewById(R.id.alerts);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        initAdapter();
        alerts.setAdapter(adapter);
        alerts.setLayoutManager(layoutManager);
    }

    private void initAdapter() {
        ArrayList<SecurityAlert> lowAlerts = new ArrayList<>();
        ArrayList<SecurityAlert> mediumAlerts = new ArrayList<>();
        ArrayList<SecurityAlert> highAlerts = new ArrayList<>();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String rawLowAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_LOW,null);
        if (rawLowAlerts != null) {
            lowAlerts.addAll(SecurityAlert.createAlerts(rawLowAlerts));
        }

        String rawMediumAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_MEDIUM,null);
        if (rawMediumAlerts != null) {
            mediumAlerts.addAll(SecurityAlert.createAlerts(rawMediumAlerts));
        }

        String rawHighAlerts = prefs.getString(mR.KEY_PREF_SECURITY_ALERTS_HIGH,null);
        if (rawHighAlerts != null) {
            highAlerts.addAll(SecurityAlert.createAlerts(rawHighAlerts));
        }
        adapter = new SecurityAlertsAdapter(lowAlerts,mediumAlerts,highAlerts,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.security_reports,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear_all:
                adapter.clearAll();
                break;
        }
        return true;
    }
}