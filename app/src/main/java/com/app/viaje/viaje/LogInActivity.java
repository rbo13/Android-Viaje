package com.app.viaje.viaje;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import helpers.ViajeConstants;
import models.Motorist;
import models.OnlineUser;

public class LogInActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
            GoogleApiClient.OnConnectionFailedListener, LocationListener{


    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference dbRef;

    protected TextView signupTextView;
    protected Button loginButton;
    protected EditText emailField;
    protected EditText passwordField;

    private LruCache<String, String> imageCache;

    Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    String lat, lon;


    GPSTracker gps;

//    @BindView(R.id.signUpText) TextView signup;
//    @BindView(R.id.emailField) EditText emailField;
//    @BindView(R.id.passwordField) EditText passwordField;
//    @BindView(R.id.loginButton) Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        buildGoogleApiClient();

        dbRef = FirebaseDatabase.getInstance().getReference();

        signupTextView = (TextView) findViewById(R.id.signUpText);
        loginButton = (Button) findViewById(R.id.loginButton);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        mFirebaseAuth = FirebaseAuth.getInstance();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        imageCache = new LruCache<>(cacheSize);

        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
    }

    private void saveMotoristInfo() {

        String email_address = emailField.getText().toString().trim();

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);


        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot motoristSnapshot : dataSnapshot.getChildren()){

                    Motorist motorist = motoristSnapshot.getValue(Motorist.class);

                    SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("email", motorist.getEmail_address());
                    editor.putString("username", motorist.getUsername());
                    editor.putString("given_name", motorist.getGiven_name());
                    editor.putString("family_name", motorist.getFamily_name());
                    editor.putString("given_name", motorist.getGiven_name());
                    editor.putString("license_number", motorist.getLicense_number());
                    editor.putString("full_name", motorist.getGiven_name()+", "+motorist.getFamily_name());
                    editor.putString("profile_pic", motorist.getProfile_pic());
                    editor.putString("contact_number", motorist.getContact_number());
                    editor.putString("vehicle_information_model_year", motorist.getVehicle_information_model_year());
                    editor.putString("vehicle_information_model_type", motorist.getVehicle_information_vehicle_type());
                    editor.putString("vehicle_information_plate_number", motorist.getVehicle_information_plate_number());
                    editor.putString("address", motorist.getAddress());
                    editor.putString("type", motorist.getType());
                    editor.apply();

                    imageCache.put(motorist.getEmail_address(), motorist.getProfile_pic());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ERROR: ", "loadPost:onCancelled", databaseError.toException());
            }
        });

        Toast.makeText(this, "Saved..", Toast.LENGTH_SHORT).show();

    }

    private void saveOnlineUserToFirebase(final double latitude, final double longitude) {

        //Get timestamp
        Long timestamp_long = System.currentTimeMillis() / 1000;
        final String timestamp = timestamp_long.toString();

        //Get the login user from firebase.
        String email_address = emailField.getText().toString().trim();

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot motoristSnapshot : dataSnapshot.getChildren()){

                    //Get single motorist and pass it to online user.
                    Motorist motorist = motoristSnapshot.getValue(Motorist.class);

                    /**
                     * Create onlineUser instance
                     * to save to "onlineusers".
                     */
                    OnlineUser onlineUser = new OnlineUser();

                    onlineUser.setLatitude(latitude);
                    onlineUser.setLongitude(longitude);
                    onlineUser.setTimestamp(timestamp);
                    onlineUser.setMotorist(motorist);

                    dbRef.child(ViajeConstants.ONLINE_USERS_KEY).push().setValue(onlineUser);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ERROR: ", "loadPost:onCancelled", databaseError.toException());
            }
        });

    }


    /**
     * Butterknife components
     */
    @OnClick(R.id.signUpText)
    void onSignupText(){
        Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.loginButton)
    void onLogin(){

        gps = new GPSTracker(LogInActivity.this);

        Toast.makeText(LogInActivity.this, "Login Button Clicked", Toast.LENGTH_SHORT).show();

        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();


        if(email.isEmpty() || password.isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
            builder.setMessage(R.string.login_error_message)
                    .setTitle(R.string.login_error_title)
                    .setPositiveButton(android.R.string.ok, null);

            AlertDialog dialog = builder.create();
            dialog.show();
        }else{

            mFirebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                /**
                                 * @description:: Save motorist to
                                 * shared preferences.
                                 */
                                saveMotoristInfo();

                                /**
                                 * Check if the user enabled
                                 * the location setting. If enabled,
                                 * save the location to SharedPref
                                 * and send request to firebase and add it
                                 * to "onlineusers" collection,
                                 * otherwise open Settings Activity.
                                 */
                                if(gps.canGetLocation()){

                                    SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

                                    double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
                                    double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

                                    //Save user to firebase and insert to "online_users"
                                    saveOnlineUserToFirebase(latitude, longitude);

                                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);

                                }else{
                                    gps.showSettingsAlert();
                                }


                            }else{
                                AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
                                builder.setMessage(task.getException().getMessage())
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(100); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());

            SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("latitude", lat);
            editor.putString("longitude", lon);
            editor.apply();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(Location location) {

        lat = String.valueOf(location.getLatitude());
        lon = String.valueOf(location.getLongitude());

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", lat);
        editor.putString("longitude", lon);
        editor.apply();

    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
}
