package ir.the_moment.carmichael_sms.utility;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import ir.the_moment.carmichael_sms.TaskExecutorService;
import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.Message;

/**
 * Created by vaas on 4/1/2017.
 */

public class SetAlarm {
    private static final String TAG = "alarm";

    public static void set(Context context,Message message,int requestCode,long interval){
        set(context,message,requestCode,interval,false);
    }

    public static void set(Context context,Message message,int requestCode,long interval,boolean disableLastOnes){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskExecutorService.class);

        intent.putExtra(mR.KEY_MESSAGES,message);
        PendingIntent pendingIntent = PendingIntent.getService(context,requestCode,intent,0);
        if (disableLastOnes) {
            alarmManager.cancel(pendingIntent);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + interval,pendingIntent);
        }else {
            alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis() + interval,pendingIntent);
        }
    }

    public static void cancel(Context context,int requestCode){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TaskExecutorService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context,requestCode,intent,0);
        alarmManager.cancel(pendingIntent);
    }
}
