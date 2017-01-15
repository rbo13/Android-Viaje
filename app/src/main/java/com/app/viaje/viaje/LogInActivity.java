package com.app.viaje.viaje;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference dbRef;

    protected TextView signupTextView;
    protected Button loginButton;
    protected EditText emailField;
    protected EditText passwordField;


    GPSTracker gps;

//    @BindView(R.id.signUpText) TextView signup;
//    @BindView(R.id.emailField) EditText emailField;
//    @BindView(R.id.passwordField) EditText passwordField;
//    @BindView(R.id.loginButton) Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        ButterKnife.bind(this);

        dbRef = FirebaseDatabase.getInstance().getReference();

        signupTextView = (TextView) findViewById(R.id.signUpText);
        loginButton = (Button) findViewById(R.id.loginButton);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    /**
      private void getSingleMotoristFromFirebase() {

        String email_address = emailField.getText().toString().trim();

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);


        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Motorist motorist = dataSnapshot.getValue(Motorist.class);

                String email = motorist.getEmail_address();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ERROR: ", "loadPost:onCancelled", databaseError.toException());
            }
        });

    }
    **/

    private void saveMotoristInfo() {

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", emailField.getText().toString());
        editor.putString("password", passwordField.getText().toString());
        editor.apply();

        Toast.makeText(this, "Saved..", Toast.LENGTH_SHORT).show();

    }

    private void saveOnlineUserToFirebase(final double latitude, final double longitude) {

        String string_latitude = Double.toString(latitude);
        String string_longitude = Double.toString(longitude);

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("latitude", string_latitude);
        editor.putString("longitude", string_longitude);
        editor.apply();

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

                    dbRef.child(ViajeConstants.USERS_ONLINE_KEY).push().setValue(onlineUser);
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

                                    Toast.makeText(LogInActivity.this, "Latitude: "+ gps.getLatitude() + " ; " + "Longitude: " + gps.getLongitude(), Toast.LENGTH_LONG).show();

                                    saveOnlineUserToFirebase(gps.getLatitude(), gps.getLongitude());

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
}
