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

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference dbRef;

    protected TextView signupTextView;
    protected Button loginButton;
    protected EditText emailField;
    protected EditText passwordField;


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

    /**
     * Get data from SharedPreferences
     */
//    private void getMotoristDataFromSharedPreference() {
//
//        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", 0);
//
//        String email = sharedPreferences.getString("email", "");
//        String password = sharedPreferences.getString("password", "");
//    }

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

                                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
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
