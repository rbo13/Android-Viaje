package com.app.viaje.viaje;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Advertisement;
import models.Motorist;
import models.Post;
import models.Safezone;

import static android.R.attr.dialogLayout;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference dbRef;
    private RelativeLayout relativeLayout;

    private String textContent;

    TextView post_content = null;
    TextView date_time = null;
    TextView postCommentContent = null;
    TextView postCommentedBy = null;
    EditText commentContentBody = null;

    //Variable to get the image url.
    private ImageView ads_image;
    private TextView adText;
    private TextView adTitle;
    private TextView shop_name;

    ArrayList<Safezone> safezones = new ArrayList<>();
    GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        relativeLayout = (RelativeLayout) findViewById(R.id.mapsRelativeLayout);

        mapFragment.getMapAsync(this);

        dbRef = FirebaseDatabase.getInstance().getReference();


    }

    @Override
    protected void onResume() {
        super.onResume();

        initMap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        clearPin();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        gps = new GPSTracker(MapsActivity.this);

        getSafezones(googleMap);
        currentUserLocation(googleMap);
        getPosts();

    }

    private void initMap(){

        gps = new GPSTracker(MapsActivity.this);

        if(gps.canGetLocation()){
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {

                    currentUserLocation(googleMap);
                    getSafezones(googleMap);
                    getPosts();
                }
            });
        }else{
            gps.showSettingsAlert();
        }


    }

    private void clearPin(){

        mMap.clear();
    }

    private void currentUserLocation(GoogleMap googleMap){

        mMap = googleMap;

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

        LatLng location = new LatLng(latitude, longitude); // User Current Location

        mMap.addMarker(new MarkerOptions().position(location)
                .title("motorist")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motorist)))
                .setTag("motorist");

        mMap.addCircle(drawCircle(location));

        mMap.moveCamera(CameraUpdateFactory.newLatLng(location));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14));


    }

    private void getSafezones(GoogleMap googleMap) {

        mMap = googleMap;

        Query queryRef = dbRef.child(ViajeConstants.USERS_KEY)
                .orderByChild(ViajeConstants.TYPE_FIELD)
                .equalTo(ViajeConstants.SAFEZONE);

        queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot != null && dataSnapshot.getValue() != null) {

                    Map<String, Safezone> td = new HashMap<String, Safezone>();

                    for(DataSnapshot safezoneSnapshot : dataSnapshot.getChildren()){

                        Safezone safezone = safezoneSnapshot.getValue(Safezone.class);
                        td.put(safezoneSnapshot.getKey(), safezone);
                    }

                    ArrayList<Safezone> values = new ArrayList<>(td.values());
                    List<String> keys = new ArrayList<String>(td.keySet());

                    for(Safezone safezone : values){

                        double latitude = safezone.getAddress().getLat();
                        double longitude = safezone.getAddress().getLng();
                        String address = safezone.getAddress().getAddress();
                        String contact_number = safezone.getContact_number();
                        String email_address = safezone.getEmail_address();
                        String owner = safezone.getOwner();
                        String service_information_type = safezone.getService_information_type();
                        String shop_name = safezone.getShop_name();
                        String type = safezone.getType();
                        String username = safezone.getUsername();

                        /**
                         * Create marker in maps
                         * with type of safezone.
                         */
                        LatLng safezone_location = new LatLng(latitude, longitude); // Safezone Current Location

                        switch (service_information_type){

                            case "repair":

                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.repair)))
                                        .setTag(safezone);
                                break;

                            case "gasoline":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.gasoline)))
                                        .setTag(safezone);
                                break;

                            case "police_station":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.raw.police)))
                                        .setTag(safezone);
                                break;

                            case "hospital":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital)))
                                        .setTag(safezone);
                                break;

                            case "towing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.towing)))
                                        .setTag(safezone);
                                break;

                            case "vulcanizing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.vulcanizing)))
                                        .setTag(safezone);
                                break;
                        }

                    }

                }//end if

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void getPosts() {

        final Query queryRef = dbRef.child(ViajeConstants.POSTS_KEY);

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    final Map<String, Post> td = new HashMap<String, Post>();
                    final ArrayList<Post> postValues = new ArrayList<>(td.values());
                    List<String> keys = new ArrayList<String>(td.keySet());

                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {

                        Post post = postSnapshot.getValue(Post.class);
                        td.put(postSnapshot.getKey(), post);

                        double lat = post.getLat();
                        double lng = post.getLng();
                        String postContent = post.getText();
                        final String username = post.getUser().getUsername();


                        LatLng location = new LatLng(lat, lng); //User current location

                        mMap.addMarker(new MarkerOptions().position(location)
                                .title("post")
                                .snippet(postSnapshot.getKey())
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)))
                                .setTag(post);

                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(final Marker marker) {

                                if(marker.getTitle().contains("post")) {

                                    /**
                                     * Layout Inflater for the
                                     * alert dialog when a marker
                                     * has been click
                                     */
                                    LayoutInflater inflater = getLayoutInflater();
                                    View dialogLayout = inflater.inflate(R.layout.dialog_layout_comment, null, false);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                                    //Create StringBuilder to have a new line in every TextView
                                    StringBuilder sb = new StringBuilder("");
                                    post_content = (TextView) dialogLayout.findViewById(R.id.postContent);
                                    date_time = (TextView)dialogLayout.findViewById(R.id.timestampID);
                                    postCommentContent = (TextView) dialogLayout.findViewById(R.id.commentContentID);
                                    postCommentedBy = (TextView) dialogLayout.findViewById(R.id.commentedByID);
                                    commentContentBody = (EditText) dialogLayout.findViewById(R.id.post);

                                    Post p = (Post) marker.getTag();
                                    System.out.println(p);

                                    post_content.append(p.getText());
                                    post_content.setPaintFlags(post_content.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                                    /**
                                     * Loop every entry of p.comments
                                     * inside the marker.getTag().
                                     */
                                    Iterator entries = p.comments.entrySet().iterator();
                                    while (entries.hasNext()) {
                                        Map.Entry myEntry = (Map.Entry) entries.next();
                                        Object key = myEntry.getKey();
                                        Post.Comment value = (Post.Comment) myEntry.getValue();

                                        sb.append(username+"\n \t \t"+value.getText());
                                        sb.append("\n");

                                        postCommentedBy.setText(sb.toString());

                                    }
                                    //Button that submits a comment to a post.
                                    builder.setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String comment = commentContentBody.getText().toString().trim();

                                            /**
                                             * Function declaration that
                                             * submits a comment to the 'post'
                                             * at firebase database.
                                             */
                                            if(comment.isEmpty()){
                                                Snackbar snackbar = Snackbar.make(relativeLayout, "Comment must not be empty..", Snackbar.LENGTH_LONG);
                                                View sbView = snackbar.getView();
                                                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                                textView.setTextColor(Color.RED);
                                                snackbar.show();
                                            }else {
                                                postComment(comment, marker.getSnippet());
                                            }

                                        }
                                    });

                                    builder.setView(dialogLayout);
                                    builder.create().show();

                                }else if (marker.getTitle().contains("safezone")){

                                    /**
                                     * If the marker type is safezone
                                     * then get the advertisement instead
                                     * of posts.
                                     */
                                    final Safezone sz = (Safezone) marker.getTag();

                                    /**
                                     * Layout Inflater for the
                                     * alert dialog when a marker
                                     * has been click
                                     */
                                    LayoutInflater inflater = getLayoutInflater();
                                    View adsDialog = inflater.inflate(R.layout.show_ads_info_window, null, false);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                                    ads_image = (ImageView) adsDialog.findViewById(R.id.ads);
                                    adText = (TextView) adsDialog.findViewById(R.id.adText);
                                    adTitle = (TextView) adsDialog.findViewById(R.id.adTitle);
                                    shop_name = (TextView) adsDialog.findViewById(R.id.shop_name);

                                    String service_type = sz.getService_information_type();
                                    String username = sz.getUsername();
                                    System.out.print(username);

                                    if(service_type.contains("repair")){

                                        /**
                                         * @description Get ads of every
                                         * safezone
                                         */
                                        getAds(sz);

                                    }else if(service_type.contains("hospital")) {

                                        /**
                                         * @description Get ads of every
                                         * safezone
                                         */
                                        getAds(sz);

                                    }else if(service_type.contains("towing")) {

                                        /**
                                         * @description Get ads of every
                                         * safezone
                                         */
                                        getAds(sz);

                                    }else if(service_type.contains("vulcanizing")) {

                                        /**
                                         * @description Get ads of every
                                         * safezone
                                         */
                                        getAds(sz);

                                    }else if(service_type.contains("gasoline")) {

                                        /**
                                         * @description Get ads of every
                                         * safezone
                                         */
                                        getAds(sz);

                                    }

                                    builder.setView(adsDialog);
                                    builder.create().show();

                                }else {
                                    Snackbar snackbar = Snackbar.make(relativeLayout, "Current Location..", Snackbar.LENGTH_LONG);
                                    View sbView = snackbar.getView();
                                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                                    textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color));
                                    snackbar.show();
                                }

                                return true;
                            }
                        });

                    }

                }//end if
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getAds(final Safezone sz) {
        Query adsQuery = dbRef.child(ViajeConstants.ADS_KEY)
                .orderByChild("user/username")
                .equalTo(sz.getUsername());

        adsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Map<String, Advertisement> td = new HashMap<String, Advertisement>();

                for (DataSnapshot advertisementSnapshot : dataSnapshot.getChildren()) {
                    Advertisement advertisement = advertisementSnapshot.getValue(Advertisement.class);
                    advertisement.setKey(advertisementSnapshot.getKey());
                    td.put(advertisementSnapshot.getKey(), advertisement);
                }

                ArrayList<Advertisement> values = new ArrayList<>(td.values());
                List<String> keys = new ArrayList<String>(td.keySet());

                for (Advertisement advertisement : values) {

                    System.out.print(advertisement);

                    String imageUrl = advertisement.getImg();
                    String text = advertisement.getText();
                    String title = advertisement.getTitle();
                    Long timestamp = advertisement.getTimestamp();

                    adText.setText(text);
                    adTitle.setText(title);
                    shop_name.setText(sz.getShop_name());

                    Picasso.with(getApplicationContext()).load(imageUrl).into(ads_image);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void postComment(final String commentText, Object key) {

        final Motorist user = new Motorist();

        //Get timestamp
        final Long timestamp_long = System.currentTimeMillis() / 1000;
        final String timestamp = timestamp_long.toString();

        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);

        //Store the data from SharePreference to Motorist object.
        user.setAddress(sharedPreferences.getString("address", ""));
        user.setContact_number(sharedPreferences.getString("contact_number", ""));
        user.setEmail_address(sharedPreferences.getString("email", ""));
        user.setFamily_name(sharedPreferences.getString("family_name", ""));
        user.setGiven_name(sharedPreferences.getString("given_name", ""));
        user.setLicense_number(sharedPreferences.getString("license_number", ""));
        user.setType(sharedPreferences.getString("type", ""));
        user.setUsername(sharedPreferences.getString("username", ""));
        user.setVehicle_information_model_year(sharedPreferences.getString("vehicle_information_model_year", ""));
        user.setVehicle_information_vehicle_type(sharedPreferences.getString("vehicle_information_model_type", ""));
        user.setVehicle_information_plate_number(sharedPreferences.getString("vehicle_information_plate_number", ""));

        Post.Comment postComment = new Post.Comment();

        postComment.setText(commentText);
        postComment.setTimestamp(timestamp_long);
        postComment.setUser(user);

        dbRef.child(ViajeConstants.POSTS_KEY+"/"+key+"/comments").push().setValue(postComment);

    }

    /**
     * @description ::
     * Shows the AlertDialog that creates
     * a new post
     */
    private void showMarkerPostDialog() {

        final EditText input = new EditText(MapsActivity.this);
        //Get the text from EditText.
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        //Create AlertDialog Builder.
        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        builder.setTitle("Post");
        builder.setMessage("What about the post?");
        builder.setView(input);
        builder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textContent = input.getText().toString().trim();

                if (textContent.isEmpty()) {
                    Snackbar snackbar = Snackbar.make(relativeLayout, "Post must not be empty..", Snackbar.LENGTH_LONG);
                    View sbView = snackbar.getView();
                    TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                    textView.setTextColor(Color.RED);
                    snackbar.show();
                }else {
                    sendThePostToFirebase(textContent);
                }
            }
        });

        builder.create().show();
    }

    /**
     * @description function that creates
     * a new post record to firebase.
     * @param text
     */
    private void sendThePostToFirebase(final String text) {

        //Get timestamp
        final Long timestamp_long = System.currentTimeMillis() / 1000;
        final String timestamp = timestamp_long.toString();

        //Shared Preference of Motorist.
        SharedPreferences sharedPreferences = getSharedPreferences("motoristInfo", Context.MODE_PRIVATE);
        String email_address = sharedPreferences.getString("email", "");

        //Shared Preference for User Coordinates.
        SharedPreferences userCoordinates = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);
        final double latitude = Double.parseDouble(userCoordinates.getString("latitude", ""));
        final double longitude = Double.parseDouble(userCoordinates.getString("longitude", ""));

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
                     * Create Post instance
                     * to save to "posts"
                     * at firebase.
                     */
                    Post post = new Post();

                    post.setLat(latitude);
                    post.setLng(longitude);
                    post.setText(text);
                    post.setTimestamp(timestamp_long);
                    post.setUser(motorist);

                    dbRef.child(ViajeConstants.POSTS_KEY).push().setValue(post);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.w("ERROR: ", "loadPost:onCancelled", databaseError.toException());
            }
        });

    }

    /**
     * @description :: Creates the circle in the map
     * @param location
     * @return
     */
    private CircleOptions drawCircle(LatLng location){

        CircleOptions options = new CircleOptions();

        options.center(location);
        options.radius(1600);
        options.fillColor(Color.argb(10, 0, R.color.app_color, 0));
        options.strokeColor(ContextCompat.getColor(getApplicationContext(), R.color.stroke_color));
        options.strokeWidth(5);

        return options;
    }

    //Butterknife Components
    @OnClick(R.id.create_pin_id)
    void onCreatePin() {

        showMarkerPostDialog();
    }

    @OnClick(R.id.back_to_menu_id)
    void onBackToMenu() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

}
