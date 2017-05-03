package com.finalyearproject.dan.ocraccountingapp.statistics;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.amazonaws.AmazonClientException;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.amazon.util.ThreadUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.DynamoDBUtils;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLOperation;
import com.finalyearproject.dan.ocraccountingapp.nosql.nosql.NoSQLResult;
import com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems.BarChartItem;
import com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems.ChartItem;
import com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems.LineChartItem;
import com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems.PieChartItem;
import com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems.RadarChartItem;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.github.mikephil.charting.utils.ColorTemplate.rgb;
import static java.util.Calendar.DAY_OF_YEAR;

public class MultiChartFragment extends Fragment {

    private static final String LOG_TAG = MultiChartFragment.class.getSimpleName();

    // The performed DB operation (month query)
    private static NoSQLOperation noSQLOperation;

    // The 2nd performed DB operation (year query)
    private static NoSQLOperation noSQLOperation2;

    // A flag indicating all results have been retrieved
    private volatile boolean doneRetrievingResults = false;

    // Executor to handle getting more results in the background.
    private final Executor singleThreadedExecutor = Executors.newSingleThreadExecutor();

    List<NoSQLResult> results = null;
    List<NoSQLResult> yearResults = null;

    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_listview_chart, container, false);

        lv = (ListView) view.findViewById(R.id.listView1);

        getResults();

        return view;
    }



    private void getResults() {
        // if there are more results to retrieve.
        if (!doneRetrievingResults) {
            doneRetrievingResults = true;
            // Get next results group in the background.
            singleThreadedExecutor.execute(new Runnable() {
                @Override
                public void run() {

                    try {
                        results = noSQLOperation.getNextResultGroup();
                        yearResults = noSQLOperation2.getNextResultGroup();
                    } catch (final AmazonClientException ex) {
                        Log.e(LOG_TAG, "Failed loading additional results.", ex);
                        DynamoDBUtils.showErrorDialogForServiceException(getActivity(),
                                getString(R.string.nosql_dialog_title_failed_loading_more_results), ex);
                    }
                    if (results == null && yearResults == null) {
                        return;
                    }
                    ThreadUtils.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doneRetrievingResults = false;
                            //resultsListAdapter.addAll(results);
                            if(results!=null) {
                                for (int i = 0; i < results.size(); i++) {
                                    System.out.println(results.get(i).getCategory());
                                }
                                for (int i = 0; i < yearResults.size(); i++) {
                                    System.out.println("yr: " + yearResults.get(i).getCategory());
                                }

                                //Create a list for storing the charts
                                ArrayList<ChartItem> list = new ArrayList<ChartItem>();

                                list.add(new PieChartItem(generateDataPie(), getActivity()));

                                list.add(new BarChartItem(generateDataBar(), getActivity()));

                                list.add(new RadarChartItem(generateDataRadar(), getActivity()));

                                list.add(new LineChartItem(generateDataLine(), getActivity()));


                                Log.e("NOSQLOP = ", String.valueOf(noSQLOperation));

                                ChartDataAdapter cda = new ChartDataAdapter(getActivity(), list);
                                lv.setAdapter(cda);
                            }

                        }
                    });
                }
            });
        }
    }












    /** adapter that supports 3 different item types */
    private class ChartDataAdapter extends ArrayAdapter<ChartItem> {

        public ChartDataAdapter(Context context, List<ChartItem> objects) {
            super(context, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).getView(position, convertView, getContext());
        }

        @Override
        public int getItemViewType(int position) {
            // return the views type
            return getItem(position).getItemType();
        }

        @Override
        public int getViewTypeCount() {
            return 4; // we have 4 different item-types
        }
    }



    private RadarData generateDataRadar() {

        float mult = 80;
        float min = 20;
        int numEntries = 7;


        // table to store current months category/total pairs
        Hashtable<Integer, Double> currentMonthCatagoryPair = new Hashtable<>();
        // table to store current month-1's category/total pairs
        Hashtable<Integer, Double> previousMonthCatagoryPair = new Hashtable<>();

        Hashtable<String, Integer> catagories = new Hashtable<>();
        catagories.put("Food", 0);
        catagories.put("Utilities", 1);
        catagories.put("Transport", 2);
        catagories.put("Clothing", 3);
        catagories.put("Recreation", 4);
        catagories.put("Health", 5);
        catagories.put("Other", 6);



        String tm = results.get(0).getFormattedDate().substring(4,6);
        int thisMonth = Integer.parseInt(tm);
        System.out.println("thismonth: " + results.get(0).getFormattedDate());
        System.out.println("thismonth: " + thisMonth);

        for(int i=0; i<yearResults.size(); i++) {

            String m = yearResults.get(i).getFormattedDate().substring(4,6);
            int month = Integer.parseInt(m);
            System.out.println("thatmonth: " + yearResults.get(i).getFormattedDate());
            System.out.println("thatmonth: " + month);


            int findCategory = catagories.get(yearResults.get(i).getCategory());


            //if results are from current month
            if(month==thisMonth) {

                if(currentMonthCatagoryPair.containsKey(findCategory)) {

                    double newval = Double.parseDouble(yearResults.get(i).getTotal());
                    double currentval = currentMonthCatagoryPair.get(findCategory);

                    System.out.println("newval: " + newval);
                    System.out.println("currentval: " + currentval);

                    // add the new value to the current value
                    currentMonthCatagoryPair.put(findCategory, newval+currentval);
                }
                // add the category and total to the map
                else {
                    // get category
                    String category = yearResults.get(i).getCategory();
                    // get total
                    double total = Double.parseDouble(yearResults.get(i).getTotal());
                    // add as new entry to hashtable
                    currentMonthCatagoryPair.put(findCategory, total);
                }
            }
            //if results are from previous month
            else if(month==thisMonth-1) {
                if(previousMonthCatagoryPair.containsKey(findCategory)) {

                    double newval = Double.parseDouble(yearResults.get(i).getTotal());
                    double currentval = previousMonthCatagoryPair.get(findCategory);

                    System.out.println("newval: " + newval);
                    System.out.println("currentval: " + currentval);

                    // add the new value to the current value
                    previousMonthCatagoryPair.put(findCategory, newval+currentval);
                }
                // add the category and total to the map
                else {
                    // get category
                    String category = yearResults.get(i).getCategory();
                    // get total
                    double total = Double.parseDouble(yearResults.get(i).getTotal());
                    // add as new entry to hashtable
                    previousMonthCatagoryPair.put(findCategory, total);
                }
            }
        }
        System.out.println("mcpair " + currentMonthCatagoryPair);
        System.out.println("prevmcpair " + previousMonthCatagoryPair);




        ArrayList<RadarEntry> entries1 = new ArrayList<RadarEntry>();
        ArrayList<RadarEntry> entries2 = new ArrayList<RadarEntry>();

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (int i = 0; i < numEntries; i++) {

            if(currentMonthCatagoryPair.containsKey(i)) {
                entries1.add(new RadarEntry(currentMonthCatagoryPair.get(i).floatValue()));
            }
            else {
                entries1.add(new RadarEntry(0f));
            }
            if (previousMonthCatagoryPair.containsKey(i)) {
                entries2.add(new RadarEntry(previousMonthCatagoryPair.get(i).floatValue()));
            }
            else {
                entries2.add(new RadarEntry(0f));
            }
        }

        RadarDataSet set1 = new RadarDataSet(entries1, "Current Month");
        set1.setColor(rgb("#f1c40f"));
        set1.setFillColor(rgb("#f1c40f"));
        set1.setDrawFilled(true);
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);

        RadarDataSet set2 = new RadarDataSet(entries2, "Previous Month");
        set2.setColor(rgb("#3498db"));
        set2.setFillColor(rgb("#3498db"));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
        sets.add(set1);
        sets.add(set2);

        RadarData cd = new RadarData(sets);
        return cd;
    }






    private LineData generateDataLine() {

        // table to store category/total pairs
        Hashtable<Integer, Double> monthTotalpair = new Hashtable<>();


        for(int i=0; i<yearResults.size(); i++) {
            String m = yearResults.get(i).getFormattedDate().substring(4,6);
            int month = Integer.parseInt(m);
            System.out.println("monthhh : " + month);

            // if the hash table contains the day, add "total" to the value
            if(monthTotalpair.containsKey(month)) {
                double newVal = Double.parseDouble(yearResults.get(i).getTotal());
                double currentVal = monthTotalpair.get(month);

                System.out.println("newval: " + newVal);
                System.out.println("currentval: " + currentVal);

                // add the new value to the current value
                monthTotalpair.put(month, newVal+currentVal);
            }
            // add the day and total to the map
            else {
                // get total
                double total = Double.parseDouble(yearResults.get(i).getTotal());
                // add as new entry to hashtable
                monthTotalpair.put(month, total);
            }
        }
        System.out.println("yearRESULTS: " + monthTotalpair);


        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < 12; i++) {
            if(monthTotalpair.containsKey(i+1)){
                entries.add(new Entry(i, monthTotalpair.get(i+1).intValue()));
            }
            else {
                // if there is no data for a month, pass 0 to the line graph
                entries.add(new Entry(i, 0));
            }
        }

        LineDataSet d1 = new LineDataSet(entries, "Monthly Breakdown");
        d1.setLineWidth(10f);
        d1.setCircleRadius(4.5f);
        d1.setHighLightColor(Color.rgb(244, 117, 117));
        d1.setDrawValues(false);

        ArrayList<ILineDataSet> sets = new ArrayList<ILineDataSet>();
        sets.add(d1);

        LineData cd = new LineData(sets);
        return cd;
    }





    private BarData generateDataBar() {

        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

        String date = results.get(0).getFormattedDate();

        // table to store category/total pairs
        Hashtable<Integer, Double> dayTotalpair = new Hashtable<>();

        for(int i=0; i<results.size(); i++) {
            String d = results.get(i).getFormattedDate().substring(6,8);
            int day = Integer.parseInt(d);
            System.out.println("dayyyy : " + day);
            // if the hash table contains the day, add "total" to the value
            if(dayTotalpair.containsKey(day)) {
                double newVal = Double.parseDouble(results.get(i).getTotal());
                double currentVal = dayTotalpair.get(day);

                System.out.println("newval: " + newVal);
                System.out.println("currentval: " + currentVal);

                // add the new value to the current value
                dayTotalpair.put(day, newVal+currentVal);
            }
            // add the day and total to the map
            else {
                // get total
                double total = Double.parseDouble(results.get(i).getTotal());
                // add as new entry to hashtable
                dayTotalpair.put(day, total);
            }
        }

        // get the number of days in the month
        int numDays = daysInMonth(date);

        for (int i = 0; i < numDays; i++) {
            if(dayTotalpair.containsKey(i)){
                entries.add(new BarEntry(i, dayTotalpair.get(i).intValue()));
            }
            else {
                entries.add(new BarEntry(i, 0));
            }
        }

        BarDataSet d = new BarDataSet(entries, "Daily Expense Breakdown");
        d.setColors(ColorTemplate.MATERIAL_COLORS);
        d.setHighLightAlpha(255);
        // disable values appearing above bars
        d.setDrawValues(false);

        BarData cd = new BarData(d);
        cd.setBarWidth(0.9f);
        return cd;
    }


    public static boolean isLeapYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        return cal.getActualMaximum(DAY_OF_YEAR) > 365;
    }


    private int daysInMonth(String date) {

        String m = date.substring(4,6);
        int month = Integer.parseInt(m);
        String y = date.substring(0,4);
        int year = Integer.parseInt(y);

        System.out.println("demonth: " + month);
        System.out.println("deyear: " + year);

        switch (month) {
            case 1 :
                return 31;
            case 2 :
                if(isLeapYear(year)) {
                    System.out.println("THIS IS LEAP YEAR");
                    return 29;
                }
                else {
                    System.out.println("THIS IS NOT LEAP YEAR");
                    return 28;
                }
            case 3 :
                return 31;
            case 4 :
                return 30;
            case 5 :
                return 31;
            case 6 :
                return 30;
            case 7 :
                return 31;
            case 8 :
                return 31;
            case 9 :
                return 30;
            case 10 :
                return 31;
            case 11 :
                return 30;
            case 12 :
                return 31;
            default :
                return 0;
        }
    }



    private PieData generateDataPie() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        // table to store catagory/total pairs
        Hashtable<String,Double> catagoryTotalpair = new Hashtable<>();

        for(int i=0; i<results.size(); i++) {
            // if the hash map contains the catagory, add "total" to the value
            if(catagoryTotalpair.containsKey(results.get(i).getCategory())) {

                double newval = Double.parseDouble(results.get(i).getTotal());
                double currentval = catagoryTotalpair.get(results.get(i).getCategory());

                System.out.println("newval: " + newval);
                System.out.println("currentval: " + currentval);

                // add the new value to the current value
                catagoryTotalpair.put(results.get(i).getCategory(), newval+currentval);
            }
            // add the category and total to the map
            else {
                // get category
                String category = results.get(i).getCategory();
                // get total
                double total = Double.parseDouble(results.get(i).getTotal());
                // add as new entry to hashtable
                catagoryTotalpair.put(category, total);
            }
        }
        System.out.println(catagoryTotalpair);

        // loop through the hashtable
        Enumeration e = catagoryTotalpair.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            System.out.println(key + " : " + catagoryTotalpair.get(key));
            // add entry to the pie chart
            entries.add(new PieEntry(catagoryTotalpair.get(key).floatValue(), key));
        }

        PieDataSet d = new PieDataSet(entries, "");

        // space between slices
        d.setSliceSpace(2f);

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.MATERIAL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        d.setColors(colors);

        PieData cd = new PieData(d);
        return cd;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        MultiChartFragment f = (MultiChartFragment) getChildFragmentManager()
                .findFragmentById(R.id.chartContainer);
        if (f != null)
            getChildFragmentManager().beginTransaction().remove(f).commit();
    }

    // receives operation1 object from parent fragment
    public void setOperation(final NoSQLOperation operation) {
        noSQLOperation = operation;
    }

    // receives operation1 object from parent fragment
    public void setOperation2(final NoSQLOperation operation) {
        noSQLOperation2 = operation;
    }


}
