package com.finalyearproject.dan.ocraccountingapp.camera;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.MainActivity;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentItem;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.ContentProgressListener;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.calendar.ViewPagerFragment;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;
import com.finalyearproject.dan.ocraccountingapp.nosql.noSQLObj;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.DynamoDBUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResult;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static com.finalyearproject.dan.ocraccountingapp.util.Orientation.exifToDegrees;

public class ReceiptEditActivity extends AppCompatActivity {

    private static final String TAG = ReceiptEditActivity.class.getSimpleName();

    ExpandableRelativeLayout expandableLayout1;
    ImageView receiptDisplayImageView;

    String OCRText;

    String filePath;

    String receiptTitle;
    String receiptTotal;
    String receiptDate;
    String receiptCatagory;

    NoSQLResult noSQLResult;
    noSQLObj noSqlObj;

    String senderID;

    Bitmap imageBitmap;

    EditText recTotalEditText, recNameEditText;
    String selectedSpinnerItem;
    EditText recDateEditText;

    // The user file manager.
    private UserFileManager userFileManager;

    // The current relative path within the UserFileManager.
    private String currentPath = "";

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_edit);

        AWSMobileClient.initializeMobileClientIfNecessary(getApplicationContext());

        // Get the bundle
        Bundle bundle = getIntent().getExtras();

        recDateEditText = (EditText)findViewById(R.id.dateEdit);
        recTotalEditText = (EditText)findViewById(R.id.total_edit);
        recNameEditText = (EditText)findViewById(R.id.recNameEdit);

        try {
            //Extract the data passed from the previous activity
            senderID = bundle.getString("id");
            Log.i("Sender id is ", senderID);
            filePath = bundle.getString("file_path_arg");
            OCRText = bundle.getString("ocr_text_arg");
            receiptTitle = bundle.getString("TITLE");
            receiptTotal = bundle.getString("TOTAL");
            receiptDate = bundle.getString("DATE");
            receiptCatagory = bundle.getString("CATEGORY");
            TextView ocrtextview = (TextView) findViewById(R.id.ocr_textview);

            recNameEditText.setText(receiptTitle, TextView.BufferType.EDITABLE);
            recTotalEditText.setText(receiptTotal, TextView.BufferType.EDITABLE);
            recDateEditText.setText(receiptDate, TextView.BufferType.EDITABLE);

            ocrtextview.setText(OCRText);
            ocrtextview.setVisibility(View.GONE);
            Log.i("prev activity: ", filePath);
            //ocrtextview.setVisibility(View.GONE);

            if(senderID.equals("results")) {
                noSqlObj = (noSQLObj) bundle.getSerializable("noSQLObj");
                assert noSqlObj != null;
                noSQLResult = noSqlObj.getObj();
                System.out.println("theresult " + noSQLResult);
            }
        }
        catch (NullPointerException ignored) {}


        Button expandableButton = (Button) findViewById(R.id.expandableButton1);
        expandableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout1 = (ExpandableRelativeLayout) findViewById(R.id.expandableLayout1);
                expandableLayout1.toggle(); // toggle expand and collapse
            }
        });

        recDateEditText.setFocusable(false);
        recDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {

                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "dd/MM/yyyy"; // your format
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.UK);

                        recDateEditText.setText(sdf.format(myCalendar.getTime()));
                    }
                };

                //new DatePickerDialog(ReceiptEditActivity.this, date, myCalendar.get(Calendar.YEAR),
                //        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

                new DatePickerDialog(ReceiptEditActivity.this, R.style.datepickerCustom, date,myCalendar.get(Calendar.YEAR),
                               myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });



        // display the image captured in the camera activity
        displayImage();

        // set up the floating action button
        configureFAB();

        Spinner categories = (Spinner) findViewById(R.id.expense_spinner);
        // Create the spinner from the expense catagories in arrays.xml and style it as spinner_item
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.expense_catagories, R.layout.spinner_item);
        categories.setAdapter(adapter);

        categories.setSelection(getIndex(categories, receiptCatagory));

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


    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }




    // set up the floating action button
    public void configureFAB(){
        // open the context menu when the fab is clicked
        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReceiptEditActivity.this);
                builder.setMessage("Are you sure you want to save?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // if the previous view was the preview activty
                                if(senderID.equals("preview")) {
                                    // rename the captured file to unique name and upload it.
                                    renameAndUploadFile();
                                }
                                // if the previous view was the results fragment
                                if(senderID.equals("results")) {
                                    // update the database entry
                                    updateReceipt();
                                }
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



    public void updateReceipt() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    assert noSQLResult != null;
                    noSQLResult.updateItem(
                            recNameEditText.getText().toString(),
                            recTotalEditText.getText().toString(),
                            recDateEditText.getText().toString(),
                            selectedSpinnerItem
                    );
                } catch (final AmazonClientException ex) {
                    Log.e(TAG, "Failed saving updated item.", ex);
                    DynamoDBUtils.showErrorDialogForServiceException(ReceiptEditActivity.this,
                            getString(R.string.nosql_dialog_title_failed_update_item_text), ex);
                }
            }
        }).start();

        // launch the main activity
        Intent main = new Intent(ReceiptEditActivity.this, MainActivity.class);
        ReceiptEditActivity.this.startActivityForResult(main, 111);
        finish();
    }


    private void renameAndUploadFile() {
        boolean bool;
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
        final String total = recTotalEditText.getText().toString();
        // get the name entered by the edit text field
        final String friendlyName = recNameEditText.getText().toString();
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

                        String date = recDateEditText.getText().toString();
                        // Get date in correct format
                        String d = date.substring(0,2);
                        System.out.println("d = " + d);
                        String m = date.substring(3,5);
                        System.out.println("m = " + m);
                        String y = date.substring(6,10);
                        System.out.println("y = " + y);

                        String formattedDate = y + m + d;
                        System.out.println("formatted = " + formattedDate);

                        // Call insertdata to add the entered data into the database
                        insertData(fileName, date, fileName, formattedDate, total, friendlyName, category);

                        // launch the main activity
                        Intent main = new Intent(ReceiptEditActivity.this, MainActivity.class);
                        ReceiptEditActivity.this.startActivityForResult(main, 111);
                        finish();

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
        bmOptions.inSampleSize = 4;

        imageBitmap = BitmapFactory.decodeFile(filePath, bmOptions);

        try {
            //Display image in the correct orientation
            if(filePath!=null) {
                ExifInterface exif = new ExifInterface(filePath);
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                }
                imageBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);
            }

        }catch(IOException ex){
            Log.e("Failed to get Exif data", "ex");
        }

        imageBitmap = BitmapFactory.decodeFile(filePath, bmOptions);

        // set image view to the processed bitmap image
        receiptDisplayImageView.setImageBitmap(imageBitmap);
    }




}


