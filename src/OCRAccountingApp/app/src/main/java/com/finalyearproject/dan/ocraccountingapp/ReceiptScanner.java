package com.finalyearproject.dan.ocraccountingapp;

/**
 * Created by daniel on 15/11/2016.
 */

public interface ReceiptScanner {
    /*
    *
    * @param - receipt image file path
    * @return
    */
    public String getTextFromReceiptImage(final String receiptImageFilePath);
}
