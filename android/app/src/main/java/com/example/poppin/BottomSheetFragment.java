package com.example.poppin;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.time.temporal.TemporalField;
import java.util.Date;

public class BottomSheetFragment extends BottomSheetDialogFragment {

    private TextView titleView;
    private TextView categoryView;
    private TextView timeView;

    public BottomSheetFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();

        Event event = (Event)bundle.getSerializable("Event");
        titleView = view.findViewById(R.id.event_title);
        titleView.setText(event.getTitle());

        timeView = view.findViewById(R.id.time);
        timeView.setText(event.getLocalTime());

        categoryView = view.findViewById(R.id.category);
        categoryView.setText(event.getCategory());

        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_bottom_sheet, container, false);

    }
}
