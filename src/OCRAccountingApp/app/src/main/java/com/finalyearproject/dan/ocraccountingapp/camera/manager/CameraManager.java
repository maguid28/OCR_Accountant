package com.finalyearproject.dan.ocraccountingapp.camera.manager;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.Size;

import java.io.File;

public interface CameraManager<CameraId, SurfaceListener> {

    void initializeCameraManager(Rotation rotation, Context context);

    void openCamera(CameraId cameraId, CameraOpenListener<CameraId, SurfaceListener> cameraOpenListener);

    void closeCamera(CameraCloseListener<CameraId> cameraCloseListener);

    void takePhoto(File photoFile, CameraPhotoListener cameraPhotoListener);

    void setFlashMode(int flashMode);

    void releaseCameraManager();

    CameraId getFaceBackCameraId();

    interface Rotation {
        int getSensorPosition();
        int getDegrees();
    }

    interface CameraPhotoListener {
        void onPhotoTaken(File photoFile);
    }

    interface CameraOpenListener<CameraId, SurfaceListener> {
        void onCameraOpened(CameraId openedCameraId, Size previewSize, SurfaceListener surfaceListener);

        void onCameraOpenError();
    }

    interface CameraCloseListener<CameraId> {
        void onCameraClosed(CameraId closedCameraId);
    }

    interface CameraView {

        Activity getActivity();

        void updateCameraPreview(Size size, View cameraPreview);

        void onPhotoTaken();

        void releaseCameraPreview();

    }
}
