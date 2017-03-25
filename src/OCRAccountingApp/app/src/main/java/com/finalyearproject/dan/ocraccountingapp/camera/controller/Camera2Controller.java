package com.finalyearproject.dan.ocraccountingapp.camera.controller;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;


import com.finalyearproject.dan.ocraccountingapp.camera.manager.Camera2Manager;
import com.finalyearproject.dan.ocraccountingapp.camera.manager.CameraManager;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.view.AutoFitTextureView;
import com.finalyearproject.dan.ocraccountingapp.util.CameraHelper;
import com.finalyearproject.dan.ocraccountingapp.util.Size;

import java.io.File;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2Controller implements CameraController,
        CameraManager.CameraOpenListener<String, TextureView.SurfaceTextureListener>,
        CameraManager.CameraPhotoListener, CameraManager.CameraCloseListener<String> {

    private final static String TAG = "Camera2Controller";

    private String currentCameraId;
    private CameraManager.Rotation rotation;
    private CameraManager<String, TextureView.SurfaceTextureListener> camera2Manager;
    private CameraManager.CameraView cameraView;
    private File outputFile;

    public Camera2Controller(CameraManager.CameraView cameraView, CameraManager.Rotation rotation) {
        this.cameraView = cameraView;
        this.rotation = rotation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = Camera2Manager.getInstance();
        camera2Manager.initializeCameraManager(rotation, cameraView.getActivity());
        currentCameraId = camera2Manager.getFaceBackCameraId();
        int FLASH_CODE_ON = 1;
        camera2Manager.setFlashMode(FLASH_CODE_ON);
    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        camera2Manager.closeCamera(null);
        cameraView.releaseCameraPreview();
    }

    @Override
    public void onDestroy() {
        camera2Manager.releaseCameraManager();
    }

    @Override
    public void takePhoto() {
        outputFile = CameraHelper.getOutputFile(cameraView.getActivity());
        camera2Manager.takePhoto(outputFile, this);
    }

    @Override
    public File getOutputFile() {
        return outputFile;
    }

    @Override
    public void onCameraOpened(String openedCameraId, Size previewSize, TextureView.SurfaceTextureListener surfaceTextureListener) {
        cameraView.updateCameraPreview(previewSize, new AutoFitTextureView(cameraView.getActivity(), surfaceTextureListener));
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(String closedCameraId) {
        cameraView.releaseCameraPreview();

        camera2Manager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(File photoFile) {
        cameraView.onPhotoTaken();
    }
}
