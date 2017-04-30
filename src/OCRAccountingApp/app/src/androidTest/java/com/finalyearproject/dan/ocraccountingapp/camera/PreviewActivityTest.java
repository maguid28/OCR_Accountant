package com.finalyearproject.dan.ocraccountingapp.camera;

import android.test.ActivityInstrumentationTestCase2;
import android.view.ViewGroup;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.yalantis.ucrop.view.UCropView;

import static org.junit.Assert.*;

/**
 * Created by daniel on 30/04/2017.
 */
public class PreviewActivityTest extends
        ActivityInstrumentationTestCase2<PreviewActivity> {

    private PreviewActivity mTestActivity;
    private UCropView imagePreview;
    private ViewGroup postprocessPanel;

    public PreviewActivityTest() {
        super(PreviewActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
            super.setUp();

            mTestActivity = getActivity();
            imagePreview = (UCropView) mTestActivity.findViewById(R.id.image_view);
            postprocessPanel = (ViewGroup) mTestActivity.findViewById(R.id.post_process_panel);
    }

    public void testNotNull() {
            assertNotNull("mTestActivity is null", mTestActivity);
            assertNotNull("mTestRecNameText is null", imagePreview);
            assertNotNull("mTestRecDateText is null", postprocessPanel);
    }

}