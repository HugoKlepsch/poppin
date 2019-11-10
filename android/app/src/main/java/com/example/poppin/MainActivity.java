package com.example.poppin;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_REQUEST = 69;

    /*
       -  This "Main" Activity has .NoHistory, and allows us to run code prior to showing the map.
       -  This also means you cannot return to this Activity once its completed.
       -  Only happens on fresh boots of the application.
       -  Most applications have something like this where they show their
            "LOGO" on fresh boots while they collect info.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        /* before anything, check to see if we can connect to google */
        /* TODO: We can also have the connect to our severs here,
            as a blocker to showing the map, or not ? */
        if (isGoogleServicesOnline()) {
            init();
        }
    }

    /**
     * @purpose: Prior to launching the map, we can use this method to log into our services
     */
    private void init() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    /**
     * @purpose: Check to see if something is wrong with GP Services before running anything.
     * Allows us to crash gracefully.
     * @in: None
     * @out: Boolean - is Google Maps Working Correctly from this device...
     */
    public boolean isGoogleServicesOnline() {
        int isAvailable =
                GoogleApiAvailability
                        .getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if (isAvailable == ConnectionResult.SUCCESS) {
            //Google Maps is connected.
            Log.d(TAG, "Google Play Connected and Working...");
            Toast.makeText(this, "Connected to Google", Toast.LENGTH_SHORT).show();

            return true;

        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable)) {
            Log.d(TAG, "Google Play Encountered an Error, but it can be recovered from.");
            //Example: Wrong version of GooglePlay Services
            Dialog dialog =
                    GoogleApiAvailability
                            .getInstance()
                            .getErrorDialog(MainActivity.this, isAvailable, ERROR_REQUEST);
            //Show the user the error that google wants to show us, including a solution link.
            dialog.show();

        } else {
            Toast.makeText(this, "You cannot make map request",
                    Toast.LENGTH_SHORT).show();
        }
        //if there was any issue at all, return false
        return false;
    }




}
