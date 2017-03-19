package com.work.train.harish.reminder.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.work.train.harish.reminder.MainActivity;
import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.task.GetCityTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Global {

    public static User userInfo = new User();

    public static Location myLocation;
    public static String cityName = "";
    static LocationManager locManager;

    public static String getResultFromUrlPost(String siteUrl, String postParam) {
        String apiResult = "";
        try {
            URL url = new URL(siteUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/application/octet-stream");
            connection.setRequestMethod("POST");

            if(postParam != null) {
                OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());
                request.write(postParam);
                request.flush();
                request.close();
            }
            String line = "";
            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            // Response from server after login process will be stored in response variable.
            apiResult = sb.toString();
            // You can perform UI operations here
            isr.close();
            reader.close();

        } catch (Exception e) {
            String error = e.getMessage();
            e.printStackTrace();

        }
        return apiResult;
    }

    public static void showToast(Context context, String message){
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void getCityName(Context context){
        if(myLocation != null)
            new GetCityTask(context, Global.myLocation.getLatitude(), Global.myLocation.getLongitude(), Global.cityName).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static void initLocation(Context context, LocationListener locationListener) {

        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (!checkNetwork(context)) {
            alertNoNetwork(context);
        } else if (!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            alertNoGps(context);
        }
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Global.myLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (Global.myLocation == null)
                Global.myLocation = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            getCityName(context);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
        } catch (Exception e) {
            showToast(context, e.getMessage());
        }
        if (Global.myLocation == null) {
            Global.myLocation = new Location("");
            Global.myLocation.setLatitude(0);
            Global.myLocation.setLongitude(0);
        }
    }

    public static boolean checkNetwork(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isAvailable()
                && activeNetwork.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    public static void alertNoNetwork(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.network_title)
                .setMessage(R.string.network_mes)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                context.startActivity(new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void alertNoGps(final Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.gps_title)
                .setMessage(R.string.gps_mes)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                context.startActivity(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public static void setupKeyboard(final Activity activity, View view) {
        if(!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(activity);
                    return false;
                }

            });
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupKeyboard(activity, innerView);
            }
        }
    }

    public static void hideKeyboard(Activity activity) {
        // Check if no view has focus:
        View view1 = activity.getCurrentFocus();
        if (view1 != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(), 0);
        }
    }

    public static int getSettingsInt(Context context, String key, int defaultValue){
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getInt(key, defaultValue);
    }

    public static String getSettingsString(Context context, String key, String defaultValue){
        return context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    public static void saveSettingsInt(Context context, String key, int value){
        context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putInt(key, value).commit();
    }

    public static void saveSettingsString(Context context, String key, String value){
        context.getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE).edit().putString(key, value).commit();
    }

    public static String callGetRequest(String urlString) {
        try {
            CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

            if (Thread.interrupted())
                throw new InterruptedException();

            InputStream response = con.getInputStream();

            InputStreamReader is = new InputStreamReader(response);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(is);
            String read = br.readLine();

            while (read != null) {
                sb.append(read);
                read = br.readLine();
            }

            String result = sb.toString();
            con.disconnect();
            return result;

        } catch (Exception e) {
            return "Error";
        }
    }

    public static String getRouteUrl (Context context, double destlat, double destlog){
        if(myLocation == null)
            return "";
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(getLatitude()));
        urlString.append(",");
        urlString.append(Double.toString(getLongitude()));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=" + context.getResources().getString(R.string.google_map_api));
        return urlString.toString();
    }

    public static double getLatitude(){
        return myLocation == null ? 0 : myLocation.getLatitude();
    }

    public static double getLongitude(){
        return myLocation == null ? 0 : myLocation.getLongitude();
    }

    public static int drawPath(String result, GoogleMap map, LatLng startPos, LatLng endPos) {

        int totalTime = 0;
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            if(list.size() == 2) {
                list.clear();
            }
            list.add(0, startPos);
            list.add(endPos);
            /*map.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(5)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );*/
            JSONArray legJsonArray = routes.getJSONArray("legs");
            for(int i = 0; i < legJsonArray.length(); i++) {
                JSONObject legJson = legJsonArray.getJSONObject(i);
                JSONObject durationJson = legJson.getJSONObject("duration");
                int time = durationJson.getInt("value");
                totalTime += time;
            }
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        }
        catch (JSONException e) {
            return -1;
        }
        return totalTime;
    }

    public static List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    public static String getTimeString(int minutes){
        String result;
        if(minutes <= 0)
            result = "";
        else if(minutes < 60)
            result = minutes + " min";
        else
            result = (minutes-1) / 60 + 1 + " hours";
        return result;
    }

    public static Dialog createDialog(Context context, int resId){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(resId);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);
//        dialog.getWindow().getDecorView().setLeft(32);
        return dialog;
    }

    public final static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static boolean checkAndRequestPermissions(Activity activity, int requestCode){
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity,Manifest.permission.CHANGE_WIFI_STATE)!= PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity,Manifest.permission.CHANGE_NETWORK_STATE)!= PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CHANGE_WIFI_STATE) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CHANGE_NETWORK_STATE)) {
                return true;
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE},
                        requestCode);
                return false;
            }
        }
        return true;
    }

    public static void sendNotification(Context context, String message) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle(context.getResources().getString(R.string.app_name));
        builder.setContentText(message);
        builder.setSmallIcon(R.drawable.app_icon);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.app_icon);
        builder.setLargeIcon(bitmap);

        builder.setAutoCancel(true); // dismiss notification on user click

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent notifyPIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        builder.setContentIntent(notifyPIntent);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        NotificationManager notiManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notiManager.notify(0, builder.build());
    }

    public static void setWifiEnabled(Context context, boolean enabled){
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(enabled);
    }

    public static void setMobileDataState(Context context, boolean enabled)
    {
        try {
            final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class.forName(conman.getClass().getName());
            final Field connectivityManagerField = conmanClass.getDeclaredField("mService");
            connectivityManagerField.setAccessible(true);
            final Object connectivityManager = connectivityManagerField.get(conman);
            final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);

            setMobileDataEnabledMethod.invoke(connectivityManager, enabled);
        }catch (Exception e){
            Log.d("SetMobileDataState", e.getMessage());
        }
    }

    public static String getMiles(double meter) {
        return String.format("%.2f mile",  meter*0.000621371192);
    }

    public static String getDistanceString(double distance){
        String result = "";
        if(distance >= 1000){
            result = String.format("%.2fkm", distance/1000);
        }else
            result = Math.round(distance) + "m";
        if(result == null) result = "";
        return result;
    }

    public static float getDistanceTo(double latitude, double longitude){
        Location home = new Location("");
        home.setLatitude(latitude);
        home.setLongitude(longitude);
        return Global.myLocation.distanceTo(home);
    }

    public static LatLng getLocationFromAddress(Context context, String locationName){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List addressList = geocoder.getFromLocationName(locationName, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = (Address)addressList.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
//            Log.e(TAG, "Unable to connect to Geocoder", e);
        }
        return null;
    }

    public static ArrayList<String> autocomplete(Context context, String input) {
        ArrayList<String> resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(Constants.PLACES_API_BASE);
            String apiKey = context.getResources().getString(R.string.google_map_api);
            sb.append("?key=" + apiKey);
//            sb.append("&components=country:gr");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            String urlString = sb.toString();
            URL url = new URL(urlString);

            System.out.println("URL: "+url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
//            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
//            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {

            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            resultList = new ArrayList<String>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
//            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }

        return resultList;
    }

    public static Date getDateFromString(String dateString, String format){
        try {
            SimpleDateFormat dateParser = new SimpleDateFormat(format);
            Date date = dateParser.parse(dateString);
            return date;
        }catch(Exception e){
            String error = e.getMessage();
            return null;
        }
    }

    public static String getDateString(Date date, String format){
        SimpleDateFormat df = new SimpleDateFormat(format);
        return df.format(date);
    }

    public static Date getUpdatedDate(Date origin, int year, int month, int day){
        Calendar cal = Calendar.getInstance();
        if(origin != null)
            cal.setTime(origin);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        return cal.getTime();
    }

    public static Date getUpdatedTime(Date origin, int hour, int minute){
        Calendar cal = Calendar.getInstance();
        if(origin != null)
            cal.setTime(origin);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        return cal.getTime();
    }
}
