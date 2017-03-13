package com.finalyearproject.dan.ocraccountingapp.amazon.downloader;

public interface ResponseHandler {
    void onSuccess(long downloadId);
    void onError(String errorMessage);
}
