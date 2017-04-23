package com.finalyearproject.dan.ocraccountingapp.calendar;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.regions.Regions;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.SQLObj;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.content.UserFileManager;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.DynamoDBUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResult;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import static com.finalyearproject.dan.ocraccountingapp.util.Orientation.exifToDegrees;

public class EditFragment extends Fragment {

    private static final String LOG_TAG = EditFragment.class.getSimpleName();

    String filePath;

    String receiptTitle;
    String receiptTotal;
    String receiptDate;
    String receiptCatagory;

    Bitmap imageBitmap;

    EditText recTotalEditText, recNameEditText;
    String selectedSpinnerItem;
    EditText recDateEditText;

    // The user file manager.
    private UserFileManager userFileManager;

    ImageView receiptDisplayImageView;

    NoSQLResult noSQLResult;
    SQLObj sqlObj;

    ExpandableRelativeLayout expandableLayout1;

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);


    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment.
        final View view = inflater.inflate(R.layout.activity_receipt_edit, container, false);

        AWSMobileClient.initializeMobileClientIfNecessary(getContext());

        // Get the bundle
        Bundle bundle = getArguments();

        recDateEditText = (EditText) view.findViewById(R.id.dateEdit);
        recTotalEditText = (EditText) view.findViewById(R.id.total_edit);
        recNameEditText = (EditText) view.findViewById(R.id.recNameEdit);

        try {
            //Extract the data passed from the previous activity
            filePath = bundle.getString("file_path_arg");
            receiptTitle = bundle.getString("TITLE");
            receiptTotal = bundle.getString("TOTAL");
            receiptDate = bundle.getString("DATE");
            receiptCatagory = bundle.getString("CATAGORY");
            sqlObj = (SQLObj) bundle.getSerializable("SQLObj");
            noSQLResult = sqlObj.getObj();
            System.out.println("theresult " + noSQLResult);


            recNameEditText.setText(receiptTitle, TextView.BufferType.EDITABLE);
            recTotalEditText.setText(receiptTotal, TextView.BufferType.EDITABLE);
            recDateEditText.setText(receiptDate, TextView.BufferType.EDITABLE);
        }
        catch (NullPointerException ignored) {}


        Button expandableButton = (Button) view.findViewById(R.id.expandableButton1);
        expandableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expandableLayout1 = (ExpandableRelativeLayout) view.findViewById(R.id.expandableLayout1);
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

                new DatePickerDialog(getContext(), R.style.datepickerCustom, date,myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });


        // display the image captured in the camera activity
        displayImage(view);

        // set up the floating action button
        configureFAB(view);

        Spinner categories = (Spinner) view.findViewById(R.id.expense_spinner);
        // Create the spinner from the expense catagories in arrays.xml and style it as spinner_item
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getContext(), R.array.expense_catagories, R.layout.spinner_item);
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

                                EditFragment.this.userFileManager = userFileManager;
                                userFileManagerCreatingLatch.countDown();
                                Log.e(LOG_TAG, "userfilemanager ..........................." + userFileManager);
                            }
                        });

        return view;
    }





    // display the image captured in the previous activity
    private void displayImage(View view){
        receiptDisplayImageView = (ImageView) view.findViewById(R.id.receipt_display);
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


    public void expandableButton1(View view) {
        expandableLayout1 = (ExpandableRelativeLayout) view.findViewById(R.id.expandableLayout1);
        expandableLayout1.toggle(); // toggle expand and collapse
    }


    // set up the floating action button
    public void configureFAB(View view){
        // open the context menu when the fab is clicked
        FloatingActionButton myFab = (FloatingActionButton) view.findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Are you sure you want to save?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // update the database entry
                                updateReceipt();
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
                    Log.e(LOG_TAG, "Failed saving updated item.", ex);
                    DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                            getString(R.string.nosql_dialog_title_failed_update_item_text), ex);
                }
            }
        }).start();

        Fragment fragment = new ViewPagerFragment();
        //fragment.setArguments(args);
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
