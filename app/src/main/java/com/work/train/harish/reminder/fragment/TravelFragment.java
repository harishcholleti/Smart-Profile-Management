package com.work.train.harish.reminder.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.work.train.harish.reminder.MainActivity;
import com.work.train.harish.reminder.R;
import com.work.train.harish.reminder.adapter.GooglePlacesAutocompleteAdapter;
import com.work.train.harish.reminder.task.DrawPathTask;
import com.work.train.harish.reminder.util.Global;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class TravelFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnMapClickListener, AdapterView.OnItemClickListener {

    View rootView;
    Context context;

    GoogleMap mMap;
    Marker myMarker, destinationMarker;

    TextView timeLabel;
    TextView distanceLabel;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();
        rootView = inflater.inflate(R.layout.fragment_travel, container, false);

        bindViews();
        Global.setupKeyboard((Activity)context, rootView);

        AutoCompleteTextView autoCompView = (AutoCompleteTextView) rootView.findViewById(R.id.location_edit);
        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(context, R.layout.autocomplete_list_item));
        autoCompView.setOnItemClickListener(this);
        return rootView;
    }

    void bindViews(){
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        timeLabel = (TextView) rootView.findViewById(R.id.time_label);
        distanceLabel = (TextView) rootView.findViewById(R.id.distance_label);
        rootView.findViewById(R.id.nav_image).setOnClickListener(this);
        ((TextView)rootView.findViewById(R.id.title_label)).setText("Travel Time");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.nav_image:
                ((MainActivity)context).onShowNavMenu(v);
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

        LatLng myLatLng = new LatLng(Global.getLatitude(), Global.getLongitude());
        myMarker = mMap.addMarker(new MarkerOptions().position(myLatLng).title("I am here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 8.0f));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(destinationMarker == null) {
            destinationMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Destination location"));
        }else
            destinationMarker.setPosition(latLng);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(new LatLng(Global.getLatitude(), Global.getLongitude()));
        boundsBuilder.include(latLng);
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
        }catch (Exception e){
            Log.e("setMapLocation", e.getMessage());
        }

        float distance = Global.getDistanceTo(latLng.latitude, latLng.longitude);
        distanceLabel.setText(Global.getMiles(distance));

        new DrawPathTask(context, mMap, latLng.latitude, latLng.longitude,
                timeLabel).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Global.hideKeyboard((Activity)context);
        String locationName = (String) adapterView.getItemAtPosition(position);
        LatLng latLng = Global.getLocationFromAddress(context, locationName);
        onMapClick(latLng);
    }
}
