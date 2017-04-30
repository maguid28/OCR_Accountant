package com.finalyearproject.dan.ocraccountingapp.camera;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.finalyearproject.dan.ocraccountingapp.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.rectangle;

public class OpenCVCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final static String TAG = "OpenCVCamera";

    // marked as true when the image processing task is in operation
    public static boolean isProcessTaskRunning = false;

    protected static final int REQUEST_PREVIEW_CODE = 1001;

    Bitmap imageBitmap = null;
    Point p1,p2,p3,p4;
    List<Point> source = new ArrayList<>();

    ProgressDialog mProgressDialog;

    private Mat mRgba;
    private Mat mIntermediateMat;
    private Mat mGray;

    private JavaCameraView mOpenCvCameraView;
    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("TAG", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,
                    this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap,"asdasdasd" , "Asd");
        }
    }


    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                return true;
            } else {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.camera_surface_view);
        mOpenCvCameraView.setVisibility(View.VISIBLE);

        mOpenCvCameraView.setMaxFrameSize(4000,4000);
        mOpenCvCameraView.setMinimumHeight(1920);
        mOpenCvCameraView.setMinimumHeight(1080);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mProgressDialog = new ProgressDialog(this);

        isStoragePermissionGranted();

        ImageButton capture = (ImageButton) findViewById(R.id.captureBTN);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // hide the cameraview
                ProcessImageTask processImageTask = new ProcessImageTask();
                processImageTask.execute();


                mOpenCvCameraView.disableView();

            }
        });
    }



    public Mat removeArtifacts(Mat srcImage){

        Mat rgbImg = new Mat();


        Size sz = new Size((srcImage.width() * 40) / 100, (srcImage.height() * 40) / 100);
        Imgproc.resize(srcImage, rgbImg, sz);


        Mat small = new Mat();

        Imgproc.cvtColor(rgbImg, small, Imgproc.COLOR_RGB2GRAY);

        Mat grad = new Mat();

        Mat morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3,3));

        Imgproc.morphologyEx(small, grad, Imgproc.MORPH_GRADIENT , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check1.jpg", grad);



        Mat bw = new Mat();

        //Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        Imgproc.threshold(grad, bw, 0.0, 255.0, Imgproc.THRESH_OTSU);

        Mat connected = new Mat();

        //Imgcodecs.imwrite(pathname + "_check1_1.jpg", bw);

        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(13,1));

        Imgproc.morphologyEx(bw, connected, Imgproc.MORPH_CLOSE  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check2.jpg", connected);


        morphKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(7,1));

        Imgproc.morphologyEx(connected, connected, Imgproc.MORPH_OPEN  , morphKernel);

        //Imgcodecs.imwrite(pathname + "_check3.jpg", connected);




        Mat mask2 = Mat.zeros(bw.size(), CvType.CV_8UC1);


        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(connected, contours, new Mat(), Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        Mat mask = Mat.zeros(bw.size(), CvType.CV_8UC1);
        for(int idx = 0; idx < contours.size(); idx++) {

            Rect rect = Imgproc.boundingRect(contours.get(idx));

            Mat maskROI = new Mat(mask, rect);

            Imgproc.drawContours(mask, contours, idx, new Scalar(255, 255, 255), Core.FILLED);

            double r = (double)Core.countNonZero(maskROI)/(rect.width*rect.height);

            if (r > .35 && (rect.height > 10 && rect.width > 10) && (rect.height < 200)) {
                rectangle(rgbImg, rect.br() , new Point( rect.br().x-rect.width ,rect.br().y-rect.height),  new Scalar(0, 255, 0));
                rectangle(mask2, rect.br() , new Point( rect.br().x-rect.width ,rect.br().y-rect.height),  new Scalar(255, 255, 255),Core.FILLED);
            }
        }

        Mat imageROI = new Mat(srcImage.size(), CV_8UC3);
        imageROI.setTo(new Scalar(255,255,255));
        //Imgcodecs.imwrite(pathname + "_ROI123.jpg", imageROI);

        sz = new Size(srcImage.width(), srcImage.height());
        Imgproc.resize(mask2, mask2, sz);

        srcImage.copyTo(imageROI, mask2);

        // convert to greyscale
        Imgproc.cvtColor(imageROI, imageROI, Imgproc.COLOR_RGB2GRAY);

        //resize image to x2 the size of original image
        Size size2 = new Size((imageROI.width() * 2.5), (imageROI.height() * 2.5));

        Mat imageROI2 = new Mat(srcImage.size(), CV_8UC3);
        Imgproc.resize(imageROI, imageROI2, size2);

        //Imgcodecs.imwrite(pathname + "_check6.jpg", rgbImg);
        //Imgcodecs.imwrite(pathname + "_ROI.jpg", imageROI);
        //Imgcodecs.imwrite(pathname + "_MASK2.jpg", mask2);
        //Imgcodecs.imwrite(pathname + "_MASK.jpg", mask);

        return imageROI;
    }



    private class ProcessImageTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Processing Image...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            source.add(p1);
            source.add(p2);
            source.add(p3);
            source.add(p4);

            int distp1p2=(int) Math.sqrt((p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y));
            int distp2p3=(int) Math.sqrt((p3.x-p2.x)*(p3.x-p2.x) + (p3.y-p2.y)*(p3.y-p2.y));

            //Mat startM = Converters.vector_Point2f_to_Mat(source);
            //Mat result=extract(mRgba, startM, distp1p2, distp2p3);

            Rect roi = new Rect((int)p1.x+15, (int)p1.y+15, distp2p3-15, distp1p2-15);
            Mat result = new Mat(mRgba, roi);

            //resize image to x3 the size of original image
            Size size2 = new Size((result.width() * 3), (result.height() * 3));
            Imgproc.resize(result, result, size2);

            // convert to greyscale
            Imgproc.cvtColor(result, result, Imgproc.COLOR_RGB2GRAY);

            //remove noise from image
            Photo.fastNlMeansDenoising(result, result);
            Imgproc.adaptiveThreshold(result,result, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 55, 2);

            imageBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(result, imageBitmap);

            imageBitmap = RotateBitmap(imageBitmap);

            Core.transpose(result, result);
            Core.flip(result, result,1);

            Imgproc.cvtColor(result, result, Imgproc.COLOR_BayerBG2RGB);

            result = removeArtifacts(result);

            //resize image to x6 the size of original image
            Size size3 = new Size((result.width() * 4), (result.height() * 4));
            Imgproc.resize(result, result, size3);

            Mat returned = new Mat();
            Utils.bitmapToMat(imageBitmap, returned);

            //returned = removeArtifacts(returned);

            String pathToFile = getOutputFile(OpenCVCamera.this).toString();
            Imgcodecs.imwrite(pathToFile, result);

            MediaStore.Images.Media.insertImage(getContentResolver(), imageBitmap,"asdasdasd" , "Asd");

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            isProcessTaskRunning = false;
            Log.e("IMGTASK COMPLETE? ", "YES");

            mProgressDialog.dismiss();
            String pathToFile = getOutputFile(OpenCVCamera.this).toString();
            startPreviewActivity(pathToFile);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }















    public static File getOutputFile(Context context) {

        File filePath;

        String IMGS_PATH = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/TesseractSample/imgs";
        // checks if the directory exists, if not create it
        prepareDirectory(IMGS_PATH);

        //path to image is /storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr.jpg
        String img_path = IMGS_PATH + "/ocr.jpg";

        Log.i(TAG, "IMGS_PATH IS NOW " +img_path);

        filePath = new File(img_path);

        return filePath;
    }

    //Prepare directory on external storage
    private static  void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }


    private void startPreviewActivity(String file) {
        Intent intent = PreviewActivity.newIntent(this, file);
        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
    }

    // rotate the bitmap correctly
    public static Bitmap RotateBitmap(Bitmap source)
    {
        Matrix matrix = new Matrix();
        // rotate 90 degrees
        matrix.postRotate(90);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    //public native void doWithMat(long matAddrGr, long matAddrRgba);

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("Width", width + "\t" + height);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
        doWithMat(mRgba);
        //process(mRgba);
        return mRgba;
    }



    private void doWithMat(Mat src) {

        Mat imageGrey = new Mat();

        // convert to greyscale
        Imgproc.cvtColor(src, imageGrey, Imgproc.COLOR_BGR2GRAY);
        // used threshold instead of canny edge detection as frame rate is higher with threshold
        //Imgproc.threshold(imageGrey, imageGrey, 150, 255.0, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        //Set gaussian blur
        int gBlurSize = 9;
        Imgproc.GaussianBlur(imageGrey, imageGrey, new Size(gBlurSize, gBlurSize), 0);

        //applied canny as it helped reduce blurring due to lower frame rate
        //apply canny edge detection
        Imgproc.Canny(imageGrey, imageGrey, 50, 140, 5, true);
        // find the contours
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(imageGrey, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        double maxVal = 0;
        int maxValIdx = 0;
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++)
        {
            double contourArea = Imgproc.contourArea(contours.get(contourIdx));
            if (maxVal < contourArea)
            {
                maxVal = contourArea;
                maxValIdx = contourIdx;
            }
        }
        //Imgproc.drawContours(mRgba, contours, maxValIdx, new Scalar(0,255,0), 5);

        // statement prevents app from crashing when no contours are found
        if(contours.size()>0) {
            Rect rect = Imgproc.boundingRect(contours.get(maxValIdx));
            rectangle(mRgba, rect.br(), new Point(rect.br().x - rect.width, rect.br().y - rect.height), new Scalar(37, 185, 153), 3);

            p1 = new Point(rect.tl().x, rect.tl().y);
            p2 = new Point(rect.tl().x, rect.br().y);
            p3 = new Point(rect.br().x, rect.br().y);
            p4 = new Point(rect.br().x, rect.tl().y);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PREVIEW_CODE) {

                if (PreviewActivity.isResultConfirm(data)) {
                    Intent resultIntent = new Intent();
                    String FILE_PATH = "FILE_PATH";
                    resultIntent.putExtra(FILE_PATH, PreviewActivity.getMediaFilePath(data));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else if (PreviewActivity.isResultCancel(data)) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    }
}

