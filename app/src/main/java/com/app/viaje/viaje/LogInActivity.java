package com.app.viaje.viaje;

import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import models.Motorist;

public class LogInActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
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

//        ButterKnife.bind(this);
        signupTextView = (TextView) findViewById(R.id.signUpText);
        loginButton = (Button) findViewById(R.id.loginButton);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);

        mFirebaseAuth = FirebaseAuth.getInstance();

        signupTextView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(), "Signup Activity Click", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
        });

    }

//    @OnClick(R.id.signUpText)
//    public void onSignupText(){
//        Intent intent = new Intent(LogInActivity.this, SignUpActivity.class);
//        startActivity(intent);
//    }

//    @OnClick(R.id.loginButton)
//    public void onLogin(View view){
//
//        Toast.makeText(getApplicationContext(), "Click Login", Toast.LENGTH_LONG).show();
//
//        String email = emailField.getText().toString().trim();
//        String password = passwordField.getText().toString().trim();
//
//        if(email.isEmpty() || password.isEmpty()) {
//
//            AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
//            builder.setMessage(R.string.login_error_message)
//                    .setTitle(R.string.login_error_title)
//                    .setPositiveButton(android.R.string.ok, null);
//
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }else{
//
//            mFirebaseAuth.signInWithEmailAndPassword(email, password)
//                    .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
//                        @Override
//                        public void onComplete(@NonNull Task<AuthResult> task) {
//                            if(task.isSuccessful()){
//                                Intent intent = new Intent(LogInActivity.this, MainActivity.class);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                startActivity(intent);
//                            }else{
//                                AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this);
//                                builder.setMessage(task.getException().getMessage())
//                                        .setTitle(R.string.login_error_title)
//                                        .setPositiveButton(android.R.string.ok, null);
//                                AlertDialog dialog = builder.create();
//                                dialog.show();
//                            }
//                        }
//                    });
//        }
//    }
}
