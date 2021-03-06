package com.finalyearproject.dan.ocraccountingapp.nav;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.finalyearproject.dan.ocraccountingapp.camera.OpenCVCamera;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.calendar.ViewPagerFragment;
import com.finalyearproject.dan.ocraccountingapp.signin.SignInActivity;
import com.finalyearproject.dan.ocraccountingapp.statistics.StatisticsFragment;
import com.finalyearproject.dan.ocraccountingapp.amazon.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityManager;
import com.finalyearproject.dan.ocraccountingapp.amazon.user.IdentityProvider;
import com.mikepenz.itemanimators.AlphaCrossFadeAnimator;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

public class NavDrawerInstaller {

    // The identity manager used to keep track of the current user account
    private IdentityManager identityManager;

    public void installOnActivity(final AppCompatActivity activity, Toolbar toolbar) {

        String userName = getUserName();
        Bitmap userImage = getUserImage();

        // Create the profile
        final IProfile profile = new ProfileDrawerItem().withName(userName).withIcon(userImage).withIdentifier(100);
        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withCompactStyle(true)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .build();

        //Create the drawer
        new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Calendar").withIcon(R.drawable.nav_calendar_icon).withIdentifier(1),
                        new PrimaryDrawerItem().withName("Receipt Capture").withIcon(R.drawable.nav_camera_icon).withIdentifier(2),
                        new PrimaryDrawerItem().withName("Statistics").withIcon(R.drawable.nav_statistics_icon).withIdentifier(3),
                        new SectionDrawerItem().withName("Account"),
                        new SecondaryDrawerItem().withName("Log Out").withIcon(R.drawable.nav_logout_icon).withIdentifier(4)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //check if the drawerItem is set.
                        //there are different reasons for the drawerItem to be null
                        //--> click on the header
                        //--> click on the footer
                        //those items don't contain a drawerItem

                        if (drawerItem != null) {
                            if (drawerItem.getIdentifier() == 1) {
                                Fragment fragment = new ViewPagerFragment();
                                FragmentManager manager = activity.getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.main_fragment_container, fragment);
                                transaction.commit();
                                //intent = new Intent(activity, VPFragment.class);
                            }
                            if (drawerItem.getIdentifier() == 2) {
                                Intent cameraIntent;

                                cameraIntent = new Intent(activity, OpenCVCamera.class);

                                //Intent cameraIntent = new Intent(activity, CamActivity.class);
                                activity.startActivity(cameraIntent);
                            }
                            if (drawerItem.getIdentifier() == 3) {
                                Fragment fragment = new StatisticsFragment();
                                FragmentManager manager = activity.getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.main_fragment_container, fragment);
                                transaction.commit();
                                //intent = new Intent(activity, ReceiptCaptureActivity.class);
                            }
                            if (drawerItem.getIdentifier() == 4) {
                                // Obtain a reference to the mobile client. It is created in the Application class,
                                // but in case a custom Application class is not used, we initialize it here if necessary.
                                AWSMobileClient.initializeMobileClientIfNecessary(activity);

                                // Obtain a reference to the mobile client. It is created in the Application class.
                                final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

                                // Obtain a reference to the identity manager.
                                identityManager = awsMobileClient.getIdentityManager();
                                // The user is currently signed in with a provider. Sign out of that provider.
                                identityManager.signOut();
                                activity.startActivity(new Intent(activity, SignInActivity.class));
                                activity.finish();
                            }
                        }

                        return false;
                    }
                })
                .withShowDrawerOnFirstLaunch(true)
                .build();
    }


    private String getUserName() {

        AWSMobileClient.defaultMobileClient().getIdentityManager().getCachedUserID();
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        final IdentityProvider identityProvider = identityManager.getCurrentIdentityProvider();

        if(!identityProvider.getUserName().equals(null)) {
            Log.e("IDENTITY PROVIDER ", identityProvider.getUserName());
            return identityProvider.getUserName();
        }
        else {
            //REPLACE WITH SHAREDPREFS OF STORED USER NAME
            return "User";
        }
    }

    private Bitmap getUserImage() {

        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        return identityManager.getUserImage();
    }

}
