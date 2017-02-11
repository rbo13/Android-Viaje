package com.app.viaje.viaje;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Emergency;

public class AssistanceActivity extends AppCompatActivity {

    private RelativeLayout relativeLayout;
    private LocationManager locationManager;
    private GPSTracker gps;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistance);

        dbRef = FirebaseDatabase.getInstance().getReference();
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_assistance);

        ButterKnife.bind(this);
    }

    /**
     * BUTTERKNIFE.
     */
    @OnClick(R.id.tire_service_button)
    void onTireService() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Tire Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
        sendEmergencyHelpToSafezone("Flatten tire, need extra tire.", "vulcanizing");

    }

    @OnClick(R.id.towing_service_button)
    void onTowingService() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Towing Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
        sendEmergencyHelpToSafezone("Towing...", "towing");
    }

    @OnClick(R.id.fuel_delivery_service_button)
    void onFuelDelivery() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Fuel Delivery Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
        sendEmergencyHelpToSafezone("Need fuel delivery.", "gasoline");
    }

    @OnClick(R.id.battery_boost_service_button)
    void onBatteryBoost() {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Battery Boost Service Help!", Snackbar.LENGTH_LONG);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
        sendEmergencyHelpToSafezone("Battery shutdown. Need rescue. Help please!", "repair");
    }

    @OnClick(R.id.back_to_menu_button)
    void onBack() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    private void sendEmergencyHelpToSafezone(String emergencyDescription, String safezoneType) {
        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        String email = sharedPreferences.getString("email", "");

        gps = new GPSTracker(AssistanceActivity.this);

        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Emergency emergency = new Emergency();

            emergency.setEmail(email);
            emergency.setDescription(emergencyDescription);
            emergency.setStatus("pending");
            emergency.setLatitude(latitude);
            emergency.setLongitude(longitude);
            emergency.setSafezoneType(safezoneType);

            dbRef.child(ViajeConstants.EMERGENCIES_KEY).push().setValue(emergency);
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        }else{
            gps.showSettingsAlert();
        }
    }

}
