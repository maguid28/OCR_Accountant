//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.14
//
package com.finalyearproject.dan.ocraccountingapp.signup;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyearproject.dan.ocraccountingapp.signin.SignInActivity;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.CognitoUserPoolsSignInProvider;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.signup.util.ViewHelper;

//Activity to prompt for account sign up information.
public class SignUpActivity extends Activity {

    private static final String LOG_TAG = SignUpActivity.class.getSimpleName();

    EditText _nameText, _emailText, _passwordText, _reEnterPasswordText;
    TextView _loginLink;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        _nameText = (EditText) findViewById(R.id.signup_given_name);
        _emailText = (EditText) findViewById(R.id.signup_email);
        _passwordText = (EditText) findViewById(R.id.signup_password);
        _reEnterPasswordText = (EditText) findViewById(R.id.input_reEnterPassword);
        _loginLink = (TextView) findViewById(R.id.link_login);

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    // Called by onclick to button
    public void signUp(final View view) {

        if (!validate()) {
            onSignupFailed();
            return;
        }

        final String givenName = ViewHelper.getStringValue(this, R.id.signup_given_name);
        final String email = ViewHelper.getStringValue(this, R.id.signup_email);
        final String password = ViewHelper.getStringValue(this, R.id.signup_password);
        String[] split = email.split("@");
        String username = split[0].toLowerCase();


        Log.d(LOG_TAG, "given_name = " + givenName);
        Log.d(LOG_TAG, "email = " + email);
        Log.d(LOG_TAG, "username = " + username);

        final Intent intent = new Intent();
        intent.putExtra(CognitoUserPoolsSignInProvider.AttributeKeys.USERNAME, username);
        intent.putExtra(CognitoUserPoolsSignInProvider.AttributeKeys.PASSWORD, password);
        intent.putExtra(CognitoUserPoolsSignInProvider.AttributeKeys.GIVEN_NAME, givenName);
        intent.putExtra(CognitoUserPoolsSignInProvider.AttributeKeys.EMAIL_ADDRESS, email);
        //intent.putExtra(CognitoUserPoolsSignInProvider.AttributeKeys.PHONE_NUMBER, phone);

        setResult(RESULT_OK, intent);

        finish();
    }


    public boolean validate() {
        boolean valid = true;

        String name = ViewHelper.getStringValue(this, R.id.signup_given_name);
        String email = ViewHelper.getStringValue(this, R.id.signup_email);
        String password = ViewHelper.getStringValue(this, R.id.signup_password);
        String reEnterPassword = ViewHelper.getStringValue(this, R.id.input_reEnterPassword);

        boolean hasUppercase = !password.equals(password.toLowerCase());
        boolean hasLowercase = !password.equals(password.toUpperCase());

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8) {
            _passwordText.setError("at least 8 characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }
        if(!hasLowercase || !hasUppercase || !(password.matches(".*\\d.*"))) {
            _passwordText.setError("Password must contain uppcase, lowercase and numeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (reEnterPassword.isEmpty()) {
            _reEnterPasswordText.setError("Field cannot be empty");
            valid = false;
        }
        else if(reEnterPassword.length() < 4) {
            _reEnterPasswordText.setError("Password must be greater than 4 characters");
            valid = false;
        }
        else if(!(reEnterPassword.equals(password))) {
            _reEnterPasswordText.setError("Passwords do not match");
            valid = false;
        }
        else {
            _reEnterPasswordText.setError(null);
        }

        return valid;
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        //_signupButton.setEnabled(true);
    }
}
