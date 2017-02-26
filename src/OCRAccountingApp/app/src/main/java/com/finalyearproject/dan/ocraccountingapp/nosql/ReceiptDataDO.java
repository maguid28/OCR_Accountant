package com.finalyearproject.dan.ocraccountingapp.nosql;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

@DynamoDBTable(tableName = "ocraccountingapp-mobilehub-1024067420-receiptData")

public class ReceiptDataDO {
    private String _userId;
    private String _recName;
    private String _date;
    private String _filepath;
    private String _total;
    private String _formattedDate;


    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBAttribute(attributeName = "userId")
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "recName")
    @DynamoDBAttribute(attributeName = "recName")
    public String getRecName() {
        return _recName;
    }

    public void setRecName(final String _recName) {
        this._recName = _recName;
    }
    @DynamoDBAttribute(attributeName = "date")
    public String getDate() {
        return _date;
    }

    public void setDate(final String _date) {
        this._date = _date;
    }
    @DynamoDBAttribute(attributeName = "filepath")
    public String getFilepath() {
        return _filepath;
    }

    public void setFilepath(final String _filepath) {
        this._filepath = _filepath;
    }
    @DynamoDBAttribute(attributeName = "total")
    public String getTotal() {
        return _total;
    }

    public void setTotal(final String _total) {
        this._total = _total;
    }
    @DynamoDBAttribute(attributeName = "formattedDate")
    public String getFormattedDate() {
        return _formattedDate;
    }

    public void setFormattedDate(final String _formattedDate) {
        this._formattedDate = _formattedDate;
    }

}
