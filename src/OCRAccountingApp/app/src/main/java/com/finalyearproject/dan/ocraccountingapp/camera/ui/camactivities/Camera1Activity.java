package com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities;

import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.finalyearproject.dan.ocraccountingapp.camera.controller.Camera1Controller;
import com.finalyearproject.dan.ocraccountingapp.camera.controller.Camera2Controller;
import com.finalyearproject.dan.ocraccountingapp.camera.controller.Camera2ControllerAPI24;
import com.finalyearproject.dan.ocraccountingapp.camera.controller.CameraController;
import com.finalyearproject.dan.ocraccountingapp.camera.manager.CameraManager;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.BaseActivity;
import com.finalyearproject.dan.ocraccountingapp.util.CameraHelper;

@SuppressWarnings("deprecation")
public class Camera1Activity extends BaseActivity {

    @Override
    public CameraController createCameraController(CameraManager.CameraView cameraView, CameraManager.Rotation rotation) {


        if (CameraHelper.hasCamera2(Camera1Activity.this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Log.e("CAMERA 24", " ");
                return new Camera2ControllerAPI24(cameraView, rotation);
            }
            else {
                Log.e("CAMERA2", " ");
                return new Camera2Controller(cameraView, rotation);
            }
        } else {
            return new Camera1Controller(cameraView, rotation);
        }
    }

}
