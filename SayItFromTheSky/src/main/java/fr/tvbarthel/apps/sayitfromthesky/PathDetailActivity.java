package fr.tvbarthel.apps.sayitfromthesky;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import fr.tvbarthel.apps.sayitfromthesky.ui.TagEntry;


public class PathDetailActivity extends FragmentActivity implements SayItMapFragment.ISayItMapFragment, TagEntry.Callback {

    public static final String EXTRA_KEY_ENCODED_PATHS = "PathDetailActivity.Extra.Key.EncodedPaths";
    private static final String FRAGMENT_TAG_MAP = "PathDetailActivity.Fragment.Tag.Map";
    private static final String BUNDLE_KEY_TAG_LIST = "PathDetailActivity.Bundle.Key.TagList";

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPathOptions;
    private ArrayList<String> mEncodedPaths;
    private LinearLayout mTagContainer;
    private LinearLayout.LayoutParams mTagLayoutParams;
    private ArrayList<String> mTagList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_detail);

        // Create a PolylineOptions to draw the paths
        mPathOptions = new PolylineOptions();
        mPathOptions.color(Color.BLUE);

        mEncodedPaths = getEncodedPaths();
        restoreTagList(savedInstanceState);
        initMapContainer();
        initTagContainer();
        createMapFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(BUNDLE_KEY_TAG_LIST, mTagList);
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

    private void restoreTagList(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_TAG_LIST)) {
            mTagList = savedInstanceState.getStringArrayList(BUNDLE_KEY_TAG_LIST);
        } else {
            mTagList = new ArrayList<String>();
        }
    }

    private void initTagContainer() {
        final EditText editTextAddTag = (EditText) findViewById(R.id.activity_path_detail_add_tag);
        mTagContainer = (LinearLayout) findViewById(R.id.activity_path_detail_tag_container);
        mTagLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mTagLayoutParams.setMargins(8, 0, 8, 0);

        editTextAddTag.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String tagContent = v.getText().toString();
                    if (!tagContent.isEmpty() && !mTagList.contains(tagContent)) {
                        final TagEntry newTagEntry = createTagEntry(tagContent);
                        mTagContainer.addView(newTagEntry, mTagLayoutParams);
                        mTagList.add(newTagEntry.getTag());
                        v.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        // Restore the tag entries
        restoreTagEntries();
    }

    private TagEntry createTagEntry(String tagContent) {
        final TagEntry tagEntry = new TagEntry(PathDetailActivity.this);
        tagEntry.setTag(tagContent);
        tagEntry.setCallback(PathDetailActivity.this);
        return tagEntry;
    }

    private void restoreTagEntries() {
        for (String tagContent : mTagList) {
            mTagContainer.addView(createTagEntry(tagContent), mTagLayoutParams);
        }
    }

    private void initMapContainer() {
        // Get the window size
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        final int actionBarSize = styledAttributes.getDimensionPixelSize(0, 0);
        int mapHeight = (windowSize.y - actionBarSize - getResources().getDimensionPixelSize(R.dimen.expand_container_height));

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

    @Override
    public void onTagDeletion(TagEntry tag) {
        mTagList.remove(tag.getTag());
        mTagContainer.removeView(tag);
    }
}
