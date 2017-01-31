package com.app.viaje.viaje;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.CloudinaryClient;
import helpers.CloudinaryConfiguration;
import helpers.ViajeConstants;
import models.Motorist;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference dbRef;

    //Caching
    private LruCache<String, String> imageCache;

    private Uri imageCaptureUri;
    InputStream inputStream;

    Cloudinary cloudinary;
    Map config = new HashMap();

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;

    //Butterknife
    @BindView(R.id.profilePic_id) ImageView profilePic;
    @BindView(R.id.edit_profile_full_name) ImageView updateFullName;
    @BindView(R.id.edit_profile_plate_number) ImageView updatePlateNumber;
    @BindView(R.id.edit_profile_email) ImageView updateEmail;
    @BindView(R.id.edit_profile_contact) ImageView updateContact;
    @BindView(R.id.edit_profile_address) ImageView updateAddress;

    //CheckMarks
    @BindView(R.id.update_profile_name_image) ImageView checkMark;
    @BindView(R.id.update_profile_plate_number) ImageView plateNumberCheckMark;
    @BindView(R.id.update_profile_email) ImageView emailCheckMark;
    @BindView(R.id.update_profile_contact) ImageView contactCheckMark;
    @BindView(R.id.update_profile_address) ImageView addressCheckMark;

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
        ButterKnife.bind(this);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        dbRef = FirebaseDatabase.getInstance().getReference();

        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        imageCache = new LruCache<>(cacheSize);

        config.put("cloud_name", ViajeConstants.CLOUD_NAME);
        config.put("api_key", ViajeConstants.API_KEY);
        config.put("api_secret", ViajeConstants.API_SECRET);
        cloudinary = new Cloudinary(config);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(resultCode != RESULT_OK) return;

        Bitmap bitmap = null;
        String path = "";

        if(requestCode == PICK_FROM_GALLERY) {
            imageCaptureUri = data.getData();
            path = getRealPathFromUri(imageCaptureUri);

            if(path == null) path = imageCaptureUri.getPath();

            if(path != null) {
                bitmap = BitmapFactory.decodeFile(path);

                try {
                    inputStream = new FileInputStream(path);
                    updateProfilePictureFieldAtFirebase(inputStream);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }

        }else {
            path = imageCaptureUri.getPath();
            bitmap = BitmapFactory.decodeFile(path);

            try{
                inputStream = new FileInputStream(path);
                updateProfilePictureFieldAtFirebase(inputStream);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        profilePic.setImageBitmap(bitmap);

    }

    public String getRealPathFromUri(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        if(cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
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

        /**
         * Hide the checkmarks
         */
        checkMark.setVisibility(View.GONE);
        plateNumberCheckMark.setVisibility(View.GONE);
        emailCheckMark.setVisibility(View.GONE);
        contactCheckMark.setVisibility(View.GONE);
        addressCheckMark.setVisibility(View.GONE);

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);

        String email_address = sharedPreferences.getString("email", "");
        String full_name = sharedPreferences.getString("full_name", "");
        String plate_number = sharedPreferences.getString("plate_number", "");
        String contact_number = sharedPreferences.getString("contact_number", "");
        String profile_pic = sharedPreferences.getString("profile_pic", "");
        String address = sharedPreferences.getString("address", "");

        //Display to TextView.
        full_name_text.setText(full_name);
        plate_number_text.setText(plate_number);
        email_text.setText(email_address);
        contact_number_text.setText(contact_number);
        address_text.setText(address);

        if(imageCache.get(email_address) != null) {

            Picasso.with(getApplicationContext()).load(profile_pic).into(profilePic);
        }else {

            final Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                    .orderByChild(ViajeConstants.EMAIL_ADDRESS_FIELD)
                    .equalTo(email_address);

            if(mFirebaseUser != null){

                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot userDataSnapshot : dataSnapshot.getChildren()){

                            Motorist motorist = userDataSnapshot.getValue(Motorist.class);

                            String imageUrl = motorist.getProfile_pic();
                            String email = motorist.getEmail_address();
                            Log.d("IMAGE_URL: ", imageUrl);

                            imageCache.put(email, imageUrl);

                            Picasso.with(getApplicationContext()).load(imageUrl).into(profilePic);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ERROR:", "onCancelled", databaseError.toException());
                    }
                });

            }
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


    private void updateProfilePictureFieldAtFirebase(final InputStream is) {

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try{
                    /**
                     * Cloudinary function to get the
                     * newly uploaded image to cloudinary.
                     */
                    Map uploadResult = cloudinary.uploader().upload(is, ObjectUtils.emptyMap());
                    final String url = (String) uploadResult.get("url");
                    Log.d("IMAGE_URL", url);

                    /**
                     * Get Shared Preferences and update the
                     * 'profile_pic' at Firebase.
                     */
                    SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
                    String email_address = sharedPreferences.getString("email", "");

                    imageCache.put(email_address, url);

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

                                HashMap<String, Object> updated_profile_pic = new HashMap<>();
                                updated_profile_pic.put("profile_pic", url);
                                queryRef.getRef().child(key).updateChildren(updated_profile_pic);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("ERROR:", "onCancelled", databaseError.toException());
                            }
                        });

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        new Thread(runnable).start();

    }

    private void selectImageFromGalleryOrCapturePhoto() {

        final String[] items = new String[] { "From Camera", "From Gallery" };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.select_dialog_item, items);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which == 0) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(), "tmp_avatar" + String.valueOf(System.currentTimeMillis())+".jpg");
                    imageCaptureUri = Uri.fromFile(file);

                    try{
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
                        intent.putExtra("return data", true);
                        startActivityForResult(intent, PICK_FROM_CAMERA);
                    }catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    dialog.cancel();
                } else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Complete Action Using"), PICK_FROM_GALLERY);
                }
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
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

    private void editProfilePlateNumber(){

        Toast.makeText(ProfileActivity.this, "Update Profile Plate Number", Toast.LENGTH_SHORT).show();


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

                    HashMap<String, Object> update_profile_plate_number = new HashMap<>();
                    update_profile_plate_number.put("vehicle_information_plate_number", plateNumberUpdate.getText().toString().trim());
                    queryRef.getRef().child(key).updateChildren(update_profile_plate_number);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERROR:", "onCancelled", databaseError.toException());
                }
            });

        }

    }

    private void editProfileContactNumber() {

        Toast.makeText(ProfileActivity.this, "Update Profile Contact Number", Toast.LENGTH_SHORT).show();


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

                    HashMap<String, Object> update_profile_contact_number = new HashMap<>();
                    update_profile_contact_number.put("contact_number", contactNumberUpdate.getText().toString().trim());
                    queryRef.getRef().child(key).updateChildren(update_profile_contact_number);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERROR:", "onCancelled", databaseError.toException());
                }
            });

        }

    }

    private void editProfileAddress() {

        Toast.makeText(ProfileActivity.this, "Update Profile Address", Toast.LENGTH_SHORT).show();


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

                    HashMap<String, Object> update_profile_address = new HashMap<>();
                    update_profile_address.put("address", addressUpdate.getText().toString().trim());
                    queryRef.getRef().child(key).updateChildren(update_profile_address);

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

        selectImageFromGalleryOrCapturePhoto();

    }

    @OnClick(R.id.edit_profile_full_name)
    void onEditProfileFullName() {

        Toast.makeText(ProfileActivity.this, "Update Profile", Toast.LENGTH_LONG).show();

        fullNameUpdate.setVisibility(View.VISIBLE);
        full_name_text.setVisibility(View.INVISIBLE);
        updateFullName.setVisibility(View.INVISIBLE);
        checkMark.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.edit_profile_plate_number)
    void onEditProfilePlateNumber() {

        Toast.makeText(ProfileActivity.this, "Update Profile Plate Number", Toast.LENGTH_SHORT).show();

        plateNumberUpdate.setVisibility(View.VISIBLE);
        plate_number_text.setVisibility(View.INVISIBLE);
        updatePlateNumber.setVisibility(View.INVISIBLE);
        plateNumberCheckMark.setVisibility(View.VISIBLE);


    }

    @OnClick(R.id.edit_profile_email)
    void onEditProfileEmail() {
        Toast.makeText(ProfileActivity.this, "Update Profile Email", Toast.LENGTH_SHORT).show();

        emailUpdate.setVisibility(View.VISIBLE);
        email_text.setVisibility(View.INVISIBLE);
        updateEmail.setVisibility(View.INVISIBLE);
        emailCheckMark.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.edit_profile_contact)
    void onEditProfileContact() {
        Toast.makeText(ProfileActivity.this, "Update Profile Contact", Toast.LENGTH_SHORT).show();

        contactNumberUpdate.setVisibility(View.VISIBLE);
        contact_number_text.setVisibility(View.INVISIBLE);
        updateContact.setVisibility(View.INVISIBLE);
        contactCheckMark.setVisibility(View.VISIBLE);

    }

    @OnClick(R.id.edit_profile_address)
    void onEditProfileAddress() {
        Toast.makeText(ProfileActivity.this, "Update Profile Address", Toast.LENGTH_SHORT).show();

        addressUpdate.setVisibility(View.VISIBLE);
        address_text.setVisibility(View.INVISIBLE);
        updateAddress.setVisibility(View.INVISIBLE);
        addressCheckMark.setVisibility(View.VISIBLE);
    }


    //Update User at firebase
    @OnClick(R.id.update_profile_name_image)
    void editFullName() {

        Toast.makeText(ProfileActivity.this, "Update at Full Name Firebase", Toast.LENGTH_LONG).show();
        fullNameUpdate.setVisibility(View.GONE);
        full_name_text.setVisibility(View.VISIBLE);
        updateFullName.setVisibility(View.VISIBLE);
        checkMark.setVisibility(View.GONE);

        full_name_text.setText(fullNameUpdate.getText().toString());

        SharedPreferences sharedPreferencesUpdatedMotoristInfo = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesUpdatedMotoristInfo.edit();
        editor.putString("full_name", fullNameUpdate.getText().toString());
        editor.apply();

        editProfileFullName();
    }

    @OnClick(R.id.update_profile_plate_number)
    void editPlateNumber() {

        Toast.makeText(ProfileActivity.this, "Update Plate Number at Firebase", Toast.LENGTH_LONG).show();

        plateNumberUpdate.setVisibility(View.GONE);
        plate_number_text.setVisibility(View.VISIBLE);
        updatePlateNumber.setVisibility(View.VISIBLE);
        plateNumberCheckMark.setVisibility(View.GONE);

        plate_number_text.setText(plateNumberUpdate.getText().toString());

        SharedPreferences sharedPreferencesUpdatedMotoristInfo = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesUpdatedMotoristInfo.edit();
        editor.putString("plate_number", plateNumberUpdate.getText().toString());
        editor.apply();

        editProfilePlateNumber();
    }

    @OnClick(R.id.update_profile_contact)
    void editContactNumber() {

        Toast.makeText(ProfileActivity.this, "Update Contact Number at Firebase", Toast.LENGTH_SHORT).show();
        contactNumberUpdate.setVisibility(View.GONE);
        contact_number_text.setVisibility(View.VISIBLE);
        updateContact.setVisibility(View.VISIBLE);
        contactCheckMark.setVisibility(View.GONE);

        SharedPreferences sharedPreferencesUpdatedMotoristInfo = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesUpdatedMotoristInfo.edit();
        editor.putString("contact_number", contactNumberUpdate.getText().toString());
        editor.apply();

        contact_number_text.setText(contactNumberUpdate.getText().toString());

        editProfileContactNumber();
    }

    @OnClick(R.id.update_profile_address)
    void editAddress() {

        Toast.makeText(ProfileActivity.this, "Update Address at Firebase", Toast.LENGTH_SHORT).show();
        addressUpdate.setVisibility(View.GONE);
        address_text.setVisibility(View.VISIBLE);
        updateAddress.setVisibility(View.VISIBLE);
        addressCheckMark.setVisibility(View.GONE);

        SharedPreferences sharedPreferencesUpdatedMotoristInfo = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferencesUpdatedMotoristInfo.edit();
        editor.putString("address", addressUpdate.getText().toString());
        editor.apply();

        address_text.setText(addressUpdate.getText().toString());

        editProfileAddress();
    }

}
