package com.finalyearproject.dan.ocraccountingapp.util;

import android.content.Context;
import android.test.mock.MockContext;

import org.junit.Test;
import org.mockito.Mock;

import java.sql.Time;
import java.util.Calendar;

import static org.junit.Assert.*;

public class TimeUtilsTest {

    @Mock
    Context mMockContext;

    @Test
    public void getPositionForYear() throws Exception {
        Calendar cal = Calendar.getInstance();
        int output;
        // as of 01/05/2017
        int expected = 117;
        output = TimeUtils.getPositionForYear(cal);
        assertEquals(expected, output);
    }

    @Test
    public void getYearForPosition() throws Exception {
        int input = 2017;
        long output;
        long expected = Calendar.getInstance().YEAR;

        output = TimeUtils.getYearForPosition(input).YEAR;
        assertEquals(expected, output);
    }

    @Test
    public void getYearFormat() throws Exception {
        String output;
        long input = Calendar.getInstance().getTimeInMillis();
        String expected = "2017";
        output = TimeUtils.getYearFormat(mMockContext,input);
        assertEquals(expected, output);
    }

}