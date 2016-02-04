package com.daksharma.android.blocspot.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Daksh on 2/3/16.
 */
public class PoiCategory extends RealmObject {

    private String categoryName;
    private RealmList<PointOfInterestModel> poiPlaces;

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName (String categoryName) {
        this.categoryName = categoryName;
    }

    public RealmList<PointOfInterestModel> getPoiPlaces () {
        return poiPlaces;
    }

    public void setPoiPlaces (RealmList<PointOfInterestModel> poiPlaces) {
        this.poiPlaces = poiPlaces;
    }
}
