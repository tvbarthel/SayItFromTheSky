package fr.tvbarthel.apps.sayitfromthesky.fragments;


import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
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
import android.widget.ProgressBar;
import android.widget.Toast;
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

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ActionBarHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

/**
 * A simple {@link android.support.v4.app.Fragment} for drawing a path while walking.
 * <p/>
 * Test purpose : example of an encoded polyline : qixvGyhqFKRs@P
 */
public class DrawingFragment extends Fragment implements SayItMapFragment.Callback {


    private static float DEFAULT_VALUE_ZOOM = 15f;
    private static float DELTA_DISTANCE_IN_METER = 5f;
    private static float ACURRACY_MINIMUM_IN_METER = 400f;
    private static final int REQUEST_CODE_SAVE_PATH = 1;

    // Bundle key used for saving instance state
    private static String BUNDLE_KEY_LOCATION = "DrawingFragment.Bundle.Key.Location";
    private static String BUNDLE_KEY_ZOOM = "DrawingFragment.Bundle.Key.Zoom";
    private static String BUNDLE_KEY_CURRENT_POLYLINE = "DrawingFragment.Bundle.Key.Current.Polyline";
    private static String BUNDLE_KEY_ENCODED_POLYLINES = "DrawingFragment.Bundle.Key.Other.Polylines";

    // UI elements
    private SayItMapFragment mMapFragment;
    private Toast mTextToast;
    @InjectView(R.id.fragment_drawing_button_line_state)
    ToggleButton mLineStateButton;
    @InjectView(R.id.fragment_drawing_button_add_point)
    Button mAddPointButton;
    @InjectView(R.id.fragment_drawing_progress_bar)
    ProgressBar mProgressBar;

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
    private Callback mCallback;

    /**
     * Default Constructor.
     * <p/>
     * lint [ValidFragment]
     * http://developer.android.com/reference/android/app/Fragment.html#Fragment()
     * Every fragment must have an empty constructor, so it can be instantiated when restoring its activity's state.
     */
    public DrawingFragment() {
        super();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof Callback) {
            mCallback = (Callback) activity;
        } else {
            throw new ClassCastException(activity.toString() + "must implement DrawingFragment.Callback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Resources resources = getResources();
        mLastKnownZoom = DEFAULT_VALUE_ZOOM;
        setHasOptionsMenu(true);

        // Create the polyline array used to store the polylines added to the map.
        mEncodedPolylines = new ArrayList<String>();

        // Create the polyline used for the current path.
        mPolylineOptionsCurrent = new PolylineOptions();
        mPolylineOptionsCurrent.color(resources.getColor(R.color.primary_color));

        // Create the polyline for the preview path.
        mPolylineOptionsPreview = new PolylineOptions();
        mPolylineOptionsPreview.color(resources.getColor(R.color.accent_color));

        // The current point/position is not in the current path yet.
        mIsCurrentPointInCurrentPath = false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_drawing, container, false);
        ButterKnife.inject(this, view);
        // Setup the button used to add a point to the current path.
        mAddPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPointToCurrentPolyline(mLastKnownLatLng);
            }
        });

        // Setup the toggle button used to start and stop a path.
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

        mMapFragment = (SayItMapFragment) getChildFragmentManager().findFragmentByTag("fragmentTagMap");
        if (mMapFragment == null) {
            // Create a new map fragment.
            mMapFragment = new SayItMapFragment();
            getChildFragmentManager().beginTransaction().add(R.id.fragment_drawing_map_container, mMapFragment, "fragmentTagMap").commit();
        } else {
            getChildFragmentManager().beginTransaction().show(mMapFragment).commit();
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
        saveCurrentPolyline(outState);
        saveEncodedPolyline(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.drawing, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.action_save) {
            if ((mEncodedPolylines != null && mEncodedPolylines.size() > 0)
                    || (mCurrentPolyline != null && mCurrentPolyline.getPoints().size() > 1)) {
                saveCurrentDrawing();
            } else {
                // TODO don't use hard coded String
                makeToast("There is nothing to save !");
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SAVE_PATH && resultCode == Activity.RESULT_OK) {
            // The save path request has been done.
            // TODO reset the current path and the other paths after
        }
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
                uiSettings.setMyLocationButtonEnabled(true);
                int actionBarSize = ActionBarHelper.getActionBarSize(getActivity());
                mGoogleMap.setPadding(0, actionBarSize, 0, 0);

                // Add the preview polyline
                mPreviewPolyline = mGoogleMap.addPolyline(mPolylineOptionsPreview);

                // Restore the polylines that were displayed on the map
                // Add ask to draw them
                if (mLastSavedInstanceState != null) {
                    restoreEncodedPolyline(true);
                    restoreCurrentPolyline(true);
                }

                // Try to restore last known location
                if (mLastKnownLocation != null) {
                    initMapLocation();
                }

                mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        if (mProgressBar.getVisibility() != View.GONE) {
                            mProgressBar.setVisibility(View.GONE);
                        }

                        if (mLastKnownLocation == null) {
                            // First location
                            setLastKnownLocation(location);
                            initMapLocation();
                        } else {
                            // New location
                            setNewLocation(location);
                        }
                    }
                });

            }
        } else {
            // Setup the circle buttons
            initCircleButtons();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        hideToast();
    }

    private void makeToast(String message) {
        hideToast();
        mTextToast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
        mTextToast.show();
    }

    private void hideToast() {
        if (mTextToast != null) {
            mTextToast.cancel();
            mTextToast = null;
        }
    }

    private void saveCurrentDrawing() {
        final ArrayList<String> encodedPaths = new ArrayList<String>();
        encodedPaths.addAll(mEncodedPolylines);
        if (mCurrentPolyline != null) {
            // Add the current polyline
            encodedPaths.add(PolyUtil.encode(mCurrentPolyline.getPoints()));
        }
        final Drawing drawingToSave = new Drawing("Default title", System.currentTimeMillis(), encodedPaths);
        mCallback.saveDrawing(drawingToSave);
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
    private void restoreCurrentPolyline(boolean shouldDrawPolyline) {
        final String encodedPoints = mLastSavedInstanceState.getString(BUNDLE_KEY_CURRENT_POLYLINE);
        if (encodedPoints != null && shouldDrawPolyline) {
            mCurrentPolyline = mGoogleMap.addPolyline(mPolylineOptionsCurrent);
            mCurrentPolyline.setPoints(PolyUtil.decode(encodedPoints));
        }
    }

    private void saveEncodedPolyline(Bundle outState) {
        if (mEncodedPolylines != null && mEncodedPolylines.size() > 0) {
            outState.putStringArrayList(BUNDLE_KEY_ENCODED_POLYLINES, mEncodedPolylines);
        }
    }

    private void restoreEncodedPolyline(boolean shouldDrawPolyline) {
        mEncodedPolylines = mLastSavedInstanceState.getStringArrayList(BUNDLE_KEY_ENCODED_POLYLINES);
        if (mEncodedPolylines == null) {
            mEncodedPolylines = new ArrayList<String>();
        } else if (shouldDrawPolyline) {
            for (String encodedPolyline : mEncodedPolylines) {
                mGoogleMap.addPolyline(mPolylineOptionsCurrent).setPoints(PolyUtil.decode(encodedPolyline));
            }
        }
    }

    private void initCircleButtons() {
        mLineStateButton.setVisibility(View.VISIBLE);
        if (mLineStateButton.isChecked()) {
            mAddPointButton.setVisibility(View.VISIBLE);
        }
    }

    private void initMapLocation() {
        initCircleButtons();
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

    /**
     * Interface definition for a callback.
     */
    public static interface Callback {
        /**
         * Called when a {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing} has to be saved.
         *
         * @param drawingToSave the drawing to save.
         */
        void saveDrawing(Drawing drawingToSave);
    }

}
