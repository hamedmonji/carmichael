//package yellowumbrella.workingtitle.tasks.takePictures;
//
//import android.content.Context;
//import android.util.Log;
//
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfRect;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.objdetect.CascadeClassifier;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.util.ArrayList;
//
//import yellowumbrella.workingtitle.Message;
//import yellowumbrella.workingtitle.R;
//import yellowumbrella.workingtitle.mR;
//import yellowumbrella.workingtitle.tasks.Task;
//import yellowumbrella.workingtitle.fileManager.FileUtility;
//
///**
// * Created by vaas on 4/28/17.
// */
//
//public class FaceDetection extends Task{
//    public static final String action = "takePictures.FaceDetection";
//    public static final String TAG = "faceDetection";
//    public static final String DATA_KEY_FILE_PATHS = "file_paths";
//    private String filePaths;
//    private ArrayList<File> files = new ArrayList<>();
//    private ArrayList<File> selectedFiles = new ArrayList<>();
//    private CascadeClassifier cascadeClassifier;
//
//
//
//    @Override
//    protected void action() {
//        parseData();
//        getFiles();
//        try {
//            // Copy the resource into a temp file so OpenCV can load it
//            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalcatface);
//            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
//            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalcatface.xml");
//            FileOutputStream os = new FileOutputStream(mCascadeFile);
//
//
//            byte[] buffer = new byte[4096];
//            int bytesRead;
//            while ((bytesRead = is.read(buffer)) != -1) {
//                os.write(buffer, 0, bytesRead);
//            }
//            is.close();
//            os.close();
//
//            // Load the cascade classifier
//            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
//
//            for (File file :
//                    files) {
//                if (detectFaces(file)){
//                    selectedFiles.add(file);
//                    Log.i(TAG, "action: " + file.getName());
//                }
//            }
//            sendRespond();
//
//        } catch (Exception e) {
//            Log.e(TAG, "Error loading cascade", e);
//        }
//    }
//
//    private void sendRespond() {
//        respond = new Message();
//        respond.putExtra(DATA_KEY_FILE_PATHS,FileUtility.getFilePaths(selectedFiles));
//        onActionFinished(true);
//    }
//
//    private boolean detectFaces(File file) {
//        Mat image = Imgcodecs.imread(file.getAbsolutePath(),Imgcodecs.CV_LOAD_IMAGE_COLOR);
//        MatOfRect detectedFaces = new MatOfRect();
//        cascadeClassifier.detectMultiScale(image,detectedFaces);
//
//        int size = detectedFaces.toArray().length;
//        Log.i(TAG, "detectFaces: size is " + size);
//
//        return size > 0;
//    }
//
//    private void getFiles() {
//        files = FileUtility.extractFiles(filePaths);
//    }
//
//    @Override
//    protected void parseData() {
//        filePaths = getMessage().getExtra(DATA_KEY_FILE_PATHS);
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
