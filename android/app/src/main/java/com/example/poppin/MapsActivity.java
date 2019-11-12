package com.example.poppin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity
        implements
        OnMapReadyCallback,
        CreateEventBottomSheetFragment.OnEventCreationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnCameraIdleListener {


    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;

    private byte[] accountId;
    private String accountKeyStoragePath = "account_id";

    private String mBaseAPIURL = "http://10.0.2.2:1221"; // local dev server
    //private String mBaseAPIURL = "http://poppintest.hugo-klepsch.tech"; // worldwide test server

    private Boolean mLocationPermissionsGranted = false;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private final float DEFAULT_ZOOM = 15f;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private FragmentManager mFragmentManager = getSupportFragmentManager();

    public Map<EventMarker, Event> markerMap;

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private LatLng currentLocation = new LatLng(0, 0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Maps is the MainView in this context.
        setContentView(R.layout.activity_maps);

        loadAccountCredentials();

        markerMap = new HashMap<>();


        Log.d(TAG, "onCreate: Forcing Permission Check");
        /* Prior to starting the maps, make sure we have location services */
        forcePermissionsRequest();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton createEventFAB = (FloatingActionButton) findViewById(R.id.create_event);

        createEventFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                CreateEventBottomSheetFragment createEventBottomSheetFragment =
                        new CreateEventBottomSheetFragment(MapsActivity.this);
                createEventBottomSheetFragment.setArguments(bundle);
                createEventBottomSheetFragment.show(getSupportFragmentManager(), "Create Event");
            }
        });

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Toast.makeText(MapsActivity.this,
                        "Please keep location services on",
                        Toast.LENGTH_LONG).show();
            }
        };
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            &&
                            checkSelfPermission(
                                    Manifest.permission.ACCESS_COARSE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED) {

                /*
                 TODO: Consider calling
                    Activity#requestPermissions
                 here to request the missing permissions, and then overriding
                   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                                          int[] grantResults)
                 to handle the case where the user grants the permission. See the documentation
                 for Activity#requestPermissions for more details.
                 */
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        5000, 10, mLocationListener);
            }
        }
    }

    @Override
    public void onEventCreate(Event event) {
        try {
            if (mLocationPermissionsGranted) {
                LocationManager locationManager =
                        (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location currentLocation = locationManager
                        .getLastKnownLocation(locationManager
                                .getBestProvider(criteria, false));

                event.setLocation(new LatLng(
                        currentLocation.getLatitude(),
                        currentLocation.getLongitude()));

                sendEventToAPI(event);

                addEventToMap(event);
            }
        } catch (SecurityException e) {
            /* Permissions are not granted - get them */
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

        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    private double chopDouble(double d) {
        return ((int) (d * 10000)) / 10000.00;
    }

    private List<WeightedLatLng> generateDemoPoints(Event event) {
        int numPoints = (int) event.getHotness() * 50;
        double weight = 1.0;//event.getHotness();
        Random random = new Random();

        List<WeightedLatLng> points = new ArrayList<>();

        for (int i = 0; i < numPoints; i++) {
            double constant = 0.0004;

            double offsetLat = (random.nextGaussian() * constant);
            double offsetLng = (random.nextGaussian() * constant);

            LatLng newPoint = new LatLng(
                    event.getLatitude() + offsetLat,
                    event.getLongitude() + offsetLng);

            double intensity = event.getHotness() * constant /
                    (Math.sqrt(Math.pow(offsetLat, 2) + Math.pow(offsetLng, 2)));

            Log.d("intensity", ": " + intensity);

            points.add(new WeightedLatLng(newPoint, intensity));
        }

        return points;
    }

    private List<WeightedLatLng> generateHeatmapWeightedList() {
        List<WeightedLatLng> list = new ArrayList<>();

        for (Event event : markerMap.values()) {
            list.add(new WeightedLatLng(
                    new LatLng(event.getLatitude(), event.getLongitude()),
                    event.getHotness()));
            list.addAll(generateDemoPoints(event));
        }
        list.add(new WeightedLatLng(currentLocation, 0.0000001)); // Can't be empty

        return list;
    }

    private void createHeatmap() {
        List<WeightedLatLng> list = generateHeatmapWeightedList();

        int[] colors = {
                Color.GREEN,    // green(0-50)
                Color.YELLOW,    // yellow(51-100)
                Color.rgb(255,165,0), //Orange(101-150)
                Color.RED,              //red(151-200)
                Color.rgb(153,50,204), //dark orchid(201-300)
                Color.rgb(165,42,42) //brown(301-500)
        };

        float[] startPoints = {
                0.09F, 0.5F, 1.0F, 2.4F, 6.6F, 13.0F
        };

        Gradient gradient = new Gradient(colors, startPoints);

        mProvider = new HeatmapTileProvider
                .Builder()
                .weightedData(list)
                .gradient(gradient)
                .build();
        mProvider.setRadius(25);
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private void updateHeatmap() {
        List<WeightedLatLng> list = generateHeatmapWeightedList();

        mProvider.setWeightedData(list);
        mOverlay.clearTileCache();

        //mProvider.setRadius(getRadiusFromZoom(mMap.getCameraPosition().zoom));
    }

    /**
     *
     * @return
     */
    private void loadEventsFromAPI() {
        JSONObject obj = ApplicationNetworkManager.getDefaultAuthenticatedRequest(this.accountId);

        Log.d(TAG, "loadEventsFromAPI start");
        LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;

        try {
            obj.put("latitude_northeast", curScreen.northeast.latitude);
            obj.put("longitude_northeast", curScreen.northeast.longitude);
            obj.put("latitude_southwest", curScreen.southwest.latitude);
            obj.put("longitude_southwest", curScreen.southwest.longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonRequest request = new JsonObjectArrayRequest(
                Request.Method.POST,
                mBaseAPIURL + "/api/events/by_location",
                obj,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, "loadEventsFromAPI onResponse. response: " + response.toString()
                                + " length: " + response.length());
                        try {

                            for (int i = 0; i < response.length(); i++) {
                                try {
                                    Event event = new Event((JSONObject) response.get(i));
                                    addEventToMap(event);
                                } catch (ParseException e) {
                                    Log.e("ERROR", "Found event with incorrect date signature");
                                }
                            }
                        } catch (JSONException e) {
                            // ignore
                            Log.e(TAG, "loadEventsFromAPI json exception" + e.toString());
                        }

                        if (mProvider == null) {
                            createHeatmap();
                        }
                        updateHeatmap();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "loadEventsFromAPI error response" + error.toString());
                    }
                }
        );

        ApplicationNetworkManager
                .getInstance(this.getApplicationContext())
                .addToRequestQueue(request);
    }

    public void sendEventToAPI(Event event) {
        JSONObject obj = ApplicationNetworkManager.getDefaultAuthenticatedRequest(this.accountId);

        Log.d(TAG, "sendEventToAPI start");

        // Merge the authenticated JSONObject with the event JSONObject. This kind of sucks.
        JSONObject event_obj = event.serialize();
        Iterator<String> keys = event_obj.keys();
        String key;
        while (keys.hasNext()) {
            key = keys.next();

            try {
                obj.put(key, event_obj.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
                break;
            }

        }

        JsonRequest request = new JsonObjectRequest(
                Request.Method.POST,
                mBaseAPIURL + "/api/event",
                obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "sendEventToAPI onResponse. response: " + response.toString()
                                + " length: " + response.length());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "sendEventToAPI error response" + error.toString());
                    }
                }
        );

        ApplicationNetworkManager
                .getInstance(this.getApplicationContext())
                .addToRequestQueue(request);

    }

    public void addEventToMap(Event event) {
        MarkerOptions options = new MarkerOptions()
                .position(event.getLocation())
                .title(event.getTitle())
                .snippet(event.getCategory());

        Marker marker = mMap.addMarker(options);

        markerMap.put(new EventMarker(marker), event);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Get the model from the hashmap based on the clicked event
        Event event = markerMap.get(new EventMarker(marker));

        Bundle bundle = new Bundle();
        bundle.putSerializable("Event", event);

        if (event != null) {
            ViewEventBottomSheetFragment viewEventBottomSheetFragment =
                    new ViewEventBottomSheetFragment();
            viewEventBottomSheetFragment.setArguments(bundle);
            viewEventBottomSheetFragment.show(getSupportFragmentManager(),
                    viewEventBottomSheetFragment.getTag());
        }

        return true;
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
        mMap.setOnCameraIdleListener(this);
        Log.d(TAG, "onMapReady: Maps are running with Full Permissions.");
        mMap.setOnMarkerClickListener(this);

        // initialize to Null, since it is generated in the following function.
        mProvider = null;
        loadEventsFromAPI();

        if (mLocationPermissionsGranted) {
            setUserLocation();
            mMap.setMyLocationEnabled(true);
        }
    }



    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving camera to location -> latitude:"
                        + latLng.latitude  + " ," + latLng.longitude );
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
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(
                                    new LatLng(
                                            currentLocation.getLatitude(),
                                            currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
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
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);
    }


    private void forcePermissionsRequest() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (ContextCompat
                .checkSelfPermission(
                        this.getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {

            mLocationPermissionsGranted = true;
            initMap(); /* Permissions already granted, present the map */
        } else {
            /* If we don't have location permissions, force ask for them! */
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult
            (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap(); /* Do not try and open the map until we have all required permissions */
            }
        }
    }

    /**
     *
     * @param zoom
     * @return
     */
    private int getRadiusFromZoom(float zoom) {
        int radius = (int) (zoom * 10);
        Log.d("Zoom", "zoom: radius = " + zoom + ": " + radius);
        return radius;
    }

    /**
     *
     */
    @Override
    public void onCameraIdle() {
        Log.d(TAG, "Camera Idle, getting events");
        markerMap.clear();
        loadEventsFromAPI();
    }
}

