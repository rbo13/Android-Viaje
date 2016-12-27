package com.app.viaje.viaje;

import android.content.Intent;
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
import models.Motorist;

public class SignUpActivity extends AppCompatActivity {

    protected EditText passwordEditText;
    protected EditText emailEditText;
    protected EditText username;
    protected EditText family_name;
    protected EditText given_name;
    protected EditText contact_number;
    protected EditText address;
    protected EditText license_number;

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
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

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


        signUpButton = (Button) findViewById(R.id.signupButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });



    }

    private void saveUserInfo(){

        Toast.makeText(getApplicationContext(), "Add New User", Toast.LENGTH_LONG).show();

        String user_name = username.getText().toString().trim();
        String familyName = family_name.getText().toString().trim();
        String givenName = given_name.getText().toString().trim();
        String contactNumber = contact_number.getText().toString().trim();
        String user_address = address.getText().toString().trim();
        String licenseNumber = license_number.getText().toString().trim();

        Motorist motorist = new Motorist();

        motorist.setUsername(user_name);
        motorist.setFamily_name(familyName);
        motorist.setGiven_name(givenName);
        motorist.setContact_number(contactNumber);
        motorist.setAddress(user_address);
        motorist.setLicense_number(licenseNumber);

        dbRef.child("users").setValue(motorist);

        Toast.makeText(this, "Information Saved...", Toast.LENGTH_LONG).show();
    }

}
