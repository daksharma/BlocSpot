package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
    private Button    detailPlaceVisitedButton;
    private Button    detailPlaceSaveButton;


    private String  placeId;
    private String  placeNameTitle;
    private String  placeNameAddress;
    private float   placeRating;
    private double  placeLatitude;
    private double  placeLongitude;
    private boolean placeVisited;
    private String  placeUserNotes;


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
        detailPlaceVisitedButton = (Button) view.findViewById(R.id.place_detail_place_visited_button);
        detailPlaceSaveButton = ( Button ) view.findViewById(R.id.place_detail_save_button);


        if ( getArguments() != null ) {
            placeId = getArguments().getString("PlaceId");
            placeNameTitle = getArguments().getString("PlaceName");
            placeNameAddress = getArguments().getString("PlaceAddress");
            placeRating = getArguments().getFloat("PlaceRating");
            placeLatitude = getArguments().getDouble("PlaceLatitude");
            placeLongitude = getArguments().getDouble("PlaceLongitude");


            detailPlaceTitle.setText(placeNameTitle);
            detailPlaceAddress.setText(placeNameAddress);
            detailPlaceRating.setText("Rating: " + placeRating*10);


        }
        
        return view;
    }


    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {

    }

}
