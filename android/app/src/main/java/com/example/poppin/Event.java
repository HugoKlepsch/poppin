package com.example.poppin;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class Event implements Serializable {
    private double latitude;
    private double longitude;
    private Date time;
    private String title;
    private String description;
    private String category;
    private int recommendedGroupSizeMin;
    private int recommendedGroupSizeMax;


    final static private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");;

    private int checkins;
    private int hype;

    private double hotness;

    /**
     *
     * @param lat
     * @param lon
     * @param title
     * @param time
     * @param description
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public Event(double lat, double lon, String title, Date time, String description,
                 String category, int groupSizeMax, int groupSizeMin) throws ParseException {

        this.setLatitude(lat);
        this.setLongitude(lon);
        this.setTitle(title);
        this.setTime(time);
        this.setDescription(description);
        this.setCategory(category);
        /* Temporary setting default data */
        this.setRecommendedGroupSizeMax(groupSizeMax);
        this.setRecommendedGroupSizeMin(groupSizeMin);

        this.setCheckins(100);
        this.setHype(100);
    }


    /**
     *
     * @param jsonObj
     */
    public Event(JSONObject jsonObj) throws JSONException, ParseException {
        this.latitude = (Double) jsonObj.get("latitude");
        this.longitude = (Double) jsonObj.get("longitude");
        this.title = (String) jsonObj.optString("title");
        this.time = formatter.parse((String) jsonObj.optString("time"));
        this.description = (String) jsonObj.optString("description");
        this.category = (String) jsonObj.optString("category");
        this.checkins = (Integer) jsonObj.optInt("checkins");
        this.hype = (Integer) jsonObj.optInt("hype");
        this.hotness = (Double) jsonObj.optDouble("hotness");
        this.recommendedGroupSizeMax = (Integer) jsonObj.get("group_size_max");
        this.recommendedGroupSizeMin = (Integer) jsonObj.get("group_size_min");
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
            json.put("title", title);
            json.put("time", getISOTime());
            json.put("description", description);
            json.put("category", category);
            json.put("checkins", checkins);
            json.put("hype", hype);
            json.put("hotness", hotness);
            json.put("group_size_max", recommendedGroupSizeMax);
            json.put("group_size_min", recommendedGroupSizeMin);

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
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
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
    public String getCategory() {
        return this.category;
    }

    /**
     *
     * @param category
     */
    public void setCategory(String category) {
        this.category = category;
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
    private Date getTime() {
        return this.time;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getLocalTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
        TimeZone timeZone = TimeZone.getDefault();
        LocalDateTime ldt = LocalDateTime.ofInstant(this.time.toInstant(), timeZone.toZoneId());

        return ldt.atOffset(ZoneOffset.UTC)
                .atZoneSameInstant(timeZone.toZoneId())
                .format(dtf);
    }

    public String getISOTime() {
        return formatter.format(this.time);
    }


    /**
     *
     * @param time
     */
    private void setTime(Date time) {
        this.time = time;
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

    /**
     * @param recommendedGroupSizeMin
     */
    public void setRecommendedGroupSizeMin(int recommendedGroupSizeMin) {
        this.recommendedGroupSizeMin = recommendedGroupSizeMin;
    }


    /**
     * @return
     */
    public int getRecommendedGroupSizeMin() {
        return recommendedGroupSizeMin;
    }


    /**
     * @param recommendedGroupSizeMax
     */
    public void setRecommendedGroupSizeMax(int recommendedGroupSizeMax) {
        this.recommendedGroupSizeMax = recommendedGroupSizeMax;
    }

    /**
     * @return
     */
    public int getRecommendedGroupSizeMax() {
        return recommendedGroupSizeMax;
    }



}
