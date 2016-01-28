package com.daksharma.android.blocspot;

import android.app.Dialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class BlocSpotMainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Realm     realmObj;
    private GoogleMap mMap;

    private MapFragment mMapFragment;

    private static final int GPS_ERRORDIALOG_REQUEST = 0;


    private Bitmap mapPinBitMap;
    private Bitmap redHeartBitMap;
    private Bitmap greenHeartBitMap;
    private Bitmap blueHeartBitMap;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        setConvertedBitMap();

        servicesOK();
        mMapFragment = ( MapFragment ) getFragmentManager().findFragmentById(R.id.main_activity_map_fragment);
        mMapFragment.getMapAsync(this);


        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);

        realmStuffTest();

    }

    @Override
    public Resources getResources () {
        return super.getResources();
    }


    @Override
    public void onMapReady (GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setIndoorEnabled(false);
        mMap.setTrafficEnabled(false);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker")); // default works
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0))
                                          .title("CustomMarker")
                                          .icon(BitmapDescriptorFactory.fromBitmap(redHeartBitMap)));
    }


    public boolean servicesOK () {
        int isAvailable = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if ( isAvailable == ConnectionResult.SUCCESS ) {
            return true;
        } else if ( GoogleApiAvailability.getInstance().isUserResolvableError(isAvailable) ) {
            Dialog dialog = GoogleApiAvailability.getInstance()
                                                 .getErrorDialog(this, isAvailable, GPS_ERRORDIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Unable to connect to Google Play Services", Toast.LENGTH_SHORT).show();
        }
        return false;
    }


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
        mapPinBitMap = convertBitMap(R.drawable.black_map_pin_full);
        redHeartBitMap = convertBitMap(R.drawable.red_like_heart);
        greenHeartBitMap = convertBitMap(R.drawable.green_like_heart);
        blueHeartBitMap = convertBitMap(R.drawable.blue_like_heart);
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
