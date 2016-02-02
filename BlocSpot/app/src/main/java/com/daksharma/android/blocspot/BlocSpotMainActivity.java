package com.daksharma.android.blocspot;

import android.*;
import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class BlocSpotMainActivity extends FragmentActivity implements OnMapReadyCallback,
                                                                      GoogleApiClient.ConnectionCallbacks,
                                                                      GoogleApiClient.OnConnectionFailedListener,
                                                                      LocationListener,
                                                                      ActivityCompat.OnRequestPermissionsResultCallback {

    public final static String TAG = "com.daksharma.blocspot: " + BlocSpotMainActivity.class.getSimpleName()
                                                                                            .toUpperCase();


    private Realm realmObj;


    private GoogleMap       mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location        mLastLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    private MapFragment mMapFragment;

    private static final float DEFAULTZOOM = 15;

    private static final int GPS_ERRORDIALOG_REQUEST = 0;
    private static final int REQUEST_LOCATION_CODE   = 2;
    private static final int REQUEST_STORAGE_CODE    = 3;


    private Menu menu;

    private Bitmap mapPinBitMap;
    private Bitmap redHeartBitMap;
    private Bitmap greenHeartBitMap;
    private Bitmap blueHeartBitMap;

    private Marker mDefaultMarker;
    private Marker mDefaultZeroMarker;


    private Button searchButton;
    private Button listViewButton;
    private Button filterButton;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        buildGoogleApiClient();
        setConvertedBitMap();

        if ( servicesOK() ) {
            setUpMapIfNeeded();
        } else {
            Log.e(TAG, "Services NOT OK: Map not set-up");
        }


        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
        realmStuffTest();

    }

    /*******************
     * GOOGLE API CLIENT
     ********************/
    protected synchronized void buildGoogleApiClient () {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                                                            .addApi(Places.GEO_DATA_API)
                                                            .addApi(Places.PLACE_DETECTION_API)
                                                            .addConnectionCallbacks(this)
                                                            .addOnConnectionFailedListener(this)
                                                            .build();
        mGoogleApiClient.connect();
    }


    @Override
    protected void onResume () {
        super.onResume();
        if ( mMap == null ) {
            setUpMapIfNeeded();
        }
        MapStateManager msManager = new MapStateManager(this);
        CameraPosition  position  = msManager.getSavedCameraPosition();
        if ( position != null && mMap != null ) {
            CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(camUpdate);
        }
        mGoogleApiClient.connect();
    }


    @Override
    protected void onPause () {
        super.onPause();
        if ( mGoogleApiClient.isConnected() ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onStop () {
        MapStateManager msManager = new MapStateManager(this);
        msManager.saveMapState(mMap);
        if ( (mGoogleApiClient != null) && (mGoogleApiClient.isConnected()) ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public Resources getResources () {
        return super.getResources();
    }

    @Override
    public void onConnected (Bundle connectionHint) {
        if ( (connectionHint != null) && (mLastLocation == null) && (mGoogleApiClient.isConnected()) ) {
            Log.e(TAG, "mGoogleApiClient Connected ? : " + mGoogleApiClient.isConnected());
            Log.e(TAG, "Requesting Location Services --- ");
            requestPermissionOnLaunch();
            //requestLocationUpdateIfNeeded();
        } else if ( mLastLocation != null ) {
            handleNewLocation(mLastLocation);
        } else {
            requestLocationUpdateIfNeeded();
        }
    }

    @Override
    public void onConnectionSuspended (int i) {
        Log.e(TAG, "Services connection SUSPENDED.");
        Toast.makeText(this, "Connection SUSPENDED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {
        Log.e(TAG, "Services connection FAILED.");
        Toast.makeText(this, "Connection FAILED", Toast.LENGTH_SHORT).show();

        if ( connectionResult.hasResolution() ) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                //Thrown if Google Play services canceled the original PendingIntent
            } catch ( IntentSender.SendIntentException e ) {
                // Log the error
                e.printStackTrace();
            }
        } else {
/*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            Log.e(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    private void initLocationUpdater () {
        if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
             && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            // if the permission exits in the package manager, only then creat the mLocationRequest;


            Log.e(TAG, "Creating mLocationRequest --- ");
            mLocationRequest = LocationRequest.create()
                                              .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                              .setInterval(20 * 1000)
                                              .setFastestInterval(2000);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            handleNewLocation(mLastLocation);

            if ( servicesOK() ) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

            return;
        } else {
            Log.e(TAG, "Location Permission not granted\nmLocationRequest is not created");
            requestPermissionOnLaunch();
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode,
                                            String[] permissions,
                                            int[] grantResults) { // returns invalid array the first time its asked
        switch ( requestCode ) {
            case REQUEST_LOCATION_CODE:
                if ( (permissions.length > 0) && (permissions[0] == android.Manifest.permission.ACCESS_FINE_LOCATION) && (grantResults[0] == PackageManager.PERMISSION_GRANTED) ) {
                    // Permission Granted
                    initLocationUpdater();
                } else {
                    Log.e(TAG, "Permission Denied --- Failed To Request Location Permission");
                }
                break;
            case REQUEST_STORAGE_CODE:
                /*if ( (permissions.length == 1) && (permissions[1] == android.Manifest.permission.ACCESS_FINE_LOCATION) && (grantResults[1] == PackageManager.PERMISSION_GRANTED) ) {
                    Log.e(TAG, "Request Storage Code Granted");
                }*/
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected void requestPermissionOnLaunch () {
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            Log.e(TAG, "On Map Ready: Requesting Location Service Permission");
            ActivityCompat.requestPermissions(BlocSpotMainActivity.this,
                                              new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
                                                           android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                              REQUEST_LOCATION_CODE);

        } else {
            // Show rationale and request permission.
            Log.e(TAG, "Location Permissions Not Requested");
        }
    }

    public void requestLocationUpdateIfNeeded () {
        try {
            if ( mLastLocation != null ) {
                Log.e(TAG, "Location is NOT null --- Trying to getLastLocation");
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                Log.e(TAG, "Location Request Update Succeeded ---");
            } else {
                Log.e(TAG, "mLastLocation is NULL --- Initiating Location Request --- ");
                initLocationUpdater();
            }
        } catch ( SecurityException sE ) {
            Log.e(TAG, "mLastLocation Security Exception : ");
            sE.printStackTrace();
        }
    }


    @Override
    public void onMapReady (GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // doesnt seem to be working ????
        try {
            mMap.setMyLocationEnabled(true);
            initLocationUpdater();
            Log.e(TAG, "mMap.isMyLocationEnabled: " + mMap.isMyLocationEnabled());
        } catch ( SecurityException sE ) {
            Log.e(TAG, "mMap.setMyLocationEnabled: " + mMap.isMyLocationEnabled());
            sE.printStackTrace();
        }
    }


    private void showMessageOKCancel (String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this).setMessage(message)
                                     .setPositiveButton("OK", okListener)
                                     .setNegativeButton("Cancel", null)
                                     .create()
                                     .show();
    }

    private void setUpMapIfNeeded () {
        if ( mMap == null ) {
            mMapFragment = ( MapFragment ) getFragmentManager().findFragmentById(R.id.main_activity_map_fragment);
            mMapFragment.getMapAsync(this);
        }
    }


    @Override
    public void onLocationChanged (Location location) {
        if ( location != null && mLastLocation != null && mDefaultMarker != null ) {
            Log.e(TAG, "Location Compare: \nmLastLocation:\t" + mLastLocation.toString()
                       + " \nmNewwLocation:\t" + location.toString());

            mDefaultMarker.remove(); // the current location of the user
            Log.e(TAG, "mDefaultMarker Removed");

            if ( mDefaultZeroMarker != null ) {
                mDefaultZeroMarker.remove(); // Default meridian, equator  ( 0, 0 )
                Log.e(TAG, "mDefault_Zero_Marker Removed");
            }
        }
        handleNewLocation(location);
    }

    private void handleNewLocation (Location location) {
        if ( location != null ) {
            Log.e(TAG, location.toString());
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            // default marker setting plus default icon setting from google maps

            mDefaultMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                                                               .position(latLng)
                                                               .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            // Move the camera to Device Location (or last known location)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULTZOOM));
        } else {
            Log.e(TAG, "Location is NULL");
            /*Log.e(TAG, "Default Location ( 0, 0 )");
            // default to ( 0, 0 ) lat long location
            mDefaultZeroMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                                                                   .title("Default Location")
                                                                   .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            Log.e(TAG, "mDeafult_Zero_Marker Added");*/
        }
    }


    public boolean servicesOK () {
        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if ( isAvailable == ConnectionResult.SUCCESS ) {
            Log.e(TAG, "ServiceOK is CONNECTED : SUCCESS = " + isAvailable); // SUCCESS = 0
            return true;
        } else if ( GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) ) {
            Dialog dialog = GoogleApiAvailability.getInstance()
                                                 .getErrorDialog(this, isAvailable, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "ServiceOK is NOT CONNECTED : FAILURE = " + isAvailable);
        }
        return false;
    }


    /**
     * @param vectorDrawable
     * @return
     */
    public Bitmap convertBitMap (int vectorDrawable) {
        Bitmap   mapPin;
        Drawable mapPinDrawable = getResources().getDrawable(vectorDrawable);
        Bitmap bitmap = Bitmap.createBitmap(mapPinDrawable.getIntrinsicWidth(),
                                            mapPinDrawable.getIntrinsicHeight(),
                                            Bitmap.Config.ARGB_8888); // can modify size here
        Canvas canvas = new Canvas(bitmap);
        mapPinDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        mapPinDrawable.draw(canvas);
        mapPin = bitmap;
        return mapPin;
    }

    public void setConvertedBitMap () {

        // Map Pins
        mapPinBitMap = convertBitMap(R.drawable.black_map_pin_full);
        redHeartBitMap = convertBitMap(R.drawable.red_like_heart);
        greenHeartBitMap = convertBitMap(R.drawable.green_like_heart);
        blueHeartBitMap = convertBitMap(R.drawable.blue_like_heart);

        // Regular App Icon
        /*
        listViewBtnIconBitMap = convertBitMap(R.drawable.listview_icon_btn);
        searchIconBitMap = convertBitMap(R.drawable.search_icon);
        filterIconBitMap = convertBitMap(R.drawable.filter_tune_icon);
        ratingStarIconBitMap = convertBitMap(R.drawable.rating_star_icon);
        deleteBtnIconBitMap = convertBitMap(R.drawable.delete_btn_icon);
        navigateBtnIconBitMap = convertBitMap(R.drawable.navigate_btn_icon);
        shareBtnIconBitMap = convertBitMap(R.drawable.share_btn_icon);
        */
    }


    /**
     * ALL MENU OPTIONS HERE
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.blocspot_menu, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.list_item_btn:
                Toast.makeText(BlocSpotMainActivity.this, "List View Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.filter_item:
                Toast.makeText(BlocSpotMainActivity.this, "Filter Button Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.search_btn:
                Toast.makeText(BlocSpotMainActivity.this, "Search Button Clicked", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void realmStuffTest () {
        PointOfInterestModel poiPlace = new PointOfInterestModel();
        poiPlace.setId("001");
        poiPlace.setPlaceName("MY Place");
        poiPlace.setPlaceAddress("My Address");

        realmObj = Realm.getDefaultInstance();
        realmObj.beginTransaction();
        realmObj.copyToRealmOrUpdate(poiPlace);
        realmObj.commitTransaction();


        realmObj.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute (Realm bgRealm) {
                PointOfInterestModel poi = bgRealm.createObject(PointOfInterestModel.class);
                poi.setId("003");
                poi.setPlaceName("My Third Place");
                poi.setPlaceAddress("My Third Address");
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess () {
                Toast.makeText(BlocSpotMainActivity.this, "address added to realm", Toast.LENGTH_SHORT).show();
                RealmResults<PointOfInterestModel> results = realmObj.where(PointOfInterestModel.class)
                                                                     .findAll();
                Log.e(TAG, "Realm Database Items: " + String.valueOf(results.size()));

            }

            @Override
            public void onError (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
