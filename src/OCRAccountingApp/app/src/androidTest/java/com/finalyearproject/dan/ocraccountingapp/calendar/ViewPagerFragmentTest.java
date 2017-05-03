package com.finalyearproject.dan.ocraccountingapp.calendar;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;

import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.TestActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ViewPagerFragmentTest {

    @Rule
    public ActivityTestRule<TestActivity> mActivityTestRule = new ActivityTestRule<TestActivity>(TestActivity.class);

    private TestActivity mActivity = null;

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityTestRule.getActivity();
    }


    @Test
    public void testLaunch(){

        assertNotNull(mActivity);
        //test if the fragment has launched
        FrameLayout rlContainer = (FrameLayout) mActivity.findViewById(R.id.test_container);

        assertNotNull(rlContainer);

        ViewPagerFragment fragment = new ViewPagerFragment();
        FragmentManager manager = mActivity.getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(rlContainer.getId(), fragment);
        transaction.commitAllowingStateLoss();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        View view = fragment.getView().findViewById(R.id.vpPager2);

        assertNotNull(view);
    }

    @After
    public void tearDown() throws Exception {
        mActivity = null;
        mActivityTestRule = null;
    }

}