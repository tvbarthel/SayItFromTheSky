package fr.tvbarthel.apps.sayitfromthesky;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;

public class SayItFragment extends Fragment {

    private MapFragment mMapFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_say_it, container, false);
        mMapFragment = MapFragment.newInstance(new GoogleMapOptions().mapType(GoogleMap.MAP_TYPE_NORMAL).compassEnabled(false));
        getFragmentManager().beginTransaction().add(R.id.fragment_say_it_map_container, mMapFragment, "fragmentTagMap").commit();
        return view;
    }

}
