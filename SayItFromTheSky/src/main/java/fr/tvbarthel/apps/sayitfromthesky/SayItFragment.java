package fr.tvbarthel.apps.sayitfromthesky;


import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
    private static float ACURRACY_MINIMUM_IN_METER = 400f;

    // Bundle key used for saving instance state
    private static String BUNDLE_KEY_LOCATION = "SayItFragment.Bundle.Key.Location";
    private static String BUNDLE_KEY_ZOOM = "SayItFragment.Bundle.Key.Zoom";
    private static String BUNDLE_KEY_CURRENT_POLYLINE = "SayItFragment.Bundle.Key.Current.Polyline";
    private static String BUNDLE_KEY_ENCODED_POLYLINES = "SayItFragment.Bundle.Key.Other.Polyline";

    // UI elements
    private SayItMapFragment mMapFragment;
    private ToggleButton mLineStateButton;
    private Button mAddPointButton;

    private GoogleMap mGoogleMap;
    private Location mLastKnownLocation;
    private LatLng mLastKnownLatLng;
    private float mLastKnownZoom;
    private PolylineOptions mPolylineOptionsCurrent;
    private Polyline mCurrentPolyline;
    private PolylineOptions mPolylineOptionsPreview;
    private Polyline mPreviewPolyline;
    private ArrayList<String> mEncodedPolylines;
    private Bundle mLastSavedInstanceState;
    private boolean mIsCurrentPointInCurrentPath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_say_it, container, false);
        mLastKnownZoom = DEFAULT_VALUE_ZOOM;
        setHasOptionsMenu(true);

        // Create the polyline array used to store the polylines added to the map.
        mEncodedPolylines = new ArrayList<String>();

        // Create the polyline used for the current path.
        mPolylineOptionsCurrent = new PolylineOptions();
        mPolylineOptionsCurrent.color(Color.BLUE);

        // Create the polyline for the preview path.
        mPolylineOptionsPreview = new PolylineOptions();
        mPolylineOptionsPreview.color(Color.RED);

        // The current point/position is not in the current path yet.
        mIsCurrentPointInCurrentPath = false;

        // Setup the button used to add a point to the current path.
        mAddPointButton = (Button) view.findViewById(R.id.fragment_say_it_button_add_point);
        mAddPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPointToCurrentPolyline(mLastKnownLatLng);
            }
        });

        // Setup the toggle button used to start and stop a path.
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
                            // The current polyline has no interest since it only contains one point.
                            mCurrentPolyline.remove();
                        } else if (mCurrentPolyline != null) {
                            // The current polyline is a part of the drawing
                            mEncodedPolylines.add(PolyUtil.encode(mCurrentPolyline.getPoints()));
                        }
                        mCurrentPolyline = null;
                        mAddPointButton.setVisibility(View.INVISIBLE);
                        mPreviewPolyline.setVisible(false);
                        mIsCurrentPointInCurrentPath = false;
                    }
                }
            }
        });

        if (savedInstanceState != null) {
            // Store savedInstanceState for future use, when the map will actually be ready.
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
        saveCurrentPolyline(outState);
        saveEncodedPolyline(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.say_it, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_save) {
            // TODO save the current work
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady() {
        if (mGoogleMap == null) {
            mGoogleMap = mMapFragment.getMap();
            if (mGoogleMap != null) {
                // The map is now active and can be manipulated
                // Enable my location
                mGoogleMap.setMyLocationEnabled(true);

                // Setup the map UI
                final UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setCompassEnabled(false);
                uiSettings.setZoomControlsEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);

                // Add the preview polyline
                mPreviewPolyline = mGoogleMap.addPolyline(mPolylineOptionsPreview);

                // Restore the polylines that were displayed on the map
                if (mLastSavedInstanceState != null) {
                    restoreEncodedPolyline();
                    restoreCurrentPolyline();
                }

                // Try to restore last known location
                if (mLastKnownLocation != null) {
                    initMapLocation();
                }

                mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        if (mLastKnownLocation == null) {
                            // First location
                            setLastKnownLocation(location);
                            initMapLocation();
                        } else if (isLocationOutdated(location)) {
                            // New location
                            setNewLocation(location);
                        }
                    }
                });

            }
        }
    }

    private void saveCurrentPolyline(Bundle outState) {
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

    private void saveEncodedPolyline(Bundle outState) {
        if (mEncodedPolylines != null && mEncodedPolylines.size() > 0) {
            outState.putStringArrayList(BUNDLE_KEY_ENCODED_POLYLINES, mEncodedPolylines);
        }
    }

    private void restoreEncodedPolyline() {
        mEncodedPolylines = mLastSavedInstanceState.getStringArrayList(BUNDLE_KEY_ENCODED_POLYLINES);
        if (mEncodedPolylines == null) {
            mEncodedPolylines = new ArrayList<String>();
        } else {
            for (String encodedPolyline : mEncodedPolylines) {
                mGoogleMap.addPolyline(mPolylineOptionsCurrent).setPoints(PolyUtil.decode(encodedPolyline));
            }
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
        boolean isLocationOutdated = false;
        if (candidateLocation.getAccuracy() < mLastKnownLocation.getAccuracy()) {
            isLocationOutdated = true;
        } else if (candidateLocation.getAccuracy() < ACURRACY_MINIMUM_IN_METER) {
            final LatLng candidateLatLng = locationToLatLng(candidateLocation);
            final double distance = SphericalUtil.computeDistanceBetween(candidateLatLng, mLastKnownLatLng);
            isLocationOutdated = distance > DELTA_DISTANCE_IN_METER;
        }
        return isLocationOutdated;
    }

    private void setNewLocation(Location newLocation) {
        setLastKnownLocation(newLocation);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(mLastKnownLatLng));
    }

    private void setLastKnownLocation(Location location) {
        if (location != null) {
            mLastKnownLocation = location;
            mLastKnownLatLng = locationToLatLng(location);
            mIsCurrentPointInCurrentPath = false;
            updatePreviewPoints();
        }
    }

    private LatLng locationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    private void addPointToCurrentPolyline(LatLng newPoint) {
        if (!mIsCurrentPointInCurrentPath) {
            final List<LatLng> currentPoints = mCurrentPolyline.getPoints();
            currentPoints.add(newPoint);
            mCurrentPolyline.setPoints(currentPoints);
            updatePreviewPoints();
            mIsCurrentPointInCurrentPath = true;
        }
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
