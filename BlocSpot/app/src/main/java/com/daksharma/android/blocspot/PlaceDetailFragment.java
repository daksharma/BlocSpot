package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private TextView  detailPlaceDescription;

    @Override
    public void onCreate (Bundle savedInstanceState) {

    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.place_detail_fragment, container, false);

        detailPlaceStreetImage = ( ImageView ) view.findViewById(R.id.detail_place_street_image);
        detailPlaceTitle = (TextView) view.findViewById(R.id.detail_place_title);
        detailPlaceAddress = (TextView) view.findViewById(R.id.detail_place_address);
        detailPlaceDescription = (TextView) view.findViewById(R.id.detail_place_description);

        return view;
    }


}
