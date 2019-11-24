package com.example.poppin;

import android.os.Build;

import com.google.android.gms.maps.model.Marker;

import java.util.Objects;

import androidx.annotation.RequiresApi;

public class EventMarker {
    public double latitude, longitude;
    public String title;
    public Marker markerRef;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventMarker that = (EventMarker) o;
        return Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                title.equals(that.title);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, title);
    }

    public EventMarker(Marker marker) {
        this.latitude = marker.getPosition().latitude;
        this.longitude = marker.getPosition().longitude;
        this.title = marker.getTitle();
        this.markerRef = marker;
    }
}
