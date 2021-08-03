package ir.the_moment.carmichael_sms.tasks.takePictures;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ir.the_moment.carmichael_sms.mR;
import ir.the_moment.carmichael_sms.R;

public class CapturePictureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_picture);
        takeSnapShots();
    }

    private void takeSnapShots() {
        SurfaceView surface = new SurfaceView(this);
        int frontFacingCameraId = getFrontFacingId();

        if (frontFacingCameraId != -1) {
            Camera camera = Camera.open(frontFacingCameraId);
            try {
                camera.setPreviewDisplay(surface.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    FileOutputStream outStream = null;
                    try {
                        File dir_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        File saveFolder  = new File(dir_path + "/Carmichael");
                        if (!saveFolder.exists()) {
                            saveFolder.mkdirs();
                        }
                        String imagePath = dir_path + "/Carmichael/" + System.currentTimeMillis() + ".jpg";
                        outStream = new FileOutputStream(imagePath);
                        outStream.write(data);
                        outStream.close();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(imagePath)));
                        Log.i(mR.TAG, "onPictureTaken: ");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        Log.i(mR.TAG, "onPictureTaken: finally");
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            });
        }
    }

    private int getFrontFacingId() {

        int cameraId = 0;
        int cameraCounts = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraInfoId = 0; cameraInfoId < cameraCounts; cameraInfoId++) {
            Camera.getCameraInfo(cameraInfoId, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = cameraInfoId;
            }

        }
        return cameraId;
    }
}
