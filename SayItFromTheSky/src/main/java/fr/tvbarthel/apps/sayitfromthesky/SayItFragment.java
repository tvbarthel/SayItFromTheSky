package fr.tvbarthel.apps.sayitfromthesky;


import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

public class SayItFragment extends Fragment implements SayItMapFragment.ISayItMapFragment {

    private static float DEFAULT_VALUE_ZOOM = 15f;
    private static float DELTA_DISTANCE_IN_METER = 5f;
    private static String BUNDLE_KEY_LOCATION = "SayItFragment.Bundle.Key.Location";
    private static String BUNDLE_KEY_ZOOM = "SayItFragment.Bundle.Key.Zoom";

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mLastKnownLocation;
    private Location mLastCandidateLocation;
    private float mLastKnownZoom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_say_it, container, false);
        mMapFragment = new SayItMapFragment(this);
        getFragmentManager().beginTransaction().add(R.id.fragment_say_it_map_container, mMapFragment, "fragmentTagMap").commit();

        mLastKnownZoom = DEFAULT_VALUE_ZOOM;

        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(BUNDLE_KEY_LOCATION);
            mLastKnownZoom = savedInstanceState.getFloat(BUNDLE_KEY_ZOOM, DEFAULT_VALUE_ZOOM);
        }

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_LOCATION, mLastKnownLocation);
        if (mGoogleMap != null) {
            outState.putFloat(BUNDLE_KEY_ZOOM, mGoogleMap.getCameraPosition().zoom);
        }
    }

    @Override
    public void onMapReady() {
        if (mGoogleMap == null) {
            mGoogleMap = mMapFragment.getMap();
            if (mGoogleMap != null) {
                //the map is now active and can be manipulated
                mGoogleMap.setMyLocationEnabled(true);
                final UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setCompassEnabled(false);
                uiSettings.setZoomControlsEnabled(false);

                //try to restore last known location
                if (mLastKnownLocation != null) {
                    initMapLocation();
                }

                mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        //first location
                        if (mLastKnownLocation == null) {
                            mLastKnownLocation = location;
                            mLastCandidateLocation = location;
                            initMapLocation();
                        } else if (isLocationIfOutdated(location)) {
                            setNewLocation(location);
                        }

                        mLastCandidateLocation = location;
                    }
                });
            }
        }
    }

    private void initMapLocation() {
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), mLastKnownZoom));
    }

    private boolean isLocationIfOutdated(Location candidateLocation) {
        final LatLng candidateLatLng = new LatLng(candidateLocation.getLatitude(), candidateLocation.getLongitude());
        final LatLng currentLatLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        final double distance = SphericalUtil.computeDistanceBetween(candidateLatLng, currentLatLng);
        Log.d("argonne", "distance : " + String.valueOf(distance));
        return distance > DELTA_DISTANCE_IN_METER;
    }

    private void setNewLocation(Location newLocation) {
        //TODO look at the elapsed time ?
        Log.d("argonne", "new location set !");
        mLastKnownLocation = newLocation;
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(
                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())));
    }

}
