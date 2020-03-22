package com.example.weatherapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();

        city = intent.getStringExtra("CITY");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        List<Address> addresses = null;


        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            while(addresses == null) {
                addresses = geocoder.getFromLocationName(this.city, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Address address = addresses.get(0);

        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        mMap.addMarker(new MarkerOptions().position(latLng).title(this.city));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        CameraUpdate loc = CameraUpdateFactory.newLatLngZoom(latLng, 5);
        mMap.animateCamera(loc);
    }

}
