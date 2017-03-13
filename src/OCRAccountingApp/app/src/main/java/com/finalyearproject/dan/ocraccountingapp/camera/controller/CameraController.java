package com.finalyearproject.dan.ocraccountingapp.camera.controller;

import android.os.Bundle;

import java.io.File;

public interface CameraController {

    void onCreate(Bundle savedInstanceState);

    void onResume();

    void onPause();

    void onDestroy();

    void takePhoto();

    File getOutputFile();

}
