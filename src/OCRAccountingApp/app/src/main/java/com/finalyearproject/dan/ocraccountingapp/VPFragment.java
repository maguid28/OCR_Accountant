package com.finalyearproject.dan.ocraccountingapp;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import com.finalyearproject.dan.ocraccountingapp.util.TimeUtils;


public class VPFragment extends Fragment {

    private static Context mContext;

    private FragmentStatePagerAdapter adapterViewPager;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(R.layout.fragment_vp, container, false);

        mContext = getContext();

        ViewPager vpPager = (ViewPager) fragmentView.findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        // set pager to current date
        vpPager.setCurrentItem(TimeUtils.getPositionForWeek(Calendar.getInstance()));
        adapterViewPager.notifyDataSetChanged();

        return fragmentView;
    }


    public static class MyPagerAdapter extends FragmentStatePagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return TimeUtils.WEEKS_OF_TIME;
        }

        @Override
        public Fragment getItem(int position) {
            Calendar firstDay, lastDay;
            long firstDayInMillis, lastDayinMillis;
            String date1, date2;

            // Get position of first day
            firstDay = TimeUtils.getWeekForPosition(position);
            firstDayInMillis = firstDay.getTimeInMillis();
            // Get position of last day
            lastDay = firstDay;
            lastDay.add(Calendar.DATE, 6);
            lastDayinMillis = lastDay.getTimeInMillis();

            // Get date in format yyyyMMdd
            date1 = TimeUtils.FormatDateForVP(mContext, firstDayInMillis);
            date2 = TimeUtils.FormatDateForVP(mContext, lastDayinMillis);

            Log.e("day1----------", date1);
            Log.e("day7----------", date2);

            return FragmentContent.newInstance(date1, date2);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar cal = TimeUtils.getWeekForPosition(position);
            Calendar endCal = TimeUtils.getWeekForPosition(position);
            // display the date at the end of the week
            endCal.add(Calendar.DATE, 6);
            // display the month name
            String month = " "  + endCal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault());
            // returns format e.g. 20 - 26 February
            return TimeUtils.getWeekFormat(mContext, cal.getTimeInMillis()) + " - " + TimeUtils.getWeekFormat(mContext, endCal.getTimeInMillis()) + month;
        }

    }

}