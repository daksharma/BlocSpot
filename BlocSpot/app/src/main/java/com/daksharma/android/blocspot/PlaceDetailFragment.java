package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daksharma.android.blocspot.model.PointOfInterestModel;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Daksh on 2/4/16.
 */
public class PlaceDetailFragment extends Fragment {

    private final static String TAG = PlaceDetailFragment.class.getSimpleName().toUpperCase();

    private ImageView detailPlaceStreetImage;
    private TextView  detailPlaceTitle;
    private TextView  detailPlaceAddress;
    private TextView  detailPlaceRating;
    //private TextView  detailPlaceDescription; Place Api does not have this in the result
    private EditText  detailPlaceEditNotes;
    private EditText  detailPlaceEditCategory;
    private CheckBox  detailPlaceVisitedButton;
    private Button    detailPlaceSaveButton;


    private String  placeId;
    private String  placeNameTitle;
    private String  placeNameAddress;
    private float   placeRating;
    private double  placeLatitude;
    private double  placeLongitude;
    private String  placeCategory;
    private boolean placeVisited;
    private String  placeUserNotes;


    private boolean isArgumentsEmpty;
    private boolean userNotesAdded;
    private boolean userCategoryAdded;

    private Realm realmObj;


    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_detail_fragment, container, false);

        detailPlaceStreetImage = ( ImageView ) view.findViewById(R.id.detail_place_street_image);
        detailPlaceTitle = ( TextView ) view.findViewById(R.id.detail_place_title);
        detailPlaceAddress = ( TextView ) view.findViewById(R.id.detail_place_address);
        detailPlaceRating = ( TextView ) view.findViewById(R.id.place_detail_rating);
        detailPlaceEditNotes = ( EditText ) view.findViewById(R.id.place_detail_edit_notes);
        detailPlaceEditCategory = ( EditText ) view.findViewById(R.id.place_detail_edit_category);
        detailPlaceVisitedButton = ( CheckBox ) view.findViewById(R.id.place_detail_place_visited_button);
        detailPlaceSaveButton = ( Button ) view.findViewById(R.id.place_detail_save_button);


        getArgumentsFromBungle();

        saveButtonSetUp();


        return view;
    }


    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {

    }


    public void getArgumentsFromBungle () {
        if ( getArguments() != null ) {
            placeId = getArguments().getString("PlaceId");
            placeNameTitle = getArguments().getString("PlaceName");
            placeNameAddress = getArguments().getString("PlaceAddress");
            placeRating = getArguments().getFloat("PlaceRating") * 10;
            placeLatitude = getArguments().getDouble("PlaceLatitude");
            placeLongitude = getArguments().getDouble("PlaceLongitude");


            if ( detailPlaceVisitedButton.isSelected() ) {
                placeVisited = detailPlaceVisitedButton.isSelected();
            } else {
                placeVisited = false;
            }


            detailPlaceTitle.setText(placeNameTitle);
            detailPlaceAddress.setText(placeNameAddress);
            detailPlaceRating.setText("Rating: " + placeRating);

            isArgumentsEmpty = false; // getArguments are not null and not empty (data is retrieved)

        } else {
            isArgumentsEmpty = true; // getArguments are null or empty. ( data is not retrieved )
        }
    }


    public void saveButtonSetUp() {
        detailPlaceSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                Log.e(TAG, "Add to Favorite BUTTON CLICKED");

                if ( detailPlaceEditCategory.length() > 0 ) {
                    placeCategory = detailPlaceEditCategory.getText().toString();
                    userCategoryAdded = true;
                } else {
                    userCategoryAdded = false;
                    Log.e(TAG, "Category is EMPTY");
                }

                if ( detailPlaceEditNotes.length() > 0 ) {
                    placeUserNotes = detailPlaceEditNotes.getText().toString();
                    userNotesAdded = true;
                } else {
                    userNotesAdded = false;
                    Log.e(TAG, "Notes are EMPTY");
                }


                if (!isArgumentsEmpty && userCategoryAdded != false && userNotesAdded != false) {
                    addPlaceDetailToRealmDB();
                } else {
                    Toast.makeText(getActivity(), "Notes and Category cannot be empty", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Notes and Category are EMPTY");
                }
            }
        });
    }


    public void addPlaceDetailToRealmDB () {
        realmObj = Realm.getDefaultInstance();
        realmObj.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute (Realm bgRealm) {
                PointOfInterestModel poi = bgRealm.createObject(PointOfInterestModel.class);
                poi.setPlaceId(placeId);
                poi.setPlaceName(placeNameTitle);
                poi.setPlaceAddress(placeNameAddress);
                poi.setPlaceRating(placeRating);
                poi.setmLatitude(placeLatitude);
                poi.setmLongitude(placeLongitude);
                poi.setmPlaceVisited(placeVisited);
                poi.setUserNotes(placeUserNotes);
                poi.setmPlaceCategory(placeCategory);
            }
        }, new Realm.Transaction.Callback() {
            @Override
            public void onSuccess () {
                Toast.makeText(getActivity(), "Place address added to realm", Toast.LENGTH_SHORT).show();
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
