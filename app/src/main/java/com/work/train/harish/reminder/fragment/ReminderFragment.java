package com.work.train.harish.reminder.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.work.train.harish.reminder.MainActivity;
import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.adapter.GooglePlacesAutocompleteAdapter;
import com.work.train.harish.reminder.task.UpdateUserTask;
import com.work.train.harish.reminder.util.Global;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ReminderFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, AdapterView.OnItemClickListener {

    View rootView;
    Context context;

    GoogleMap mMap;
    Marker marker;
    EditText notificationEdit;
    TextView dateLabel, timeLabel;
    Date notificationDate;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_reminder, container, false);
        notificationDate = Global.userInfo.notificationDate;

        bindViews();
        refreshPickupDate();
        refreshPickupTime();
        Global.setupKeyboard((Activity)context, rootView);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) rootView.findViewById(R.id.location_edit);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(context, R.layout.autocomplete_list_item));
        autoCompView.setOnItemClickListener(this);
        return rootView;
    }

    void bindViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        notificationEdit = (EditText)rootView.findViewById(R.id.notification_edit);
        notificationEdit.setText(Global.userInfo.notificationText);
        dateLabel = (TextView)rootView.findViewById(R.id.date_label);
        timeLabel = (TextView)rootView.findViewById(R.id.time_label);
        dateLabel.setOnClickListener(this);
        timeLabel.setOnClickListener(this);

        rootView.findViewById(R.id.submit_button).setOnClickListener(this);
        rootView.findViewById(R.id.nav_image).setOnClickListener(this);
        ((TextView)rootView.findViewById(R.id.title_label)).setText("Reminder");
    }

    void onSubmit(){
        UpdateUserTask task = new UpdateUserTask(context, true);
        if(marker != null) {
            task.notificationLatitude = marker.getPosition().latitude;
            task.notificationLongitude = marker.getPosition().longitude;
        }
        if(notificationDate != null)
            task.notificationDate = notificationDate;
        task.notificationText = notificationEdit.getText().toString();
        task.execute();
    }

    public void onSelectPickupDate(){
        Calendar calendar = Calendar.getInstance();
        if(notificationDate != null)
            calendar.setTime(notificationDate);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DATE);
        new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int month, int day) {
                notificationDate = Global.getUpdatedDate(notificationDate, year, month, day);
                refreshPickupDate();
            }
        }, year, month, day).show();
    }

    public void onSelectPickupTime(){
        Calendar calendar = Calendar.getInstance();
        if(notificationDate != null)
            calendar.setTime(notificationDate);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hour, int minute) {
                notificationDate = Global.getUpdatedTime(notificationDate, hour, minute);
                refreshPickupTime();
            }
        }, hour, minute,false).show();
    }

    public void refreshPickupDate(){
        if(notificationDate != null)
            dateLabel.setText(Global.getDateString(notificationDate, "yyyy-MM-dd"));
    }

    public void refreshPickupTime(){
        if(notificationDate != null)
            timeLabel.setText(Global.getDateString(notificationDate, "h:m a"));
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
            case R.id.date_label:
                onSelectPickupDate();
                break;
            case R.id.time_label:
                onSelectPickupTime();
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Global.showToast(context, "Permission denied");
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng sydney = new LatLng(Global.userInfo.notificationLatitude, Global.userInfo.notificationLongitude);
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Home"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 8.0f));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        marker.setPosition(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Global.hideKeyboard((Activity)context);
        String locationName = (String) adapterView.getItemAtPosition(position);
        LatLng latLng = Global.getLocationFromAddress(context, locationName);
        onMapClick(latLng);
    }
}
