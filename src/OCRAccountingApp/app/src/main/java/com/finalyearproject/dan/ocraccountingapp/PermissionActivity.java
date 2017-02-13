package com.finalyearproject.dan.ocraccountingapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PermissionActivity extends AppCompatActivity implements View.OnClickListener{


    private Context context;
    private Activity activity;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        context = getApplicationContext();
        activity = this;
        Button check_permission = (Button)findViewById(R.id.check_permission);
        Button request_permission = (Button)findViewById(R.id.request_permission);
        Button start_camera = (Button)findViewById(R.id.start_camera);
        check_permission.setOnClickListener(this);
        request_permission.setOnClickListener(this);
        start_camera.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        view = v;

        int id = v.getId();
        switch (id) {
            case R.id.check_permission:
                if (checkPermission()) {

                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();

                } else {

                    Snackbar.make(view, "Please request permission.", Snackbar.LENGTH_LONG).show();
                }
                break;
            case R.id.request_permission:
                if (!checkPermission()) {

                    requestPermission();

                } else {

                    Snackbar.make(view, "Permission already granted.", Snackbar.LENGTH_LONG).show();

                }
                break;

            case R.id.start_camera:
                Intent i = new Intent(this, CameraActivity.class);
                startActivity(i);
                break;
        }
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (result == PackageManager.PERMISSION_GRANTED){

            return true;

        } else {

            return false;

        }
    }

    private void requestPermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)){

            Toast.makeText(context,"We need to access th camera to capture receipts.",Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Snackbar.make(view,"Permission Granted, Now you can access camera.",Snackbar.LENGTH_LONG).show();
                    Intent i = new Intent(this, CameraActivity.class);
                    startActivity(i);

                } else {

                    Snackbar.make(view,"Permission Denied, You cannot access camera feature.",Snackbar.LENGTH_LONG).show();

                }
                break;
        }
    }

}
