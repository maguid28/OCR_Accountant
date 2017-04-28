package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.view.LayoutInflater;
import android.view.View;

interface NoSQLOperationListItem {
    int getViewType();
    View getView(LayoutInflater inflater, View convertView);
}
