package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.daksharma.android.blocspot.model.PointOfInterestModel;
import com.daksharma.android.blocspot.ui.adapter.PoiCardViewAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Daksh on 2/4/16.
 */
public class CardsRecyclerViewFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private final static String TAG = CardsRecyclerViewFragment.class.getSimpleName().toUpperCase();

    private RecyclerView               mRecyclerView;
    private PoiCardViewAdapter         mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Spinner categorySelectionSpinner;

    @Override
    public void onCreate (Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recycler_view_adapter, container, false);


        mRecyclerView = ( RecyclerView ) rootView.findViewById(R.id.recycler_view_adapter);
        categorySelectionSpinner = ( Spinner ) rootView.findViewById(R.id.category_selection_spinner);
        categorySelectionSpinner.setOnItemSelectedListener(this);


        Realm                              newRealm = Realm.getDefaultInstance();
        RealmResults<PointOfInterestModel> poiData  = newRealm.where(PointOfInterestModel.class).findAll();

        // Extract category strings from poiData into a new set
        HashSet<String> catName = new HashSet<>();
        catName.add("All Categories");
        for ( PointOfInterestModel p : poiData ) {
            catName.add(p.getmPlaceCategory());
        }


        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter(getActivity(), R.layout.category_item);
        spinnerAdapter.addAll(catName);
        int allCategoriValue = spinnerAdapter.getPosition("All Categories");
        categorySelectionSpinner.setAdapter(spinnerAdapter);
        categorySelectionSpinner.setSelection(allCategoriValue);

        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerViewAdapter = new PoiCardViewAdapter(poiData);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mRecyclerViewAdapter);
        // Markers can be updated with
        //BlocSpotMainActivity.mapMarkers.add(marker);

        return rootView;
    }

    public RealmResults<PointOfInterestModel> categoryResult (String catName) {
        Realm catResultRealm = Realm.getDefaultInstance();
        RealmResults<PointOfInterestModel> catResult;
        if ( !catName.equals("All Categories")) {
            catResult = catResultRealm.where(PointOfInterestModel.class)
                                      .equalTo("mPlaceCategory", catName)
                                      .findAll();
        } else {
            catResult = catResultRealm.where(PointOfInterestModel.class).findAll();
        }
        return catResult;
    }


    @Override
    public void onItemSelected (AdapterView<?> parent, View view, int position, long id) {

        Log.e(TAG, "AdapterView Parent Position String: " + parent.getItemAtPosition(position));

        if ( !parent.getItemAtPosition(position).equals("All Categories") ) {
            mRecyclerViewAdapter = new PoiCardViewAdapter(categoryResult(parent.getItemAtPosition(position).toString()));
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Category Selection : " + parent.getItemAtPosition(position));
            mRecyclerViewAdapter = new PoiCardViewAdapter(categoryResult("All Categories"));
            mRecyclerView.setAdapter(mRecyclerViewAdapter);
            mRecyclerViewAdapter.notifyDataSetChanged();
        }


        // Re-query poidata with specified category in the where method
        // update the recyclerViewAdapter with new poiData
        // adapter.notifyDataSetChanged()
    }

    @Override
    public void onNothingSelected (AdapterView<?> parent) {

    }
}
