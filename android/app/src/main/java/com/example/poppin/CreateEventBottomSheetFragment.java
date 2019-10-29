package com.example.poppin;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.ParseException;
import java.util.Date;

public class CreateEventBottomSheetFragment extends BottomSheetDialogFragment {

    private EditText titleEdit;
    private NumberPicker groupSizeMin, groupSizeMax, categorySelect;
    private TextView location, time, category;
    private EditText description;
    private ImageButton createEventButton;

    private String[] categories = {"Fun", "Professional","Party",  "Academic"};

    private OnEventCreationListener listener;

    public interface OnEventCreationListener {
        void onEventCreate(Event event);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
                checkForEmptyText();
        }
    };


    public void checkForEmptyText() {

        String titleText = titleEdit.getText().toString();
        titleText.trim();

        if (titleText.equals("")) {
            createEventButton.setEnabled(false);
            createEventButton.setAlpha(.3f);
        }
        else {
            createEventButton.setEnabled(true);
            createEventButton.setAlpha(1.0f);

        }


    }

    public CreateEventBottomSheetFragment(OnEventCreationListener listener) {
        super();
        this.listener = listener;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();

        titleEdit = view.findViewById(R.id.event_title);
        categorySelect = view.findViewById(R.id.category_picker);
        location = view.findViewById(R.id.location);
        time = view.findViewById(R.id.time);
        category = view.findViewById(R.id.category);
        description = view.findViewById(R.id.description);
        createEventButton = view.findViewById(R.id.createEventButton);


        titleEdit.addTextChangedListener(mTextWatcher);
        checkForEmptyText();
        setupGroupSizePickers(view);
        setupCategoryPicker(view);

        try {
            // Doing this just to get the current local time formatted right.
            Event event = new Event(0, 0, "",
                    new Date(),
                    "", "", 99, 1);
            time.setText(event.getLocalTime());
        } catch (ParseException e) {
            e.printStackTrace();
            time.setText("XX:XX");
        }

        createEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Event event = new Event(0, 0, // These lat/lon aren't correct, but I can't figure out
                                                  // How to get the location inside this fragment.
                            titleEdit.getText().toString(),
                            new Date(), // TODO get the time from user input on this fragment
                            description.getText().toString(),
                            categories[categorySelect.getValue()],
                            groupSizeMax.getValue(),
                            groupSizeMin.getValue());

                    listener.onEventCreate(event);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                getDialog().dismiss();
            }
        });

        super.onViewCreated(view, savedInstanceState);
    }

    private void setupGroupSizePickers(View view) {
        groupSizeMax = view.findViewById(R.id.recommended_group_size_max);
        groupSizeMin = view.findViewById(R.id.recommended_group_size_min);

        groupSizeMax.setMinValue(1);
        groupSizeMin.setMinValue(1);

        groupSizeMax.setMaxValue(99);
        groupSizeMin.setMaxValue(99);

        groupSizeMax.setWrapSelectorWheel(false);
        groupSizeMin.setWrapSelectorWheel(false);

        groupSizeMax.setValue(6);
        groupSizeMin.setValue(1);

        groupSizeMax.setClickable(false);
        groupSizeMin.setClickable(false);
        groupSizeMax.setLongClickable(false);
        groupSizeMin.setLongClickable(false);
    }

    private void setupCategoryPicker(View view) {
        categorySelect.setMinValue(0);
        categorySelect.setMaxValue(2);
        categorySelect.setClickable(false);
        categorySelect.setLongClickable(false);
        categorySelect.setDisplayedValues(categories);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.create_event_fragment_bottom_sheet, container, false);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return rootview;
    }
}
