package com.example.poppin;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private byte[] accountId;
    private String accountKeyStoragePath = "account_id";
    private String mBaseAPIURL = "";

    private ArrayList<Event> events;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.events = new ArrayList<Event>();
        loadAccountCredentials();
        getEvents();

        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        JSONObject obj = ApplicationNetworkManager.getDefaultCredentialRequest(this.accountId);

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
        JSONObject obj = ApplicationNetworkManager.getDefaultCredentialRequest(this.accountId);

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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
