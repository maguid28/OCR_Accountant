package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;
import android.view.View;

public interface NoSQLResult {
    // update an item.
    void updateItem();

    // delete an item.
    void deleteItem();

    // get file path
    String getFilePath();

    View getView(Context context, final View convertView, int position);
}

