package com.finalyearproject.dan.ocraccountingapp;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.nosql.ReceiptDataDO;

/**
 * Created by daniel on 13/02/2017.
 */

public class InsertData {

    public void insertData(Activity activity) {
        // Fetch the default configured DynamoDB ObjectMapper
        final DynamoDBMapper dynamoDBMapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        final ReceiptDataDO receipt = new ReceiptDataDO(); // Initialize the Notes Object

        // The userId has to be set to user's Cognito Identity Id for private / protected tables.
        // User's Cognito Identity Id can be fetched by using:
        // AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID()
        receipt.setUserId(AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID());
        receipt.setRecName("receipt1_test");
        receipt.setDate("14-02-2017");
        receipt.setFilepath("example/file/path"); // GMT: Fri, 19 Aug 2016 21:53:47 GMT
        receipt.setTotal("100.00");
        AmazonClientException lastException = null;

        try {
            dynamoDBMapper.save(receipt);
        } catch (final AmazonClientException ex) {
            Log.e("INSERT DATA", "Failed saving item : " + ex.getMessage(), ex);
            lastException = ex;
        }
    }
}
