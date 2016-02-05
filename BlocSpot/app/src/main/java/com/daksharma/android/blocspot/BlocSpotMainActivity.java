package com.daksharma.android.blocspot;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
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

public class BlocSpotMainActivity extends AppCompatActivity implements PlaceSelectionListener {

    public final static String TAG = BlocSpotMainActivity.class.getSimpleName().toUpperCase();


    private Realm realmObj;

    private static final int PLACE_PICKER_CODE = 4;


    private Menu menu;

    MainMapFragment mapFragObject;


    MapFragment mMainMapFragment;
    Fragment mPlaceDetailFragment;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        setUpToolBar();

        mMainMapFragment = new MainMapFragment();
        mPlaceDetailFragment = new PlaceDetailFragment();

        getFragmentManager().beginTransaction().add(R.id.content_fragment, mMainMapFragment, "MainMap").commit();


        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);
        realmStuffTest();

    }

    @Override
    public Resources getResources () {
        return super.getResources();
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

    public void setUpToolBar () {
        Toolbar toolbar = ( Toolbar ) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.list_item_btn:
                Toast.makeText(BlocSpotMainActivity.this, "List View Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.filter_item_button:
                Toast.makeText(BlocSpotMainActivity.this, "Filter Button Clicked", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.search_btn:
                Log.e(TAG, "Search Button Tapped");
                searchForPlace();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Launch the Place Search Activity
     * @link https://developers.google.com/places/android-api/autocomplete
     */
    public void searchForPlace () {

        if ( mapFragObject.googleApiClientConnected() ) {
            try {
                Intent placeIntent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this);
                startActivityForResult(placeIntent, PLACE_PICKER_CODE);
            } catch ( GooglePlayServicesNotAvailableException gPlayErr ) {
                Log.e(TAG, "Play Service Not Available ---");
                gPlayErr.printStackTrace();
            } catch ( GooglePlayServicesRepairableException repairableErr ) {
                Log.e(TAG, "Play Service Repairable Exception ---");
                repairableErr.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }



    public void realmStuffTest () {
        PointOfInterestModel poiPlace = new PointOfInterestModel();
        poiPlace.setPlaceId("001");
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
                poi.setPlaceId("003");
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


    @Override
    public void onPlaceSelected (Place place) {

    }

    @Override
    public void onError (Status status) {

    }
}
