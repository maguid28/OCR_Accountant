package com.finalyearproject.dan.ocraccountingapp;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentItem;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentProgressListener;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static com.finalyearproject.dan.ocraccountingapp.util.Orientation.exifToDegrees;

public class ReceiptEditActivity extends AppCompatActivity {

    private static final String TAG = ReceiptEditActivity.class.getSimpleName();

    ExpandableRelativeLayout expandableLayout1;
    ImageView receiptDisplayImageView;


    String filePath;

    String OCRText;

    Bitmap imageBitmap;

    EditText totalEdit, recNameEdit;
    String selectedSpinnerItem;

    // The user file manager.
    private UserFileManager userFileManager;

    // The current relative path within the UserFileManager.
    private String currentPath = "";

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        // Get the bundle
        Bundle bundle = getIntent().getExtras();
        try {
            //Extract the data passed from the previous activity
            filePath = bundle.getString("file_path_arg");
            OCRText = bundle.getString("ocr_text_arg");
            TextView ocrtextview = (TextView) findViewById(R.id.ocr_textview);
            ocrtextview.setText(OCRText);
            Log.i("prev activity: ", filePath);
        }
        catch (NullPointerException ignored) {}

        totalEdit = (EditText) findViewById(R.id.total_edit);
        recNameEdit = (EditText) findViewById(R.id.recNameEdit);



        // find the last occurence of '/'
        int p=filePath.lastIndexOf("/");
        // e is the string value after the last occurence of '/'
        String e=filePath.substring(p+1);
        // split the string at the value of e to remove the it from the string and get the dir path
        String[] a = filePath.split(e);
        String dirPath = a[0];

        File file = new File(dirPath + "ocrtext.txt");

        try {
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(OCRText);

            bw.close();
            System.out.println("done!!");

        } catch (IOException i) {
            i.printStackTrace();
        }





        // display the image captured in the camera activity
        displayImage();

        // set up the floating action button
        configureFAB();

        Spinner categories = (Spinner) findViewById(R.id.expense_spinner);
        // Create the spinner from the expense catagories in arrays.xml and style it as spinner_item
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.expense_catagories, R.layout.spinner_item);
        categories.setAdapter(adapter);

        // listen for the selected value of the spinner
        categories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedSpinnerItem =(String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        final String identityId = AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID();

        // The Amazon s3 bucket
        String bucket = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET;
        // Amazon S3 bucket region, which will be set to
        Regions region = AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET_REGION;
        // prefix will be set to private which means users will not have access to other users data
        String prefix = "private/" + identityId + "/";

        AWSMobileClient.defaultMobileClient()
                .createUserFileManager(bucket, prefix,region,
                        new UserFileManager.BuilderResultHandler() {
                            @Override
                            public void onComplete(final UserFileManager userFileManager) {

                                ReceiptEditActivity.this.userFileManager = userFileManager;
                                userFileManagerCreatingLatch.countDown();
                                Log.e(TAG, "userfilemanager ..........................." + userFileManager);
                            }
                        });

    }





    public void expandableButton1(View view) {
        expandableLayout1 = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout1);
        expandableLayout1.toggle(); // toggle expand and collapse
    }



    // set up the floating action button
    private void configureFAB(){
        // open the context menu when the fab is clicked
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiptEditActivity.this);
                builder.setMessage("Are you sure you want to save?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // rename the captured file to unique name and upload it.
                                renameAndUploadFile();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }


    private void renameAndUploadFile() {
        boolean bool = false;
        //path to file we want to rename
        File oldName = new File(filePath);

        // find the last occurence of '/'
        int p=filePath.lastIndexOf("/");
        // e is the string value after the last occurence of '/'
        String e=filePath.substring(p+1);
        // split the string at the value of e to remove the it from the string and get the dir path
        String[] a = filePath.split(e);
        String dirPath = a[0];

        if(oldName.exists()) {
            // Create a unique image file name
            String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmmss").format(new Date());
            String fileName = "rec" + timeStamp + ".jpg";
            File newName = new File(dirPath + fileName);

            // rename file
            bool = oldName.renameTo(newName);

            // print
            Log.d(TAG, "File renamed? "+bool);
            // print
            Log.d(TAG, "File name is now: " + oldName.getName());

            uploadData(newName, fileName);
        }
        else {
            new android.app.AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                    .setMessage("Please capture an image first")
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
        }
    }


    /*--------------------------------------UPLOAD FILE-------------------------------------------*/


    public void uploadData(File filepath, final String fileName) {
        final String path = filepath.getAbsolutePath();
        //"/storage/emulated/0/Android/data/com.finalyearproject.dan.ocraccountingapp/files/Pictures/TesseractSample/imgs/ocr_receiptimage.jpg";
        Log.d(TAG, "file path: " + path);

        // progress dialog that will stay open while the image is being uploaded
        final ProgressDialog dialog = new ProgressDialog(this, R.style.Dialog1);
        dialog.setTitle("Saving Image...");
        dialog.setMax((int) new File(path).length());
        dialog.setCancelable(false);
        dialog.show();

        // get the total entered in the edit text field
        final String total = totalEdit.getText().toString();
        // get the name entered by the edit text field
        final String friendlyName = recNameEdit.getText().toString();
        // get the value selected on the spinner
        final String category = selectedSpinnerItem;

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
                        // Call insertdata to add the entered data into the database
                        insertData(fileName, date, fileName, formattedDate, total, friendlyName, category);

                        // launch the main activity
                        Intent main = new Intent(ReceiptEditActivity.this, MainActivity.class);
                        ReceiptEditActivity.this.startActivityForResult(main, 111);

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
        new android.app.AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setMessage(getString(resId, (Object[]) args))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }


    /*------------------------------------END UPLOAD FILE-----------------------------------------*/


    /*-----------------------------INSERT DATA INTO DATABASE--------------------------------------*/


    public void insertData(final String recName, final String date, final String filepath,
                           final String formattedDate, final String total, final String friendlyName, String category) {

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
        receipt.setFriendlyName(friendlyName);
        receipt.setCategory(category);

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


    /*-----------------------------END INSERT DATA INTO DATABASE--------------------------------------*/


    // display the image captured in the previous activity
    private void displayImage(){
        receiptDisplayImageView = (ImageView) findViewById(R.id.receipt_display);
        //Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bmOptions);

        //determine how much to scale down the image
        //int scalefactor = Math.min(photoW/targetW, photoH/targetH);

        //decode the image file into a bitmap sized to fill the view
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 3;

        imageBitmap = BitmapFactory.decodeFile(filePath, bmOptions);

        try {
            //Display image in the correct orientation
            ExifInterface exif = new ExifInterface(filePath);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);
            Matrix matrix = new Matrix();
            if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}
            imageBitmap = Bitmap.createBitmap(imageBitmap,0,0, imageBitmap.getWidth(),imageBitmap.getHeight(), matrix, true);

        }catch(IOException ex){
            Log.e("Failed to get Exif data", "ex");
        }

        imageBitmap = BitmapFactory.decodeFile(filePath, bmOptions);

        // set image view to the processed bitmap image
        receiptDisplayImageView.setImageBitmap(imageBitmap);
    }



}