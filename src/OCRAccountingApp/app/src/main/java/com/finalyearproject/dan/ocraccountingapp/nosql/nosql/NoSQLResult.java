package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;
import android.view.View;

public interface NoSQLResult {
    // update an item
    void updateItem(String title, String total, String date, String category);

    // delete an item
    void deleteItem();

    // get file path
    String getFilePath();

    // get date
    String getDate();

    String getFormattedDate();

    String getTotal();

    String getCategory();

    String getFriendlyName();


    View getView(Context context, final View convertView, int position);
}

