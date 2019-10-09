package com.example.poppin;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Singleton pattern class designed to preserve the network queue.
 * adapted from https://developer.android.com/training/volley/requestqueue#java
 */
public class ApplicationNetworkManager {
    private static ApplicationNetworkManager instance;
    private RequestQueue requestQueue;
    private static Context applicationContext;

    private ApplicationNetworkManager(Context ctx) {
        applicationContext = ctx;
        requestQueue = getRequestQueue();
    }

    public static JSONObject getDefaultAuthenticatedRequest(byte[] accountID) {
        JSONObject defaultObject;
        defaultObject = new JSONObject();

        try {
            defaultObject.put("device_key", accountID);
        } catch (JSONException e) {
            return null;
        }

        return defaultObject;
    }

    public static synchronized ApplicationNetworkManager getInstance(Context context) {
        if (instance == null) {
            instance = new ApplicationNetworkManager(context);
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
