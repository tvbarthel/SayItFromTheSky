package fr.tvbarthel.apps.sayitfromthesky;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.SupportMapFragment;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        if (mInterface != null) {
            mInterface.onMapReady();
        }
        return v;
    }

    public interface ISayItMapFragment {
        public void onMapReady();
    }
}
