package fr.tvbarthel.apps.sayitfromthesky;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

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


public class PathDetailActivity extends FragmentActivity implements SayItMapFragment.ISayItMapFragment {

    public static final String EXTRA_KEY_ENCODED_PATHS = "PathDetailActivity.Extra.Key.EncodedPaths";
    private static final String FRAGMENT_TAG_MAP = "PathDetailActivity.Fragment.Tag.Map";
    private static final float MAP_HEIGHT_PROPORTION = 0.70f; // the map height is about 70% of the screen height.

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPathOptions;
    private ArrayList<String> mEncodedPaths;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_detail);

        // Create a PolylineOptions to draw the paths
        mPathOptions = new PolylineOptions();
        mPathOptions.color(Color.BLUE);

        mEncodedPaths = getEncodedPaths();
        initMapContainer();
        createMapFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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

    private ArrayList<String> getEncodedPaths() {
        ArrayList<String> encodedPaths = new ArrayList<String>();
        final Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_KEY_ENCODED_PATHS)) {
            encodedPaths = extras.getStringArrayList(EXTRA_KEY_ENCODED_PATHS);
        }
        return encodedPaths;
    }

    private void initMapContainer() {
        // Get the window size
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        int mapHeight = (int) (windowSize.y * MAP_HEIGHT_PROPORTION);

        // Set the map container height
        View mapContainer = findViewById(R.id.activity_path_detail_map_container);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mapContainer.getLayoutParams();
        layoutParams.height = mapHeight;
        mapContainer.setLayoutParams(layoutParams);

        // Set the top padding of the scroll view
        findViewById(R.id.activity_path_detail_scroll_view).setPadding(0, mapHeight, 0, 0);
    }

    private void createMapFragment() {
        mMapFragment = (SayItMapFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_MAP);
        if (mMapFragment == null) {
            // Create a new map fragment.
            mMapFragment = new SayItMapFragment(this);
            getSupportFragmentManager().beginTransaction().add(R.id.activity_path_detail_map_container, mMapFragment,
                    FRAGMENT_TAG_MAP).commit();
        } else {
            // Re-use the old map fragment.
            mMapFragment.setInterface(this);
            getSupportFragmentManager().beginTransaction().show(mMapFragment).commit();
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
