package com.finalyearproject.dan.ocraccountingapp.signup;

import android.support.v7.widget.AppCompatButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;

public class MFAActivityTest extends
        ActivityInstrumentationTestCase2<MFAActivity> {

    private MFAActivity mTestActivity;

    private EditText mCode;
    private Button mMfaButton;


    public MFAActivityTest() {
        super(MFAActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mCode = (EditText) mTestActivity
                .findViewById(R.id.mfa_code);
        mMfaButton = (Button) mTestActivity
                .findViewById(R.id.mfa_button);

    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mCode is null", mCode);
        assertNotNull("mButton is null", mMfaButton);
    }
}