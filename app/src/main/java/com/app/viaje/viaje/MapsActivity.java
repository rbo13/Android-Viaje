package com.app.viaje.viaje;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import helpers.ViajeConstants;
import models.Motorist;
import models.Post;
import models.Safezone;

import static android.R.attr.dialogLayout;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private DatabaseReference dbRef;

    private String textContent;

    TextView post_content = null;
    TextView date_time = null;
    TextView postCommentContent = null;
    TextView postCommentedBy = null;
    EditText commentContentBody = null;

    ArrayList<Safezone> safezones = new ArrayList<>();
    GPSTracker gps;

    @Nullable
    @BindView(R.id.commentContentID) TextView commentContent;
    @Nullable
    @BindView(R.id.commentedByID) TextView commentedBy;
    @Nullable
    @BindView(R.id.postContent) TextView postContent;
    @Nullable
    @BindView(R.id.postedByID) TextView postedBy;
    @Nullable
    @BindView(R.id.timestampID) TextView timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

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

        Toast.makeText(MapsActivity.this, "Map Ready..", Toast.LENGTH_SHORT).show();

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
                .title("Current Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motorist)));

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
                                        .setTag("safezone");

//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;

                            case "gasoline":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.gasoline)))
                                        .setTag("safezone");
//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;

                            case "police_station":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.raw.police)))
                                        .setTag("safezone");
//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;

                            case "hospital":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.hospital)))
                                        .setTag("safezone");
//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;

                            case "towing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.towing)))
                                        .setTag("safezone");
//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;

                            case "vulcanizing":
                                mMap.addMarker(new MarkerOptions().position(safezone_location)
                                        .title("safezone")
                                        .snippet(shop_name + " Owned By: " + owner)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.vulcanizing)))
                                        .setTag("safezone");
//                                mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
                                break;
                        }

                    }

                } //end if

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getPosts() {

        Toast.makeText(MapsActivity.this, "Getting all Post..", Toast.LENGTH_SHORT).show();

        final Query queryRef = dbRef.child(ViajeConstants.POSTS_KEY);

        queryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null) {

                    Map<String, Post> td = new HashMap<String, Post>();

                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){

                        Post post = postSnapshot.getValue(Post.class);
                        td.put(postSnapshot.getKey(), post);
                    }

                    final ArrayList<Post> values = new ArrayList<>(td.values());
                    List<String> keys = new ArrayList<String>(td.keySet());

                    for(final Post post : values) {
                        double lat = post.getLat();
                        double lng = post.getLng();

                        System.out.println(post);

                        final String postContent = post.getText();

                        final String fullname = post.getUser().getGiven_name()+ ", " + post.getUser().getFamily_name();

                        LatLng location = new LatLng(lat, lng); // User Current Location

                        mMap.addMarker(new MarkerOptions().position(location)
                                .title("post")
                                .snippet(postContent)
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher)))
                                .setTag("post");


                        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {

                                if(marker.getTitle().equals("post")){
                                    Toast.makeText(MapsActivity.this, "A post has been clicked", Toast.LENGTH_SHORT).show();

                                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    View dialogLayout = inflater.inflate(R.layout.dialog_layout_comment, null);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

                                    post_content = (TextView) dialogLayout.findViewById(R.id.postContent);
                                    date_time = (TextView)dialogLayout.findViewById(R.id.timestampID);
                                    postCommentContent = (TextView) dialogLayout.findViewById(R.id.commentContentID);
                                    postCommentedBy = (TextView) dialogLayout.findViewById(R.id.commentedByID);
                                    commentContentBody = (EditText) dialogLayout.findViewById(R.id.post);

                                    //Create StringBuilder to have a new line in every TextView
                                    StringBuilder sb = new StringBuilder("");

                                    post_content.append(marker.getSnippet());
                                    post_content.setPaintFlags(post_content.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                                    //Get all comment content.
                                    for (Post.Comment postComment : post.comments.values()) {

                                        sb.append(fullname+ "\n \t \t"+postComment.getText());
                                        sb.append("\n");
                                    }

                                    postCommentedBy.setText(sb.toString());

                                    builder.setPositiveButton("Post Comment", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            Toast.makeText(MapsActivity.this, commentContentBody.getText(), Toast.LENGTH_LONG).show();

                                        }
                                    });

                                    builder.setView(dialogLayout);
                                    builder.create().show();

                                }else {

                                    Toast.makeText(MapsActivity.this, "A safezone has been clicked", Toast.LENGTH_SHORT).show();
                                }

                                return true;
                            }
                        });
                    }




                } //end if
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

//    private void showPostCommentOnMarkerClicked(final ArrayList<Post> posts) {
//
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//
//                Toast.makeText(MapsActivity.this, "Show Alert Dialog to Comment", Toast.LENGTH_SHORT).show();
//
//
//                showPostCommentDialog(posts, marker);
//
//                return true;
//            }
//        });
//    }

//    private void showPostCommentDialog(ArrayList<Post> post, Marker marker) {
//
//        System.out.print(post);
//        String postContent = null;
//        String string_timestamp = null;
//
//        StringBuilder postContentStringBuilder = new StringBuilder("");
//        StringBuilder commentContentStringBuilder = new StringBuilder("");
//
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        View dialogLayout = inflater.inflate(R.layout.dialog_layout_comment, null);
//
//        post_content = (TextView) dialogLayout.findViewById(R.id.postContent);
//        date_time = (TextView)dialogLayout.findViewById(R.id.timestampID);
//        postCommentContent = (TextView) dialogLayout.findViewById(R.id.commentContentID);
//        postCommentedBy = (TextView) dialogLayout.findViewById(R.id.commentedByID);
//
//        for (Post p : post){
//            postContentStringBuilder.append(marker.getSnippet());
////            postContentStringBuilder.append(p.getTimestamp());
//            postContentStringBuilder.append("\n");
////            postContent = p.getText();
////            string_timestamp = p.getTimestamp();
//
//            //Loop all the comments
//            for (Post.Comment postComment : p.comments.values()) {
//                System.out.println(postComment.getText());
//                System.out.println(postComment.getUser().getFamily_name());
//
//                commentContentStringBuilder.append(postComment.getText());
//                commentContentStringBuilder.append(postComment.getUser().getEmail_address());
//                commentContentStringBuilder.append("\n");
//
////                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
////                View dialogLayout = inflater.inflate(R.layout.dialog_layout_comment, null);
//
////                TextView post_content = (TextView) dialogLayout.findViewById(R.id.postContent);
////                TextView date_time = (TextView)dialogLayout.findViewById(R.id.timestampID);
////                TextView postCommentContent = (TextView) dialogLayout.findViewById(R.id.commentContentID);
////                TextView postCommentedBy = (TextView) dialogLayout.findViewById(R.id.commentedByID);
//
//                post_content.append(postContentStringBuilder.toString());
////                date_time.append(postContentStringBuilder.toString());
//                postCommentContent.append(commentContentStringBuilder.toString());
////                postCommentedBy.append(commentContentStringBuilder.toString());
//
//            }
//        }
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
//        builder.setView(dialogLayout);
//
//        builder.create().show();
//
//        //Convert timestamp to date.
//        //TODO: Parse timestamp
//
//
////        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
////        View dialogLayout = inflater.inflate(R.layout.dialog_layout_comment, null);
////
////        TextView post_content = (TextView) dialogLayout.findViewById(R.id.postContent);
////        TextView date_time = (TextView)dialogLayout.findViewById(R.id.timestampID);
////
////        post_content.setText(postContent);
////        date_time.setText(string_timestamp);
//
//
//
////        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
////        builder.setView(dialogLayout);
////
////        builder.create().show();
//
//    }


    private void createMarkerBasedOnLocation() {

        SharedPreferences sharedPreferences = getSharedPreferences("userCoordinates", Context.MODE_PRIVATE);

        double latitude = Double.parseDouble(sharedPreferences.getString("latitude", ""));
        double longitude = Double.parseDouble(sharedPreferences.getString("longitude", ""));

        LatLng safezone_location = new LatLng(latitude, longitude); // Safezone Current Location

        mMap.addMarker(new MarkerOptions().position(safezone_location)
                .title("Current Location")
                .snippet("Your Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.motorist)));
    }

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
                textContent = input.getText().toString();
                Toast.makeText(MapsActivity.this, "Send it bitch", Toast.LENGTH_LONG).show();
                sendThePostToFirebase(textContent);
            }
        });

        builder.create().show();
    }

    private void sendThePostToFirebase(final String text) {

        Toast.makeText(MapsActivity.this, text, Toast.LENGTH_LONG).show();

        //Get timestamp
        Long timestamp_long = System.currentTimeMillis() / 1000;
        final String timestamp = timestamp_long.toString();

        //Shared Preference for Motorist Info.
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
                    post.setTimestamp(timestamp);
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

        Toast.makeText(MapsActivity.this, "Create a Pin", Toast.LENGTH_SHORT).show();
        showMarkerPostDialog();
//        createMarkerBasedOnLocation();
    }

    @OnClick(R.id.back_to_menu_id)
    void onBackToMenu() {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

//    //In-Line Class for InfoWindowAdapter
//    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
//
//        private final View myContentsView;
//
//        MyInfoWindowAdapter() {
//            myContentsView = getLayoutInflater().inflate(R.layout.dialog_layout_comment, null);
//        }
//
//        @Override
//        public View getInfoWindow(Marker marker) {
//            return null;
//        }
//
//        @Override
//        public View getInfoContents(Marker marker) {
//
//            post_content = (TextView) myContentsView.findViewById(R.id.postContent);
//            date_time = (TextView) myContentsView.findViewById(R.id.timestampID);
//            postCommentContent = (TextView) myContentsView.findViewById(R.id.commentContentID);
//            postCommentedBy = (TextView) myContentsView.findViewById(R.id.commentedByID);
//            commentContentBody = (EditText) myContentsView.findViewById(R.id.post);
//            return myContentsView;
//        }
//    }

}
