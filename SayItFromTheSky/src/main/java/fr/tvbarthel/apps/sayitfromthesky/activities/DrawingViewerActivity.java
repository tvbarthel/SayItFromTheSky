package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.util.List;

import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.fragments.SayItMapFragment;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;


public class DrawingViewerActivity extends FragmentActivity implements SayItMapFragment.ISayItMapFragment {

    public static final String EXTRA_KEY_DRAWING = "DrawingViewerActivity.Extra.Key.Drawing";
    private static final String FRAGMENT_TAG_MAP = "DrawingViewerActivity.Fragment.Tag.Map";

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPathOptions;
    private Drawing mDrawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_detail);

        // hide the title in the action bar
        getActionBar().setDisplayShowTitleEnabled(false);

        // Create a PolylineOptions to draw the paths
        mPathOptions = new PolylineOptions();
        mPathOptions.color(Color.BLUE);

        mDrawing = getDrawing();
        createMapFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
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
                if (!mDrawing.getEncodedPolylines().isEmpty()) {
                    final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    for (String encodedPath : mDrawing.getEncodedPolylines()) {
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

    private Drawing getDrawing() {
        Drawing drawing = Drawing.EMPTY;
        final Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(EXTRA_KEY_DRAWING)) {
            drawing = extras.getParcelable(EXTRA_KEY_DRAWING);
        }
        return drawing;
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
