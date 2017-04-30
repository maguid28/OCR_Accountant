package com.finalyearproject.dan.ocraccountingapp;

import org.junit.Test;

import static org.junit.Assert.*;
import java.util.regex.Pattern;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.test.suitebuilder.TestSuiteBuilder;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import com.finalyearproject.dan.ocraccountingapp.calendar.DisplayResultsFragment;
import com.finalyearproject.dan.ocraccountingapp.imgtotext.TextExtraction;

/**
 * Example local unit testing, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    DisplayResultsFragment displayResultsFragment = new DisplayResultsFragment();

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void addition_isNotCorrect() throws Exception {
        assertEquals("Numbers isn't equals!", 5, 2 + 2);
    }

    @Test
    public void iCorrect() throws Exception {

    }
}