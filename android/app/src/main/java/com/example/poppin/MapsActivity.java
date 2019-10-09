package com.example.poppin;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, CreateEventFragment.OnInputListener, View.OnClickListener, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;

    private byte[] accountId;
    private String accountKeyStoragePath = "account_id";
    private String mBaseAPIURL = "";


    /* JULIANS WAY */
    private ArrayList<Event> events;

    private Boolean mLocationPermissionsGranted = false;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private final float DEFAULT_ZOOM = 15f;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FragmentManager mFragmentManager = getSupportFragmentManager();

    public Map<Marker, Event> markerMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Maps is the MainView in this context.
        setContentView(R.layout.activity_maps);

        loadAccountCredentials();
        getEvents();

        markerMap = new HashMap<>();


        Log.d(TAG, "onCreate: Forcing Permission Check");
        /* Prior to starting the maps, make sure we have location services */
        forcePermissionsRequest();

       // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button createEventClick = (Button) findViewById(R.id.create_event);

        createEventClick.setOnClickListener(MapsActivity.this);
    }




    public void sendInput(String input) {
        Log.e(TAG, "got the input: " + input);
        Toast.makeText(this, "Inputted:" + input, Toast.LENGTH_SHORT).show();

        try {
            if (mLocationPermissionsGranted) {

                LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                Location currentLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                Marker marker = createMarker(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), input);


                Event event = new Event(currentLocation.getLatitude(), currentLocation.getLongitude(), input, "Description");


                addEvent(marker,  event);
            }
        }catch (SecurityException e) {
            /* Permissions are not granted - get them*/
                forcePermissionsRequest();

        }
    }

    /**
     *
     * @return
     */
    private byte[] generateAccountCredentials() {
        byte[] accountId;
        Random r = new Random();
        accountId = new byte[256];

        r.nextBytes(accountId);

        try {
            FileOutputStream fOut = openFileOutput(accountKeyStoragePath, Context.MODE_PRIVATE);
            fOut.write(accountId);
            fOut.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
        return accountId;
    }

    /**
     *
     */
    private void submitCredentialsForRegistration() {
        JSONObject obj = ApplicationNetworkManager.getDefaultAuthenticatedRequest(this.accountId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                mBaseAPIURL + "/api/signup",
                obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // load all the events into the program
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        ApplicationNetworkManager.getInstance(this.getApplicationContext()).addToRequestQueue(request);

        return;
    }

    /**
     *
     */
    private void loadAccountCredentials() {
        FileInputStream fIn;
        this.accountId = new byte[256];

        try {
            byte[] bytes = new byte[256];
            fIn = openFileInput(accountKeyStoragePath);
            fIn.read(bytes);
            System.arraycopy(bytes, 0, this.accountId, 0, 256);

        } catch (FileNotFoundException e) {
            byte[] bytes;
            bytes = generateAccountCredentials();
            System.arraycopy(bytes, 0, this.accountId, 0, 256);
            submitCredentialsForRegistration();
        } catch (IOException e) {
            System.out.println(e.toString());
        }

        return;
     }


    /**
     *
     * @return
     */
    private void getEvents() {
        JSONObject obj = ApplicationNetworkManager.getDefaultAuthenticatedRequest(this.accountId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                mBaseAPIURL + "/api/events",
                obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray eventsArray;
                        try {
                            eventsArray = response.getJSONArray("events");

                            for (int i = 0; i < eventsArray.length(); i++) {
                                Event e = new Event((JSONObject) eventsArray.get(i));
                                events.add(e);
                            }
                        } catch (JSONException e) {
                            // ignore
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );

        ApplicationNetworkManager.getInstance(this.getApplicationContext()).addToRequestQueue(request);

        return; // explicit return
    }

    public void addEvent(Marker marker, Event event) {
            markerMap.put(marker, event);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Get the model from the hashmap based on the clicked event
            Event event = markerMap.get(marker);

            if (event != null) {

                Toast.makeText(this, "Clicked Event: " + event.getName(), Toast.LENGTH_SHORT).show();

                BottomSheetFragment bottomSheetFragment = new BottomSheetFragment();
                bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

            }

        return true;

    }

    private Marker createMarker(LatLng latLng, String title) {
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet("DESCRIPTION");
        Marker marker =  mMap.addMarker(options);
        marker.showInfoWindow();
        return marker;
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
        Log.d(TAG, "onMapReady: Maps are running with Full Permissions.");
        Toast.makeText(this, "Ready to Map things", Toast.LENGTH_SHORT).show();
        mMap.setOnMarkerClickListener(this);

        if (mLocationPermissionsGranted) {


            setUserLocation();
            mMap.setMyLocationEnabled(true);
            /* remove the google default button that cannot be changed. We can create our own 'Center to me' Button that doesn't get in the way */
            //mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }


    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to location -> latitude:" + latLng.latitude  + " ," + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void setUserLocation() {

        Log.d(TAG, "getUserLocation: Getting the users location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                    Task location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "found location");
                                Location  currentLocation = (Location) task.getResult();
                                moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                            }
                            else {
                                Log.d(TAG, "could not find location");

                            }
                        }
                    });
            }

        }
        catch (SecurityException e){
            /* Permissions are not granted - get them*/
            forcePermissionsRequest();
        }
    }


    private void initMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);


    }


    private void forcePermissionsRequest() {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap(); /* Permissions already granted, present the map */

            }
            else {
                /* If we don't have location permissions, force ask for them! */
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE );
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = true;
                            initMap(); /* Do not try and open the map until we have all required permissions */
                    }

            }
        }


    }

    /* TODO: Support multiple buttons being clicked, currently any button press will execute this code */
    @Override
    public void onClick(View v) {
        CreateEventFragment fragment = new CreateEventFragment();
        fragment.show(mFragmentManager, "Create Event");

    }
}

