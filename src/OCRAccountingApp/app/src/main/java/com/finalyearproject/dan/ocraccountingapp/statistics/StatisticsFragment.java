package com.finalyearproject.dan.ocraccountingapp.statistics;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLTableBase;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLTableFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

public class StatisticsFragment extends Fragment {

    public static Context mContext;

    String selectedSpinnerItem;

    Spinner monthSpinner, yearSpinner;

    Button goButton;

    private static View view;

    // The name of the nosql DB table
    private static final String tableName = "receiptData";


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            // Inflate the layout for this fragment
            view = inflater.inflate(R.layout.fragment_statistics, container, false);
        } catch (InflateException e) {
            Log.e("fragment", "already created");
        }

        // change the toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Statistics");

        mContext = getContext();

        Calendar myCalendar = Calendar.getInstance();

        // get the current month and year
        String year = new SimpleDateFormat("yyyy").format(myCalendar.getTime());
        int month = Integer.parseInt(new SimpleDateFormat("MM").format(myCalendar.getTime()));

        // Date must be in the format yyyyMMdd
        String date;

        // Date must be in the format yyyy0000
        String yearDate = year + "0000";

        if(month < 10) {
            // add a '0' between year and month to conform with date format
            date = year + "0" + String.valueOf(month) + "00";
        }
        else {
            date = year + String.valueOf(month) + "00";
        }

        Log.e("date123123 ", date);

        // DB operation that specifies to retrieve the receipts for the queried month
        NoSQLOperation operation = getNoSQLOperation(date);

        // DB operation that specifies to retrieve the receipts for the queried year
        NoSQLOperation operation2 = getYearNoSQLOperation(yearDate);

        if(operation!=null && operation2!=null){
            showResultsForOperation(operation, operation2);
            Log.e("OPERATION ", "SUCCESS");
        }
        else handleNoResultsFound();


        // The NoSQL Table operations will be run against
        NoSQLTableFactory.instance(getContext()
                .getApplicationContext())
                .getNoSQLTableByTableName(tableName);

        // set up spinners with month and year values and set default to current date
        configureSpinners(view);

        goButton = (Button) view.findViewById(R.id.goButton);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the selected month and year from the spinner
                int selectedMonth = monthSpinner.getSelectedItemPosition()+1;
                String selectedYear = yearSpinner.getSelectedItem().toString();
                Log.e("Selected Month ", String.valueOf(selectedMonth));
                Log.e("Selected Year ", selectedYear);

                // Date must be in the format yyyyMMdd
                String date;

                // Date must be in the format yyyy0000
                String yearDate = selectedYear + "0000";

                if(selectedMonth < 10) {
                    // add a '0' between year and month to conform with date format
                    date = selectedYear + "0" + String.valueOf(selectedMonth) + "00";
                }
                else {
                    date = selectedYear + String.valueOf(selectedMonth) + "00";
                }


                Log.i("year = ", String.valueOf(selectedYear));
                Log.i("month = ", String.valueOf(selectedMonth));
                Log.i("date = ", String.valueOf(date));

                // DB operation that specifies to retrieve the receipts for the queried month
                NoSQLOperation operation = getNoSQLOperation(date);

                // DB operation that specifies to retrieve the receipts for the queried month
                NoSQLOperation operation2 = getYearNoSQLOperation(yearDate);

                if(operation!=null && operation2!=null){
                    showResultsForOperation(operation, operation2);
                    Log.e("OPERATION ", "SUCCESS");
                }
                else handleNoResultsFound();

            }
        });

        return view;
    }

    public static NoSQLOperation getYearNoSQLOperation(String date1){

        NoSQLOperation operation = null;

        // Get the integer value of date2
        int date2Int = Integer.parseInt(date1) + 1300;
        // Get date in format yyyy1300
        String date2 = String.valueOf(date2Int);

        // Pass date1 and date2 to the QueryDatabaseTask
        MyTaskParams params = new MyTaskParams(date1, date2);
        QueryDatabaseTask queryDatabaseTask = new QueryDatabaseTask();
        try {
            operation = queryDatabaseTask.execute(params).get();
            Log.e("noSQLOperation = ", operation.toString());

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        Log.e("yeardate1 = ", date1);
        Log.e("yeardate2 = ", date2);

        return operation;
    }

    public static NoSQLOperation getNoSQLOperation(String date1){

        NoSQLOperation operation = null;

        // Get the integer value of date2
        int date2Int = Integer.parseInt(date1) + 32;
        // Get date in format yyyyMMdd
        String date2 = String.valueOf(date2Int);

        // Pass date1 and date2 to the QueryDatabaseTask
        MyTaskParams params = new MyTaskParams(date1, date2);
        QueryDatabaseTask queryDatabaseTask = new QueryDatabaseTask();
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

    static class MyTaskParams {
        String date1, date2;

        MyTaskParams(String date1, String date2) {
            this.date1 = date1;
            this.date2 = date2;
        }
    }

    private static class QueryDatabaseTask extends AsyncTask<MyTaskParams, Void, NoSQLOperation> {

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

    private void showResultsForOperation(final NoSQLOperation operation, final NoSQLOperation operation2) {
        // On execution complete, open the DisplayResultsFragment.
        final MultiChartFragment multiChartFragment = new MultiChartFragment();
        // Pass the DB operation to the next fragment
        multiChartFragment.setOperation(operation);
        multiChartFragment.setOperation2(operation2);

        // Add the child fragment to Statistics fragment
        FragmentManager manager = getChildFragmentManager();
        android.support.v4.app.FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.chartContainer, multiChartFragment);
        transaction.commit();
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

    void configureSpinners(View view) {

        Calendar myCalendar = Calendar.getInstance();

        // get the current month and year
        String year = new SimpleDateFormat("yyyy").format(myCalendar.getTime());
        String month = new SimpleDateFormat("MMMM").format(myCalendar.getTime());

        Log.e("year: ", year);
        Log.e("month: ", month);

        monthSpinner = (Spinner) view.findViewById(R.id.MonthSpin);
        // Create the spinner from months in arrays.xml and style it as spinner_item
        ArrayAdapter monthAdapter = ArrayAdapter.createFromResource(mContext, R.array.months, R.layout.spinner_item);
        monthSpinner.setAdapter(monthAdapter);

        yearSpinner = (Spinner) view.findViewById(R.id.YearSpin);
        // Create the spinner from years in arrays.xml and style it as spinner_item
        ArrayAdapter yearAdapter = ArrayAdapter.createFromResource(mContext, R.array.years, R.layout.spinner_item);
        yearSpinner.setAdapter(yearAdapter);

        monthSpinner.setSelection(getIndex(monthSpinner, month));

        // listen for the selected value of the spinner
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedSpinnerItem =(String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        yearSpinner.setSelection(getIndex(yearSpinner, year));

        // listen for the selected value of the spinner
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedSpinnerItem =(String) parent.getItemAtPosition(pos);
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private int getIndex(Spinner spinner, String myString) {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        StatisticsFragment f = (StatisticsFragment) getFragmentManager()
                .findFragmentById(R.id.chartContainer);
        if (f != null)
            getFragmentManager().beginTransaction().remove(f).commit();
    }
}
