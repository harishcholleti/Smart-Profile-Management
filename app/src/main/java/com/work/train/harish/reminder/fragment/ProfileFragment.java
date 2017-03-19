package com.work.train.harish.reminder.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.work.train.harish.reminder.MainActivity;
import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.task.UpdateUserTask;
import com.work.train.harish.reminder.util.Constants;
import com.work.train.harish.reminder.util.Global;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment implements View.OnClickListener {

    View rootView;
    Context context;
    EditText emailEdit;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        bindViews();
        return rootView;
    }

    public void bindViews(){
        emailEdit = (EditText)rootView.findViewById(R.id.email_edit);
        emailEdit.setText(Global.userInfo.email);

        rootView.findViewById(R.id.save_button).setOnClickListener(this);
        rootView.findViewById(R.id.change_password_button).setOnClickListener(this);
        rootView.findViewById(R.id.nav_image).setOnClickListener(this);
    }

    public void onSave(){
        Global.hideKeyboard((Activity) context);
        String email = emailEdit.getText().toString().trim();
        if(email.isEmpty()){
            emailEdit.setError("Enter email");
            return;
        }else if(!Global.isEmailValid(email)){
            emailEdit.setError("Enter valid email");
            return;
        }

        UpdateUserTask task = new UpdateUserTask(context, true);
        task.email = email;
        task.setListener(new UpdateUserTask.UpdateUserTaskListener() {
            @Override
            public void onUpdateSuccess() {
                ((MainActivity)context).refreshNavHeader();
                Global.saveSettingsString(context, Constants.USER_EMAIL_KEY, Global.userInfo.email);
            }
        });
        task.execute();
    }

    public void onChangePassword(){
        Global.hideKeyboard((Activity) context);
        final Dialog dialog = Global.createDialog(context, R.layout.dialog_change_password);
        dialog.findViewById(R.id.change_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.hideKeyboard((Activity)context);
                EditText dlgOldPasswordEdit = (EditText)dialog.findViewById(R.id.old_password_edit);
                EditText dlgNewPasswordEdit = (EditText)dialog.findViewById(R.id.new_password_edit);
                EditText dlgNewPassword2Edit = (EditText)dialog.findViewById(R.id.password2_edit);

                final String oldPassword = dlgOldPasswordEdit.getText().toString();
                final String newPassword = dlgNewPasswordEdit.getText().toString();
                String newPassword2 = dlgNewPassword2Edit.getText().toString();
                if(oldPassword.isEmpty()){
                    dlgOldPasswordEdit.setError("Enter old password");
                    return;
                }
                if(newPassword.isEmpty()){
                    dlgNewPasswordEdit.setError("Enter new password");
                    return;
                }

                if(!newPassword.equals(newPassword2)){
                    dlgNewPassword2Edit.setError("Enter new password correctly");
                    return;
                }
                new android.support.v7.app.AlertDialog.Builder(context)
                        .setTitle("Reset password")
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                new ChangePasswordTask(context, oldPassword, newPassword).execute();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_image:
                ((MainActivity)context).onShowNavMenu(v);
                break;
            case R.id.save_button:
                onSave();
                break;
            case R.id.change_password_button:
                onChangePassword();
                break;
        }
    }


    public class ChangePasswordTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        Context context;
        String oldPassword;
        String newPassword;

        public ChangePasswordTask(Context context, String oldPassword, String newPassword){
            this.context = context;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Update...");
            progressDialog.show();

        }

        protected String doInBackground(String... params) {
            String siteUrl = Constants.WEB_URL_BASE + Constants.CHANGE_PASSWORD_URL;
            JSONObject requestJson = new JSONObject();
            try {
                requestJson.accumulate("id", Global.userInfo.id);
                requestJson.accumulate("password", oldPassword);
                requestJson.accumulate("new_password", newPassword);
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
                Global.showToast(context, "Failed to change password");
            }
            progressDialog.dismiss();
        }
    }
}
