package com.work.train.harish.reminder.task;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.work.train.harish.reminder.util.Global;



public class DrawPathTask extends AsyncTask<Void, Void, String> {
    GoogleMap googleMap;
    double destLatitude;
    double destLongitude;
    Context context;

    TextView timeLabel;

    public DrawPathTask(Context context, GoogleMap map, double destLatitude, double destLongitude, TextView timeLabel){
        this.context = context;
        googleMap = map;
        this.destLatitude = destLatitude;
        this.destLongitude = destLongitude;
        this.timeLabel = timeLabel;
    }
    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
    }
    @Override
    protected String doInBackground(Void... params) {
        String url = Global.getRouteUrl(context, destLatitude, destLongitude);
        String json = Global.callGetRequest(url);
        return json;
    }
    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(result!=null){
            int time = Global.drawPath(result, googleMap, new LatLng(Global.getLatitude(), Global.getLongitude()), new LatLng(destLatitude, destLongitude));
            if(time >= 0 && timeLabel != null){
                String deadlineString = Global.getTimeString((time - 1) / 60 + 1);
                if(deadlineString.isEmpty())
                    deadlineString = "0 min";
                timeLabel.setText(deadlineString);
            }else
                timeLabel.setText("");
        }
    }
}
