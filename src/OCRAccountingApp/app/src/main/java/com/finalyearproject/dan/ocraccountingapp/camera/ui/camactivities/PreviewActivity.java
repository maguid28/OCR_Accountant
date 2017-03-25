package com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;

import com.finalyearproject.dan.ocraccountingapp.OCR;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.RECSCANNER_UPDATE;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.BaseActivity;
import com.finalyearproject.dan.ocraccountingapp.ReceiptEditActivity;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.UCropView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;

import static com.finalyearproject.dan.ocraccountingapp.util.Orientation.exifToDegrees;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String FILE_PATH = "file_path_arg";
    private final static String RESPONSE_CODE = "response_code_arg";
    private final static String OCR_TEXT = "ocr_text_arg";



    private String previewFilePath;
    private String displayFilePath;
    private UCropView imagePreview;
    private ViewGroup preprocessPanel;
    private ViewGroup postprocessPanel;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    Bitmap imageBitmap;
    Bitmap displayBitmap;
    Mat test2;

    public static Intent newIntent(Context context, int mediaAction,
                                   String filePath) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(FILE_PATH, filePath);
    }

    public static boolean isResultConfirm(@NonNull Intent resultIntent) {
        return BaseActivity.ACTION_CONFIRM == resultIntent.getIntExtra(RESPONSE_CODE, -1);
    }

    public static String getMediaFilePath(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(FILE_PATH);
    }

    public static boolean isResultCancel(@NonNull Intent resultIntent) {
        return BaseActivity.ACTION_CANCEL == resultIntent.getIntExtra(RESPONSE_CODE, -1);
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

        previewFilePath = args.getString(FILE_PATH);

        displayImage();

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


            OCR ocr = new OCR();
            // get the text from the image and store it in ocrText
            String ocrText = ocr.OCRImage(previewFilePath, this);

            Log.e("filepath is ...", previewFilePath);

            Intent i;
            i = new Intent(this, ReceiptEditActivity.class);
            i.putExtra(FILE_PATH, previewFilePath);
            i.putExtra(OCR_TEXT, ocrText);
            //Start receipt edit activity
            this.startActivityForResult(i, 111);
            finish();

        } else if (view.getId() == R.id.re_take_media) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, BaseActivity.ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else if (view.getId() == R.id.re_take_media2) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, BaseActivity.ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, BaseActivity.ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action2) {
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, BaseActivity.ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
        else if (view.getId() == R.id.process_media) {
            processMediaFile();
            //resultIntent.putExtra(RESPONSE_CODE, BaseActivity.ACTION_CANCEL);
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

    private class ProcessImageTask extends AsyncTask<String, Void, String> {

        ProgressDialog mProgressDialog;

        public ProcessImageTask(PreviewActivity activity) {
            mProgressDialog = new ProgressDialog(activity);
        }

        @Override
        protected String doInBackground(String... params) {
            RECSCANNER_UPDATE rec = new RECSCANNER_UPDATE();
            //Mat test = rec.correctReceipt(img_path);
            //Imgcodecs.imwrite(img_path, test);

            //test2 = rec.receiptPic(previewFilePath);
            //Imgcodecs.imwrite(previewFilePath, test2);

            //Mat test = rec.correctReceipt(img_path);
            //Imgcodecs.imwrite(img_path, test);


            // get the directory of the previewfilepath
            // find the last occurence of '/'
            int p=previewFilePath.lastIndexOf("/");
            // e is the string value after the last occurence of '/'
            String e=previewFilePath.substring(p+1);
            // split the string at the value of e to remove the it from the string and get the dir path
            String[] a = previewFilePath.split(e);
            String dirPath = a[0];

            System.out.println("previewfilepath " + previewFilePath);
            System.out.println("dirpath " + dirPath);
            displayFilePath = dirPath + "displayImage.jpg";

            test2 = rec.correctReceipt(previewFilePath);
            Imgcodecs.imwrite(previewFilePath, test2);

            Imgcodecs.imwrite(displayFilePath, test2);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mProgressDialog.dismiss();
            //turn off the image preview to allow for the processed image to be displayed
            imagePreview.setVisibility(View.GONE);

            ImageView processedImage = (ImageView) findViewById(R.id.processed_Image);

            //Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(previewFilePath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;


            //decode the image file into a bitmap sized to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 4;

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



            //displayBitmap


            // set image view to the processed bitmap image
            processedImage.setImageBitmap(imageBitmap);

            Log.e("BITMAP WIDTH ", String.valueOf(imageBitmap.getWidth()));
            Log.e("BITMAP HEIGHT ", String.valueOf(imageBitmap.getHeight()));

            // Hide the preprocess panel
            preprocessPanel.setVisibility(View.GONE);
            // Bring the post process panel into view
            postprocessPanel.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPreExecute() {
            // Set the progress dialog attributes
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Processing, please wait...");
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    private void processMediaFile() {

        Log.e("PROCESSING...", previewFilePath);

        int width = imagePreview.getWidth();
        int height = imagePreview.getHeight();

        Log.e("Height of preview...", String.valueOf(height));
        Log.e("Width of preview", String.valueOf(width));

        ProcessImageTask processImageTask = new ProcessImageTask(this);
        processImageTask.execute();
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
