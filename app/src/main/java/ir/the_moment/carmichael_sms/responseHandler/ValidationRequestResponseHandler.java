package ir.the_moment.carmichael_sms.responseHandler;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.ui.auth.SigningActivity;
import ir.the_moment.carmichael_sms.R;
import ir.the_moment.carmichael_sms.tasks.requests.validationRequest.ValidationRequest;

/**
 * Created by vaas on 6/30/17.
 */

public class ValidationRequestResponseHandler extends ResponseHandler {

    private static final int NOTIFICATION_ID_VALIDATION = 3701;
    private static final int RC_VALIDATION = 3402;

    @Override
    protected void handle() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String code = getResponse().getExtra(ValidationRequest.DATA_KEY_LICENCE_CODE);
        prefs.edit().putString(getContext().getString(R.string.key_pref_received_validation_code),code).apply();
        Intent validationIntent = new Intent(getContext(),SigningActivity.class);
        validationIntent.putExtra(mR.KEY_MESSAGES,getResponse());
        PendingIntent validationPending =
                PendingIntent.getActivity(getContext(),RC_VALIDATION,validationIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        showNotification(validationPending,getContext().getString(R.string.validation_code_received),NOTIFICATION_ID_VALIDATION,R.drawable.license);
    }
}
