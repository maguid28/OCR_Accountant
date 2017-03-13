package com.finalyearproject.dan.ocraccountingapp.camera.manager;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.finalyearproject.dan.ocraccountingapp.camera.utils.Size;

abstract class BaseCameraManager<CameraId, SurfaceListener>
        implements CameraManager<CameraId, SurfaceListener> {

    private static final String TAG = "BaseCameraManager";

    protected Context context;
    Rotation rotation;


    CameraId currentCameraId = null;
    CameraId faceFrontCameraId = null;
    CameraId faceBackCameraId = null;
    int numberOfCameras = 0;
    int faceFrontCameraOrientation;
    int faceBackCameraOrientation;

    Size photoSize;
    Size previewSize;
    Size windowSize;

    private HandlerThread backgroundThread;
    Handler backgroundHandler;
    Handler uiHandler = new Handler(Looper.getMainLooper());

    @Override
    public void initializeCameraManager(Rotation rotation, Context context) {
        this.context = context;
        this.rotation = rotation;
        startBackgroundThread();
    }

    @Override
    public void releaseCameraManager() {
        this.context = null;
        stopBackgroundThread();
    }

    protected abstract void prepareCameraOutputs();

    protected abstract int getPhotoOrientation(int sensorPosition);

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (Build.VERSION.SDK_INT > 17) {
            backgroundThread.quitSafely();
        } else backgroundThread.quit();

        try {
            backgroundThread.join();
        } catch (InterruptedException e) {
            Log.e(TAG, "stopBackgroundThread: ", e);
        } finally {
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    public CameraId getFaceBackCameraId() {
        return faceBackCameraId;
    }



}
