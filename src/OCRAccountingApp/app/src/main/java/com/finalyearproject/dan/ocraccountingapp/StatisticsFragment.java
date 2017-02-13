package com.finalyearproject.dan.ocraccountingapp;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

public class StatisticsFragment extends Fragment {

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_statistics, container, false);

        return fragmentView;
    }
}
