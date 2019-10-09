package com.example.poppin;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton pattern class designed to preserve the network queue.
 * adapted from https://developer.android.com/training/volley/requestqueue#java
 */
public class ApplicationNetworkQueue {
    private static ApplicationNetworkQueue instance;
    private RequestQueue requestQueue;
    private static Context applicationContext;

    private ApplicationNetworkQueue(Context ctx) {
        applicationContext = ctx;
        requestQueue = getRequestQueue();
    }

    public static synchronized ApplicationNetworkQueue getInstance(Context context) {
        if (instance == null) {
            instance = new ApplicationNetworkQueue(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(applicationContext.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request) {
        getRequestQueue().add(request);
    }
}
