package com.finalyearproject.dan.ocraccountingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.finalyearproject.dan.ocraccountingapp.nav.NavDrawerInstaller;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Add nav drawer
        NavDrawerInstaller navDrawerInstaller = new NavDrawerInstaller();
        navDrawerInstaller.installOnActivity(this, toolbar);
    }
}
