package com.example.poppin;

import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.android.volley.VolleyLog.TAG;

public class ViewEventBottomSheetFragment extends BottomSheetDialogFragment {

    private TextView titleView;
    private TextView checkinsView;
    private TextView hypeView;
    private TextView categoryView;
    private TextView timeView;
    private TextView descriptionView;
    private TextView locationView;
    private TextView txtGroupSize;
    private TextView distance;
    private ImageButton hypeButton;
    private ImageButton checkinButton;


    public ViewEventBottomSheetFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();
        Geocoder geocoder = new Geocoder(getContext().getApplicationContext());

        final Event event = (Event)bundle.getSerializable("Event");
        titleView = view.findViewById(R.id.event_title);
        titleView.setText(event.getTitle());

        checkinsView = view.findViewById(R.id.checkins);
        checkinsView.setText(Integer.toString(event.getCheckins()));

        hypeView = view.findViewById(R.id.hypes);
        hypeView.setText(Integer.toString(event.getHype()));

        timeView = view.findViewById(R.id.time);
        timeView.setText(event.getLocalTime());

        categoryView = view.findViewById(R.id.category);
        categoryView.setText(event.getCategory());

        descriptionView = view.findViewById(R.id.description);
        descriptionView.setText(event.getDescription());

        locationView = view.findViewById(R.id.location);

        checkinButton = view.findViewById(R.id.checkin);

        distance = view.findViewById(R.id.distance);

        LatLng currentLocation = ((MapsActivity)getActivity()).getCurrentLocation();
        double distanceMetres = calculateHaversineDistance(currentLocation, event.getLocation());

        if (distanceMetres > 1000.00) {
            distance.setText(String.format("%.1f km", distanceMetres / 1000));
        } else {
            distance.setText(String.format("%.0f m", distanceMetres));
        }

        if (event.getWasCheckedIn()) {
            checkinButton.setEnabled(false);
            checkinButton.setAlpha(.3f);
        }

        try {
            locationView.setText(
                    geocoder.getFromLocation(
                            event.getLatitude(),
                            event.getLongitude(),
                            1)
                            .get(0)
                            .getAddressLine(0));
        } catch (IOException e) {
            locationView.setText("(" + event.getLatitude() + ", " + event.getLongitude() + ")");
        }

        txtGroupSize = view.findViewById(R.id.expected_group_size);
        String groupSizeDialog = String.format("Recommended Group Size: (%d - %d)",
                event.getRecommendedGroupSizeMin(), event.getRecommendedGroupSizeMax());
        txtGroupSize.setText(groupSizeDialog);

        hypeButton = view.findViewById(R.id.hype);
        if (event.wasHyped()) {
            hypeButton.setEnabled(false);
            hypeButton.setAlpha(.3f);
        } else {
            hypeButton.setEnabled(true);
        }

        /*
        Below this point is all the listeners for the buttons present on the page.
         */

        hypeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = ApplicationNetworkManager
                        .getDefaultAuthenticatedRequest(DeviceKey
                                .getDeviceKey(getContext().getApplicationContext()));
                try {
                    obj.put("event_id", event.getId());
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to apply keys to JSON request object");
                    return;
                }

                JsonRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApplicationNetworkManager.baseAPIURL + "/api/hype/by_id",
                        obj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG,
                                        "Hype Event: onResponse. response: "
                                                + response.toString()
                                                + " length: " + response.length());
                                event.setWasHyped(true);
                                event.setHype(event.getHype() + 1);
                                hypeView.setText(Integer.toString(event.getHype()));
                                hypeButton.setEnabled(false);
                                hypeButton.setAlpha(.3f);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Hype Event: error response" + error.toString());
                            }
                        }
                );

                ApplicationNetworkManager
                        .getInstance(getContext().getApplicationContext())
                        .addToRequestQueue(request);
            }
        });

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject obj = ApplicationNetworkManager
                        .getDefaultAuthenticatedRequest(DeviceKey
                                .getDeviceKey(getContext().getApplicationContext()));

                try {
                    obj.put("event_id", event.getId());
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to apply keys to JSON request object");
                    return;
                }

                if (!isEventNearby(100.00f, event)) {
                    Toast notifyUser = Toast.makeText(getActivity(),
                            "You must to be within 100 meters of an event to check in.",
                            Toast.LENGTH_LONG);
                    notifyUser.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 10);
                    notifyUser.show();
                    return;
                }

                JsonRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        ApplicationNetworkManager.baseAPIURL + "/api/checkin/by_id",
                        obj,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG,
                                        "Checkin Event: onResponse. response: "
                                                + response.toString()
                                                + " length: " + response.length());
                                event.setWasCheckedIn(true);
                                event.setCheckins(event.getCheckins() + 1);
                                checkinsView.setText(Integer.toString(event.getCheckins()));

                                checkinButton.setEnabled(false);
                                checkinButton.setAlpha(.3f);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.e(TAG, "Checkin Event: error response" + error.toString());
                            }
                        }
                );

                ApplicationNetworkManager
                        .getInstance(getContext().getApplicationContext())
                        .addToRequestQueue(request);
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private Boolean isEventNearby(float maxMetersAway, Event event) {
        LatLng currentLocation = ((MapsActivity)getActivity()).getCurrentLocation();

        double distanceMetres = calculateHaversineDistance(currentLocation, event.getLocation());

        // a simplified "if greater than, return false"
        return !(distanceMetres > maxMetersAway);
    }

    public static double calculateHaversineDistance(LatLng a, LatLng b) {
        double distance;
        double earth_radius = 6373.0;

        double latitudeARadians = Math.toRadians(a.latitude);
        double longitudeARadians = Math.toRadians(a.longitude);

        double latitudeBRadians = Math.toRadians(b.latitude);
        double longitudeBRadians = Math.toRadians(b.longitude);

        double deltaLatitude = latitudeBRadians - latitudeARadians;
        double deltaLongitude = longitudeBRadians - longitudeARadians;

        double constA = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                Math.cos(latitudeARadians) * Math.cos(latitudeBRadians) *
                        Math.pow(Math.sin(deltaLongitude / 2), 2);

        double constC = 2 * Math.atan2(Math.sqrt(constA), Math.sqrt(1 - constA));

        distance = earth_radius * constC;

        return distance * 1000;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.view_event_fragment_bottom_sheet, container, false);
    }
}