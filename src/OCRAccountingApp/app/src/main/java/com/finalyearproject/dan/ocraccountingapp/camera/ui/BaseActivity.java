package com.finalyearproject.dan.ocraccountingapp.camera.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.camactivities.PreviewActivity;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.view.CameraControlPanel;
import com.finalyearproject.dan.ocraccountingapp.camera.ui.view.CaptureButton;
import com.finalyearproject.dan.ocraccountingapp.camera.utils.Size;


public abstract class BaseActivity extends CameraActivity
        implements
        CaptureButton.RecordButtonListener{

    public static final int SENSOR_POSITION_UP = 90;
    public static final int SENSOR_POSITION_UP_SIDE_DOWN = 270;
    public static final int SENSOR_POSITION_LEFT = 0;
    public static final int SENSOR_POSITION_RIGHT = 180;
    public static final int SENSOR_POSITION_UNSPECIFIED = -1;

    public static final int ACTION_CONFIRM = 900;
    public static final int ACTION_RETAKE = 901;
    public static final int ACTION_CANCEL = 902;
    protected static final int REQUEST_PREVIEW_CODE = 1001;
    protected int PHOTO_ACTION_CODE = 101;

    private CameraControlPanel cameraControlPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onProcessBundle(Bundle savedInstanceState) {
        super.onProcessBundle(savedInstanceState);
    }

    @Override
    protected void onCameraControllerReady() {
        super.onCameraControllerReady();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraControlPanel.lockControls();
        cameraControlPanel.allowRecord(false);
    }


    @Override
    View getUserContentView(LayoutInflater layoutInflater, ViewGroup parent) {
        cameraControlPanel = (CameraControlPanel) layoutInflater.inflate(R.layout.camera_user_control_layout, parent, false);

        if (cameraControlPanel != null) {
            cameraControlPanel.setup();
            cameraControlPanel.setRecordButtonListener(this);
        }
        return cameraControlPanel;
    }



    @Override
    public void onTakePhotoButtonPressed() {
        getCameraController().takePhoto();
    }

    @Override
    protected void onScreenRotation(int degrees) {}


    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void updateCameraPreview(Size size, View cameraPreview) {
        cameraControlPanel.unLockControls();
        cameraControlPanel.allowRecord(true);

        setCameraPreview(cameraPreview, size);
    }


    @Override
    public void onPhotoTaken() {
        startPreviewActivity();
    }


    @Override
    public void releaseCameraPreview() {
        clearCameraPreview();
    }

    private void startPreviewActivity() {
        Intent intent = PreviewActivity.newIntent(this,
                PHOTO_ACTION_CODE, getCameraController().getOutputFile().toString());
        startActivityForResult(intent, REQUEST_PREVIEW_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PREVIEW_CODE) {

                if (PreviewActivity.isResultConfirm(data)) {
                    Intent resultIntent = new Intent();
                    String FILE_PATH = "FILE_PATH";
                    resultIntent.putExtra(FILE_PATH, PreviewActivity.getMediaFilePath(data));
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else if (PreviewActivity.isResultCancel(data)) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        }
    }

}
