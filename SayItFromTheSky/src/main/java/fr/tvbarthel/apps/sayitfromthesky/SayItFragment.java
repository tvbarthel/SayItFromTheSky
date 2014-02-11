package fr.tvbarthel.apps.sayitfromthesky;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

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
    private LatLng mLastKnownLatLng;
    private float mLastKnownZoom;
    private ToggleButton mDrawButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_say_it, container, false);
        mLastKnownZoom = DEFAULT_VALUE_ZOOM;

        if (savedInstanceState != null) {
            setLastKnownLocation((Location) savedInstanceState.getParcelable(BUNDLE_KEY_LOCATION));
            mLastKnownZoom = savedInstanceState.getFloat(BUNDLE_KEY_ZOOM, DEFAULT_VALUE_ZOOM);
        }

        mDrawButton = (ToggleButton) view.findViewById(R.id.fragment_say_it_draw_button);
        mDrawButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //TODO create a new PolyLine
                    Log.d("argonne", "checked !");
                }
            }
        });

        mMapFragment = new SayItMapFragment(this);
        getChildFragmentManager().beginTransaction().add(R.id.fragment_say_it_map_container, mMapFragment, "fragmentTagMap").commit();

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
                            setLastKnownLocation(location);
                            initMapLocation();
                        } else if (isLocationOutdated(location)) {
                            setNewLocation(location);
                        }
                    }
                });

            }
        }
    }

    private void initMapLocation() {
        mDrawButton.setVisibility(View.VISIBLE);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), mLastKnownZoom));
    }

    private boolean isLocationOutdated(Location candidateLocation) {
        final LatLng candidateLatLng = locationToLatLng(candidateLocation);
        final double distance = SphericalUtil.computeDistanceBetween(candidateLatLng, mLastKnownLatLng);
        return distance > DELTA_DISTANCE_IN_METER;
    }

    private void setNewLocation(Location newLocation) {
        //TODO look at the elapsed time ?
        Log.d("argonne", "set new location");
        setLastKnownLocation(newLocation);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(mLastKnownLatLng));
    }

    private void setLastKnownLocation(Location location) {
        if (location != null) {
            Log.d("argonne", "set last known location");
            mLastKnownLocation = location;
            mLastKnownLatLng = locationToLatLng(location);
        }
    }

    private LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

}
