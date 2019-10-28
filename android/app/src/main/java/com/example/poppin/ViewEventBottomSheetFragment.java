package com.example.poppin;

import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.time.temporal.TemporalField;
import java.util.Date;

public class ViewEventBottomSheetFragment extends BottomSheetDialogFragment {

    private TextView titleView;
    private TextView categoryView;
    private TextView timeView;
    private TextView descriptionView;
    private TextView locationView;
    private TextView txtTitle;
    private TextView txtGroupSize;
    private ImageView imgGroupSize;


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
        Geocoder geocoder = new Geocoder(getContext());

        Event event = (Event)bundle.getSerializable("Event");
        titleView = view.findViewById(R.id.event_title);
        titleView.setText(event.getTitle());

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
        String groupSizeDialog = String.format("Recommended Group Size: (%d - %d)", event.getRecommendedGroupSizeMin(), event.getRecommendedGroupSizeMax());
        txtGroupSize.setText(groupSizeDialog);


        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.view_event_fragment_bottom_sheet, container, false);
    }
}
