package com.finalyearproject.dan.ocraccountingapp;

import android.content.Intent;
import android.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.googlecode.tesseract.android.TessBaseAPI;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View v) {

        if(v.getId() == R.id.ocrscan_button){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan.class);
            startActivity(i);
        }

        if(v.getId() == R.id.ocr_button_2){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan2.class);
            startActivity(i);
        }


        if(v.getId() == R.id.ocr_button_3){
            //this is the activity we need to launch
            Intent i = new Intent(this, OCRScan3.class);
            startActivity(i);
        }
    }
}
