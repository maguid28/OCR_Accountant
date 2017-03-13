package com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities;

import android.util.Log;

import com.finalyearproject.dan.ocraccountingapp.camera.controller.Camera1Controller;
import com.finalyearproject.dan.ocraccountingapp.camera.controller.CameraController;
import com.finalyearproject.dan.ocraccountingapp.camera.manager.CameraManager;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.BaseActivity;

@SuppressWarnings("deprecation")
public class Camera1Activity extends BaseActivity {

    @Override
    public CameraController createCameraController(CameraManager.CameraView cameraView, CameraManager.Rotation rotation) {
        Log.e("CAMERA1", " ");
        return new Camera1Controller(cameraView, rotation);
    }

}
