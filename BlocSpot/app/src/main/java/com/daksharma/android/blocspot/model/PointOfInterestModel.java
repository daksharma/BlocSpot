package com.daksharma.android.blocspot.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Daksh on 1/24/16.
 */
public class PointOfInterestModel extends RealmObject {

    @PrimaryKey
    private String placeAddress; // no address will be the same  or use long/lat

    private String id;
    private String placeName;
    private String userNotes;


//    public PointOfInterestModel(String placeName, String placeAddr) {
//        this.placeName = placeName;
//        this.placeAddress = placeAddr;
//    }


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

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }

    public String getUserNotes() {
        return userNotes;
    }

    public void setUserNotes(String notes) {
        this.userNotes = notes;
    }


}