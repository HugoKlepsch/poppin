package com.example.poppin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Event implements Serializable {
    private double latitude;
    private double longitude;
    private String name;
    private String description;

    private int checkins;
    private int hype;

    private double hotness;

    /**
     *
     * @param lat
     * @param lon
     * @param name
     * @param description
     */
    public Event(double lat, double lon, String name, String description) {
        this.setLatitude(lat);
        this.setLongitude(lon);
        this.setName(name);
        this.setDescription(description);

        this.setCheckins(100);
        this.setHype(100);
    }

    /**
     *
     * @param jsonObj
     */
    public Event(JSONObject jsonObj) throws JSONException {
        this.latitude = (Double) jsonObj.get("latitude");
        this.longitude = (Double) jsonObj.get("longitude");
        this.name = (String) jsonObj.optString("title");
        this.description = (String) jsonObj.optString("description");
        this.checkins = (Integer) jsonObj.optInt("checkins");
        this.hype = (Integer) jsonObj.optInt("hype");
    }

    /**
     *
     * @return
     */
    public JSONObject serialize() {
        JSONObject json;

        json = new JSONObject();

        try {
            json.put("latitude", latitude);
            json.put("longitude", longitude);
            json.put("title", name);
            json.put("description", description);
            json.put("checkins", checkins);
            json.put("hype", hype);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    /**
     *
     * @return
     */
    public String toString() {
        String serialJson;

        serialJson = "{";

        serialJson += "\"latitude\":" + "\"" + latitude + "\",";
        serialJson += "\"longitude\":" + "\"" + longitude + "\",";
        serialJson += "\"title\":" + "\"" + name + "\",";
        serialJson += "\"description\":" + "\"" + description + "\",";
        serialJson += "\"checkins\":" + "\"" + checkins + "\",";
        serialJson += "\"hype\":" + "\"" + hype + "\"}";

        return serialJson;
    }


    /**
     *
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     *
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     *
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     */
    public int getCheckins() {
        return checkins;
    }

    /**
     *
     * @param checkins
     */
    public void setCheckins(int checkins) {
        this.checkins = checkins;
    }



    /**
     *
     * @return
     */
    public int getHype() {
        return hype;
    }

    /**
     *
     * @param hype
     */
    public void setHype(int hype) {
        this.hype = hype;
    }

    /**
     *
     * @return
     */
    public double getHotness() {
        return hotness;
    }

    /**
     *
     * @param hotness
     */
    public void setHotness(double hotness) {
        this.hotness = hotness;
    }
}
