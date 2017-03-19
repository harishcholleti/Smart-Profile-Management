package com.work.train.harish.reminder.task;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

import com.work.train.harish.reminder.util.Global;

import java.io.IOException;
import java.util.List;
import java.util.Locale;



public class GetCityTask extends AsyncTask<String, Void, String> {

    Context context;
    double latitude;
    double longitude;
    String cityName;

    public GetCityTask(Context context, double latitude, double longitude, String cityName){
        this.context = context;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cityName = cityName;
    }

    protected String doInBackground(String... params) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try
        {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses.size() > 0) {
                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName();
                cityName = address;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return cityName;
    }

    @Override
    protected void onPostExecute(String apiResult) {
        super.onPostExecute(apiResult);
        Global.cityName = apiResult;
    }
}
