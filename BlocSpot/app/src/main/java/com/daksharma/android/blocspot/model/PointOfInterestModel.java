package com.daksharma.android.blocspot.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Daksh on 1/24/16.
 */
public class PointOfInterestModel extends RealmObject {

    @PrimaryKey
    private String placeName;
    private String placeAddress;

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
}