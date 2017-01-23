package com.app.viaje.viaje;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Motorist;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference dbRef;

    private static final int SELECT_PICTURE = 1;

    //Butterknife
    @BindView(R.id.profilePic_id) ImageView profilePic;
    @BindView(R.id.edit_profile_full_name) ImageView updateFullName;
    @BindView(R.id.update_profile_name_image) ImageView checkMark;


    @BindView(R.id.full_name_text_id) TextView full_name_text;
    @BindView(R.id.plate_number_text_id) TextView plate_number_text;
    @BindView(R.id.email_text_id) TextView email_text;
    @BindView(R.id.contact_number_text_id) TextView contact_number_text;
    @BindView(R.id.address_text_id) TextView address_text;

    @BindView(R.id.update_full_name_edit) EditText fullNameUpdate;
    @BindView(R.id.update_plate_number_edit) EditText plateNumberUpdate;
    @BindView(R.id.update_email_edit) EditText emailUpdate;
    @BindView(R.id.update_contact_edit) EditText contactNumberUpdate;
    @BindView(R.id.update_address_edit) EditText addressUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();


        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayUserInformation();

    }

    @Override
    protected void onResume() {
        super.onResume();

//        displayUserInformation();
    }

    private void displayUserInformation(){

        /**
         * Hide the EditText onStart.
         */
        fullNameUpdate.setVisibility(View.GONE);
        plateNumberUpdate.setVisibility(View.GONE);
        emailUpdate.setVisibility(View.GONE);
        contactNumberUpdate.setVisibility(View.GONE);
        addressUpdate.setVisibility(View.GONE);
        checkMark.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);

        String email_address = sharedPreferences.getString("email", "");
        String full_name = sharedPreferences.getString("full_name", "");
        String plate_number = sharedPreferences.getString("plate_number", "");
        String contact_number = sharedPreferences.getString("contact_number", "");
        String address = sharedPreferences.getString("address", "");

        //Display to TextView.
        full_name_text.setText(full_name);
        plate_number_text.setText(plate_number);
        email_text.setText(email_address);
        contact_number_text.setText(contact_number);
        address_text.setText(address);

        final Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);

        if(mFirebaseUser != null){

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    for (DataSnapshot userDataSnapshot : dataSnapshot.getChildren()){

                        Motorist motorist = userDataSnapshot.getValue(Motorist.class);

                        try {
                            Bitmap image = decodeFromFirebase64(motorist.getProfile_pic());
                            profilePic.setImageBitmap(image);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERROR:", "onCancelled", databaseError.toException());
                }
            });

        }
    }

    private void deleteUserRecordOnFirebase() {

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        String email_address = sharedPreferences.getString("email", "");

        dbRef = FirebaseDatabase.getInstance().getReference()
                .child(ViajeConstants.ONLINE_USERS_KEY);

        dbRef.orderByChild(ViajeConstants.MOTORIST_EMAIL_ADDRESS_KEY)
                .equalTo(email_address)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();
                        String key = nodeDataSnapshot.getKey();

                        dbRef.child(key).removeValue(new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                if(databaseError == null){
                                    mFirebaseAuth.signOut();
                                    loadLoginView();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ERROR:", "onCancelled", databaseError.toException());
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == SELECT_PICTURE)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                if (imageReturnedIntent != null)
                {
                    try
                    {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), imageReturnedIntent.getData());
                        profilePic.setImageBitmap(bitmap);
                        updateProfilePicture(bitmap);
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED)
            {
                Toast.makeText(getApplication(), "Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateProfilePicture(final Bitmap bitmap) {

        Toast.makeText(ProfileActivity.this, "Update Profile Picture", Toast.LENGTH_SHORT).show();

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        String email_address = sharedPreferences.getString("email", "");

        final Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);

        if(mFirebaseUser != null){

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    String imageEncoded = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                    String key = "";

                    for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()){
                        key = nodeDataSnapshot.getKey();
                    }

                    HashMap<String, Object> updated_profile_pic = new HashMap<>();
                    updated_profile_pic.put("profile_pic", imageEncoded);

                    try {
                        Bitmap image = decodeFromFirebase64(imageEncoded);
                        profilePic.setImageBitmap(image);

                        queryRef.getRef().child(key).updateChildren(updated_profile_pic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERROR:", "onCancelled", databaseError.toException());
                }
            });

        }

    }


    private Bitmap decodeFromFirebase64(String image) throws IOException{

        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    private void onSelectImage() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , SELECT_PICTURE);//one can be replaced with any action code

    }


    private void editProfileFullName() {
        Toast.makeText(ProfileActivity.this, "Update Profile Full Name", Toast.LENGTH_SHORT).show();


        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        String email_address = sharedPreferences.getString("email", "");

        final Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                .equalTo(email_address);

        if(mFirebaseUser != null){

            queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {


                    String key = "";

                    for (DataSnapshot nodeDataSnapshot : dataSnapshot.getChildren()){
                        key = nodeDataSnapshot.getKey();
                    }

                    HashMap<String, Object> updated_profile_full_name = new HashMap<>();
                    updated_profile_full_name.put("full_name", fullNameUpdate.getText().toString().trim());
                    queryRef.getRef().child(key).updateChildren(updated_profile_full_name);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERROR:", "onCancelled", databaseError.toException());
                }
            });

        }
    }

    private void loadLoginView(){
        Intent intent = new Intent(this, LogInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Butterknife components
    @OnClick(R.id.logout_button)
    void onLogout(){
        deleteUserRecordOnFirebase();
    }

    @OnClick(R.id.edit_profile_pic)
    void onEditProfilePic() {

        onSelectImage();

    }

    @OnClick(R.id.edit_profile_full_name)
    void onEditProfileFullName() {

        Toast.makeText(ProfileActivity.this, "Update Profile", Toast.LENGTH_LONG).show();

        fullNameUpdate.setVisibility(View.VISIBLE);
        full_name_text.setVisibility(View.INVISIBLE);
        updateFullName.setVisibility(View.INVISIBLE);
        checkMark.setVisibility(View.VISIBLE);

    }

    //Update User at firebase
    @OnClick(R.id.update_profile_name_image)
    void editFullName() {

        Toast.makeText(ProfileActivity.this, "Update at Firebase", Toast.LENGTH_LONG).show();
        fullNameUpdate.setVisibility(View.GONE);
        full_name_text.setVisibility(View.VISIBLE);
        updateFullName.setVisibility(View.VISIBLE);
        checkMark.setVisibility(View.GONE);

        SharedPreferences sharedPreferencesUpdatedMotoristInfo = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesUpdatedMotoristInfo.edit();
        editor.putString("full_name", fullNameUpdate.getText().toString().trim());
        editor.apply();

        editProfileFullName();
    }

}
