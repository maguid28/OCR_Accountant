package com.finalyearproject.dan.ocraccountingapp.signup;

import android.support.v7.widget.AppCompatButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.signin.SignInActivity;

import static org.junit.Assert.*;

public class SignUpActivityTest extends
        ActivityInstrumentationTestCase2<SignUpActivity> {

    private SignUpActivity mTestActivity;

    private EditText mEmail;
    private EditText mPassword;
    private EditText mName;
    private EditText mReEnterPassword;
    private AppCompatButton mCreateAccountButton;
    private TextView mAlreadyAMember;


    public SignUpActivityTest() {
        super(SignUpActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mEmail = (EditText) mTestActivity
                .findViewById(R.id.signup_email);
        mPassword = (EditText) mTestActivity
                .findViewById(R.id.signup_password);
        mName = (EditText) mTestActivity
                .findViewById(R.id.signup_given_name);
        mReEnterPassword = (EditText) mTestActivity
                .findViewById(R.id.input_reEnterPassword);
        mCreateAccountButton = (AppCompatButton) mTestActivity
                .findViewById(R.id.signup_create_account);
        mAlreadyAMember = (TextView) mTestActivity
                .findViewById(R.id.link_login);
    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mEmail is null", mEmail);
        assertNotNull("mPassword is null", mPassword);
        assertNotNull("mName is null", mName);
        assertNotNull("mReEnterPassword is null", mReEnterPassword);
        assertNotNull("mCreateAccountButton is null", mCreateAccountButton);
        assertNotNull("mAlreadyAMember is null", mAlreadyAMember);
    }
}