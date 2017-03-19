package com.work.train.harish.reminder;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.TimeUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.work.train.harish.reminder.fragment.HomeFragment;
import com.work.train.harish.reminder.fragment.OfficeFragment;
import com.work.train.harish.reminder.fragment.ProfileFragment;
import com.work.train.harish.reminder.fragment.ReminderFragment;
import com.work.train.harish.reminder.fragment.SettingsFragment;
import com.work.train.harish.reminder.fragment.TravelFragment;
import com.work.train.harish.reminder.task.UpdateUserTask;
import com.work.train.harish.reminder.util.Constants;
import com.work.train.harish.reminder.util.Global;
import com.work.train.harish.reminder.util.NavDrawerItem;
import com.work.train.harish.reminder.adapter.NavDrawerListAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.microedition.khronos.opengles.GL;

public class MainActivity extends AppCompatActivity {

    Context context;
    final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            onMyLocationChanged(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };
    LocationManager locManager;

    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mDrawerToggle;
    View navHeaderView;
    // slide menu items
    String[] navMenuTitles;
//    TypedArray navMenuIcons;

    ArrayList<NavDrawerItem> navDrawerItems;
    NavDrawerListAdapter navDrawerListAdapter;

    int fragmentPosition = 0;
    ProfileFragment profileFragment = new ProfileFragment();
    HomeFragment homeFragment = new HomeFragment();
    OfficeFragment officeFragment = new OfficeFragment();
    ReminderFragment reminderFragment = new ReminderFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    TravelFragment travelFragment = new TravelFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = MainActivity.this;

        Global.initLocation(context, mLocationListener);

        Global.setupKeyboard(this, findViewById(R.id.drawer_layout));
        initNavMenu();
        displayView(Constants.HOME_FRAGMENT);
    }

    void initNavMenu(){
        navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        navDrawerItems = new ArrayList<>();

        for(int i = 0; i < navMenuTitles.length; i++)
            navDrawerItems.add(new NavDrawerItem(navMenuTitles[i], -1));

        LayoutInflater inflater =  (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        navHeaderView = inflater.inflate(R.layout.nav_header, null, false);
        ((TextView)navHeaderView.findViewById(R.id.user_name_label)).setText(Global.userInfo.email);
        mDrawerList.addHeaderView(navHeaderView);

        navDrawerListAdapter = new NavDrawerListAdapter(getApplicationContext(),
                navDrawerItems);
        mDrawerList.setAdapter(navDrawerListAdapter);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.nav_icon, //nav menu toggle icon
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ){
            public void onDrawerClosed(View view) {
//                getActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
//                getActionBar().setTitle(mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mDrawerLayout.closeDrawer(mDrawerList);
            switch (position){
                case Constants.PROFILE_FRAGMENT:
                case Constants.HOME_FRAGMENT:
                case Constants.OFFICE_FRAGMENT:
                case Constants.REMINDER_FRAGMENT:
                case Constants.SETTINGS_FRAGMENT:
                case Constants.TRAVEL_FRAGMENT:
                    displayView(position);
                    break;
                case Constants.LOGOUT:
                    onLogout();
                    break;
            }
            navDrawerListAdapter.setSelPosition(position - 1);
        }
    }

    Fragment getFragment(int position){
        Fragment fragment = null;
        switch (position) {
            case Constants.PROFILE_FRAGMENT:
                fragment = profileFragment;
                break;
            case Constants.HOME_FRAGMENT:
                fragment = homeFragment;
                break;
            case Constants.OFFICE_FRAGMENT:
                fragment = officeFragment;
                break;
            case Constants.REMINDER_FRAGMENT:
                fragment = reminderFragment;
                break;
            case Constants.SETTINGS_FRAGMENT:
                fragment = settingsFragment;
                break;
            case Constants.TRAVEL_FRAGMENT:
                fragment = travelFragment;
                break;
        }
        return fragment;
    }

    public void refreshNavHeader(){
        ((TextView)navHeaderView.findViewById(R.id.user_name_label)).setText(Global.userInfo.email);
    }

    public Fragment displayView(int position) {
        fragmentPosition = position;
        navDrawerListAdapter.setSelPosition(position - 1);

        Fragment fragment = getFragment(position);
        if (fragment != null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_container, fragment).commitAllowingStateLoss();

            mDrawerList.setItemChecked(position, true);
            mDrawerList.setSelection(position);
            mDrawerLayout.closeDrawer(mDrawerList);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
        return fragment;
    }

    public void onShowNavMenu(View v){
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    void onLogout() {
        new AlertDialog.Builder(context)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Global.saveSettingsString(context, Constants.USER_EMAIL_KEY, "");
                        Global.saveSettingsString(context, Constants.USER_PASSWORD_KEY, "");
                        gotoLogin();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    void gotoLogin(){
        Intent intent = new Intent(context, LoginActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.disappear, R.anim.push_right_out);
    }

    void onMyLocationChanged(final Location location){
        if (location == null)
            return;
        Global.myLocation = location;

        double deltaDistance = 10;
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        float toHome = Global.getDistanceTo(Global.userInfo.homeLatitude, Global.userInfo.homeLongitude);
        float toOffice = Global.getDistanceTo(Global.userInfo.officeLatitude, Global.userInfo.officeLongitude);
        if(toHome < deltaDistance) {
            if(am.getRingerMode() != Global.userInfo.homeRingMode)
                am.setRingerMode(Global.userInfo.homeRingMode);

            if(!Global.userInfo.wifiEnabled || Global.userInfo.mobileDataEnabled) {
                UpdateUserTask task = new UpdateUserTask(context, false);
                task.wifiEnabled = 1;
                task.mobileDataEnabled = 0;
                task.execute();
            }
            Global.setWifiEnabled(context, true);
            Global.setMobileDataState(context, false);
        }else if(toOffice < deltaDistance){
            if(am.getRingerMode() != Global.userInfo.officeRingMode)
                am.setRingerMode(Global.userInfo.officeRingMode);

            if(Global.userInfo.wifiEnabled || Global.userInfo.mobileDataEnabled) {
                UpdateUserTask task = new UpdateUserTask(context, false);
                task.wifiEnabled = 0;
                task.mobileDataEnabled = 1;
                task.execute();
            }
            Global.setWifiEnabled(context, false);
            Global.setMobileDataState(context, true);
        }

        if(!Global.userInfo.notificationText.isEmpty()) {
            boolean bShow = true;
            if(Global.userInfo.notificationDate != null){
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                if(now.before(Global.userInfo.notificationDate))
                    bShow = false;
                else{
                    long diffInMs = now.getTime() - Global.userInfo.notificationDate.getTime();
                    if(diffInMs > 60000)
                        bShow = false;
                }
            }
            Global.userInfo.notificationDate = null;
            if(bShow) {
                float toNotification = Global.getDistanceTo(Global.userInfo.notificationLatitude, Global.userInfo.notificationLongitude);
                if (toNotification < deltaDistance)
                    Global.sendNotification(context, Global.userInfo.notificationText);
            }
        }

    }

}
