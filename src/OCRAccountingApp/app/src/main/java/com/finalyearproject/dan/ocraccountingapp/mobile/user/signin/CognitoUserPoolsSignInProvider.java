package com.finalyearproject.dan.ocraccountingapp.mobile.user.signin;
//
// Copyright 2017 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.14
//

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.finalyearproject.dan.ocraccountingapp.mobile.AWSConfiguration;
import com.finalyearproject.dan.ocraccountingapp.mobile.user.IdentityManager;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.finalyearproject.dan.ocraccountingapp.R;

import com.finalyearproject.dan.ocraccountingapp.signup.ForgotPasswordActivity;
import com.finalyearproject.dan.ocraccountingapp.signup.MFAActivity;
import com.finalyearproject.dan.ocraccountingapp.signup.SignUpActivity;
import com.finalyearproject.dan.ocraccountingapp.signup.SignUpConfirmActivity;
import com.finalyearproject.dan.ocraccountingapp.signup.util.ViewHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static com.finalyearproject.dan.ocraccountingapp.R.string.login_failed;
import static com.finalyearproject.dan.ocraccountingapp.R.string.login_success;
import static com.finalyearproject.dan.ocraccountingapp.R.string.password_change_failed;
import static com.finalyearproject.dan.ocraccountingapp.R.string.password_change_success;
import static com.finalyearproject.dan.ocraccountingapp.R.string.sign_up_confirm_failed;
import static com.finalyearproject.dan.ocraccountingapp.R.string.sign_up_confirm_success;

import static com.finalyearproject.dan.ocraccountingapp.R.string.sign_up_failed;
import static com.finalyearproject.dan.ocraccountingapp.R.string.sign_up_success;
import static com.finalyearproject.dan.ocraccountingapp.R.string.title_activity_forgot_password;
import static com.finalyearproject.dan.ocraccountingapp.R.string.title_activity_sign_in;
import static com.finalyearproject.dan.ocraccountingapp.R.string.title_activity_sign_up;
import static com.finalyearproject.dan.ocraccountingapp.R.string.title_activity_sign_up_confirm;


/**
 * Manages sign-in using Cognito User Pools.
 */
public class CognitoUserPoolsSignInProvider implements SignInProvider {
    /**
     * Cognito User Pools attributes.
     */
    public final class AttributeKeys {
        /** Username attribute. */
        public static final String USERNAME = "username";

        /** Password attribute. */
        public static final String PASSWORD = "password";

        /** Verification code attribute. */
        public static final String VERIFICATION_CODE = "verification_code";

        /** Given name attribute. */
        public static final String GIVEN_NAME = "given_name";

        /** Email address attribute. */
        public static final String EMAIL_ADDRESS = "email";

        /** Phone number attribute. */
        public static final String PHONE_NUMBER = "phone_number";
    }

    /** Log tag. */
    private static final String LOG_TAG = CognitoUserPoolsSignInProvider.class.getSimpleName();

    /** Resource ID of the Username field. */
    private static final int EDIT_TEXT_USERNAME_ID = R.id.signIn_editText_email;

    /** Resource ID of the Password field. */
    private static final int EDIT_TEXT_PASSWORD_ID = R.id.signIn_editText_password;

    /** Resource ID of the Forgot Password button. */
    private static final int TEXT_VIEW_FORGOT_PASSWORD_ID = R.id.signIn_textView_ForgotPassword;

    /** Resource ID of the Create Account button. */
    private static final int TEXT_VIEW_CREATE_ACCOUNT_ID = R.id.signIn_textView_CreateNewAccount;

    /** Start of Intent request codes owned by the Cognito User Pools app. */
    private static final int REQUEST_CODE_START = 0x2970;

    /** Request code for password reset Intent. */
    private static final int FORGOT_PASSWORD_REQUEST_CODE = REQUEST_CODE_START + 42;

    /** Request code for account registration Intent. */
    private static final int SIGN_UP_REQUEST_CODE = REQUEST_CODE_START + 43;

    /** Request code for MFA Intent. */
    private static final int MFA_REQUEST_CODE = REQUEST_CODE_START + 44;

    /** Request code for account verification Intent. */
    private static final int VERIFICATION_REQUEST_CODE = REQUEST_CODE_START + 45;

    /** Request codes that the Cognito User Pools can handle. */
    private static final Set<Integer> REQUEST_CODES = new HashSet<Integer>() {{
        add(FORGOT_PASSWORD_REQUEST_CODE);
        add(SIGN_UP_REQUEST_CODE);
        add(MFA_REQUEST_CODE);
        add(VERIFICATION_REQUEST_CODE);
    }};

    /** The sign-in results adapter from the SignInManager. */
    private IdentityManager.SignInResultsHandler resultsHandler;

    /** Forgot Password processing provided by the Cognito User Pools SDK. */
    private ForgotPasswordContinuation forgotPasswordContinuation;

    /** MFA processing provided by the Cognito User Pools SDK. */
    private MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation;

    /** Android context. */
    private final Context context;

    /** Invoking Android Activity. */
    private Activity activity;

    /** Sign-in username. */
    private String username;

    /** Sign-in password. */
    private String password;

    /** Sign-in verification code. */
    private String verificationCode;

    /** Active Cognito User Pool. */
    private CognitoUserPool cognitoUserPool;

    /** Active Cognito User Pools session. */
    private CognitoUserSession cognitoUserSession;

    /** Latch to ensure Cognito User Pools SDK is initialized before attempting to read the authorization token. */
    private final CountDownLatch initializedLatch = new CountDownLatch(1);

    /**
     * Handle callbacks from the Forgot Password flow.
     */
    private ForgotPasswordHandler forgotPasswordHandler = new ForgotPasswordHandler() {
        @Override
        public void onSuccess() {
            Log.d(LOG_TAG, "Password change succeeded.");
            ViewHelper.showDialog(activity, activity.getString(title_activity_forgot_password),
                    activity.getString(password_change_success));
        }

        @Override
        public void getResetCode(final ForgotPasswordContinuation continuation) {
            forgotPasswordContinuation = continuation;

            final Intent intent = new Intent(context, ForgotPasswordActivity.class);
            activity.startActivityForResult(intent, FORGOT_PASSWORD_REQUEST_CODE);
        }

        @Override
        public void onFailure(final Exception exception) {
            Log.e(LOG_TAG, "Password change failed.", exception);
            ViewHelper.showDialog(activity, activity.getString(title_activity_forgot_password),
                    activity.getString(password_change_failed) + " " + exception);
        }
    };

    /**
     * Handle callbacks from the Sign Up flow.
     */
    private SignUpHandler signUpHandler = new SignUpHandler() {
        @Override
        public void onSuccess(final CognitoUser user, final boolean signUpConfirmationState,
                              final CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails) {
            if (signUpConfirmationState) {
                Log.d(LOG_TAG, "Signed up. User ID = " + user.getUserId());
                ViewHelper.showDialog(activity, activity.getString(title_activity_sign_up),
                        activity.getString(sign_up_success) + " " + user.getUserId());
            } else {
                Log.w(LOG_TAG, "Additional confirmation for sign up.");

                final Intent intent = new Intent(context, SignUpConfirmActivity.class);
                activity.startActivityForResult(intent, VERIFICATION_REQUEST_CODE);
            }
        }

        @Override
        public void onFailure(final Exception exception) {
            Log.e(LOG_TAG, "Sign up failed.", exception);
            ViewHelper.showDialog(activity, activity.getString(title_activity_sign_up),
                    activity.getString(sign_up_failed));
        }
    };

    /**
     * Handle callbacks from the Sign Up - Confirm Account flow.
     */
    private GenericHandler signUpConfirmationHandler = new GenericHandler() {
        @Override
        public void onSuccess() {
            Log.i(LOG_TAG, "Confirmed.");
            ViewHelper.showDialog(activity, activity.getString(title_activity_sign_up_confirm),
                    activity.getString(sign_up_confirm_success));
        }

        @Override
        public void onFailure(Exception exception) {
            Log.e(LOG_TAG, "Failed to confirm user.", exception);
            ViewHelper.showDialog(activity, activity.getString(title_activity_sign_up_confirm),
                    activity.getString(sign_up_confirm_failed) + " " + exception);
        }
    };

    /**
     * Handle callbacks from the Authentication flow. Includes MFA handling.
     */
    private AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
        @Override
        public void onSuccess(final CognitoUserSession userSession, final CognitoDevice newDevice) {
            Log.i(LOG_TAG, "Logged in. " + userSession.getIdToken());

            cognitoUserSession = userSession;

            if (null != resultsHandler) {
                ViewHelper.showDialog(activity, activity.getString(title_activity_sign_in),
                        activity.getString(login_success) + " " + userSession.getIdToken());

                resultsHandler.onSuccess(CognitoUserPoolsSignInProvider.this);
            }

            initializedLatch.countDown();
        }

        @Override
        public void getAuthenticationDetails(
                final AuthenticationContinuation authenticationContinuation, final String userId) {

            if (null != username && null != password) {
                final AuthenticationDetails authenticationDetails = new AuthenticationDetails(
                        username,
                        password,
                        null);

                authenticationContinuation.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation.continueTask();
            }

            initializedLatch.countDown();
        }

        @Override
        public void getMFACode(final MultiFactorAuthenticationContinuation continuation) {
            multiFactorAuthenticationContinuation = continuation;

            final Intent intent = new Intent(context, MFAActivity.class);
            activity.startActivityForResult(intent, MFA_REQUEST_CODE);
        }

        @Override
        public void authenticationChallenge(final ChallengeContinuation continuation) {
            throw new UnsupportedOperationException("Not supported in this sample.");
        }

        @Override
        public void onFailure(final Exception exception) {
            Log.e(LOG_TAG, "Failed to login.", exception);

            if (null != resultsHandler) {
                ViewHelper.showDialog(activity, activity.getString(R.string.title_activity_sign_in),
                        activity.getString(login_failed) + " " + exception);

                resultsHandler.onError(CognitoUserPoolsSignInProvider.this, exception);
            }

            initializedLatch.countDown();
        }
    };

    /**
     * Constructor. Initializes the Cognito User Pool.
     * @param context Android context.
     */
    public CognitoUserPoolsSignInProvider(final Context context) {
        this.context = context;

        this.cognitoUserPool = new CognitoUserPool(context,
                AWSConfiguration.AMAZON_COGNITO_USER_POOL_ID,
                AWSConfiguration.AMAZON_COGNITO_USER_POOL_CLIENT_ID,
                AWSConfiguration.AMAZON_COGNITO_USER_POOL_CLIENT_SECRET,
                AWSConfiguration.AMAZON_COGNITO_REGION);

        cognitoUserPool.getCurrentUser().getSession(authenticationHandler);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRequestCodeOurs(final int requestCode) {
        return REQUEST_CODES.contains(requestCode);
    }

    /** {@inheritDoc} */
    @Override
    public void handleActivityResult(final int requestCode,
                                     final int resultCode,
                                     final Intent data) {

        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case FORGOT_PASSWORD_REQUEST_CODE:
                    password = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.PASSWORD);
                    verificationCode = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.VERIFICATION_CODE);

                    Log.d(LOG_TAG, "verificationCode = " + verificationCode);

                    forgotPasswordContinuation.setPassword(password);
                    forgotPasswordContinuation.setVerificationCode(verificationCode);
                    forgotPasswordContinuation.continueTask();
                    break;
                case SIGN_UP_REQUEST_CODE:
                    username = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.USERNAME);
                    password = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.PASSWORD);
                    final String givenName = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.GIVEN_NAME);
                    final String email = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.EMAIL_ADDRESS);
                    final String phone = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.PHONE_NUMBER);

                    Log.d(LOG_TAG, "username = " + username);
                    Log.d(LOG_TAG, "given_name = " + givenName);
                    Log.d(LOG_TAG, "email = " + email);
                    Log.d(LOG_TAG, "phone = " + phone);

                    final CognitoUserAttributes userAttributes = new CognitoUserAttributes();
                    userAttributes.addAttribute(CognitoUserPoolsSignInProvider.AttributeKeys.GIVEN_NAME, givenName);
                    userAttributes.addAttribute(CognitoUserPoolsSignInProvider.AttributeKeys.EMAIL_ADDRESS, email);

                    if (null != phone && phone.length() > 0) {
                        userAttributes.addAttribute(CognitoUserPoolsSignInProvider.AttributeKeys.PHONE_NUMBER, phone);
                    }

                    cognitoUserPool.signUpInBackground(username, password, userAttributes,
                            null, signUpHandler);

                    break;
                case MFA_REQUEST_CODE:
                    verificationCode = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.VERIFICATION_CODE);

                    Log.d(LOG_TAG, "verificationCode = " + verificationCode);

                    multiFactorAuthenticationContinuation.setMfaCode(verificationCode);
                    multiFactorAuthenticationContinuation.continueTask();

                    break;
                case VERIFICATION_REQUEST_CODE:
                    username = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.USERNAME);
                    verificationCode = data.getStringExtra(CognitoUserPoolsSignInProvider.AttributeKeys.VERIFICATION_CODE);

                    Log.d(LOG_TAG, "username = " + username);
                    Log.d(LOG_TAG, "verificationCode = " + verificationCode);

                    final CognitoUser cognitoUser = cognitoUserPool.getUser(username);

                    cognitoUser.confirmSignUpInBackground(verificationCode, true, signUpConfirmationHandler);

                    break;
            }
        }

    }

    /** {@inheritDoc} */
    @Override
    public View.OnClickListener initializeSignInButton(final Activity signInActivity,
                                                       final View buttonView,
                                                       final IdentityManager.SignInResultsHandler resultsHandler) {

        this.activity = signInActivity;
        this.resultsHandler = resultsHandler;

        // User Pools requires sign in with the username or verified channel.
        // Mobile Hub does not set up email verification because it requires SES verification.
        // Hence, prompt customers to login using the username or phone number.
        final EditText emailField = (EditText) activity.findViewById(EDIT_TEXT_USERNAME_ID);
        emailField.setHint(R.string.sign_in_username);

        activity.findViewById(TEXT_VIEW_CREATE_ACCOUNT_ID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SignUpActivity.class);
                activity.startActivityForResult(intent, SIGN_UP_REQUEST_CODE);
            }
        });

        activity.findViewById(TEXT_VIEW_FORGOT_PASSWORD_ID).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = ViewHelper.getStringValue(activity, EDIT_TEXT_USERNAME_ID);
                if (null == username || username.length() < 1) {
                    Log.w(LOG_TAG, "Missing username.");
                    ViewHelper.showDialog(activity, activity.getString(title_activity_sign_in),
                            "Missing username.");
                } else {

                    final CognitoUser cognitoUser = cognitoUserPool.getUser(username);

                    cognitoUser.forgotPasswordInBackground(forgotPasswordHandler);
                }
            }
        });

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = ViewHelper.getStringValue(activity, EDIT_TEXT_USERNAME_ID);
                password = ViewHelper.getStringValue(activity, EDIT_TEXT_PASSWORD_ID);

                final CognitoUser cognitoUser = cognitoUserPool.getUser(username);

                cognitoUser.getSessionInBackground(authenticationHandler);
            }
        };

        buttonView.setOnClickListener(listener);
        return listener;
    }

    /** {@inheritDoc} */
    @Override
    public String getDisplayName() {
        return "Amazon Cognito Your User Pools";
    }

    /** {@inheritDoc} */
    @Override
    public String getCognitoLoginKey() {
        final String key = "cognito-idp." + AWSConfiguration.AMAZON_COGNITO_REGION.getName()
                + ".amazonaws.com/" + AWSConfiguration.AMAZON_COGNITO_USER_POOL_ID;
        Log.d(LOG_TAG, key);

        return key;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isUserSignedIn() {
        try {
            initializedLatch.await();
        } catch (final InterruptedException ex) {
            Log.e(LOG_TAG,"Unexpected interrupt.", ex);
        }

        return null != cognitoUserSession && cognitoUserSession.isValid();
    }

    /** {@inheritDoc} */
    @Override
    public String getToken() {
        return null == cognitoUserSession ? null : cognitoUserSession.getIdToken().getJWTToken();
    }

    /** {@inheritDoc} */
    @Override
    public String refreshToken() {
        // Cognito User Pools SDK handles token refresh.
        return getToken();
    }

    /** {@inheritDoc} */
    @Override
    public void signOut() {
        if (null != cognitoUserPool && null != cognitoUserPool.getCurrentUser()) {
            cognitoUserPool.getCurrentUser().signOut();

            cognitoUserSession = null;
            username = null;
            password = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getUserName() {
        if (null == username) {
            if (null != cognitoUserPool && null != cognitoUserPool.getCurrentUser()) {
                username = cognitoUserPool.getCurrentUser().getUserId();
            }
        }

        return username;
    }

    /** {@inheritDoc} */
    @Override
    public String getUserImageUrl() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void reloadUserInfo() {
        // Cognito User Pools SDK handles user details refresh when token is refreshed.
        getToken();
    }
}
