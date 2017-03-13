package com.finalyearproject.dan.ocraccountingapp;

import android.os.Bundle;

//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.14
//

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.SignInManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.SignInProvider;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityProvider;
import com.finalyearproject.dan.ocraccountingapp.signin.SignInActivity;

import java.util.concurrent.CountDownLatch;

public class SplashActivity extends Activity {
    private static final String LOG_TAG = SplashActivity.class.getSimpleName();
    private final CountDownLatch timeoutLatch = new CountDownLatch(1);
    private SignInManager signInManager;

    // SignInResultsHandler handles the results from sign-in for a previously signed in user.
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler {
        // onSuccess is launched if the user is already signed in
        @Override
        public void onSuccess(final IdentityProvider provider) {
            Log.d(LOG_TAG, String.format("User sign-in with previous %s provider succeeded",
                    provider.getDisplayName()));

            // The sign-in manager is no longer needed once signed in.
            SignInManager.dispose();

            Toast.makeText(SplashActivity.this, String.format("Sign-in with %s succeeded.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();

            AWSMobileClient.defaultMobileClient()
                    .getIdentityManager()
                    .loadUserInfoAndImage(provider, new Runnable() {
                        @Override
                        public void run() {
                            goMain();
                        }
                    });
        }

        @Override
        public void onCancel(final IdentityProvider provider) {}

        // onError occurs if the user
        @Override
        public void onError(final IdentityProvider provider, Exception ex) {
            Log.e(LOG_TAG,
                    String.format("Cognito credentials refresh with %s provider failed. Error: %s",
                            provider.getDisplayName(), ex.getMessage()), ex);

            Toast.makeText(SplashActivity.this, String.format("Sign-in with %s failed.",
                    provider.getDisplayName()), Toast.LENGTH_LONG).show();
            goSignIn();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Thread thread = new Thread(new Runnable() {
            public void run() {
                signInManager = SignInManager.getInstance(SplashActivity.this);

                final SignInProvider provider = signInManager.getPreviouslySignedInProvider();

                // if the user was already previously in to a provider.
                if (provider != null) {
                    // asynchronously handle refreshing credentials and call our handler.
                    signInManager.refreshCredentialsWithProvider(SplashActivity.this,
                            provider, new SignInResultsHandler());
                } else {
                    // Asyncronously go to the sign-in page (after the splash delay has expired).
                    goSignIn();
                }

                // Wait for the splash timeout.
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) { }

                // Expire the splash page delay.
                timeoutLatch.countDown();
            }
        });
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Touch event bypasses waiting for the splash timeout to expire.
        timeoutLatch.countDown();
        return true;
    }

    // Start activity after timeout
    private void goAfterSplashTimeout(final Intent intent) {
        final Thread thread = new Thread(new Runnable() {
            public void run() {
                // wait for the splash timeout expiry or for the user to tap.
                try {
                    timeoutLatch.await();
                } catch (InterruptedException ignored) {
                }

                SplashActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });
        thread.start();
    }

    // Launch main activity
    protected void goMain() {
        Log.d(LOG_TAG, "Launching Main Activity...");
        goAfterSplashTimeout(new Intent(this, MainActivity.class));
    }

    // Launch sign in activity
    protected void goSignIn() {
        Log.d(LOG_TAG, "Launching Sign-in Activity...");
        goAfterSplashTimeout(new Intent(this, SignInActivity.class));
    }
}

