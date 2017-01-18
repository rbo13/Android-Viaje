package com.app.viaje.viaje;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import helpers.ViajeConstants;
import models.Safezone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference dbRef;

    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onResume() {
        super.onResume();

        initMap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearPin();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        gps = new GPSTracker(MapsActivity.this);

        Toast.makeText(MapsActivity.this, "Map Ready!", Toast.LENGTH_SHORT).show();
        getSafezones(googleMap);

        /**
         * @description :: Get user location
         * base on GPS Location and apply it
         * to the map.
         */
//        setUpMapPin(googleMap);
    }

    private void currentUserLocation(GoogleMap googleMap){

        mMap = googleMap;

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

        LatLng location = new LatLng(latitude, longitude); // User Current Location
        mMap.addMarker(new MarkerOptions().position(location).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
    }

    private void initMap(){

        gps = new GPSTracker(MapsActivity.this);

        if(gps.canGetLocation()){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    currentUserLocation(googleMap);
                }
            });
        }else{
            gps.showSettingsAlert();
        }


    }

    private void clearPin(){

        mMap.clear();
    }

    private void getSafezones(GoogleMap googleMap) {

        mMap = googleMap;

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.TYPE_FIELD)
                .equalTo(ViajeConstants.SAFEZONE);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot safezoneSnapshot : dataSnapshot.getChildren()){

                    Safezone safezone = safezoneSnapshot.getValue(Safezone.class);

                    double latitude = safezone.getAddress().getLatitude();
                    double longitude = safezone.getAddress().getLongitude();
                    String address = safezone.getAddress().getAddress();
                    String contact_number = safezone.getContact_number();
                    String email_address = safezone.getEmail_address();
                    String owner = safezone.getOwner();
                    String service_information_type = safezone.getService_information_type();
                    String shop_name = safezone.getShop_name();
                    String type = safezone.getType();
                    String username = safezone.getUsername();

                    /**
                     * Create marker in maps
                     * with type of safezone.
                     */
                    LatLng safezone_location = new LatLng(latitude, longitude); // Safezone Current Location
                    mMap.addMarker(new MarkerOptions().position(safezone_location).title(shop_name));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLng(safezone_location));
//                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(safezone_location, 16));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
