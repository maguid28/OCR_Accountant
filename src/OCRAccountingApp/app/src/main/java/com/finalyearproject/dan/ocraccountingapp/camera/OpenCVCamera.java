package com.finalyearproject.dan.ocraccountingapp.camera;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;
import java.util.List;

import static com.finalyearproject.dan.ocraccountingapp.camera.ImageProcessing.getOutputFile;

public class OpenCVCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // marked as true when the image processing task is in operation
    public static boolean isProcessTaskRunning = false;

    protected static final int REQUEST_PREVIEW_CODE = 1001;

    Bitmap imageBitmap = null;
    List<Point> rectCorners = new ArrayList<>();

    ProgressDialog mProgressDialog;

    private Mat mRgba;
    private Mat mIntermediateMat;
    //private Mat mGray;

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

        mOpenCvCameraView.setMaxFrameSize(8000,8000);
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


            ImageProcessing imageProcessing = new ImageProcessing();
            imageProcessing.writeToStorage(rectCorners.get(0), rectCorners.get(1),
                    rectCorners.get(2), imageBitmap, mRgba, OpenCVCamera.this);

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            mProgressDialog.dismiss();
            isProcessTaskRunning = false;
            Log.e("IMGTASK COMPLETE? ", "YES");

            String pathToFile = getOutputFile(OpenCVCamera.this).toString();
            startPreviewActivity(pathToFile);
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    private void startPreviewActivity(String file) {
        Intent intent = PreviewActivity.newIntent(this, file);
        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d("Width", width + "\t" + height);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        //mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        //mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // input frame has RGBA format
        mRgba = inputFrame.rgba();
        //mGray = inputFrame.gray();
        rectCorners = new ImageProcessing().drawBoundingRectangle(mRgba);
        //process(mRgba);
        return mRgba;
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

