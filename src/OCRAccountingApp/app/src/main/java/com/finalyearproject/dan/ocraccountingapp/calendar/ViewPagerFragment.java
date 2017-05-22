package com.finalyearproject.dan.ocraccountingapp.calendar;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLTableBase;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLTableFactory;
import com.finalyearproject.dan.ocraccountingapp.util.TimeUtils;

import java.util.Calendar;

public class ViewPagerFragment extends Fragment {

    public static Context mContext;

    ViewPager viewPager;
    MyPagerAdapter myPagerAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_calendar_viewpager, container, false);

        // change the toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Calendar");


        mContext = getContext();

        viewPager = (ViewPager) view.findViewById(R.id.vpPager2);
        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        viewPager.setAdapter(myPagerAdapter);

        // set pager to current date
        viewPager.setCurrentItem(TimeUtils.getPositionForYear(Calendar.getInstance()));

        // change the color of the tab strip in the view pager
        PagerTabStrip pagerTabStrip = (PagerTabStrip) view.findViewById(R.id.pagertabstrip);
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTabIndicatorColor(Color.parseColor("#BFEAE1"));

        //viewPager.setOffscreenPageLimit(10);

        return view;
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public int getCount() {
            return TimeUtils.WEEKS_OF_TIME;
        }

        @Override
        public Fragment getItem(int position) {

            Calendar cal = TimeUtils.getYearForPosition(position);
            String year = TimeUtils.getYearFormat(mContext, cal.getTimeInMillis());

            return MonthsFragment.newInstance(year);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Calendar cal = TimeUtils.getYearForPosition(position);
            // returns format e.g. 2017
            return TimeUtils.getYearFormat(mContext, cal.getTimeInMillis());
        }
    }

    /*-------------------------------ASYNC TASK for querying database-----------------------------*/

    static class MyTaskParams {
        String date1, date2;

        MyTaskParams(String date1, String date2) {
            this.date1 = date1;
            this.date2 = date2;
        }
    }


    static class QueryDatabaseTask extends AsyncTask<MyTaskParams, Void, NoSQLOperation> {

        @Override
        protected NoSQLOperation doInBackground(MyTaskParams... params) {

            final String date1 = params[0].date1;
            final String date2 = params[0].date2;
            // The NoSQL table operations will be run against.
            NoSQLTableBase table = NoSQLTableFactory.instance(mContext
                    .getApplicationContext()).getNoSQLTableByTableName("receiptData");

            NoSQLOperation nso = table.getSupportedOperations(mContext, new NoSQLTableBase.SupportedOperationsHandler() {
                @Override
                public void onSupportedOperationsReceived(final NoSQLOperation noSQLOperation) {
                    try {
                        noSQLOperation.executeOperation(date1, date2);
                    } catch (final AmazonClientException ignored) {}
                }
            });
            Log.e("nso = ", nso.toString());


            return nso;
        }

        @Override
        protected void onPostExecute(NoSQLOperation result) {}

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}
