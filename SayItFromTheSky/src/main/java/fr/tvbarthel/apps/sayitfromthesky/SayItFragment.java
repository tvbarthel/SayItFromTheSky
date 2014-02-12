package fr.tvbarthel.apps.sayitfromthesky;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class SayItFragment extends Fragment implements SayItMapFragment.ISayItMapFragment {

    private static float DEFAULT_VALUE_ZOOM = 15f;
    private static float DELTA_DISTANCE_IN_METER = 5f;
    private static String BUNDLE_KEY_LOCATION = "SayItFragment.Bundle.Key.Location";
    private static String BUNDLE_KEY_ZOOM = "SayItFragment.Bundle.Key.Zoom";
    private static String BUNDLE_KEY_CURRENT_POLYLINE = "SayItFragment.Bundle.Key.Current.Polyline";

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Location mLastKnownLocation;
    private LatLng mLastKnownLatLng;
    private float mLastKnownZoom;
    private ToggleButton mLineStateButton;
    private Button mAddPointButton;
    private PolylineOptions mPolylineOptionsCurrent;
    private Polyline mCurrentPolyline;
    private PolylineOptions mPolylineOptionsPreview;
    private Polyline mPreviewPolyline;
    private Bundle mLastSavedInstanceState;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_say_it, container, false);
        mLastKnownZoom = DEFAULT_VALUE_ZOOM;

        //Create the polyline used for the current path.
        mPolylineOptionsCurrent = new PolylineOptions();
        mPolylineOptionsCurrent.color(Color.BLUE);

        //Create the polyline for the preview path.
        mPolylineOptionsPreview = new PolylineOptions();
        mPolylineOptionsPreview.color(Color.RED);

        //Setup the button used to add a point to the current path.
        mAddPointButton = (Button) view.findViewById(R.id.fragment_say_it_button_add_point);
        mAddPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPointToCurrentPolyline(mLastKnownLatLng);
            }
        });

        //Setup the toggle button used to start and stop a path.
        mLineStateButton = (ToggleButton) view.findViewById(R.id.fragment_say_it_button_line_state);
        mLineStateButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mGoogleMap != null) {
                    if (isChecked) {
                        if (mCurrentPolyline == null) {
                            mCurrentPolyline = mGoogleMap.addPolyline(mPolylineOptionsCurrent);
                        }
                        mPreviewPolyline.setVisible(true);
                        addPointToCurrentPolyline(mLastKnownLatLng);
                        mAddPointButton.setVisibility(View.VISIBLE);
                    } else {
                        if (mCurrentPolyline != null && mCurrentPolyline.getPoints().size() <= 1) {
                            //the current polyline has no interest since it only contains one point.
                            mCurrentPolyline.remove();
                        }
                        mCurrentPolyline = null;
                        mAddPointButton.setVisibility(View.INVISIBLE);
                        mPreviewPolyline.setVisible(false);
                    }
                }
            }
        });

        if (savedInstanceState != null) {
            //Store savedInstanceState for future use when the map is actually ready.
            mLastSavedInstanceState = savedInstanceState;
            setLastKnownLocation((Location) savedInstanceState.getParcelable(BUNDLE_KEY_LOCATION));
            mLastKnownZoom = savedInstanceState.getFloat(BUNDLE_KEY_ZOOM, DEFAULT_VALUE_ZOOM);
        }

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
        storeCurrentPolyline(outState);
        //TODO store other polylines are displayed on the map
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

                //add the preview polyline
                mPreviewPolyline = mGoogleMap.addPolyline(mPolylineOptionsPreview);

                //Restore the polylines that were displayed on the amap
                if (mLastSavedInstanceState != null) {
                    restoreCurrentPolyline();
                    //TODO restore other polylines that were displayed on the map
                }

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

    private void storeCurrentPolyline(Bundle outState) {
        if (mCurrentPolyline != null) {
            outState.putString(BUNDLE_KEY_CURRENT_POLYLINE, PolyUtil.encode(mCurrentPolyline.getPoints()));
        }
    }

    /**
     * Restore the current polyline from the last savedInstanceState.
     * This method should be called only after the map is ready.
     */
    private void restoreCurrentPolyline() {
        final String encodedPoints = mLastSavedInstanceState.getString(BUNDLE_KEY_CURRENT_POLYLINE);
        if (encodedPoints != null) {
            mCurrentPolyline = mGoogleMap.addPolyline(mPolylineOptionsCurrent);
            mCurrentPolyline.setPoints(PolyUtil.decode(encodedPoints));
        }
    }

    private void initMapLocation() {
        mLineStateButton.setVisibility(View.VISIBLE);
        if (mLineStateButton.isChecked()) {
            mAddPointButton.setVisibility(View.VISIBLE);
        }
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLastKnownLatLng, mLastKnownZoom));
    }

    private boolean isLocationOutdated(Location candidateLocation) {
        final LatLng candidateLatLng = locationToLatLng(candidateLocation);
        final double distance = SphericalUtil.computeDistanceBetween(candidateLatLng, mLastKnownLatLng);
        return distance > DELTA_DISTANCE_IN_METER;
    }

    private void setNewLocation(Location newLocation) {
        //TODO look at the elapsed time ?
        //TODO look at the accuracy ?
        Log.d("argonne", "new location with accuracy -> " + newLocation.getAccuracy());
        setLastKnownLocation(newLocation);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(mLastKnownLatLng));
    }

    private void setLastKnownLocation(Location location) {
        if (location != null) {
            mLastKnownLocation = location;
            mLastKnownLatLng = locationToLatLng(location);
            updatePreviewPoints();
        }
    }

    private LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void addPointToCurrentPolyline(LatLng newPoint) {
        final List<LatLng> currentPoints = mCurrentPolyline.getPoints();
        currentPoints.add(newPoint);
        mCurrentPolyline.setPoints(currentPoints);
        updatePreviewPoints();
    }

    private void updatePreviewPoints() {
        if (mCurrentPolyline != null) {
            final List<LatLng> previewPoints = new ArrayList<LatLng>();
            final List<LatLng> currentPoints = mCurrentPolyline.getPoints();
            previewPoints.add(currentPoints.get(currentPoints.size() - 1));
            previewPoints.add(mLastKnownLatLng);
            mPreviewPolyline.setPoints(previewPoints);
        }
    }

}
