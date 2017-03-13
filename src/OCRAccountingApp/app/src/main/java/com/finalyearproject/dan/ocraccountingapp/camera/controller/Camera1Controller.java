package com.finalyearproject.dan.ocraccountingapp.camera.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;


import com.finalyearproject.dan.ocraccountingapp.camera.manager.Camera1Manager;
import com.finalyearproject.dan.ocraccountingapp.camera.manager.CameraManager;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.view.AutoFitSurfaceView;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.CameraHelper;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.Size;

import java.io.File;


@SuppressWarnings("deprecation")
public class Camera1Controller implements CameraController,
        CameraManager.CameraOpenListener<Integer, SurfaceHolder.Callback>, CameraManager.CameraPhotoListener, CameraManager.CameraCloseListener<Integer> {

    private final static String TAG = "Camera1Controller";

    private Integer currentCameraId;
    private CameraManager.Rotation rotation;
    private CameraManager<Integer, SurfaceHolder.Callback> cameraManager;
    private CameraManager.CameraView cameraView;

    private File outputFile;

    public Camera1Controller(CameraManager.CameraView cameraView, CameraManager.Rotation rotation) {
        this.cameraView = cameraView;
        this.rotation = rotation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        cameraManager = Camera1Manager.getInstance();
        cameraManager.initializeCameraManager(rotation, cameraView.getActivity());
        currentCameraId = cameraManager.getFaceBackCameraId();
        int FLASH_CODE_ON = 1;
        cameraManager.setFlashMode(FLASH_CODE_ON);
    }

    @Override
    public void onResume() {
        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPause() {
        cameraManager.closeCamera(null);
    }

    @Override
    public void onDestroy() {
        cameraManager.releaseCameraManager();
    }

    @Override
    public void takePhoto() {
        outputFile = CameraHelper.getOutputMediaFile(cameraView.getActivity());
        cameraManager.takePhoto(outputFile, this);
    }


    @Override
    public File getOutputFile() {
        return outputFile;
    }


    @Override
    public void onCameraOpened(Integer cameraId, Size previewSize, SurfaceHolder.Callback surfaceCallback) {
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(cameraView.getActivity(), surfaceCallback));
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        cameraManager.openCamera(currentCameraId, this);
    }

    @Override
    public void onPhotoTaken(File photoFile) {
        Log.e("PHOTOFILE ", photoFile.toString());

        cameraView.onPhotoTaken();
    }
}
