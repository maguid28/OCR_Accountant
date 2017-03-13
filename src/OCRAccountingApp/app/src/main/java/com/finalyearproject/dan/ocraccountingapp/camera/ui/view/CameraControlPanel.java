package com.finalyearproject.dan.ocraccountingapp.camera.ui.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;

import com.finalyearproject.dan.ocraccountingapp.R;

public class CameraControlPanel extends RelativeLayout
        implements CaptureButton.RecordButtonListener{

    private Context context;

    private CaptureButton captureButton;

    private CaptureButton.RecordButtonListener recordButtonListener;

    public CameraControlPanel(Context context) {
        this(context, null);
    }

    public CameraControlPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        LayoutInflater.from(context).inflate(R.layout.camera_control_panel_layout, this);
        setBackgroundColor(Color.TRANSPARENT);

        captureButton = (CaptureButton) findViewById(R.id.record_button);
        setRecordButtonListener(recordButtonListener);

    }

    public void lockControls() {
        captureButton.setEnabled(false);
    }

    public void unLockControls() {
        captureButton.setEnabled(true);
    }

    public void setup() {
        captureButton.setup(this);
    }

    public void setRecordButtonListener(CaptureButton.RecordButtonListener recordButtonListener) {
        this.recordButtonListener = recordButtonListener;
    }

    @Override
    public void onTakePhotoButtonPressed() {
        if (recordButtonListener != null)
            recordButtonListener.onTakePhotoButtonPressed();
    }

    public void allowRecord(boolean isAllowed) {
        captureButton.setEnabled(isAllowed);
    }

}
