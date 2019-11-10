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
    private byte[] accountID;
    final public static String baseAPIURL = "http://10.0.2.2:1221"; // local dev server

    private ApplicationNetworkManager(Context ctx, byte[] aID) {
        applicationContext = ctx;
        requestQueue = getRequestQueue();
        this.accountID = aID;
    }

    public static JSONObject getDefaultAuthenticatedRequest() {
        JSONObject defaultObject;
        defaultObject = new JSONObject();

        try {
            defaultObject.put("device_key", instance.accountID);
        } catch (JSONException e) {
            return null;
        }

        return defaultObject;
    }

    public static void initialize(Context context, byte[] accountID) {
        if (instance == null) {
            instance = new ApplicationNetworkManager(context, accountID);
        }
    }

    public static synchronized ApplicationNetworkManager getInstance(Context context, byte[] accountID) {
        if (instance == null) {
            if (accountID == null) {
                throw new NullPointerException();
            }
            instance = new ApplicationNetworkManager(context, accountID);
        }

        return instance;
    }

    public static synchronized ApplicationNetworkManager getExistingInstance() {
        if (instance == null) {
            throw new NullPointerException();
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
