package com.finalyearproject.dan.ocraccountingapp.nosql.nosql;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

class NoSQLOperationListAdapter extends ArrayAdapter<NoSQLOperationListItem> {

    public enum ViewType {
        HEADER, OPERATION
    }

    public NoSQLOperationListAdapter(final Context context, final int resource) {
        super(context, resource);
    }

    @Override
    public int getItemViewType(final int position) {
        return getItem(position).getViewType();
    }

    @Override
    public int getViewTypeCount() {
        return ViewType.values().length;
    }

    @NonNull
    @Override
    public View getView(final int position, final View convertView,
                        @NonNull final ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        final NoSQLOperationListItem listItem = getItem(position);
        return listItem.getView(layoutInflater, convertView);
    }
}
