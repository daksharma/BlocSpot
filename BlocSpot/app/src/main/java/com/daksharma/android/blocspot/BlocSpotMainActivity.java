package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;


import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class BlocSpotMainActivity extends AppCompatActivity implements PlaceSelectionListener {

    public final static String TAG = BlocSpotMainActivity.class.getSimpleName().toUpperCase();


    private Realm realmObj;

    private static final int PLACE_PICKER_CODE = 4;


    private Menu menu;


    Fragment mMainMapFragment;
    Fragment mPlaceDetailFragment;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        setUpToolBar();

        mMainMapFragment = new MainMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_fragment, mMainMapFragment).commit();


        RealmConfiguration config = new RealmConfiguration.Builder(this).deleteRealmIfMigrationNeeded()
                                                                        .build();
        Realm.setDefaultConfiguration(config);
        realmStuffTest();

    }

    public void showMapFragment() {
        mMainMapFragment = new MainMapFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_fragment, mMainMapFragment).commit();
    }

    public void showPlaceListFragment() {
        mPlaceDetailFragment = new PlaceDetailFragment();
        getFragmentManager().beginTransaction().replace(R.id.content_fragment, mPlaceDetailFragment).commit();
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
                Log.e(TAG, "List Button Tapped");
                return true;
            case R.id.show_map_view:
                showMapFragment();
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
     *
     * @link https://developers.google.com/places/android-api/autocomplete
     */
    public void searchForPlace () {
        //boolean apiEnabled = mapFragObject.servicesOK();
        //if ( apiEnabled ) {
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
        //}
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if ( requestCode == PLACE_PICKER_CODE ) {
            if ( resultCode == RESULT_OK ) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                if (place != null) {
                    placeResultArgumentToFragment(place);
                } else {
                    Log.e(TAG, "Place Result is NULL");
                }
            } else if ( resultCode == PlaceAutocomplete.RESULT_ERROR ) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if ( resultCode == RESULT_CANCELED ) {
                // The user canceled the operation.
            }
        }
    }

    public void placeResultArgumentToFragment (Place place) {
        Log.i(TAG, "Place: " + place.getName());
        mPlaceDetailFragment = new PlaceDetailFragment();
        Bundle mPlaceDetailArgs = new Bundle();
        mPlaceDetailArgs.putString("PlaceId", place.getId());
        mPlaceDetailArgs.putCharSequence("PlaceName", place.getName());
        mPlaceDetailArgs.putCharSequence("PlaceAddress", place.getAddress());
        mPlaceDetailArgs.putFloat("PlaceRating", place.getRating());
        mPlaceDetailArgs.putDouble("PlaceLatitude", place.getLatLng().latitude);
        mPlaceDetailArgs.putDouble("PlaceLongitude", place.getLatLng().longitude);
        mPlaceDetailFragment.setArguments(mPlaceDetailArgs);
        getFragmentManager().beginTransaction()
                            .replace(R.id.content_fragment, mPlaceDetailFragment)
                            .commit();
    }

    @Override
    public void onPlaceSelected (Place place) {

    }

    @Override
    public void onError (Status status) {

    }


    public void realmStuffTest () {
        final PointOfInterestModel poiPlace = new PointOfInterestModel();
        poiPlace.setPlaceId("001");
        poiPlace.setPlaceName("MY Place");
        poiPlace.setPlaceAddress("My Address");
        poiPlace.setPlaceRating(4.5f);
        poiPlace.setmLatitude(12.093485);
        poiPlace.setmLongitude(12.093485);
        poiPlace.setmPlaceVisited(false);
        poiPlace.setUserNotes("Some Kind of Note");

        realmObj = Realm.getDefaultInstance();
        realmObj.beginTransaction();
        realmObj.copyToRealmOrUpdate(poiPlace);
        realmObj.commitTransaction();


        realmObj.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute (Realm bgRealm) {
                PointOfInterestModel poi = bgRealm.createObject(PointOfInterestModel.class);
                poi.setPlaceId("002");
                poi.setPlaceName("My Third Place");
                poi.setPlaceAddress("My Third Address");
                poi.setPlaceRating(3.6f);
                poi.setmLatitude(12.12345);
                poi.setmLongitude(12.12345);
                poi.setmPlaceVisited(true);
                poi.setUserNotes("Some other kind of note");
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
