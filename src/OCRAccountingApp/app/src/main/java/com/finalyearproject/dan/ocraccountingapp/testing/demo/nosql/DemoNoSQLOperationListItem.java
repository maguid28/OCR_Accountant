package com.finalyearproject.dan.ocraccountingapp.testing.demo.nosql;

import android.view.LayoutInflater;
import android.view.View;

public interface DemoNoSQLOperationListItem {
    int getViewType();
    View getView(LayoutInflater inflater, View convertView);
}
