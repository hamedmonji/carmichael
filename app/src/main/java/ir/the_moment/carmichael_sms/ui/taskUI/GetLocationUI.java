package ir.the_moment.carmichael_sms.ui.taskUI;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import ir.the_moment.carmichael_sms.Message;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.tasks.location.GetLocation;
import ir.the_moment.carmichael_sms.ui.location.SavedLocations;
import ir.the_moment.carmichael_sms.ui.taskUI.base.TaskUI;

/**
 * Created by vaas on 6/14/17.
 */

public class GetLocationUI extends TaskUI {
    @Override
    protected View constructView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.get_location_options,null);

        Spinner updateFrequency = view.findViewById(R.id.update_frequency);
        updateFrequency.setAdapter(getAdapter(R.array.location_update_frequency));
        final LinearLayout locationIntervalContainer = view.findViewById(R.id.request_location_container);
        updateFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    locationIntervalContainer.setVisibility(View.GONE);
                }else {
                    locationIntervalContainer.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Spinner interval = view.findViewById(R.id.interval);
        interval.setAdapter(getAdapter(R.array.location_interval));

        view.findViewById(R.id.show_location_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getContext().getSharedPreferences(mR.CurrentDevice.number,Context.MODE_PRIVATE);
                String allLocations = prefs.getString("all_coordinates",null);
                if (allLocations == null) {
                    Toast.makeText(getContext(), R.string.no_saved_location, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent locationHistoryIntent = new Intent(getContext(), SavedLocations.class);
                getContext().startActivity(locationHistoryIntent);
            }
        });

        return view;
    }

    @Override
    protected Message constructMessage(View view) {
        if (view != null){
            Spinner updateFrequency = view.findViewById(R.id.update_frequency);
            String update = String.valueOf(updateFrequency.getSelectedItemPosition());

            Spinner interval = view.findViewById(R.id.interval);
            String intervalValue = String.valueOf(interval.getSelectedItemPosition());

            Message message = new Message();
            message.action = GetLocation.action;
            if (updateFrequency.getSelectedItemPosition() != 0) {
                message.addFlag(GetLocation.FLAG_REQUEST_LOCATION_PERIODICALLY);

                message.putExtra(GetLocation.DATA_KEY_SEND_RESPONSE_INTERVAL,update);
                message.putExtra(GetLocation.DATA_KEY_REQUEST_LOCATION_INTERVAL,intervalValue);
            }

            return message;
        }

        return null;
    }

    private ArrayAdapter<String> getAdapter(int id) {
        return new ArrayAdapter<>(getContext(),android.R.layout.simple_spinner_item,
                getContext().getResources().getStringArray(id));
    }

    @Override
    public void show() {
        SharedPreferences prefs = getContext().getSharedPreferences(mR.CurrentDevice.number,Context.MODE_PRIVATE);
        boolean isLocationSent = prefs.getBoolean(getContext().getString(R.string.key_pref_is_location_request_sent),false);
        if (isLocationSent) {
            CharSequence[] items = {getContext().getString(R.string.disable),getContext().getString(R.string.reconfigure)
                    ,getContext().getString(R.string.cancel)};
            new android.support.v7.app.AlertDialog.Builder(getContext())
                    .setTitle(R.string.location_already_sent_title)
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0 :
                                    Message message = new Message();
                                    message.action = GetLocation.action;
                                    message.type = Message.Type.command;
                                    message.addFlag(GetLocation.FLAG_DISABLE);
                                    onSendClicked(message);
                                    break;
                                case 1:
                                    GetLocationUI.super.show();
                                    break;
                            }
                        }
                    }).show();
        }else {
            super.show();
        }
    }

    @Override
    public void onSendClicked(Message message) {
        super.onSendClicked(message);
        SharedPreferences prefs = getContext().getSharedPreferences(mR.CurrentDevice.number, Context.MODE_PRIVATE);
        String isLocationSentKey = getContext().getString(R.string.key_pref_is_location_request_sent);
        if (message.hasFlag(GetLocation.FLAG_DISABLE) || !message.hasFlag(GetLocation.FLAG_REQUEST_LOCATION_PERIODICALLY)) {
            prefs.edit().putBoolean(isLocationSentKey,false).commit();
        }else {
            prefs.edit().putBoolean(isLocationSentKey,true).commit();
        }
    }
}
