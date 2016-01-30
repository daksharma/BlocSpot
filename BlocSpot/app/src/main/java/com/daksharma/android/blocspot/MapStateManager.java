package com.daksharma.android.blocspot;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Daksh on 1/29/16.
 */
public class MapStateManager {

    private static final String LONGITUDE = "longitude";
    private static final String LATITUDE = "latitude";
    private static final String ZOOM = "zoom";
    private static final String BEARING = "bearing";
    private static final String TILT = "tilt";
    private static final String MAPTYPE = "MAPTYPE";

    private static final String PREFS_NAME = "mapCameraState";

    private SharedPreferences mapStatePrefs;

    public MapStateManager(Context context) {
        mapStatePrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveMapState(GoogleMap gMap) {
        SharedPreferences.Editor prefEditor = mapStatePrefs.edit();

        CameraPosition camPosition = gMap.getCameraPosition();

        prefEditor.putFloat(LATITUDE, (float) camPosition.target.latitude);
        prefEditor.putFloat(LONGITUDE, (float) camPosition.target.longitude);
        prefEditor.putFloat(ZOOM, camPosition.zoom);
        prefEditor.putFloat(TILT, camPosition.tilt);
        prefEditor.putFloat(BEARING, camPosition.bearing);
        prefEditor.putInt(MAPTYPE, gMap.getMapType());

        prefEditor.commit();
    }

    public CameraPosition getSavedCameraPosition() {
        double latitude = mapStatePrefs.getFloat(LATITUDE, 0);
        if(latitude == 0) { return null; }

        double longitude = mapStatePrefs.getFloat(LONGITUDE, 0);
        LatLng targetLocation = new LatLng(latitude, longitude);

        float zoom = mapStatePrefs.getFloat(ZOOM, 0);
        float bearing = mapStatePrefs.getFloat(BEARING, 0);
        float tilt = mapStatePrefs.getFloat(TILT, 0);
        int mapType = mapStatePrefs.getInt(MAPTYPE, 0);

        CameraPosition cameraPositon = new CameraPosition(targetLocation, zoom, tilt, bearing);
        return cameraPositon;
    }

}
