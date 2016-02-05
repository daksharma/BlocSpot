package com.daksharma.android.blocspot;

import android.*;
import android.app.Dialog;
import android.app.Fragment;


import android.app.FragmentManager;
import android.app.PendingIntent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.FragmentActivity;

import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Daksh on 2/4/16.
 */
public class MainMapFragment extends Fragment implements OnMapReadyCallback,
                                                         GoogleApiClient.ConnectionCallbacks,
                                                         GoogleApiClient.OnConnectionFailedListener,
                                                         LocationListener,
                                                         ActivityCompat.OnRequestPermissionsResultCallback {

    private final static String TAG = MainMapFragment.class.getSimpleName().toUpperCase();


    private boolean gpsEnabled  = false;
    private boolean wifiEnabled = false;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location        mLastLocation;

    // Google Map
    private GoogleMap mMap;

    private static final float DEFAULTZOOM = 15;

    // Map Fragments
    private MapFragment mMapFragment;

    // Map Markers
    private Marker mUserLocationMarker;
    private Marker mDefaultZeroMarker;
    // Map Marker Images
    private Bitmap mapPinBitMap;
    private Bitmap redHeartBitMap;
    private Bitmap greenHeartBitMap;
    private Bitmap blueHeartBitMap;


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final int GPS_ERRORDIALOG_REQUEST               = 0;
    private static final int REQUEST_LOCATION_CODE                 = 2;
    private static final int REQUEST_STORAGE_CODE                  = 3;
    private static final int PLACE_PICKER_CODE                     = 4;


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_map_fragment, container, false);

        buildGoogleApiClient();
        if ( servicesOK() ) {
            setUpMapIfNeeded();
        } else {
            Log.e(TAG, "Services NOT OK: Map not set-up");
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mMap == null)  {
            setUpMapIfNeeded();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ( servicesOK() ) {
            setUpMapIfNeeded();
        } else {
            Log.e(TAG, "Services NOT OK: Map not set-up");
        }

    }

    @Override
    public void onResume () {
        super.onResume();
        if ( mMap == null ) {
            setUpMapIfNeeded();
        }
        MapStateManager msManager = new MapStateManager(getActivity());
        CameraPosition  position  = msManager.getSavedCameraPosition();
        if ( position != null && mMap != null ) {
            CameraUpdate camUpdate = CameraUpdateFactory.newCameraPosition(position);
            mMap.moveCamera(camUpdate);
        }
        mGoogleApiClient.connect();
    }


    @Override
    public void onPause () {
        super.onPause();
        if ( mGoogleApiClient.isConnected() ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop () {
        MapStateManager msManager = new MapStateManager(getActivity());
        msManager.saveMapState(mMap);
        if ( (mGoogleApiClient != null) && (mGoogleApiClient.isConnected()) ) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    private void setUpMapIfNeeded () {
        if ( mMap == null ) {
            //mMapFragment = ( MapFragment ) getFragmentManager().findFragmentById(R.id.main_map_fragment);
            FragmentManager fm = getChildFragmentManager();
            mMapFragment = (MapFragment) fm.findFragmentById(R.id.main_map_fragment);
            if (mMapFragment == null ) {
                mMapFragment.newInstance();
                fm.beginTransaction().replace(R.id.content_fragment, mMapFragment).commit();
            }
            if (mMapFragment != null) {
                mMapFragment.getMapAsync(this);
            }
        }
    }


    @Override
    public void onMapReady (GoogleMap map) {
        if (mMapFragment != null) {
            mMap = map;
            if (mMap != null) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                mMap.setIndoorEnabled(false);
                mMap.setTrafficEnabled(false);
                try {
                    // check if location is enabled based on wifi or gps
                    if ( gpsEnabled || wifiEnabled ) {
                        mMap.getUiSettings().setMyLocationButtonEnabled(true); // doesnt seem to be working ????
                        mMap.setMyLocationEnabled(true);
                        initLocationUpdater();
                        Log.e(TAG, "mMap.isMyLocationEnabled: " + mMap.isMyLocationEnabled());
                    } else {
                        Log.e(TAG, "Location toggle is NOT Enabled");
                    }
                } catch ( SecurityException sE ) {
                    Log.e(TAG, "mMap.setMyLocationEnabled: " + mMap.isMyLocationEnabled());
                    sE.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onLocationChanged (Location location) {
        if ( (mMapFragment != null) && (location != null) && (mUserLocationMarker != null) ) {
            Log.e(TAG, "Location Compare: \nmLastLocation:\t" + mLastLocation.toString()
                       + " \nmNewwLocation:\t" + location.toString());

            mUserLocationMarker.remove(); // the current location of the user
            Log.e(TAG, "mUserLocationMarker Removed");
            handleNewLocation(location);
            Log.e(TAG, "mUserLocationMarker UPDATED");
        }
        if ( mDefaultZeroMarker != null ) {
            mDefaultZeroMarker.remove(); // Default meridian, equator  ( 0, 0 )
            Log.e(TAG, "mDefault_Zero_Marker Removed");
        }

    }

    private void handleNewLocation (Location location) {
        if ( location != null && mMapFragment != null) {
            Log.e(TAG, location.toString());
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // default marker setting plus default icon setting from google maps

            mUserLocationMarker = mMap.addMarker(new MarkerOptions().title("Current Location")
                                                                    .position(latLng)
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            // Move the camera to Device Location (or last known location)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULTZOOM));
        } else if ( mMapFragment != null && location == null) {
            Log.e(TAG, "Location is NULL --- resorting to default location");
            Log.e(TAG, "Default Location ( 0, 0 )");
            // default to ( 0, 0 ) lat long location
            mDefaultZeroMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                                                                   .title("Default Location")
                                                                   .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            Log.e(TAG, "mDeafult_Zero_Marker Added");
        } else {
            Log.e(TAG, "Something with Location went horribly wrong");
        }
    }


    private void initLocationUpdater () {
        if ( ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
             && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && mGoogleApiClient != null ) {
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

            if ( mLastLocation != null && servicesOK() ) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
            handleNewLocation(mLastLocation);

        } else {
            Log.e(TAG, "Location Permission not granted\nmLocationRequest is not created");
            requestPermissionOnLaunch();
        }
    }

    protected synchronized void buildGoogleApiClient () {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API)
                                                                     .addApi(Places.GEO_DATA_API)
                                                                     .addApi(Places.PLACE_DETECTION_API)
                                                                     .addConnectionCallbacks(this)
                                                                     .addOnConnectionFailedListener(this)
                                                                     .build();
        mGoogleApiClient.connect();
    }

    public boolean googleApiClientConnected() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            return true;
        }else {
            buildGoogleApiClient();
        }
        return false;
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
        if ( ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            Log.e(TAG, "On Map Ready: Requesting Location Service Permission");
            ActivityCompat.requestPermissions(getActivity(),
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

    public boolean servicesOK () {
        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getActivity());

        if ( isAvailable == ConnectionResult.SUCCESS ) {
            Log.e(TAG, "ServiceOK is CONNECTED : SUCCESS = " + isAvailable); // SUCCESS = 0
            return true;
        } else if ( GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) ) {
            Dialog dialog = GoogleApiAvailability.getInstance()
                                                 .getErrorDialog(getActivity(), isAvailable, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Log.e(TAG, "ServiceOK is NOT CONNECTED : FAILURE = " + isAvailable);
        }
        return false;
    }


    @Override
    public void onConnected (Bundle connectionHint) {
        if ( (connectionHint != null) && (mLastLocation == null) && (mGoogleApiClient.isConnected()) ) {
            Log.e(TAG, "mGoogleApiClient Connected ? : " + mGoogleApiClient.isConnected());
            Log.e(TAG, "Requesting Location Services --- ");

            LocationManager userGpsEnabled = ( LocationManager ) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            gpsEnabled = userGpsEnabled.isProviderEnabled(LocationManager.GPS_PROVIDER);
            wifiEnabled = userGpsEnabled.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.e(TAG, "GPS_Enabled: " + gpsEnabled + "\nWifi_Enabled: " + wifiEnabled);

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
        Toast.makeText(getActivity(), "Connection SUSPENDED", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed (ConnectionResult connectionResult) {
        Log.e(TAG, "Services connection FAILED.");
        Toast.makeText(getActivity(), "Connection FAILED", Toast.LENGTH_SHORT).show();

        if ( connectionResult.hasResolution() ) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(getActivity(), CONNECTION_FAILURE_RESOLUTION_REQUEST);
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


}


