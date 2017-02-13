package com.finalyearproject.dan.ocraccountingapp.nav;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.finalyearproject.dan.ocraccountingapp.FileBrowserFragment;
import com.finalyearproject.dan.ocraccountingapp.R;
import com.finalyearproject.dan.ocraccountingapp.ReceiptCaptureFragment;
import com.finalyearproject.dan.ocraccountingapp.Sandbox;
import com.finalyearproject.dan.ocraccountingapp.SignInActivity;
import com.finalyearproject.dan.ocraccountingapp.StatisticsFragment;
import com.finalyearproject.dan.ocraccountingapp.VPFragment;
import com.finalyearproject.dan.ocraccountingapp.mobile.AWSMobileClient;
import com.finalyearproject.dan.ocraccountingapp.mobile.user.IdentityManager;
import com.finalyearproject.dan.ocraccountingapp.mobile.user.IdentityProvider;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
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

    //save our header or result
    private AccountHeader headerResult = null;
    // The identity manager used to keep track of the current user account
    private IdentityManager identityManager;

    public void installOnActivity(final AppCompatActivity activity, Toolbar toolbar) {

        String userName = getUserName();
        Bitmap userImage = getUserImage();

        // Create the profile
        final IProfile profile = new ProfileDrawerItem().withName(userName).withIcon(userImage).withIdentifier(100);
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(activity)
                .withCompactStyle(true)
                .withTranslucentStatusBar(true)
                .withHeaderBackground(R.drawable.header)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(profile)
                .build();

        //Create the drawer
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withHasStableIds(true)
                .withItemAnimator(new AlphaCrossFadeAnimator())
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Calendar").withIcon(R.drawable.nav_calendar_icon).withIdentifier(1),
                        new PrimaryDrawerItem().withName("Receipt Capture").withIcon(R.drawable.nav_receipt_icon).withIdentifier(2),
                        new PrimaryDrawerItem().withName("Statistics").withIcon(R.drawable.nav_statistics_icon).withIdentifier(3),
                        new PrimaryDrawerItem().withName("Sandbox").withIcon(R.drawable.nav_receipt_icon).withIdentifier(4),
                        new PrimaryDrawerItem().withName("File Browser").withIcon(R.drawable.nav_receipt_icon).withIdentifier(5),
                        new SectionDrawerItem().withName("Account"),
                        new SecondaryDrawerItem().withName("Settings").withIcon(FontAwesome.Icon.faw_cog),
                        new SecondaryDrawerItem().withName("Log Out").withIcon(R.drawable.nav_logout_icon).withIdentifier(6)
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
                            Intent intent = null;
                            if (drawerItem.getIdentifier() == 1) {
                                Fragment fragment = new VPFragment();
                                FragmentManager manager = activity.getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.main_fragment_container, fragment);
                                transaction.commit();
                                //intent = new Intent(activity, VPFragment.class);
                            }
                            if (drawerItem.getIdentifier() == 2) {
                                Fragment fragment = new ReceiptCaptureFragment();
                                FragmentManager manager = activity.getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.main_fragment_container, fragment);
                                transaction.commit();
                                //intent = new Intent(activity, ReceiptCaptureActivity.class);
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
                                intent = new Intent(activity, Sandbox.class);
                            }
                            if (drawerItem.getIdentifier() == 5) {
                                Fragment fragment = new FileBrowserFragment();
                                FragmentManager manager = activity.getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.main_fragment_container, fragment);
                                transaction.commit();
                            }
                            if (drawerItem.getIdentifier() == 6) {

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
                            if (intent != null) {
                                activity.startActivity(intent);
                            }
                        }

                        return false;
                    }
                })
                .withShowDrawerOnFirstLaunch(true)
//                .withShowDrawerUntilDraggedOpened(true)
                .build();
    }


    private String getUserName() {
        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        final IdentityProvider identityProvider = identityManager.getCurrentIdentityProvider();
        return identityProvider.getUserName();
    }


    private Bitmap getUserImage() {

        final IdentityManager identityManager = AWSMobileClient.defaultMobileClient().getIdentityManager();
        return identityManager.getUserImage();
    }



}
