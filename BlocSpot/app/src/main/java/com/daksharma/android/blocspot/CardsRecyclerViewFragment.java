package com.daksharma.android.blocspot;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daksharma.android.blocspot.ui.adapter.PoiCardViewAdapter;

/**
 * Created by Daksh on 2/4/16.
 */
public class CardsRecyclerViewFragment extends Fragment {

    private final static String TAG = CardsRecyclerViewFragment.class.getSimpleName().toUpperCase();

    private RecyclerView mRecyclerView;
    private PoiCardViewAdapter mRecyclerViewAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.recycler_view_adapter, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_adapter);
        mLayoutManager = new LinearLayoutManager(getActivity());


        return rootView;
    }
}
