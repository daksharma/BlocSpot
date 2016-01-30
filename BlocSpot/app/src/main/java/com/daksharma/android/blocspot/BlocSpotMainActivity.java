package com.daksharma.android.blocspot;

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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class BlocSpotMainActivity extends AppCompatActivity implements OnMapReadyCallback,
                                                                       GoogleApiClient.ConnectionCallbacks,
                                                                       GoogleApiClient.OnConnectionFailedListener,
                                                                       LocationListener,
                                                                       ActivityCompat.OnRequestPermissionsResultCallback {

    public final static String TAG = "blocspot." + BlocSpotMainActivity.class.getSimpleName().toUpperCase();


    private Realm realmObj;


    private GoogleMap       mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location        mLastLocation;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;


    private MapFragment mMapFragment;

    private static final int GPS_ERRORDIALOG_REQUEST = 0;
    private static final int REQUEST_LOCATION_CODE   = 2;
    //private final        int locationPermissionCode  = ContextCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION");


    private Menu menu;

    private Bitmap mapPinBitMap;
    private Bitmap redHeartBitMap;
    private Bitmap greenHeartBitMap;
    private Bitmap blueHeartBitMap;
    private Bitmap listViewBtnIconBitMap;
    private Bitmap searchIconBitMap;
    private Bitmap filterIconBitMap;
    private Bitmap ratingStarIconBitMap;
    private Bitmap navigateBtnIconBitMap;
    private Bitmap shareBtnIconBitMap;
    private Bitmap deleteBtnIconBitMap;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                                                            .addOnConnectionFailedListener(this)
                                                            .addApi(LocationServices.API)
                                                            .build();
        mLocationRequest = LocationRequest.create()
                                          .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                          .setInterval(10 * 1000)
                                          .setFastestInterval(1000);
        setConvertedBitMap();

        if ( servicesOK() ) {
            setUpMapIfNeeded();
        }

        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);

        realmStuffTest();

    }

    @Override
    protected void onResume () {
        super.onResume();
        if (mMap == null ) {
            setUpMapIfNeeded();
        }
        MapStateManager msManager = new MapStateManager(this);
        CameraPosition position = msManager.getSavedCameraPosition();
        if (position != null && mMap != null) {
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
    protected void onStop() {
        super.onStop();
        MapStateManager msManager = new MapStateManager(this);
        msManager.saveMapState(mMap);
    }

    @Override
    public Resources getResources () {
        return super.getResources();
    }

    @Override
    public void onConnected (Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if ( (mLastLocation == null) && (mGoogleApiClient.isConnected()) && (ContextCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED) ) {
            Log.e(TAG, mLastLocation.toString());
            Log.e(TAG, "mGoogleAPIclient Connected: " + mGoogleApiClient.isConnected());
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(mLastLocation);
        }
        Log.e(TAG, "Services CONNECTED.");
        Toast.makeText(this, "Service CONNECTED", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) { // returns invalid array
        switch ( requestCode ) {
            case REQUEST_LOCATION_CODE:
                if ( (grantResults.length == 1) && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    // Permission Granted
                    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    Log.e(TAG, "Permission Granted");
                } else {
                    // Permission Denied
                    Log.e(TAG, "Permission Denied");
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public void onMapReady (GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
        if ( ContextCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
        } else {
            // Show rationale and request permission.
            Log.e(TAG, "On Map Ready: Request Location Service Permission");
            showMessageOKCancel("Location Service Required",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick (DialogInterface dialog, int which) {
                                        ActivityCompat.requestPermissions(BlocSpotMainActivity.this,
                                                                          new String[]{"Manifest.permission.ACCESS_FINE_LOCATION"},
                                                                          REQUEST_LOCATION_CODE);


                                    }
                                });
            //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker")); // default works
            if ( mLastLocation != null )

            {
                mMap.addMarker(new MarkerOptions().position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                                                  .title("Current Location")
                                                  .icon(BitmapDescriptorFactory.fromBitmap(redHeartBitMap)));
            } else

            {
                mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                                                  .title("Default Location")
                                                  .icon(BitmapDescriptorFactory.fromBitmap(redHeartBitMap)));
            }
        }



        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, "Manifest.permission.ACCESS_FINE_LOCATION") != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, "Manifest.permission.ACCESS_FINE_LOCATION")) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                                                  new String[]{"Manifest.permission.ACCESS_FINE_LOCATION"},
                                                  REQUEST_LOCATION_CODE);
            }
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
        handleNewLocation(location);
    }

    private void handleNewLocation (Location location) {
        if ( location != null ) {
            Log.e(TAG, location.toString());
            double currentLatitude = location.getLatitude();
            double currentLongitude = location.getLongitude();
            LatLng latLng = new LatLng(currentLatitude, currentLongitude);
            //mMap.addMarker(new MarkerOptions().position(new LatLng(currentLatitude, currentLongitude)).title("Current Location"));
            MarkerOptions options = new MarkerOptions().position(latLng)
                                                       .title("I am here!");
            mMap.addMarker(options);

            // Move the camera to Device Location (or last known location)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        } else {
            Log.e(TAG, "LOCATION NULL");
        }
    }


    public boolean servicesOK () {
        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if ( isAvailable == ConnectionResult.SUCCESS ) {
            Log.e(TAG, "ServiceOK: SUCCESS = " + isAvailable); // SUCCESS = 0 :
            return true;
        } else if ( GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) ) {
            Dialog dialog = GoogleApiAvailability.getInstance()
                                                 .getErrorDialog(this, isAvailable, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "ServiceOK: FAILURE = " + isAvailable);
            Toast.makeText(this, "Google Play Services NOT CONNECTED", Toast.LENGTH_SHORT).show();
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
                Log.e("BlocSpotMainActivity: ", String.valueOf(results.size()));

            }

            @Override
            public void onError (Exception e) {
                e.printStackTrace();
            }
        });
    }


}
