package com.finalyearproject.dan.ocraccountingapp.signin;

import android.support.v7.widget.AppCompatButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.camera.OpenCVCamera;

import org.opencv.android.JavaCameraView;

import static org.junit.Assert.*;

public class SignInActivityTest extends
        ActivityInstrumentationTestCase2<SignInActivity> {

    private SignInActivity mTestActivity;
    private EditText mEmail;
    private EditText mPassword;
    private AppCompatButton mSignInButton;
    private TextView mCreateNewAccount;
    private TextView mForgotPassword;
    private TextView mVerifyAccount;
    private ImageButton mFBLoginButton;


    public SignInActivityTest() {
        super(SignInActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mTestActivity = getActivity();
        mFBLoginButton = (ImageButton) mTestActivity
                .findViewById(R.id.fb_login_button);
        mEmail = (EditText) mTestActivity
                .findViewById(R.id.signIn_editText_email);
        mPassword = (EditText) mTestActivity
                .findViewById(R.id.signIn_editText_password);
        mSignInButton = (AppCompatButton) mTestActivity
                .findViewById(R.id.signIn_imageButton_login);
        mCreateNewAccount = (TextView) mTestActivity
                .findViewById(R.id.signIn_textView_CreateNewAccount);
        mForgotPassword = (TextView) mTestActivity
                .findViewById(R.id.signIn_textView_ForgotPassword);
        mVerifyAccount = (TextView) mTestActivity
                .findViewById(R.id.signIn_textView_verifyAccount);

    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mEmail is null", mEmail);
        assertNotNull("mPassword is null", mPassword);
        assertNotNull("mSignInButton is null", mSignInButton);
        assertNotNull("mCreateNewAccount is null", mCreateNewAccount);
        assertNotNull("mForgotPassword is null", mForgotPassword);
        assertNotNull("mVerifyAccount is null", mVerifyAccount);
        assertNotNull("mFBLoginButton is null", mFBLoginButton);
    }
}