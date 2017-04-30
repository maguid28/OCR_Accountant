package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.app.AlertDialog;
import android.support.v4.app.FragmentActivity;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;


public final class DynamoDBUtils {
    /**
     * Utility class has private constructor.
     */
    private DynamoDBUtils() {
    }

    public static void showErrorDialogForServiceException(final FragmentActivity activity,
                                                          final String title,
                                                          final AmazonClientException ex) {
        if (activity == null) {
            return;
        }

        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(ex.getMessage())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
    }
}
