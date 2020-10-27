//package yellowumbrella.workingtitle.tasks.takePictures;
//
//import android.hardware.Sensor;
//import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;
//import android.util.Log;
//
//import yellowumbrella.workingtitle.mR;
//import yellowumbrella.workingtitle.tasks.Task;
//import yellowumbrella.workingtitle.utility.Utility;
//
//import static android.content.Context.SENSOR_SERVICE;
//
///**
// * Created by vaas on 4/23/17.
// * takes a picture when ever the phones angle changes to a specific angle.
// */
//
//public class SmartSnap extends Task {
//    public static final String action = "takePictures.SmartSnap";
//
//    @Override
//    protected void action() {
//        getAngle();
//    }
//    private boolean isTaking = true;
//    private void getAngle() {
//        SensorManager sensorManager = (SensorManager)context.getSystemService(SENSOR_SERVICE);
//
//        final SensorEventListener sensorListener = new SensorEventListener() {
//            @Override
//            public void onSensorChanged(SensorEvent mSensorEvent) {
//                float X_Axis = mSensorEvent.values[0];
//                float Y_Axis = mSensorEvent.values[1];
//                double angle = Math.atan2(X_Axis, Y_Axis)/(Math.PI/180);
//
//                if (angle > -5 && angle < -2 || angle > 2 && angle < 5 && Math.abs(angle) != 0 && isTaking){
//                    isTaking = false;
//                    Utility.takeSnapShots(getContext());
//                    Log.i("test", "onSensorChanged: profile_image taken");
//                }
//
//            }
//
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            }
//        };
//        sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(
//                Sensor.TYPE_ACCELEROMETER),1000000);
//
//    }
//
//    @Override
//    protected void parseData() {
//
//    }
//
//    @Override
//    protected void parseFlags() {
//
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//
//    @Override
//    public int getPermission() {
//        return mR.Permissions.CAPTURE_PICTURES;
//    }
//}
