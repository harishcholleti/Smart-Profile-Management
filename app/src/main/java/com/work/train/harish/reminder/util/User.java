package com.work.train.harish.reminder.util;

import android.util.Log;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;


public class User {
    public long id;
    public String email;
    public String password;
    public double homeLatitude, homeLongitude;
    public double officeLatitude, officeLongitude;
    public double notificationLatitude, notificationLongitude;
    public boolean wifiEnabled;
    public boolean mobileDataEnabled;
    public int homeRingMode, officeRingMode;
    public String notificationText;
    public Date notificationDate;

    public User(){}

    public void fromJson(JSONObject json){
        try {
            id = json.has("id") ? json.getLong("id") : 0;
            email = json.getString("email");
            password = json.getString("password");
            notificationText = json.getString("noti_text");
            homeLatitude = json.getDouble("home_lat");
            homeLongitude = json.getDouble("home_lon");
            officeLatitude = json.getDouble("office_lat");
            officeLongitude = json.getDouble("office_lon");
            notificationLatitude = json.getDouble("noti_lat");
            notificationLongitude = json.getDouble("noti_lon");
            wifiEnabled = json.getInt("wifi") == 0 ? false : true;
            mobileDataEnabled = json.getInt("mob_data") == 0 ? false : true;
            homeRingMode = json.getInt("home_ring");
            officeRingMode = json.getInt("office_ring");
            String dateString = json.getString("noti_datetime");
            notificationDate = Global.getDateFromString(dateString, Constants.DATETIME_FORMAT);
            if(notificationDate != null) {
                Calendar calendar = Calendar.getInstance();
                Date now = calendar.getTime();
                if(now.getTime()- notificationDate.getTime() > 10000)
                    notificationDate = null;
            }
        }catch(Exception e){
            Log.d("User", e.getMessage());
        }
    }

}
