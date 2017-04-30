package com.finalyearproject.dan.ocraccountingapp.camera;

import android.support.design.widget.FloatingActionButton;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.finalyearproject.dan.ocraccountingapp.R;

import org.junit.Test;

import static org.junit.Assert.*;

public class ReceiptEditActivityTest extends
        ActivityInstrumentationTestCase2<ReceiptEditActivity> {

    private ReceiptEditActivity mTestActivity;
    private TextView mTestRecNameText;
    private TextView mTestRecDateText;
    private TextView mTestRecTotalText;
    private Spinner mTestRecCategory;
    private FloatingActionButton mFab;

    public ReceiptEditActivityTest() {
        super(ReceiptEditActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Starts the activity under test using
        // the default Intent with:
        // action = {@link Intent#ACTION_MAIN}
        // flags = {@link Intent#FLAG_ACTIVITY_NEW_TASK}
        // All other fields are null or empty.
        mTestActivity = getActivity();
        mTestRecNameText = (EditText) mTestActivity
                .findViewById(R.id.recNameEdit);
        mTestRecDateText = (EditText) mTestActivity
                .findViewById(R.id.dateEdit);
        mTestRecTotalText = (EditText) mTestActivity
                .findViewById(R.id.total_edit);
        mTestRecCategory = (Spinner) mTestActivity
                .findViewById(R.id.expense_spinner);
        mFab = (FloatingActionButton) mTestActivity
                .findViewById(R.id.fab);
    }

    public void testNotNull() {
        assertNotNull("mTestActivity is null", mTestActivity);
        assertNotNull("mTestRecNameText is null", mTestRecNameText);
        assertNotNull("mTestRecDateText is null", mTestRecDateText);
        assertNotNull("mTestRecCategory is null", mTestRecCategory);
        assertNotNull("mTestRecNameTotal is null", mTestRecTotalText);
        assertNotNull("mFab is null", mFab);
    }

    public void testView_labelText() {
        String expected = "";
        String actual = mTestRecNameText.getText().toString();
        String actual2 = mTestRecDateText.getText().toString();
        String actual3 = mTestRecTotalText.getText().toString();
        assertEquals("mTestRecNameText contains wrong text",
                expected, actual);
        assertEquals("mTestRecDateText contains wrong text",
                expected, actual2);
        assertEquals("mTestRecTotalText contains wrong text",
                expected, actual3);
    }
}