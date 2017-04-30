package com.finalyearproject.dan.ocraccountingapp.signup;

import android.support.v7.widget.AppCompatButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;

import static org.junit.Assert.*;

public class SignUpConfirmActivityTest  extends
        ActivityInstrumentationTestCase2<SignUpConfirmActivity> {

    private SignUpConfirmActivity mTestActivity;

    private EditText mEmail;
    private EditText mConfirmCode;
    private Button mConfirmButton;


    public SignUpConfirmActivityTest() {
        super(SignUpConfirmActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mEmail = (EditText) mTestActivity
                .findViewById(R.id.confirm_account_username);
        mConfirmCode = (EditText) mTestActivity
                .findViewById(R.id.confirm_account_confirmation_code);
        mConfirmButton = (Button) mTestActivity
                .findViewById(R.id.confirm_account_button);
    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mEmail is null", mEmail);
        assertNotNull("mConfirmCode is null", mConfirmCode);
        assertNotNull("mConfirmButton is null", mConfirmButton);
    }
}
