package com.daksharma.android.blocspot.model;

import android.widget.ImageView;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Daksh on 1/24/16.
 */
public class PointOfInterestModel extends RealmObject {

    @PrimaryKey
    private String placeId; // placeId will come from Google Places result object

    private String placeName;
    private String placeAddress;
    private float placeRating;
    private double mLongitude;
    private double mLatitude;
    private String mPlaceCategory;
    private boolean mPlaceVisited;
    private String userNotes;


    public String getPlaceId () {
        return placeId;
    }

    public void setPlaceId (String placeId) {
        this.placeId = placeId;
    }


    public String getPlaceName () {
        return placeName;
    }

    public void setPlaceName (String name) {
        this.placeName = name;
    }

    public String getPlaceAddress () {
        return placeAddress;
    }

    public void setPlaceAddress (String placeAddr) {
        this.placeAddress = placeAddr;
    }

    public void setPlaceRating (float placeRating) {
        this.placeRating = placeRating;
    }

    public float getPlaceRating() {
        return placeRating;
    }

    public double getmLatitude () {
        return mLatitude;
    }

    public void setmLatitude (double mLatitude) {
        this.mLatitude = mLatitude;
    }

    public double getmLongitude () {
        return mLongitude;
    }

    public void setmLongitude (double mLongitude) {
        this.mLongitude = mLongitude;
    }


    public void setmPlaceCategory(String category) {
        this.mPlaceCategory = category;
    }

    public String getmPlaceCategory() {
        return mPlaceCategory;
    }

    public boolean getmPlaceVisited() {
        return mPlaceVisited;
    }

    public void setmPlaceVisited(boolean placeVisited) {
        this.mPlaceVisited = placeVisited;
    }

    public String getUserNotes () {
        return userNotes;
    }

    public void setUserNotes (String notes) {
        this.userNotes = notes;
    }


}