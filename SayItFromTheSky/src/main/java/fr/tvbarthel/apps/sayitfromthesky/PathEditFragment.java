package fr.tvbarthel.apps.sayitfromthesky;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class PathEditFragment extends Fragment implements SayItMapFragment.ISayItMapFragment {

    private static final String BUNDLE_KEY_ENCODED_PATHS = "PathEditFragment.Bundle.Key.EncodedPaths";

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPathOptions;
    private ArrayList<String> mEncodedPaths;

    public static PathEditFragment newInstance(ArrayList<String> encodedPaths) {
        final PathEditFragment instance = new PathEditFragment();
        final Bundle arguments = new Bundle();
        arguments.putStringArrayList(BUNDLE_KEY_ENCODED_PATHS, encodedPaths);
        instance.setArguments(arguments);
        return instance;
    }

    public PathEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_path_edit, container, false);

        // Create a PolylineOptions to draw the paths
        mPathOptions = new PolylineOptions();
        mPathOptions.color(Color.BLUE);

        // Get the encoded paths
        final Bundle arguments = getArguments();
        if (arguments.containsKey(BUNDLE_KEY_ENCODED_PATHS)) {
            mEncodedPaths = arguments.getStringArrayList(BUNDLE_KEY_ENCODED_PATHS);
        } else {
            mEncodedPaths = new ArrayList<String>();
        }

        mMapFragment = (SayItMapFragment) getChildFragmentManager().findFragmentByTag("fragmentTagMap");
        if (mMapFragment == null) {
            // Create a new map fragment.
            mMapFragment = new SayItMapFragment(this);
            getChildFragmentManager().beginTransaction().add(R.id.fragment_path_edit_map, mMapFragment, "fragmentTagMap").commit();
        } else {
            // Re-use the old map fragment.
            mMapFragment.setInterface(this);
            getChildFragmentManager().beginTransaction().show(mMapFragment).commit();
        }

        return rootView;
    }

    @Override
    public void onMapReady() {
        if (mGoogleMap == null) {
            mGoogleMap = mMapFragment.getMap();
            if (mGoogleMap != null) {
                // You are good to use the map =)
                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setCompassEnabled(false);
                uiSettings.setZoomControlsEnabled(false);

                // Draw the paths
                if (!mEncodedPaths.isEmpty()) {
                    final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (String encodedPath : mEncodedPaths) {
                        Polyline path = mGoogleMap.addPolyline(mPathOptions);
                        List<LatLng> pathPoints = PolyUtil.decode(encodedPath);
                        for (LatLng point : pathPoints) {
                            boundsBuilder.include(point);
                        }
                        path.setPoints(PolyUtil.decode(encodedPath));
                    }
                    animateCameraToBounds(boundsBuilder.build());
                }
            }
        }
    }

    private void animateCameraToBounds(final LatLngBounds bounds) {
        try {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        } catch (IllegalStateException e) {
            // layout not yet initialized
            final View mapView = mMapFragment.getView();
            if (mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressWarnings("deprecation")
                    // We check which build version we are using.
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            mapView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }
        }
    }
}
