package com.daksharma.android.blocspot.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daksharma.android.blocspot.R;
import com.daksharma.android.blocspot.model.PointOfInterestModel;
import com.daksharma.android.blocspot.ui.activity.PointOfInterestItemView;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by Daksh on 1/28/16.
 */
public class PoiCardViewAdapter extends RecyclerView.Adapter<PoiCardViewAdapter.PoiCardsViewHolder> {

    public final static String TAG = "com.daksharma.android.blocspot.PoiCardViewAdapter";

    private List<PointOfInterestModel> poiData; // will contain the data for the poi items

    public PoiCardViewAdapter(List<PointOfInterestModel> listItems) {
        this.poiData = listItems;
    }

    @Override
    public PoiCardsViewHolder onCreateViewHolder (final ViewGroup poiCardsVh, int i) {
        View v = LayoutInflater.from(poiCardsVh.getContext()).inflate(R.layout.poi_item_card, null);
        PoiCardsViewHolder viewHolder = new PoiCardsViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder (PoiCardsViewHolder poiCardsViewHolder, int position) {
        PointOfInterestModel poiCardsItems = poiData.get(position);

        poiCardsViewHolder.locationNameTv.setText(poiCardsItems.getPlaceName());
        poiCardsViewHolder.locationNoteTv.setText(poiCardsItems.getPlaceName());

    }


    @Override
    public int getItemCount () {
        return (null != poiData ? poiData.size() : 0);
    }


    public class PoiCardsViewHolder extends RecyclerView.ViewHolder {

        public final static String TAG = "com.daksharma.android.blocspot.PoiCardsViewHolder";

        protected ImageView locationStreetIv;
        protected TextView  locationNameTv;
        protected TextView  locationNoteTv;


        public PoiCardsViewHolder (View cardView) {
            super(cardView);
            locationStreetIv = ( ImageView ) cardView.findViewById(R.id.locationStreetViewIv);
            locationNameTv = (TextView) cardView.findViewById(R.id.locationNameTv);
            locationNoteTv = (TextView) cardView.findViewById(R.id.locationNotesTv);

        }
    }
}


