package com.example.poppin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ViewEventBottomSheetFragment extends BottomSheetDialogFragment {


    private TextView txtTitle;
    private TextView txtGroupSize;
    private ImageView imgGroupSize;
    private TextView titleView;
    private TextView categoryView;
    private TextView timeView;
    private TextView descriptionView;


    public ViewEventBottomSheetFragment() {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();

        Event event = (Event)bundle.getSerializable("Event");
        txtTitle = view.findViewById(R.id.event_title);
        txtTitle.setText(event.getTitle());


        txtGroupSize = view.findViewById(R.id.expected_group_size);
        String groupSizeDialog = String.format("Recommended Group Size: (%d - %d)", event.getRecommendedGroupSizeMin(), event.getRecommendedGroupSizeMax());
        txtGroupSize.setText(groupSizeDialog);


        descriptionView = view.findViewById(R.id.description);
        descriptionView.setText(event.getDescription());



        super.onViewCreated(view, savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.view_event_fragment_bottom_sheet, container, false);

    }
}
