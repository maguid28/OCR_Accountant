package com.finalyearproject.dan.ocraccountingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.finalyearproject.dan.ocraccountingapp.nav.NavDrawerInstaller;

import org.opencv.android.OpenCVLoader;


public class Sandbox extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add nav drawer
        NavDrawerInstaller navDrawerInstaller = new NavDrawerInstaller();
        navDrawerInstaller.installOnActivity(this, toolbar);

        if (!OpenCVLoader.initDebug()) {
            Log.e(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.d(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }


    }

    public void onButtonClick(View v) {

        if(v.getId() == R.id.ocrscan_button){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan.class);
            startActivity(i);
        }

        if(v.getId() == R.id.ocr_button_2){
            //this is the activity we need to launch
            Intent i = new Intent(this, ReceiptCaptureActivity.class);
            startActivity(i);
        }


        if(v.getId() == R.id.ocr_button_3){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan3.class);
            startActivity(i);
        }

        if(v.getId() == R.id.ocr_button_4){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan4.class);
            startActivity(i);
        }

        if(v.getId() == R.id.button5){
            //this is the activity we need to launch
                Intent i = new Intent(this, SplashActivity.class);
            startActivity(i);
        }
    }
}
