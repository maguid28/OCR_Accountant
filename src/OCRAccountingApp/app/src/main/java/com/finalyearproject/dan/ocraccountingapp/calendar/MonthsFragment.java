package com.finalyearproject.dan.ocraccountingapp.calendar;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLTableFactory;

import java.util.concurrent.ExecutionException;

public class MonthsFragment extends Fragment {

    // The name of the nosql DB table
    private static final String tableName = "receiptData";

    private static String yearID = "year";
    private String year;

    // create an instance of CalDisplayFragment
    public static MonthsFragment newInstance(String year) {
        MonthsFragment fragmentFirst = new MonthsFragment();
        Bundle args = new Bundle();

        args.putString(yearID, year);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_calendar_months, container, false);

        year = getArguments().getString(yearID);

        Log.i("YEAR IS", year);

        // The NoSQL Table demo operations will be run against
        NoSQLTableFactory.instance(getContext()
                .getApplicationContext())
                .getNoSQLTableByTableName(tableName);

        // The list view containing the months of the year
        final ListView monthListView = (ListView) view.findViewById(R.id.month_listview);


        String[] months = getResources().getStringArray(R.array.months);
        MyColoringAdapter adapter = new MyColoringAdapter(getContext(),months);
        monthListView.setAdapter(adapter);



        monthListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // adjust month for position e.g. position[0] = january
                int month = position+1;
                String day = "00";

                // Date must be in the format yyyyMMdd
                String date;

                if(month < 10) {
                    // add a '0' between year and month to conform with date format
                    date = year + "0" + String.valueOf(month) + day;
                }
                else {
                    date = year + String.valueOf(month) + day;
                }


                Log.i("year = ", String.valueOf(year));
                Log.i("month = ", String.valueOf(month));
                Log.i("day = ", String.valueOf(day));
                Log.i("date = ", String.valueOf(date));

                // DB operation that specifies to retrieve the receipts for the queried month
                NoSQLOperation operation = getNoSQLOperation(date);

                if(operation!=null){
                    showResultsForOperation(operation);
                }
                else handleNoResultsFound();

            }
        });

        return view;
    }


    private class MyColoringAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;

        public MyColoringAdapter(Context context, String[] values) {
            super(context, R.layout.month_list_item, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.month_list_item, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.list_textview);
            // Set text
            textView.setText(values[position]);

            textView.setTextColor(Color.WHITE);
            return rowView;
        }
    }



    private void showResultsForOperation(final NoSQLOperation operation) {
        // On execution complete, open the DisplayResultsFragment.
        final DisplayResultsFragment displayResultsFragment = new DisplayResultsFragment();
        // Pass the DB operation to the next fragment
        displayResultsFragment.setOperation(operation);

        final FragmentActivity fragmentActivity = getActivity();

        if (fragmentActivity != null) {
            // change the fragment to the DisplayResultsFragment
            fragmentActivity.getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_fragment_container, displayResultsFragment)
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
    }


    private void handleNoResultsFound() {
        ThreadUtils.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Re-enable the operations list view.
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                dialogBuilder.setTitle(R.string.nosql_dialog_title_no_results_text);
                dialogBuilder.setMessage(R.string.nosql_dialog_message_no_results_text);
                dialogBuilder.setNegativeButton(R.string.nosql_dialog_ok_text, null);
                dialogBuilder.show();
            }
        });
    }



    public static NoSQLOperation getNoSQLOperation(String date1){

        NoSQLOperation operation = null;

        // Get the integer value of date2
        int date2Int = Integer.parseInt(date1) + 32;
        // Get date in format yyyyMMdd
        String date2 = String.valueOf(date2Int);

        // Pass date1 and date2 to the QueryDatabaseTask
        ViewPagerFragment.MyTaskParams params = new ViewPagerFragment.MyTaskParams(date1, date2);
        ViewPagerFragment.QueryDatabaseTask queryDatabaseTask = new ViewPagerFragment.QueryDatabaseTask();
        try {
            operation = queryDatabaseTask.execute(params).get();
            Log.e("noSQLOperation = ", operation.toString());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.e("date1 = ", date1);
        Log.e("date2 = ", date2);

        return operation;
    }
}
