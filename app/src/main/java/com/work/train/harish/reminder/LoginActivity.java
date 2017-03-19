package com.work.train.harish.reminder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.work.train.harish.reminder.util.Constants;
import com.work.train.harish.reminder.util.Global;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity{

    Context context;
    EditText emailEdit, passwordEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        context = LoginActivity.this;

        bindViews();
        Global.setupKeyboard(LoginActivity.this, findViewById(R.id.activity_login));
    }

    @Override
    protected void onResume() {
        super.onResume();
        String email = Global.getSettingsString(this, Constants.USER_EMAIL_KEY, "");
        String password = Global.getSettingsString(this, Constants.USER_PASSWORD_KEY, "");
        if(!email.isEmpty()) {
            emailEdit.setText(email);
            passwordEdit.setText(password);
            onLogin(null);
        }
    }

    void bindViews(){
        emailEdit = (EditText)findViewById(R.id.email_edit);
        passwordEdit = (EditText)findViewById(R.id.password_edit);
    }

    public void onLogin(View sender){
        String email = emailEdit.getText().toString().trim();
        if(email.isEmpty()){
            emailEdit.setError("Enter email");
            return;
        }else if(!Global.isEmailValid(email)){
            emailEdit.setError("Enter valid email");
            return;
        }

        String password = passwordEdit.getText().toString();
        if(password.isEmpty()){
            passwordEdit.setError("Enter password");
            return;
        }
        new LoginTask(this, email, password).execute();
    }

    public void onResetPassword(View sender) {
        final Dialog dialog = Global.createDialog(this, R.layout.dialog_reset_password);
        dialog.findViewById(R.id.reset_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.hideKeyboard((Activity) context);
                EditText dlgEmailEdit = (EditText)dialog.findViewById(R.id.email_edit);
                final String email = dlgEmailEdit.getText().toString().trim();
                if(email.isEmpty()){
                    emailEdit.setError("Enter email");
                    return;
                }else if(!Global.isEmailValid(email)){
                    emailEdit.setError("Enter valid email");
                    return;
                }
                new android.support.v7.app.AlertDialog.Builder(context)
                        .setTitle("Reset password")
                        .setMessage("Are you sure you?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new ResetPasswordTask(context, email).execute();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        dialog.show();
    }

    public void gotoMainActivity(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.disappear);
    }

    public void onRegister(View sender){
        finish();
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.disappear);
    }

    public class LoginTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        Context context;
        String email;
        String password;

        public LoginTask(Context context, String email, String password){
            this.context = context;
            this.email = email;
            this.password = password;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Login...");
            progressDialog.show();

        }

        protected String doInBackground(String... params) {
            String siteUrl = Constants.WEB_URL_BASE + Constants.LOGIN_USER_URL;
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.accumulate("email", email);
                requestJson.accumulate("password", password);
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
                    Global.saveSettingsString(context, Constants.USER_EMAIL_KEY, email);
                    Global.saveSettingsString(context, Constants.USER_PASSWORD_KEY, password);
                    gotoMainActivity();
                } else {
                    String resultMessage = reader.getString(Constants.API_RESULT_MESSAGE);
                    Global.showToast(context, resultMessage);
                }
            }catch (JSONException e) {
                Global.showToast(context, "Failed to login");
            }
            progressDialog.dismiss();
        }
    }

    public class ResetPasswordTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        Context context;
        String email;

        public ResetPasswordTask(Context context, String email){
            this.context = context;
            this.email = email;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Login...");
            progressDialog.show();

        }

        protected String doInBackground(String... params) {
            String siteUrl = Constants.WEB_URL_BASE + Constants.RESET_PASSWORD_URL;
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.accumulate("email", email);
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
                    Global.showToast(context, "Success");
                } else {
                    String resultMessage = reader.getString(Constants.API_RESULT_MESSAGE);
                    Global.showToast(context, resultMessage);
                }
            }catch (JSONException e) {
                Global.showToast(context, "Failed to login");
            }
            progressDialog.dismiss();
        }
    }
}
