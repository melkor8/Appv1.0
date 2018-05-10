package com.example.frank47.parkit;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoFire.CompletionListener;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class CustomerMapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener {

    public View mMapView;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequuest;
    private String destination;
    private LatLng destinationLatLng;

    private Button mLogOut;
    private Button mRent;
    Marker mMarker;
    private int searchcount=0;

    private int radius=1;
    private Boolean spacefound=false;
    private String spaceId;
    public static String rentId;
    double rentlatitude, rentlongitude;

    private Spinner mSpinner;

    public static LatLng current_latlng, parking_space_latlng;
    public static int spinner_selection;


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public static class location{
        public String glat,glong;
        public location(String glat, String glong){
            this.glat = glat;
            this.glong = glong;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMapView =  mapFragment.getView();


        mLogOut =(Button) findViewById(R.id.logout);
        mRent = (Button) findViewById(R.id.rent);

        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(CustomerMapsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mRent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getClosestParkingSpace();


            }
        });

        mSpinner= findViewById(R.id.duration_spinner);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(CustomerMapsActivity.this, "You have selected to book for "+parent.getItemAtPosition(position).toString(), Toast.LENGTH_LONG).show();

                spinner_selection=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                searchcount++;
                if(searchcount>1){
                    //remove previous marker
                    mMarker.remove();
                }

                destination = place.getName().toString();
                destinationLatLng = place.getLatLng();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng,15));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(destinationLatLng)
                        .title("Current Position")
                .draggable(true));

                 current_latlng=destinationLatLng;
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Toast.makeText(CustomerMapsActivity.this, "Not recognized", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void getClosestParkingSpace() {
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference().child("parkingspaces");
        DatabaseReference assignment_ref = FirebaseDatabase.getInstance().getReference().child("assignment");



        final GeoFire geofire = new GeoFire(dbref);
        final GeoFire geofirerent = new GeoFire(assignment_ref);

        GeoQuery query = geofire.queryAtLocation(new GeoLocation(mMarker.getPosition().latitude,mMarker.getPosition().longitude),radius);
        query.removeAllListeners();

        query.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if(!spacefound){
                    spacefound=true;
                    spaceId=key;
                    //Toast.makeText(CustomerMapsActivity.this, ""+spaceId, Toast.LENGTH_LONG).show();

                    System.out.println(spaceId);
                    rentId=spaceId;
                    rentlatitude = location.latitude;
                    rentlongitude= location.longitude;

                    parking_space_latlng=new LatLng(rentlatitude,rentlongitude);

                    geofirerent.setLocation(rentId, location, new GeoFire.CompletionListener() {
                            @Override
                        public void onComplete(String key, DatabaseError error) {

                        }
                    });


                    DatabaseReference dbrefremove = FirebaseDatabase.getInstance().getReference().child("parkingspaces");
                    final GeoFire geofireremove = new GeoFire(dbrefremove);
                    geofireremove.removeLocation(rentId, new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            System.out.println("gvgvgvl removed");

                            Intent intent= new Intent(CustomerMapsActivity.this, NavigationMapsActivity.class);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    });



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
                if(!spacefound){
                    radius++;
                    getClosestParkingSpace();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng kol = new LatLng(22.57, 88.36);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kol, 12));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();

        mMap.setMyLocationEnabled(true);

        View myLocationButton = mMapView.findViewWithTag("GoogleMapMyLocationButton");
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    }
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();


    }


    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }




    public void onConnected(@Nullable Bundle bundle) {
/*        mLocationRequuest = new LocationRequest();
        mLocationRequuest.setInterval(1000);
        mLocationRequuest.setFastestInterval(1000);
        mLocationRequuest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequuest, this);
*/
    }


    public void onConnectionSuspended(int i) {

    }


    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
