package com.finalyearproject.dan.ocraccountingapp.signup;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;

import com.finalyearproject.dan.ocraccountingapp.R;

import static org.junit.Assert.*;

public class ForgotPasswordActivityTest extends
        ActivityInstrumentationTestCase2<ForgotPasswordActivity> {

    private ForgotPasswordActivity mTestActivity;

    private EditText mPassword;
    private EditText mCode;
    private Button mSetPasswordButton;


    public ForgotPasswordActivityTest() {
        super(ForgotPasswordActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mPassword = (EditText) mTestActivity
                .findViewById(R.id.forgot_password_password);
        mCode = (EditText) mTestActivity
                .findViewById(R.id.verification_code);
        mSetPasswordButton = (Button) mTestActivity
                .findViewById(R.id.set_password_button);

    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mCode is null", mCode);
        assertNotNull("mSetPasswordButton is null", mSetPasswordButton);
        assertNotNull("mPassword is null", mPassword);

    }
}
