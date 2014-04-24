package fr.tvbarthel.apps.sayitfromthesky.fragments;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * A Simple {@link SupportMapFragment} that can notify a {@link ISayItMapFragment} when its map is ready.
 */
public class SayItMapFragment extends SupportMapFragment {

    private ISayItMapFragment mInterface;

    public SayItMapFragment() {
        super();
    }

    public SayItMapFragment(ISayItMapFragment iSayItMapFragment) {
        super();
        mInterface = iSayItMapFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mInterface != null) {
            mInterface.onMapReady();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInterface = null;
    }

    public void setInterface(ISayItMapFragment iSayItMapFragment) {
        mInterface = iSayItMapFragment;
    }

    public interface ISayItMapFragment {
        public void onMapReady();
    }
}
