package com.app.viaje.viaje;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Motorist;
import models.Safezone;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference dbRef;

    ArrayList<Safezone> safezones = new ArrayList<>();
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

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

        getSafezones(googleMap);
        currentUserLocation(googleMap);
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
                    getSafezones(googleMap);
                }
            });
        }else{
            gps.showSettingsAlert();
        }


    }

    private void clearPin(){

        mMap.clear();
    }

    private void currentUserLocation(GoogleMap googleMap){

        mMap = googleMap;

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

        LatLng location = new LatLng(latitude, longitude); // User Current Location

        mMap.addMarker(new MarkerOptions().position(location)
                .title("Current Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motorist)));

        mMap.addCircle(drawCircle(location));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));


    }

    private void getSafezones(GoogleMap googleMap) {

        mMap = googleMap;

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.TYPE_FIELD)
                .equalTo(ViajeConstants.SAFEZONE);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.getValue() != null) {

                    Map<String, Safezone> td = new HashMap<String, Safezone>();

                    for(DataSnapshot safezoneSnapshot : dataSnapshot.getChildren()){

                        Safezone safezone = safezoneSnapshot.getValue(Safezone.class);
                        td.put(safezoneSnapshot.getKey(), safezone);
                    }

                    ArrayList<Safezone> values = new ArrayList<>(td.values());
                    List<String> keys = new ArrayList<String>(td.keySet());

                    for(Safezone safezone : values){

                        double latitude = safezone.getAddress().getLat();
                        double longitude = safezone.getAddress().getLng();
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

                        switch (service_information_type){

                            case "repair":

                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.repair)));
                                break;

                            case "gasoline":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.gasoline)));
                                break;

                            case "police_station":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.raw.police)));
                                break;

                            case "hospital":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital)));
                                break;

                            case "towing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.towing)));
                                break;

                            case "vulcanizing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title(shop_name)
                                        .snippet(owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.vulcanizing)));
                                break;
                        }

                    }

                } //end if

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private CircleOptions drawCircle(LatLng location){

        CircleOptions options = new CircleOptions();

        options.center(location);
        options.radius(1600);
        options.fillColor(Color.argb(10, 0, R.color.app_color, 0));
        options.strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.stroke_color));
        options.strokeWidth(5);

        return options;
    }

    private void createMarkerBasedOnLocation() {

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

        LatLng safezone_location = new LatLng(latitude, longitude); // Safezone Current Location

        mMap.addMarker(new MarkerOptions().position(safezone_location)
                .title("Current Location")
                .snippet("Your Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motorist)));
    }

    //Butterknife Components
    @OnClick(R.id.create_pin_id)
    void onCreatePin() {

        Toast.makeText(MapsActivity.this, "Create a Pin", Toast.LENGTH_SHORT).show();
        createMarkerBasedOnLocation();
    }

    @OnClick(R.id.back_to_menu_id)
    void onBackToMenu() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
