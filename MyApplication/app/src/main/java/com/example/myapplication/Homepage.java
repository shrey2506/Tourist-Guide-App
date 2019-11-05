package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.example.myapplication.services.sendNotification;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

public class Homepage extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,com.google.android.gms.location.LocationListener {
    private FirebaseUser user;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Toolbar mToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference ref;
    private Button RequestButton;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int Request_Code = 101;
    private LatLng pickUpLocation;
    private FloatingActionButton Fab;
    private Marker pickUpMarker;
    private LinearLayout mGuideInfo;
    private TextView mGuideName;
    private TextView mGuidePhoneNo;
    private LinearLayout bottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;

    private TextView mTourDuration;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light,R.color.quantum_googred,R.color.quantum_yellow,R.color.quantum_googgreen};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);
        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Notification Key").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mToolbar =findViewById(R.id.homepage_toolbar);
        RequestButton =  findViewById(R.id.requestButton);

        bottomSheet=findViewById(R.id.bottomSheet);
       // bottomSheetBehavior=BottomSheetBehavior.from(bottomSheet);








        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("User App");
        user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();
        ref = FirebaseDatabase.getInstance().getReference();
        Fab = findViewById(R.id.fab);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_homepage);
        mapFragment.getMapAsync(this);
        mGuideInfo=findViewById(R.id.guideInfo);
        mGuideName=findViewById(R.id.guideName);
        mGuidePhoneNo=findViewById(R.id.guidePhoneNo);
        mTourDuration=findViewById(R.id.tourDuration);
        polylines = new ArrayList<>();

        RequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double latitude=mLastLocation.getLatitude();
                double longitude=mLastLocation.getLongitude();
                String uid = user.getUid();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("User Request");
                GeoFire geoFire = new GeoFire(databaseRef);
                pickUpLocation = new LatLng(latitude, longitude);
                geoFire.setLocation(uid, new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                    }
                });
                if(pickUpMarker!=null){
                    pickUpMarker.remove();
                }
                pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLocation).title("Pick Up Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.tourist_marker_foreground)));
                RequestButton.setText("Getting Your Guide ...");
                getClosestGuide();
            }

        });
        Fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                geoQuery.removeAllListeners();
                guideLocationRef.removeEventListener(guideLocationListener);
                if (guideFoundId != null) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Guides").child(guideFoundId).child("UserId");
                    ref.removeValue();
                    guideFoundId = null;
                }
                guideFound = false;
                radius = 1;
                if (pickUpMarker != null) {
                    pickUpMarker.remove();
                }
                if (mGuideMarker != null) {
                    mGuideMarker.remove();
                }
                String uid = user.getUid();
                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();
                DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("User Request");
                GeoFire geoFire = new GeoFire(databaseRef);
                pickUpLocation = new LatLng(latitude, longitude);
                geoFire.removeLocation(uid, new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                    }
                });
                mGuideInfo.setVisibility(View.GONE);
                mGuideName.setText("");
                mGuidePhoneNo.setText("");
                RequestButton.setVisibility(View.VISIBLE);
                RequestButton.setText("Request A Guide");
                Fab.hide();
            }
        });
        Fab.hide();
    }




    private int radius = 1;
    private Boolean guideFound = false;
    private String guideFoundId;
    private String NotificationId;
    GeoQuery geoQuery;

    private void getClosestGuide() {
        DatabaseReference guideLocation = FirebaseDatabase.getInstance().getReference().child("Guides Available");
        GeoFire geoFire = new GeoFire(guideLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickUpLocation.latitude, pickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!guideFound) {
                    guideFound = true;
                    guideFoundId = key;

                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Guides").child(guideFoundId);
                    String uid = user.getUid();
                    HashMap map = new HashMap();
                    map.put("UserId", uid);
                    ref.updateChildren(map);
                    getGuideLocation();
                    RequestButton.setText("Looking For Guides Nearby ...");
                    ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists()){
                                        NotificationId=dataSnapshot.child("Notification Key").getValue().toString();
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });
                    new sendNotification("You have been connected to a Guide!","Alert!",NotificationId);

                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                if (!guideFound) {
                    radius++;
                    getClosestGuide();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private Marker mGuideMarker;
    private DatabaseReference guideLocationRef;
    private ValueEventListener guideLocationListener;

    private void getGuideLocation() {
        guideLocationRef = FirebaseDatabase.getInstance().getReference().child("Guides Working").child(guideFoundId).child("l");
        guideLocationListener = guideLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0;
                    double locationLong = 0;
                    // RequestButton.setText("Guide Found");
                    if (map.get(0) != null) {
                        locationLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null) {
                        locationLong = Double.parseDouble(map.get(1).toString());
                    }
                    LatLng guideLatLon = new LatLng(locationLat, locationLong);
                    if (mGuideMarker != null) {
                        mGuideMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(pickUpLocation.latitude);
                    loc1.setLongitude(pickUpLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(guideLatLon.latitude);
                    loc2.setLongitude(guideLatLon.longitude);

                    mGuideMarker = mMap.addMarker(new MarkerOptions().position(guideLatLon).title("Guide Is Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.guide_marker_foreground)));
                    double distance = loc1.distanceTo(loc2);

                    if (distance < 10) {
                        getGuideInfo();
                        RequestButton.setText("Guide has arrived");
                        RequestButton.setVisibility(View.GONE);
                        Fab.show();
                    } else {
                        getGuideInfo();
                        Fab.show();
                        RequestButton.setText("Guide Found " + String.format(":%.2f", distance) + "m");
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    private void getGuideInfo() {
        mGuideInfo.setVisibility(View.VISIBLE);
          ref.child("Guides").child(guideFoundId).child("Guide Profile")
                  .addValueEventListener(new ValueEventListener() {
                      @Override
                      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                          if(dataSnapshot.exists()&& dataSnapshot.hasChild("name")){
                              String Rname=dataSnapshot.child("name").getValue().toString();
                              String RphoneNumber=dataSnapshot.child("phoneNo").getValue().toString();
                              String Rlang2=dataSnapshot.child("Tour Duration").getValue().toString();
                              mGuideName.setText("Name: "+Rname);
                              mGuidePhoneNo.setText("Phone Numner: +91"+RphoneNumber);
                              mTourDuration.setText("Tour Duration: "+Rlang2);
                          }
                      }
                      @Override
                      public void onCancelled(@NonNull DatabaseError databaseError) {
                      }
                  });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Request_Code);
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Request_Code);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_logout) {
            SignOut();
            Intent intent = new Intent(Homepage.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.menu_profile) {
            Intent intent = new Intent(Homepage.this, Profile.class);
            startActivity(intent);
        }

        return true;
    }

    private void SignOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(Homepage.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (user == null) {
            Intent intent = new Intent(Homepage.this, MainActivity.class);
            startActivity(intent);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Request_Code);
            return;
        }
    }




}
