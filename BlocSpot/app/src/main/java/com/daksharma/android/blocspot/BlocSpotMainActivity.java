package com.daksharma.android.blocspot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;
import com.google.android.gms.maps.GoogleMap;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class BlocSpotMainActivity extends AppCompatActivity {

    private Realm realmObj;
    GoogleMap mMap;


    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloc_spot_main);

        RealmConfiguration config = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(config);

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
