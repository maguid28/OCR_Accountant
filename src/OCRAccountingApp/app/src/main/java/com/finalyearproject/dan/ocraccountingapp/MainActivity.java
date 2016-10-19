package com.finalyearproject.dan.ocraccountingapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
    }
}
