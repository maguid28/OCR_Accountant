package com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.camera.ReceiptScanner;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.BaseActivity;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.Utils;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.UCropView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String MEDIA_ACTION_ARG = "media_action_arg";
    private final static String FILE_PATH_ARG = "file_path_arg";
    private final static String RESPONSE_CODE_ARG = "response_code_arg";
    private final static String MIME_TYPE_IMAGE = "image";

    private String previewFilePath;
    private UCropView imagePreview;
    private ViewGroup preprocessPanel;
    private ViewGroup postprocessPanel;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    Bitmap imageBitmap;
    Mat test2;

    public static Intent newIntent(Context context, int mediaAction,
                                   String filePath) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(MEDIA_ACTION_ARG, mediaAction)
                .putExtra(FILE_PATH_ARG, filePath);
    }

    public static boolean isResultConfirm(@NonNull Intent resultIntent) {
        return BaseActivity.ACTION_CONFIRM == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }

    public static String getMediaFilePath(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(FILE_PATH_ARG);
    }

    public static boolean isResultCancel(@NonNull Intent resultIntent) {
        return BaseActivity.ACTION_CANCEL == resultIntent.getIntExtra(RESPONSE_CODE_ARG, -1);
    }






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_preview);

        imagePreview = (UCropView) findViewById(R.id.image_view);
        preprocessPanel = (ViewGroup) findViewById(R.id.pre_process_panel);
        postprocessPanel = (ViewGroup) findViewById(R.id.post_process_panel);

        // Hide the post process panel until processing is complete
        postprocessPanel.setVisibility(View.GONE);

        View confirmMediaResult = findViewById(R.id.confirm_media_result);
        View reTakeMedia = findViewById(R.id.re_take_media);
        View reTakeMedia2 = findViewById(R.id.re_take_media2);
        View cancelMediaAction = findViewById(R.id.cancel_media_action);
        View cancelMediaAction2 = findViewById(R.id.cancel_media_action2);
        View processImageAction = findViewById(R.id.process_media);

        if (confirmMediaResult != null)
            confirmMediaResult.setOnClickListener(this);

        if (reTakeMedia != null)
            reTakeMedia.setOnClickListener(this);

        if (reTakeMedia2 != null)
            reTakeMedia2.setOnClickListener(this);

        if (cancelMediaAction != null)
            cancelMediaAction.setOnClickListener(this);

        if (cancelMediaAction2 != null)
            cancelMediaAction2.setOnClickListener(this);

        if (processImageAction != null)
            processImageAction.setOnClickListener(this);

        Bundle args = getIntent().getExtras();

        int mediaAction = args.getInt(MEDIA_ACTION_ARG);
        previewFilePath = args.getString(FILE_PATH_ARG);

        int PHOTO_CODE = 101;
        if (mediaAction == PHOTO_CODE) {
            displayImage();
        } else {
            String mimeType = Utils.getMimeType(previewFilePath);
            if (mimeType.contains(MIME_TYPE_IMAGE)) {
                displayImage();
            } else finish();
        }
/*
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        */
    }










    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            showImagePreview();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mediaController != null) {
            mediaController.hide();
            mediaController = null;
        }
    }

    private void displayImage() {
        showImagePreview();
    }

    private void showImagePreview() {
        try {
            Uri uri = Uri.fromFile(new File(previewFilePath));
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(new File(uri.getPath()).getAbsolutePath(), options);
            Log.e("PATH: ", previewFilePath);

            imagePreview.getCropImageView().setImageUri(uri, null);
            imagePreview.getOverlayView().setShowCropFrame(false);
            imagePreview.getOverlayView().setShowCropGrid(false);
            imagePreview.getCropImageView().setRotateEnabled(false);
            imagePreview.getOverlayView().setDimmedColor(Color.TRANSPARENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onClick(View view) {
        Intent resultIntent = new Intent();
        if (view.getId() == R.id.confirm_media_result) {
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_CONFIRM);
            resultIntent.putExtra(FILE_PATH_ARG, previewFilePath);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.re_take_media) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else if (view.getId() == R.id.re_take_media2) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action2) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else if (view.getId() == R.id.process_media) {
            processMediaFile();
            //resultIntent.putExtra(RESPONSE_CODE_ARG, BaseActivity.ACTION_CANCEL);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        deleteMediaFile();
    }

    private boolean deleteMediaFile() {
        File mediaFile = new File(previewFilePath);
        return mediaFile.delete();
    }

    private void processMediaFile() {

        Log.e("PROCESSING...", previewFilePath);

        int width = imagePreview.getWidth();
        int height = imagePreview.getHeight();

        Log.e("Height...", String.valueOf(height));
        Log.e("Width...", String.valueOf(width));

        imagePreview.setVisibility(View.GONE);
        ImageView mImageView = (ImageView) findViewById(R.id.processed_Image);
        ReceiptScanner rec = new ReceiptScanner();
        //Mat test = rec.correctReceipt(img_path);
        //Imgcodecs.imwrite(img_path, test);

        test2 = rec.receiptPic(previewFilePath);
        Imgcodecs.imwrite(previewFilePath, test2);



        //Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(previewFilePath, bmOptions);

        //determine how much to scale down the image
        //int scalefactor = Math.min(photoW/targetW, photoH/targetH);

        //decode the image file into a bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scalefactor;

        imageBitmap = BitmapFactory.decodeFile(previewFilePath, bmOptions);

        try {
            //Display image in the correct orientation
            ExifInterface exif = new ExifInterface(previewFilePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
            imageBitmap = Bitmap.createBitmap(imageBitmap,0,0, imageBitmap.getWidth(),imageBitmap.getHeight(), matrix, true);

        }catch(IOException ex){
            Log.e("Failed to get Exif data", "ex");
        }

        imageBitmap = BitmapFactory.decodeFile(previewFilePath, bmOptions);

        // set image view to the processed bitmap image
        mImageView.setImageBitmap(imageBitmap);

        // Hide the preprocess panel
        preprocessPanel.setVisibility(View.GONE);
        // Bring the post process panel into view
        postprocessPanel.setVisibility(View.VISIBLE);
    }



    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }




    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    test2 = new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    // when resumed, check if OpenCV library have been loaded and initialized from within current application package or not
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

}
