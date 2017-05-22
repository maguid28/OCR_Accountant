package com.finalyearproject.dan.ocraccountingapp.camera;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.imgtotext.OCR;
import com.finalyearproject.dan.ocraccountingapp.imgtotext.TextExtraction;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.view.UCropView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String FILE_PATH = "file_path_arg";
    private final static String RESPONSE_CODE = "response_code_arg";
    private final static String OCR_TEXT = "ocr_text_arg";

    private String previewFilePath;
    private UCropView imagePreview;

    OCRTask OCRTask;

    public static final int ACTION_CONFIRM = 900;
    public static final int ACTION_RETAKE = 901;
    public static final int ACTION_CANCEL = 902;

    private MediaController mediaController;
    private MediaPlayer mediaPlayer;

    Mat test2;

    ProgressDialog progressDialog;

    String ocrResult;

    String recTitle, recTotal, recDate, recCategory;

    // marked as true when the image processing task is in operation
    public static boolean processButtonClicked = false;

    // marked as true when the image ocr task is in operation
    public static boolean isOCRTaskRunning = false;
    public static boolean ocrButtonClicked = false;


    public static Intent newIntent(Context context, String filePath) {

        return new Intent(context, PreviewActivity.class)
                .putExtra(FILE_PATH, filePath);
    }

    public static boolean isResultConfirm(@NonNull Intent resultIntent) {
        return ACTION_CONFIRM == resultIntent.getIntExtra(RESPONSE_CODE, -1);
    }

    public static String getMediaFilePath(@NonNull Intent resultIntent) {
        return resultIntent.getStringExtra(FILE_PATH);
    }

    public static boolean isResultCancel(@NonNull Intent resultIntent) {
        return ACTION_CANCEL == resultIntent.getIntExtra(RESPONSE_CODE, -1);
    }






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_preview);


        imagePreview = (UCropView) findViewById(R.id.image_view);

        progressDialog = new ProgressDialog(this);

        View confirmMediaResult = findViewById(R.id.confirm_media_result);
        View reTakeMedia2 = findViewById(R.id.re_take_media2);
        View cancelMediaAction2 = findViewById(R.id.cancel_media_action2);

        if (confirmMediaResult != null)
            confirmMediaResult.setOnClickListener(this);

        if (reTakeMedia2 != null)
            reTakeMedia2.setOnClickListener(this);

        if (cancelMediaAction2 != null)
            cancelMediaAction2.setOnClickListener(this);

        Bundle args = getIntent().getExtras();

        try {
            previewFilePath = args.getString(FILE_PATH);
            displayImage();
            processMediaFile();

        } catch (Exception ignored){}
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

            Log.e("filepath is ...", previewFilePath);

            ocrButtonClicked = true;
            if(isOCRTaskRunning){
                Log.e("OCRTASK COMPLETE? ", "NO");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setMessage("Extracting text...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
            else{
                startReceiptEdit();
            }

        }
        else if (view.getId() == R.id.re_take_media2) {
            processButtonClicked = false;
            ocrButtonClicked = false;
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, ACTION_RETAKE);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else if (view.getId() == R.id.cancel_media_action2) {
            processButtonClicked = false;
            ocrButtonClicked = false;
            deleteMediaFile();
            resultIntent.putExtra(RESPONSE_CODE, ACTION_CANCEL);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    private class OCRTask extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            isOCRTaskRunning = true;
        }

        @Override
        protected String doInBackground(Void... params) {

            String path = previewFilePath;

            Log.e("FILEPATH IN 1 ", path);

            String ocrText;

            OCR ocr = new OCR();
            ocrText = ocr.OCRImage(path, PreviewActivity.this);

            // Write the result to a txt file and store it in the same dir as the temp img
            // find the last occurence of '/'
            int p=previewFilePath.lastIndexOf("/");
            // e is the string value after the last occurence of '/'
            String e=previewFilePath.substring(p+1);
            // split the string at the value of e to remove the it from the string and get the dir path
            String[] a = previewFilePath.split(e);
            String dirPath = a[0];

            String fileString = dirPath + "ocrtext.txt";
            File file = new File(fileString);

            try {
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(ocrText);
                bw.close();
                System.out.println("done!!");

            } catch (IOException i) {
                i.printStackTrace();
            }

            // extract text on this thread
            TextExtraction te = new TextExtraction();

            String DATA_PATH = PreviewActivity.this.getFilesDir() + "/TesseractSample/tessdata/";
            recTitle = te.getTitle(fileString, DATA_PATH);
            recTotal = te.getTotal(fileString);
            recDate = te.getDate(fileString);
            recCategory = te.getCategory(fileString, recTitle);

            return ocrText;
        }

        @Override
        protected void onPostExecute(String result) {

            isOCRTaskRunning = false;
            Log.e("OCR TASK COMPLETE? ", "YES");
            ocrResult = result;

            if(ocrButtonClicked) {
                // dismiss the progress dialog
                progressDialog.dismiss();
                startReceiptEdit();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }





    private void startReceiptEdit() {
        ocrButtonClicked = false;

        System.out.println("title " + recTitle);
        System.out.println("dat " + recDate);
        System.out.println("cat: " + recCategory);
        System.out.println("total " + recTotal);

        Intent i;
        i = new Intent(PreviewActivity.this, ReceiptEditActivity.class);

        // pass an identifier to allow the edit activity to know which activity preceded it
        i.putExtra("id", "preview");
        // Pass the file path and text result to the receipt edit activity
        i.putExtra(FILE_PATH, previewFilePath);
        Log.e("OCR TEXT: ", ocrResult);
        i.putExtra(OCR_TEXT, ocrResult);

        // pass the title, total, date, and catagory to receipt edit activity
        i.putExtra("TITLE", recTitle);
        i.putExtra("TOTAL", recTotal);
        i.putExtra("DATE", recDate);
        i.putExtra("CATEGORY", recCategory);
        // Start receipt edit activity
        PreviewActivity.this.startActivityForResult(i, 111);
        finish();
    }







    @Override
    public void onBackPressed() {
        super.onBackPressed();
        OCRTask.cancel(true);
        deleteMediaFile();
        finish();
    }

    private boolean deleteMediaFile() {
        File mediaFile = new File(previewFilePath);
        return mediaFile.delete();
    }







    private void processMediaFile() {

        Log.e("PROCESSING...", previewFilePath);

        int width = imagePreview.getWidth();
        int height = imagePreview.getHeight();

        Log.e("Height of preview...", String.valueOf(height));
        Log.e("Width of preview", String.valueOf(width));

        // run ocr in asynctask
        OCRTask = new OCRTask();
        OCRTask.execute();
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
