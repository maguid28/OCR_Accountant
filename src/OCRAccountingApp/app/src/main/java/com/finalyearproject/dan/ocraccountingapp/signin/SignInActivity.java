package com.finalyearproject.dan.ocraccountingapp.signin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.finalyearproject.dan.ocraccountingapp.MainActivity;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityProvider;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.CognitoUserPoolsSignInProvider;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.FacebookSignInProvider;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.signin.SignInManager;
import com.finalyearproject.dan.ocraccountingapp.signup.SignUpConfirmActivity;

public class SignInActivity extends Activity {
    private static final String LOG_TAG = SignInActivity.class.getSimpleName();

    private SignInManager signInManager;

    EditText emailText;
    EditText passwordText;
    Button loginButton;
    TextView signupLink;
    TextView verifyAccount;

    // Start of Intent request codes owned by the Cognito User Pools app.
    private static final int REQUEST_CODE_START = 0x2970;
    /** Request code for account verification Intent. */
    private static final int VERIFICATION_REQUEST_CODE = REQUEST_CODE_START + 45;


    /**
     * SignInResultsHandler handles the final result from sign in. Making it static is a best
     * practice since it may outlive the SplashActivity's life span.
     */
    private class SignInResultsHandler implements IdentityManager.SignInResultsHandler {
        /**
         * Receives the successful sign-in result and starts the main activity.
         * @param provider the identity provider used for sign-in.
         */
        @Override
        public void onSuccess(final IdentityProvider provider) {
            Log.d(LOG_TAG, String.format("User sign-in with %s succeeded",
                provider.getDisplayName()));

            // The sign-in manager is no longer needed once signed in.
            SignInManager.dispose();

            Toast.makeText(SignInActivity.this, String.format("Sign-in with %s succeeded.",
                provider.getDisplayName()), Toast.LENGTH_LONG).show();

            // Load user name and image.
            AWSMobileClient.defaultMobileClient()
                .getIdentityManager().loadUserInfoAndImage(provider, new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Launching Main Activity...");
                    startActivity(new Intent(SignInActivity.this, MainActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    // finish should always be called on the main thread.
                    finish();
                }
            });
        }

        /**
         * Receives the sign-in result indicating the user canceled and shows a toast.
         * @param provider the identity provider with which the user attempted sign-in.
         */
        @Override
        public void onCancel(final IdentityProvider provider) {
            Log.d(LOG_TAG, String.format("User sign-in with %s canceled.",
                provider.getDisplayName()));

            Toast.makeText(SignInActivity.this, String.format("Sign-in with %s canceled.",
                provider.getDisplayName()), Toast.LENGTH_LONG).show();
        }

        /**
         * Receives the sign-in result that an error occurred signing in and shows a toast.
         * @param provider the identity provider with which the user attempted sign-in.
         * @param ex the exception that occurred.
         */
        @Override
        public void onError(final IdentityProvider provider, final Exception ex) {
            Log.e(LOG_TAG, String.format("User Sign-in failed for %s : %s",
                provider.getDisplayName(), ex.getMessage()), ex);

            final AlertDialog.Builder errorDialogBuilder = new AlertDialog.Builder(SignInActivity.this);
            errorDialogBuilder.setTitle("Sign-In Error");
            errorDialogBuilder.setMessage(
                String.format("Sign-in with %s failed.\n%s", provider.getDisplayName(), ex.getMessage()));
            errorDialogBuilder.setNeutralButton("Ok", null);
            errorDialogBuilder.show();
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailText = (EditText) findViewById(R.id.signIn_editText_email);
        passwordText = (EditText) findViewById(R.id.signIn_editText_password);
        loginButton = (Button) findViewById(R.id.signIn_imageButton_login);
        signupLink = (TextView) findViewById(R.id.signIn_textView_CreateNewAccount);
        verifyAccount = (TextView) findViewById(R.id.signIn_textView_verifyAccount);
        signInManager = SignInManager.getInstance(this);


        signInManager.setResultsHandler(this, new SignInResultsHandler());

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        verifyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent;

                cameraIntent = new Intent(SignInActivity.this, SignUpConfirmActivity.class);

                //Intent cameraIntent = new Intent(activity, CamActivity.class);
                SignInActivity.this.startActivityForResult(cameraIntent, VERIFICATION_REQUEST_CODE);
            }
        });


        signInManager.initializeSignInButton(CognitoUserPoolsSignInProvider.class,
                this.findViewById(R.id.signIn_imageButton_login));


        // Initialize sign-in buttons.
        signInManager.initializeSignInButton(FacebookSignInProvider.class,
            this.findViewById(R.id.fb_login_button));


        /*
        //code to generate hash for facebook
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.finalyearproject.dan.ocraccountingapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {} catch (NoSuchAlgorithmException e) {}
        */
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        signInManager.handleActivityResult(requestCode, resultCode, data);
    }







    public void login() {
        Log.d(LOG_TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        loginButton.setEnabled(false);


        // TODO: Implement your own authentication logic here.

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                    }
                }, 3000);
    }



    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        } else {
            emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            passwordText.setError("between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            passwordText.setError(null);
        }

        return valid;
    }
}
