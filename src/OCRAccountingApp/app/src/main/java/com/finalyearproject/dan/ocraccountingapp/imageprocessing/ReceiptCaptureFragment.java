package com.finalyearproject.dan.ocraccountingapp.imageprocessing;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentItem;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentProgressListener;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities.Camera1Activity;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static android.app.Activity.RESULT_OK;

public class ReceiptCaptureFragment extends Fragment {

    private static final String TAG = ReceiptCaptureFragment.class.getSimpleName();
    static final int PHOTO_REQUEST_CODE = 1;
    private TessBaseAPI tessBaseApi;
    TextView textView;
    Uri outputFileUri;
    private static final String lang = "eng";
    String result = "empty";

    String DATA_PATH = "";
    private static final String TESSDATA = "tessdata";

    ImageView mImageView;
    String IMGS_PATH;
    String img_path;
    Bitmap imageBitmap;


    Mat test2;

    /** Log tag. */
    private static final String LOG_TAG = ReceiptCaptureFragment.class.getSimpleName();

    /** The user file manager. */
    private UserFileManager userFileManager;

    /** The current relative path within the UserFileManager. */
    private String currentPath = "";

    /** The s3 bucket. */
    private String bucket;

    /** The S3 bucket region. */
    private Regions region;

    /** The s3 Prefix at which the UserFileManager is rooted. */
    private String prefix;

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);

    final String pathname = "/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/";



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_receiptcapture, container, false);


        final String identityId = AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID();
        bucket = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET;
        prefix = "private/" + identityId + "/";
        region = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET_REGION;

        AWSMobileClient.defaultMobileClient()
                .createUserFileManager(bucket, prefix,region,
                        new UserFileManager.BuilderResultHandler() {
                            @Override
                            public void onComplete(final UserFileManager userFileManager) {

                                ReceiptCaptureFragment.this.userFileManager = userFileManager;
                                userFileManagerCreatingLatch.countDown();
                                Log.e(LOG_TAG, "userfilemanager ..........................." + userFileManager);
                            }
                        });

        mImageView = (ImageView) fragmentView.findViewById(R.id.mImageView);

        //imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test2);

        DATA_PATH = getActivity().getFilesDir() + "/TesseractSample/";

        //img capture button
        Button captureImg = (Button) fragmentView.findViewById(R.id.action_btn);
        if (captureImg != null) {
            captureImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCameraActivity();
                }
            });
        }

        Button uploadImg = (Button) fragmentView.findViewById(R.id.upload_button);
        if (uploadImg != null) {
            uploadImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{

                        boolean bool = false;
                        //path to file we want to rename
                        File oldName = new File(pathname + "ocr_receiptimage.jpg");

                        if(oldName.exists()) {
                            // Create a unique image file name
                            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
                            String fileName = "rec" + timeStamp + ".jpg";
                            File newName = new File(pathname + fileName);

                            // rename file
                            bool = oldName.renameTo(newName);

                            // print
                            Log.d(LOG_TAG, "File renamed? "+bool);
                            // print
                            Log.d(LOG_TAG, "File name is now: " + oldName.getName());

                            uploadData(newName, fileName);
                        }
                        else {
                            new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
                                    .setMessage("Please capture an image first")
                                    .setPositiveButton(android.R.string.ok, null)
                                    .show();
                        }


                    }catch(Exception e){
                        // if any error occurs
                        e.printStackTrace();
                    }
                }
            });
        }

        // add to database button
        Button addtodb = (Button) fragmentView.findViewById(R.id.addToDB);
        if (addtodb != null) {
            addtodb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new InsertDataInBackground().execute();
                    insertData("test6", "12-02-2017", "example/path/to/file", "20170212", "500.00");
                }
            });
        }


        textView = (TextView) fragmentView.findViewById(R.id.textResult);

        return fragmentView;
    }



    /*-----------------------------INSERT DATA INTO DATABASE--------------------------------------*/


    public void insertData(final String recName, final String date, final String filepath, final String formattedDate, final String total) {

        // Fetch the default configured DynamoDB ObjectMapper
        final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final ReceiptDataDO receipt = new ReceiptDataDO(); // Initialize the receipt Object
        // The userId has to be set to user's Cognito Identity Id for private / protected tables.
        // User's Cognito Identity Id can be fetched by using:
        // AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID()
        receipt.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());
        receipt.setRecName(recName);
        receipt.setDate(date);
        receipt.setFilepath(filepath); // GMT: Fri, 19 Aug 2016 21:53:47 GMT
        receipt.setTotal(total);
        receipt.setFormattedDate(formattedDate);

        // create another thread for saving to the DB.
        new Thread(new Runnable() {
            @Override
            public void run() {
                AmazonClientException lastException = null;
                try {
                    //save the new entry to the db
                    dynamoDBMapper.save(receipt);
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
               //             Toast.makeText(getActivity(), "SUCCESS!", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (final AmazonClientException ex) {
                    Log.e("INSERT DATA", "Failed saving item : " + ex.getMessage(), ex);
                    //Toast.makeText(getActivity(), "FAILED!", Toast.LENGTH_LONG).show();
                    lastException = ex;
                }
            }
        }).start();
    }

    /*--------------------------------------UPLOAD FILE-------------------------------------------*/


    public void uploadData(File filepath, final String fileName) {
        final String path = filepath.getAbsolutePath();
        //"/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr_receiptimage.jpg";
        Log.d(LOG_TAG, "file path: " + path);
        final ProgressDialog dialog = new ProgressDialog(getActivity(), R.style.Dialog1);
        dialog.setTitle("Saving Image...");
        //dialog.setMessage(getString(R.string.user_files_browser_progress_dialog_message_upload_file, path));
        //dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMax((int) new File(path).length());
        dialog.setCancelable(false);
        dialog.show();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    userFileManagerCreatingLatch.await();
                } catch (final InterruptedException ex) {
                    // This thread should never be interrupted.
                    throw new RuntimeException(ex);
                }
                final File file = new File(path);
                userFileManager.uploadContent(file, currentPath + file.getName(), new ContentProgressListener() {
                    @Override
                    public void onSuccess(final ContentItem contentItem) {
                        dialog.dismiss();

                        // Get date in correct format
                        String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
                        String formattedDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
                        // Call insertdata to add the following to the database: insertData(receiptname, date, filepath, total)
                        insertData(fileName, date, fileName, formattedDate, "n/a");

                        Toast.makeText(getActivity(), "SAVED!", Toast.LENGTH_SHORT).show();
                        // Workaround until I figure out whats wrong with arrayadapter in FragmentContent.
                        getActivity().recreate();

                        //FragmentContent f = (FragmentContent) getSupportFragmentManager().findFragmentByTag("yourFragTag");


                    }
                    @Override
                    public void onProgressUpdate(final String fileName, final boolean isWaiting,
                                                 final long bytesCurrent, final long bytesTotal) {
                        dialog.setProgress((int) bytesCurrent);
                    }

                    @Override
                    public void onError(final String fileName, final Exception ex) {
                        dialog.dismiss();
                        showError(R.string.user_files_browser_error_message_upload_file,
                                ex.getMessage());
                    }
                });
            }
        }).start();
    }

    private void showError(final int resId, Object... args) {
        new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(resId, (Object[]) args))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    /*------------------------------------END UPLOAD FILE-----------------------------------------*/



    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
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

    // check if OpenCV library have been loaded and initialized from within current application package or not
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, getActivity(), mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }



    //get high resolution image from camera
    private void startCameraActivity() {
        try {
            //Path to image is set to /storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs
            IMGS_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/TesseractSample/imgs";
            prepareDirectory(IMGS_PATH);
            Log.i(TAG, "IMGS_PATH IS NOW " +IMGS_PATH);

            //path to image is /storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr.jpg
            img_path = IMGS_PATH + "/ocr.jpg";

            outputFileUri = Uri.fromFile(new File(img_path));

            //launch camera intent
            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //store image at .../TesseractSample/imgs/ocr.jpg
            //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);

            Intent cameraIntent = new Intent(getContext(), Camera1Activity.class);
            getActivity().startActivityForResult(cameraIntent, 368);
            //if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            //    startActivityForResult(takePictureIntent, PHOTO_REQUEST_CODE);
            //}
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 368 && resultCode == RESULT_OK) {
            //Log.e("File", "" + data.getStringExtra(CameraConfiguration.Arguments.FILE_PATH));
            Toast.makeText(getContext(), "Media captured.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getContext(), "MERP", Toast.LENGTH_SHORT).show();

        }

        //making photo
        if (requestCode == PHOTO_REQUEST_CODE && resultCode == RESULT_OK) {

            ReceiptScanner rec = new ReceiptScanner();
            //Mat test = rec.correctReceipt(img_path);
            //Imgcodecs.imwrite(img_path, test);

            test2 = rec.receiptPic(img_path);
            Imgcodecs.imwrite(img_path, test2);



            //Get the dimensions of the view
            int targetW = mImageView.getWidth();
            int targetH = mImageView.getHeight();

            //Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(img_path, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            //determine how much to scale down the image
            int scalefactor = Math.min(photoW/targetW, photoH/targetH);

            //decode the image file into a bitmap sized to fill the view
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scalefactor;

            imageBitmap = BitmapFactory.decodeFile(img_path, bmOptions);

            try {
                //Display image in the correct orientation
                ExifInterface exif = new ExifInterface(img_path);
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
                imageBitmap = Bitmap.createBitmap(imageBitmap,0,0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

            }catch(IOException ex){
                Log.e("Failed to get Exif data", "ex");
            }

            mImageView.setImageBitmap(imageBitmap);

            //create folder and store tessdata here
            prepareTesseract();

            //startOCR(outputFileUri);
            new runOCRinBackground().execute(outputFileUri);
        } else {
            Toast.makeText(getActivity(), "ERROR: Image was not obtained.", Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "Activity.RESULT_OK" + RESULT_OK, Toast.LENGTH_SHORT).show();
            Toast.makeText(getActivity(), "PHOTO_REQUEST_CODE" + requestCode, Toast.LENGTH_SHORT).show();
        }
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }





    //Prepare directory on external storage
    private void prepareDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "ERROR: Creation of directory " + path + " failed, check does Android Manifest have permission to write to external storage.");
            }
        } else {
            Log.i(TAG, "Created directory " + path);
        }
    }




    private void prepareTesseract() {
        try {
            prepareDirectory(DATA_PATH + TESSDATA);
        } catch (Exception e) {
            e.printStackTrace();
        }

        copyTessDataFiles(TESSDATA);
    }

    //Copy tessdata files (located on assets/tessdata) to destination directory
    private void copyTessDataFiles(String path) {
        try {
            String fileList[] = getActivity().getAssets().list(path);

            for (String fileName : fileList) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                String pathToDataFile = DATA_PATH + path + "/" + fileName;
                if (!(new File(pathToDataFile)).exists()) {

                    InputStream in = getActivity().getAssets().open(path + "/" + fileName);

                    OutputStream out = new FileOutputStream(pathToDataFile);

                    // Transfer bytes from in to out
                    byte[] buf = new byte[1024];
                    int len;

                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied " + fileName + " to tessdata");
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable to copy files to tessdata " + e.toString());
        }
    }


    /**
     * don't run this code in main thread - it stops UI thread. Create AsyncTask instead.
     * http://developer.android.com/intl/ru/reference/android/os/AsyncTask.html
     */
    private String startOCR(Uri imgUri) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4; // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            Bitmap bitmap = BitmapFactory.decodeFile(imgUri.getPath(), options);

            try {
                ExifInterface exif = new ExifInterface(imgUri.getPath());
                int exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                Log.v(TAG, "Orient: " + exifOrientation);

                int rotate = 0;

                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                }

                Log.v(TAG, "Rotation: " + rotate);

                if (rotate != 0) {

                    // Getting width & height of the given image.
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    // Setting pre rotate
                    Matrix mtx = new Matrix();
                    mtx.postRotate(rotate);

                    // Rotating Bitmap
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                }
            }catch (Exception e) {
                Log.e(TAG, "IMAGE WAS NOT ROTATED");
            }

            result = extractText(bitmap);

            textView.setText(result);

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return result;
    }





    private String extractText(Bitmap bitmap) {
        try {
            tessBaseApi = new TessBaseAPI();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            if (tessBaseApi == null) {
                Log.e(TAG, "TessBaseAPI is null. TessFactory not returning tess object.");
            }
        }

        tessBaseApi.init(DATA_PATH, lang);

        //If we only want to detect digits, uppercase and lowercase
        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "1234567890qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM");


        //       //EXTRA SETTINGS
        //        //blackList Example
        //        tessBaseApi.setVariable(TessBaseAPI.VAR_CHAR_BLACKLIST, "!@#$%^&*()_+=-qwertyuiop[]}{POIU" +
        //                "YTRWQasdASDfghFGHjklJKLl;L:'\"\\|~`xcvXCVbnmBNM,./<>?");

        Log.d(TAG, "Training file loaded");
        tessBaseApi.setImage(bitmap);
        String extractedText = "empty result";
        try {
            extractedText = tessBaseApi.getUTF8Text();
        } catch (Exception e) {
            Log.e(TAG, "Error in recognizing text.");
        }
        tessBaseApi.end();
        return extractedText;
    }



    public class runOCRinBackground extends AsyncTask<Uri, Void, String> {

        @Override
        protected String doInBackground(Uri... params) {

            Uri fileUri = params[0];

            String ocr = startOCR(fileUri);

            return ocr;
        }
        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}

