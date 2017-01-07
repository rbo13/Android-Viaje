package com.app.viaje.viaje;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Emergency;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference dbRef;
    private EditText email;

    private LocationManager locationManager;
    private LocationListener locationListener;

    /**
     * Set this variable to true
     * after going to Settings.
     */
    //boolean firstTime = true;

    //GPSTracker Service
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        if(mFirebaseUser == null){
            // Not logged-in.
            loadLoginView();
        }
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.i("onRESUME HERE: ", "onResume");
//
//        if(firstTime){
//            Log.i("FOR FIRST TIME: ", "FIRST TIME HERE");
//            firstTime = false;
//        }else{
//            Log.i("NOT FIRST", "ITS NOT FIRST");
//        }
//
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadLoginView(){
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void sendEmergencyHelp() {

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        String email = sharedPreferences.getString("email", "");

        gps = new GPSTracker(MainActivity.this);

        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            Emergency emergency = new Emergency();

            emergency.setEmail(email);
            emergency.setDescription("50/50");
            emergency.setStatus("pending");
            emergency.setLatitude(latitude);
            emergency.setLongitude(longitude);

            dbRef.child(ViajeConstants.EMERGENCIES_KEY).push().setValue(emergency);
            //Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        }else{
            gps.showSettingsAlert();
        }

    }


    /**
     * Butterknife components.
     */
    @OnClick(R.id.logout_button)
    void onLogout(){
        mFirebaseAuth.signOut();
        loadLoginView();
    }

    @OnClick(R.id.emergency_button_id)
    void onEmergencyClick(){
        Toast.makeText(getApplicationContext(), "Send Emergency Help", Toast.LENGTH_LONG).show();

        sendEmergencyHelp();
    }

    @OnClick(R.id.to_map_button_id)
    void toMapActivity(){

        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.to_profile_button_id)
    void toProfileActivity(){

        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.to_assistance_button_id)
    void toAssistanceActivity(){

        Intent intent = new Intent(getApplicationContext(), AssistanceActivity.class);
        startActivity(intent);
    }
}
