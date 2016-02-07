package com.daksharma.android.blocspot.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.daksharma.android.blocspot.R;
import com.daksharma.android.blocspot.model.PointOfInterestModel;

import java.util.List;

import io.realm.RealmResults;

/**
 * Created by Daksh on 1/28/16.
 */
public class PoiCardViewAdapter extends RecyclerView.Adapter<PoiCardViewAdapter.PoiCardsViewHolder> {

    public final static String TAG = PoiCardsViewHolder.class.getSimpleName().toUpperCase();

    private List<PointOfInterestModel> poiData; // will contain the data for the poi items

    public PoiCardViewAdapter (List<PointOfInterestModel> listItems) {
        this.poiData = listItems;
    }

    @Override
    public PoiCardsViewHolder onCreateViewHolder (final ViewGroup poiCardsVh, int i) {
        View v = LayoutInflater.from(poiCardsVh.getContext())
                               .inflate(R.layout.poi_item_card, null);
        PoiCardsViewHolder viewHolder = new PoiCardsViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder (PoiCardsViewHolder poiCardsViewHolder, int position) {
        PointOfInterestModel poiCardsItems = poiData.get(position);

        Log.e(TAG, poiCardsItems.getPlaceId() + "\n" +
                   poiCardsItems.getPlaceName() + "\n" +
                   poiCardsItems.getPlaceAddress() + "\n" +
                   poiCardsItems.getmPlaceCategory() + "\n" +
                   poiCardsItems.getUserNotes() + "\n" +
                   poiCardsItems.getmPlaceVisited() + "\n" +
                   poiCardsItems.getmLatitude() + "\n" +
                   poiCardsItems.getmLongitude());

        poiCardsViewHolder.cardsPlaceNameTv.setText(poiCardsItems.getPlaceName());
        poiCardsViewHolder.cardsPlaceAddressTv.setText(poiCardsItems.getPlaceAddress());
        poiCardsViewHolder.cardsPlaceItemRating.setText(Float.toString(poiCardsItems.getPlaceRating()));

        if ( poiCardsItems.getmPlaceVisited() == true ) {
            poiCardsViewHolder.cardsPlaceVisitedCheckBox.setText("Visited");
        } else {
            poiCardsViewHolder.cardsPlaceVisitedCheckBox.setText("Not Yet Visited");
        }

        poiCardsViewHolder.cardsPlaceNoteTv.setText(poiCardsItems.getUserNotes());

    }


    @Override
    public int getItemCount () {
        return (null != poiData ? poiData.size() : 0);
    }


    public class PoiCardsViewHolder extends RecyclerView.ViewHolder {

        public final String TAG = PoiCardsViewHolder.class.getSimpleName().toUpperCase();

        protected ImageView cardsPlaceStreetIv;
        protected TextView  cardsPlaceNameTv;
        protected TextView  cardsPlaceNoteTv;
        protected TextView  cardsPlaceAddressTv;
        protected TextView  cardsPlaceDistanceTv;
        protected TextView  cardsPlaceItemRating;
        protected CheckBox  cardsPlaceVisitedCheckBox;
        protected Button    cardsEditButton;
        protected Button    cardsShowOnMapButton;
        protected ImageView cardsCategoryColorIndicator;


        public PoiCardsViewHolder (View cardView) {
            super(cardView);
            cardsPlaceStreetIv = ( ImageView ) cardView.findViewById(R.id.locationStreetViewIv);
            cardsPlaceNameTv = ( TextView ) cardView.findViewById(R.id.locationNameTv);
            cardsPlaceAddressTv = ( TextView ) cardView.findViewById(R.id.card_view_place_address);
            cardsPlaceNoteTv = ( TextView ) cardView.findViewById(R.id.locationNotesTv);
            cardsPlaceDistanceTv = ( TextView ) cardView.findViewById(R.id.card_view_distance);
            cardsPlaceItemRating = ( TextView ) cardView.findViewById(R.id.card_view_item_rating);
            cardsPlaceVisitedCheckBox = ( CheckBox ) cardView.findViewById(R.id.card_view_visited_checkbox);
            cardsEditButton = ( Button ) cardView.findViewById(R.id.card_view_edit_button);
            cardsShowOnMapButton = ( Button ) cardView.findViewById(R.id.card_view_show_on_map_button);
            cardsCategoryColorIndicator = ( ImageView ) cardView.findViewById(R.id.card_category_color_indicator);

        }
    }
}


