package com.work.train.harish.reminder.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.work.train.harish.reminder.util.Constants;
import com.work.train.harish.reminder.util.Global;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;



public class UpdateUserTask extends AsyncTask<String, Void, String> {
    ProgressDialog progressDialog;
    Context context;
    public String email;
    public double homeLatitude = Double.MAX_VALUE, homeLongitude = Double.MAX_VALUE;
    public double officeLatitude = Double.MAX_VALUE, officeLongitude = Double.MAX_VALUE;
    public double notificationLatitude = Double.MAX_VALUE, notificationLongitude = Double.MAX_VALUE;
    public int wifiEnabled = -1;
    public int mobileDataEnabled = -1;
    public int homeRingMode = -1, officeRingMode = -1;
    public String notificationText;
    public Date notificationDate;

    boolean bShow;

    UpdateUserTaskListener listener;
    public void setListener(UpdateUserTaskListener listener){
        this.listener = listener;
    }

    public UpdateUserTask(Context context, boolean show){
        this.context = context;
        bShow = show;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(bShow) {
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Login...");
            progressDialog.show();
        }
    }

    protected String doInBackground(String... params) {
        String siteUrl = Constants.WEB_URL_BASE + Constants.UPDATE_USER_URL;
        JSONObject requestJson = new JSONObject();
        try {
            requestJson.accumulate("id", Global.userInfo.id);
            if(email != null)
                requestJson.accumulate("email", email);
            if(notificationText != null)
                requestJson.accumulate("noti_text", notificationText);
            if(homeLatitude != Double.MAX_VALUE)
                requestJson.accumulate("home_lat", homeLatitude);
            if(homeLongitude != Double.MAX_VALUE)
                requestJson.accumulate("home_lon", homeLongitude);
            if(officeLatitude != Double.MAX_VALUE)
                requestJson.accumulate("office_lat", officeLatitude);
            if(officeLongitude != Double.MAX_VALUE)
                requestJson.accumulate("office_lon", officeLongitude);
            if(notificationLatitude != Double.MAX_VALUE)
                requestJson.accumulate("noti_lat", notificationLatitude);
            if(notificationLongitude != Double.MAX_VALUE)
                requestJson.accumulate("noti_lon", notificationLongitude);
            if(wifiEnabled != -1)
                requestJson.accumulate("wifi", wifiEnabled == 0 ? false : true);
            if(mobileDataEnabled != -1)
                requestJson.accumulate("mob_data", mobileDataEnabled == 0 ? false : true);
            if(homeRingMode != -1)
                requestJson.accumulate("home_ring", homeRingMode);
            if(officeRingMode != -1)
                requestJson.accumulate("office_ring", officeRingMode);
            if(notificationDate != null)
                requestJson.accumulate("noti_datetime", Global.getDateString(notificationDate, Constants.DATETIME_FORMAT));
        }catch (Exception e){

        }
        String postParams =requestJson.toString();
        String apiResult = Global.getResultFromUrlPost(siteUrl, postParams);
        return apiResult;
    }

    @Override
    protected void onPostExecute(String apiResult) {
        super.onPostExecute(apiResult);
        try {
            JSONObject reader = new JSONObject(apiResult);
            if (!reader.getBoolean(Constants.API_RESULT_IS_ERROR)) {
                JSONObject resultJson = reader.getJSONObject(Constants.API_RESULT_RESULT);
                JSONObject userJson = resultJson.getJSONObject("user_info");
                Global.userInfo.fromJson(userJson);
                if(bShow)
                    Global.showToast(context, "Success");
                if(listener != null)
                    listener.onUpdateSuccess();
            } else {
                String resultMessage = reader.getString(Constants.API_RESULT_MESSAGE);
                if(bShow)
                    Global.showToast(context, resultMessage);
            }
        }catch (JSONException e) {
            if(bShow)
                Global.showToast(context, "Failed to update");
        }
        if(bShow)
            progressDialog.dismiss();
    }

    public interface UpdateUserTaskListener{
        void onUpdateSuccess();
    }
}
