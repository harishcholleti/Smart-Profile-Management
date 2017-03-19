package com.work.train.harish.reminder.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.work.train.harish.reminder.MainActivity;
import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.task.UpdateUserTask;
import com.work.train.harish.reminder.util.Global;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    View rootView;
    Context context;
    RadioButton wifiEnabledOption, wifiDisabledOption, mobileDataEnabledOption, mobileDataDisabledOption;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        bindViews();
        return rootView;
    }

    void bindViews(){
        wifiEnabledOption = (RadioButton) rootView.findViewById(R.id.wifi_enable_option);
        wifiDisabledOption = (RadioButton) rootView.findViewById(R.id.wifi_disable_option);
        mobileDataEnabledOption = (RadioButton) rootView.findViewById(R.id.mobile_data_enable_option);
        mobileDataDisabledOption = (RadioButton) rootView.findViewById(R.id.mobile_data_disable_option);

        if(Global.userInfo.wifiEnabled)
            wifiEnabledOption.setChecked(true);
        else
            wifiDisabledOption.setChecked(true);
        if(Global.userInfo.mobileDataEnabled)
            mobileDataEnabledOption.setChecked(true);
        else
        mobileDataDisabledOption.setChecked(true);

        rootView.findViewById(R.id.nav_image).setOnClickListener(this);
        rootView.findViewById(R.id.submit_button).setOnClickListener(this);

        ((TextView)rootView.findViewById(R.id.title_label)).setText("Settings");
    }

    void onSubmit(){
        UpdateUserTask task = new UpdateUserTask(context, true);
        task.wifiEnabled = wifiEnabledOption.isChecked() ? 1 : 0;
        task.mobileDataEnabled = mobileDataEnabledOption.isChecked() ? 1 : 0;
        task.setListener(new UpdateUserTask.UpdateUserTaskListener() {
            @Override
            public void onUpdateSuccess() {
                Global.setWifiEnabled(context, Global.userInfo.wifiEnabled);
                Global.setMobileDataState(context, Global.userInfo.mobileDataEnabled);
            }
        });
        task.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_image:
                ((MainActivity)context).onShowNavMenu(v);
                break;
            case R.id.submit_button:
                onSubmit();
                break;
        }
    }
}
