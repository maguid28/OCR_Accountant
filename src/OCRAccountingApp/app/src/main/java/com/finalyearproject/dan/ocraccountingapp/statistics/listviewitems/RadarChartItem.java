package com.finalyearproject.dan.ocraccountingapp.statistics.listviewitems;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

public class RadarChartItem extends ChartItem {

    @Override
    public int getItemType() {
        return TYPE_RADARCHART;
    }

    public RadarChartItem(ChartData<?> cd, Context c) {
        super(cd);
    }

    @Override
    public View getView(int position, View convertView, Context c) {

        ViewHolder holder = null;

        if (convertView == null) {

            holder = new ViewHolder();

            convertView = LayoutInflater.from(c).inflate(
                    R.layout.list_item_radar, null);
            holder.chart = (RadarChart) convertView.findViewById(R.id.radarchart);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.chart.getDescription().setEnabled(false);

        holder.chart.setWebLineWidth(1f);
        holder.chart.setWebColor(Color.LTGRAY);
        holder.chart.setWebLineWidthInner(1f);
        holder.chart.setWebColorInner(Color.LTGRAY);
        holder.chart.setWebAlpha(100);

        // create a custom MarkerView (extend MarkerView) and specify the layout
        // to use for it
        //MarkerView mv = new RadarMarkerView(this, R.layout.radar_markerview);
        //mv.setChartView(mChart); // For bounds control
        //holder.chart.setMarker(mv); // Set the marker to the chart

        holder.chart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = holder.chart.getXAxis();
        xAxis.setTextSize(12f);
        xAxis.setYOffset(0f);
        xAxis.setXOffset(0f);
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private String[] mActivities = new String[]{"Food", "Utilities", "Transport", "Clothing", "Recreation", "Health", "Other"};

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return mActivities[(int) value % mActivities.length];
            }
        });
        xAxis.setTextColor(Color.WHITE);

        YAxis yAxis = holder.chart.getYAxis();
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(20f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(200f);
        yAxis.setDrawLabels(false);

        // set data
        holder.chart.setData((RadarData) mChartData);

        Legend l = holder.chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
        l.setTextColor(Color.WHITE);
        l.setTextSize(12f);

        return convertView;
    }

    private static class ViewHolder {
        RadarChart chart;
    }

}
