package com.finalyearproject.dan.ocraccountingapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import com.finalyearproject.dan.ocraccountingapp.util.TimeUtils;


public class VPFragment extends Fragment {

    private static Context mContext;

    private CachingFragmentStatePagerAdapter adapterViewPager;



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.activity_vp, container, false);

        mContext = getContext();

        ViewPager vpPager = (ViewPager) fragmentView.findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        // set pager to current date
        vpPager.setCurrentItem(TimeUtils.getPositionForWeek(Calendar.getInstance()));

        int pop = TimeUtils.getPositionForWeek(Calendar.getInstance());
        return fragmentView;
    }

    public static class MyPagerAdapter extends CachingFragmentStatePagerAdapter {

        private Calendar cal;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return TimeUtils.WEEKS_OF_TIME;
        }

        @Override
        public Fragment getItem(int position) {
            long timeForPosition = TimeUtils.getWeekForPosition(position).getTimeInMillis();
            return FragmentContent.newInstance(timeForPosition);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar cal = TimeUtils.getWeekForPosition(position);
            Calendar endCal = TimeUtils.getWeekForPosition(position);
            // display the date at the end of the week
            endCal.add(Calendar.DATE, 6);
            // display the month name
            String month = " "  + endCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            return TimeUtils.getWeekFormat(mContext, cal.getTimeInMillis()) + " - " + TimeUtils.getWeekFormat(mContext, endCal.getTimeInMillis()) + month;
        }


    }

}