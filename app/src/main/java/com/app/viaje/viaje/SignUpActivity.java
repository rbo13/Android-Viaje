package com.app.viaje.viaje;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import models.Motorist;
import helpers.ViajeConstants;

public class SignUpActivity extends AppCompatActivity {

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected EditText username;
    protected EditText family_name;
    protected EditText given_name;
    protected EditText contact_number;
    protected EditText address;
    protected EditText license_number;
    protected EditText model_year;
    protected EditText plate_number;
    protected EditText vehicle_type;

    //Butterknife
    /**
     * @BindView(R.id.usernameField) EditText username;
     * @BindView(R.id.familyNameField) EditText famiy_name;
     * @BindView(R.id.givenNameField) EditText given_name;
     * @BindView(R.id.contactNoField) EditText contact_number;
     * @BindView(R.id.addressField) EditText address;
     * @BindView(R.id.licenseNoField) EditText license_number;
     */

    protected Button signUpButton;
    private FirebaseAuth mFirebaseAuth;

    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference();

        passwordEditText = (EditText) findViewById(R.id.passwordField);
        emailEditText = (EditText) findViewById(R.id.emailField);
        username = (EditText) findViewById(R.id.usernameField);
        family_name = (EditText) findViewById(R.id.familyNameField);
        given_name = (EditText) findViewById(R.id.givenNameField);
        contact_number = (EditText) findViewById(R.id.contactNoField);
        address = (EditText) findViewById(R.id.addressField);
        license_number = (EditText) findViewById(R.id.licenseNoField);
        model_year = (EditText) findViewById(R.id.modelYearField);
        plate_number = (EditText) findViewById(R.id.plateNoField);
        vehicle_type = (EditText) findViewById(R.id.vehicleTypeField);

    }

    @OnClick(R.id.signupButton)
    void onSignup(){

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty()){

            AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
            builder.setMessage(R.string.signup_error_message)
                    .setTitle(R.string.signup_error_title)
                    .setPositiveButton(android.R.string.ok, null);
            AlertDialog dialog = builder.create();
            dialog.show();

        }else{

            mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {

                                saveUserInfo();

                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
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

    private void saveUserInfo(){

        String user_name = username.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String familyName = family_name.getText().toString().trim();
        String givenName = given_name.getText().toString().trim();
        String contactNumber = contact_number.getText().toString().trim();
        String user_address = address.getText().toString().trim();
        String licenseNumber = license_number.getText().toString().trim();
        String plateNumber = plate_number.getText().toString().trim();
        String modelYear = model_year.getText().toString().trim();
        String vehicleType = vehicle_type.getText().toString().trim();

        Motorist motorist = new Motorist();

        motorist.setUsername(user_name);
        motorist.setFamily_name(familyName);
        motorist.setEmail_address(email);
        motorist.setGiven_name(givenName);
        motorist.setContact_number(contactNumber);
        motorist.setAddress(user_address);
        motorist.setLicense_number(licenseNumber);
        motorist.setVehicle_information_plate_number(plateNumber);
        motorist.setVehicle_information_model_year(modelYear);
        motorist.setVehicle_information_vehicle_type(vehicleType);
        motorist.setProfile_pic("http://hotscope.tv/files/default-profile.png");

        motorist.setType("motorist");

        dbRef.child(ViajeConstants.USERS_KEY).push().setValue(motorist);

        /**
         * Save motorist to Shared Preference.
         */
        saveToSharedPreference(email, familyName, givenName, contactNumber, user_address, plateNumber);

        Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();
    }

    private void saveToSharedPreference(String email, String familyName, String givenName, String contactNumber, String user_address, String plateNumber) {
        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.putString("plate_number", plateNumber);
        editor.putString("given_name", givenName);
        editor.putString("family_name", familyName);
        editor.putString("full_name", givenName + ", " + familyName);
        editor.putString("contact_number", contactNumber);
        editor.putString("address", user_address);
        editor.putString("profile_pic", "http://hotscope.tv/files/default-profile.png");
        editor.putString("type", "motorist");
        editor.apply();


    }

}
