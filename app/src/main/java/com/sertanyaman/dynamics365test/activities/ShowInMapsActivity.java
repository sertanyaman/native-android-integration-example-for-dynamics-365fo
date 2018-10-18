package com.sertanyaman.dynamics365test.activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sertanyaman.dynamics365test.R;
import com.sertanyaman.dynamics365test.models.Task;
import com.sertanyaman.dynamics365test.database.TasksDBHelper;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class ShowInMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Task callingTask;
    private String address;
    private String custName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_in_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        callingTask = intent.getParcelableExtra("TASK");
        address = callingTask.getAddress();
        custName = callingTask.getCustName();

        TasksDBHelper dbHelper = TasksDBHelper.getInstance(this);
        dbHelper.markTaskAsRead(callingTask);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses = null;

        if(address!=null && !address.equals("")) {
            try {
                addresses = geocoder.getFromLocationName(address, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            LatLng coord = null;

            if(addresses!=null && !addresses.isEmpty())
            {
                if(!addresses.get(0).getAddressLine(0).equals(""))
                {
                    Address address = addresses.get(0);
                    coord = new LatLng(address.getLatitude(), address.getLongitude());
                }
            }

            if(coord!=null) {
                mMap.addMarker(new MarkerOptions().position(coord).title(custName));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(coord, 10));
            }
        }
    }
}
