package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class NoSQLResultListAdapter extends ArrayAdapter<NoSQLResult> {
    private final Context context;

    public NoSQLResultListAdapter(final Context context) {
        super(context, -1);
        this.context = context;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final NoSQLResult listItem = getItem(position);
        return listItem.getView(context, convertView, position);
    }

    public void refresh(){

    }


}