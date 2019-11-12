package com.example.poppin;

import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.temporal.TemporalField;
import java.util.Date;

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
    private Button hypeButton;


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

        hypeButton = view.findViewById(R.id.hypebutton);
        if (event.wasHyped()) {
            hypeButton.setEnabled(false);
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
                        .getDefaultAuthenticatedRequest(DeviceKey.getDeviceKey(getContext().getApplicationContext()));
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

        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.view_event_fragment_bottom_sheet, container, false);
    }
}
