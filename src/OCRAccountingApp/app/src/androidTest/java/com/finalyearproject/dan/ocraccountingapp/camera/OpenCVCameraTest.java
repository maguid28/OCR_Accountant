package com.finalyearproject.dan.ocraccountingapp.camera;

import android.support.design.widget.FloatingActionButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;

import org.opencv.android.JavaCameraView;

import static org.junit.Assert.*;

public class OpenCVCameraTest  extends
        ActivityInstrumentationTestCase2<OpenCVCamera> {

    private OpenCVCamera mTestActivity;
    private ImageButton mCaptureButton;
    private JavaCameraView mSurfaceView;


    public OpenCVCameraTest() {
        super(OpenCVCamera.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mCaptureButton = (ImageButton) mTestActivity
                .findViewById(R.id.captureBTN);
        mSurfaceView = (JavaCameraView) mTestActivity
                .findViewById(R.id.camera_surface_view);
    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mCaptureButton is null", mCaptureButton);
        assertNotNull("mSurfaceView is null", mSurfaceView);
    }
}
