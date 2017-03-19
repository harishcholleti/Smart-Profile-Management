package com.work.train.harish.reminder;

import android.app.ProgressDialog;
import android.content.Context;
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

public class RegisterActivity extends AppCompatActivity {

    EditText emailEdit, passwordEdit, password2Edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        bindViews();
        Global.setupKeyboard(RegisterActivity.this, findViewById(R.id.activity_register));
    }

y    void bindViews(){
        emailEdit = (EditText)findViewById(R.id.email_edit);
        passwordEdit = (EditText)findViewById(R.id.password_edit);
        password2Edit = (EditText)findViewById(R.id.password2_edit);
    }

    public void onLogin(View sender){
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_left_in, R.anim.disappear);
    }

    public void gotoMainActivity(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.disappear);
    }

    public void onRegister(View sender){
        Global.hideKeyboard(RegisterActivity.this);
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
        String password2 = password2Edit.getText().toString();
        if(!password.equals(password2)){
            password2Edit.setError("Enter password correctly");
            return;
        }
        new RegisterTask(this, email, password).execute();
    }

    public class RegisterTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        Context context;
        String email;
        String password;

        public RegisterTask(Context context, String email, String password){
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
            String siteUrl = Constants.WEB_URL_BASE + Constants.REGISTER_USER_URL;
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
                Global.showToast(context, "Failed to register");
            }
            progressDialog.dismiss();
        }
    }
}
