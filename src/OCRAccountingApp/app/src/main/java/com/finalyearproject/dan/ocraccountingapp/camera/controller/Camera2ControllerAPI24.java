package com.finalyearproject.dan.ocraccountingapp.camera.controller;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import com.finalyearproject.dan.ocraccountingapp.camera.manager.Camera1Manager;
import com.finalyearproject.dan.ocraccountingapp.camera.manager.CameraManager;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.view.AutoFitSurfaceView;
import com.finalyearproject.dan.ocraccountingapp.util.CameraHelper;
import com.finalyearproject.dan.ocraccountingapp.util.Size;

import java.io.File;

@TargetApi(Build.VERSION_CODES.N)
public class Camera2ControllerAPI24 implements CameraController,
        CameraManager.CameraOpenListener<Integer, SurfaceHolder.Callback>,
        CameraManager.CameraPhotoListener, CameraManager.CameraCloseListener<Integer> {

    private final static String TAG = "Camera2Controller";

    private String currentCameraId;
    private CameraManager.Rotation rotation;
    private CameraManager<Integer, SurfaceHolder.Callback> camera2Manager;
    private CameraManager.CameraView cameraView;

    private File outputFile;

    public Camera2ControllerAPI24(CameraManager.CameraView cameraView, CameraManager.Rotation rotation) {
        this.cameraView = cameraView;
        this.rotation = rotation;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        camera2Manager = Camera1Manager.getInstance();
        camera2Manager.initializeCameraManager(rotation, cameraView.getActivity());
        currentCameraId = String.valueOf(camera2Manager.getFaceBackCameraId());
        // turn flash on
        int FLASH_CODE_ON = 1;
        camera2Manager.setFlashMode(FLASH_CODE_ON);
    }

    @Override
    public void onResume() {
        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
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
    public void onCameraOpened(Integer openedCameraId, Size previewSize, SurfaceHolder.Callback surfaceTextureListener) {
        cameraView.updateCameraPreview(previewSize, new AutoFitSurfaceView(cameraView.getActivity(), surfaceTextureListener));
    }

    @Override
    public void onCameraOpenError() {
        Log.e(TAG, "onCameraOpenError");
    }

    @Override
    public void onCameraClosed(Integer closedCameraId) {
        cameraView.releaseCameraPreview();

        camera2Manager.openCamera(Integer.valueOf(currentCameraId), this);
    }

    @Override
    public void onPhotoTaken(File photoFile) {
        cameraView.onPhotoTaken();
    }
}
