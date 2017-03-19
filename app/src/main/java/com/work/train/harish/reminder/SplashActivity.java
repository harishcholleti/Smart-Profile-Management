package com.work.train.harish.reminder;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.work.train.harish.reminder.util.Constants;
import com.work.train.harish.reminder.util.Global;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(Global.checkAndRequestPermissions(SplashActivity.this, Constants.PERMISSION_REQUEST_CODE))
            showMainActivity();

    }

    void showMainActivity(){
        Thread timerThread = new Thread(){
            public void run(){
                try{
                    sleep(300);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }finally{
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        };
        timerThread.start();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE:
                int count = 0;
                for (int i = 0; i < grantResults.length; i++)
                    if(grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        count++;
                    }
                if(count < grantResults.length) {
                    Global.showToast(getApplicationContext(), "Permission Denied, You cannot access app.");
                }else {
                    showMainActivity();
                }
                break;

        }
    }

}
