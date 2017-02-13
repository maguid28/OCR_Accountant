package com.finalyearproject.dan.ocraccountingapp.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.finalyearproject.dan.ocraccountingapp.R;


public class TimeUtils {


    public static final Calendar FIRST_DAY_OF_TIME;
    public static final Calendar LAST_DAY_OF_TIME;
    public static final int DAYS_OF_TIME;
    public static final int WEEKS_OF_TIME;
    public static final int MONTHS_OF_TIME;


    static {
        FIRST_DAY_OF_TIME = Calendar.getInstance();
        FIRST_DAY_OF_TIME.set(1900, Calendar.JANUARY, 1);
        LAST_DAY_OF_TIME = Calendar.getInstance();
        LAST_DAY_OF_TIME.set(2100, Calendar.DECEMBER, 31);
        DAYS_OF_TIME = 73413; //(int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        WEEKS_OF_TIME = 10487; // (int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (7 * 24 * 60 * 60 * 1000));
        MONTHS_OF_TIME = 2400; // (int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (12 * 7 * 24 * 60 * 60 * 1000));
    }

    /**
     * Get the position in the ViewPager for a given day
     *
     * @param day
     * @return the position or 0 if day is null
     */
    public static int getPositionForDay(Calendar day) {
        if (day != null) {
            return (int) ((day.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis())
                    / 86400000  //(24 * 60 * 60 * 1000)
            );
        }
        return 0;
    }

    /**
     * Get the position in the ViewPager for a given week
     *
     * @param week
     * @return the position or 0 if week is null
     */
    public static int getPositionForWeek(Calendar week) {
        if (week != null) {
            return (int) ((week.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis())
                    / 604800000L);  //(7 * 24 * 60 * 60 * 1000)
        }
        return 0;
    }


    public static int getPositionForMonth(Calendar month) {
        if (month != null) {
            return (int) ((month.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis())
                    / 7257600000L);  //(12 * 7 * 24 * 60 * 60 * 1000)
        }
        return 0;
    }

    /**
     * Get the day for a given position in the ViewPager
     *
     * @param position
     * @return the day
     * @throws IllegalArgumentException if position is negative
     */
    public static Calendar getDayForPosition(int position) throws IllegalArgumentException {
        if (position < 0) {
            throw new IllegalArgumentException("position cannot be negative");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(FIRST_DAY_OF_TIME.getTimeInMillis());
        cal.add(Calendar.DAY_OF_YEAR, position);
        return cal;
    }

    /**
     * Get the week for a given position in the ViewPager
     *
     * @param position
     * @return the week
     * @throws IllegalArgumentException if position is negative
     */
    public static Calendar getWeekForPosition(int position) throws IllegalArgumentException {
        if (position < 0) {
            throw new IllegalArgumentException("position cannot be negative");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(FIRST_DAY_OF_TIME.getTimeInMillis());
        cal.add(Calendar.WEEK_OF_YEAR, position);
        return cal;
    }


    /**
     * Get the week for a given position in the ViewPager
     *
     * @param position
     * @return the month
     * @throws IllegalArgumentException if position is negative
     */
    public static Calendar getMonthForPosition(int position) throws IllegalArgumentException {
        if (position < 0) {
            throw new IllegalArgumentException("position cannot be negative");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(FIRST_DAY_OF_TIME.getTimeInMillis());
        cal.add(Calendar.MONTH, position);
        return cal;
    }



    public static String getFormattedDate(Context context, long date) {
        final String defaultPattern = "yyyy-MM-dd";

        String pattern = null;
        if (context != null) {
            pattern = context.getString(R.string.date_format);
        }
        if (pattern == null) {
            pattern = defaultPattern;
        }
        SimpleDateFormat simpleDateFormat = null;
        try {
            simpleDateFormat = new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException e) {
            simpleDateFormat = new SimpleDateFormat(defaultPattern);
        }

        return simpleDateFormat.format(new Date(date));
    }

    //function to provide week view format
    public static String getWeekFormat(Context context, long date) {
        final String defaultPattern = "yyyy-MM-dd";

        String pattern = null;
        if (context != null) {
            pattern = context.getString(R.string.week_date_format);
        }
        if (pattern == null) {
            pattern = defaultPattern;
        }
        SimpleDateFormat simpleDateFormat = null;
        try {
            simpleDateFormat = new SimpleDateFormat(pattern);
        } catch (IllegalArgumentException e) {
            simpleDateFormat = new SimpleDateFormat(defaultPattern);
        }

        return simpleDateFormat.format(new Date(date));
    }


}
