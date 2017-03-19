package com.work.train.harish.reminder.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;

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

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class OfficeFragment extends Fragment
        implements View.OnClickListener,OnMapReadyCallback, GoogleMap.OnMapClickListener, AdapterView.OnItemClickListener {

    View rootView;
    Context context;
    RadioButton generalOption, silentOption, vibrateOption;
    GoogleMap mMap;
    Marker marker;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        bindViews();
        Global.setupKeyboard((Activity) context, rootView);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) rootView.findViewById(R.id.location_edit);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(context, R.layout.autocomplete_list_item));
        autoCompView.setOnItemClickListener(this);

        return rootView;
    }

    void bindViews() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        generalOption = (RadioButton) rootView.findViewById(R.id.general_option);
        silentOption = (RadioButton) rootView.findViewById(R.id.silent_option);
        vibrateOption = (RadioButton) rootView.findViewById(R.id.vibrate_option);

        switch (Global.userInfo.officeRingMode) {
            case AudioManager.RINGER_MODE_SILENT:
                silentOption.setChecked(true);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                vibrateOption.setChecked(true);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                generalOption.setChecked(true);
                break;
        }
        rootView.findViewById(R.id.submit_button).setOnClickListener(this);
        rootView.findViewById(R.id.nav_image).setOnClickListener(this);
        ((TextView) rootView.findViewById(R.id.title_label)).setText("Office Management");
    }

    void onSubmit() {
        UpdateUserTask task = new UpdateUserTask(context, true);
        if (marker != null) {
            task.officeLatitude = marker.getPosition().latitude;
            task.officeLongitude = marker.getPosition().longitude;
        }
        task.officeRingMode = getRingMode();
        task.execute();
    }

    int getRingMode() {
        if (silentOption.isChecked())
            return AudioManager.RINGER_MODE_SILENT;
        else if (vibrateOption.isChecked())
            return AudioManager.RINGER_MODE_VIBRATE;
        else if (generalOption.isChecked())
            return AudioManager.RINGER_MODE_NORMAL;
        return -1;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_image:
                ((MainActivity) context).onShowNavMenu(v);
                break;
            case R.id.submit_button:
                onSubmit();
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
        LatLng sydney = new LatLng(Global.userInfo.officeLatitude, Global.userInfo.officeLongitude);
        if(Global.userInfo.officeLatitude == 0 && Global.userInfo.officeLongitude == 0)
            sydney = new LatLng(Global.getLatitude(), Global.getLongitude());
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Office"));
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
