package com.finalyearproject.dan.ocraccountingapp.mobile.downloader;

public interface ResponseHandler {
    void onSuccess(long downloadId);
    void onError(String errorMessage);
}
