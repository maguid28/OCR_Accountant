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
    public static final int YEARS_OF_TIME;


    static {
        FIRST_DAY_OF_TIME = Calendar.getInstance();
        FIRST_DAY_OF_TIME.set(1900, Calendar.JANUARY, 1);
        LAST_DAY_OF_TIME = Calendar.getInstance();
        LAST_DAY_OF_TIME.set(2100, Calendar.DECEMBER, 31);
        DAYS_OF_TIME = 73413; //(int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (24 * 60 * 60 * 1000));
        WEEKS_OF_TIME = 10487; // (int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (7 * 24 * 60 * 60 * 1000));
        MONTHS_OF_TIME = 2400; // (int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (12 * 7 * 24 * 60 * 60 * 1000));
        YEARS_OF_TIME = 200; //(int) ((LAST_DAY_OF_TIME.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis()) / (24 * 60 * 60 * 1000));
    }


    public static int getPositionForYear(Calendar year) {
        if (year != null) {
            return (int) ((year.getTimeInMillis() - FIRST_DAY_OF_TIME.getTimeInMillis())
                    / 31536000000L);  //(365 * 24 * 60 * 60 * 1000)
        }
        return 0;
    }

    // Get the year for a given position in the ViewPager
    public static Calendar getYearForPosition(int position) throws IllegalArgumentException {
        if (position < 0) {
            throw new IllegalArgumentException("position cannot be negative");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(FIRST_DAY_OF_TIME.getTimeInMillis());
        cal.add(Calendar.YEAR, position);
        return cal;
    }


    //function to provide year view format
    public static String getYearFormat(Context context, long date) {
        final String defaultPattern = "yyyy";

        String pattern = null;
        if (context != null) {
            pattern = context.getString(R.string.year_date_format);
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
