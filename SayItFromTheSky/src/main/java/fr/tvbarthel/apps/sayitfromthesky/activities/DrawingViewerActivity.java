package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import butterknife.ButterKnife;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.fragments.SayItMapFragment;
import fr.tvbarthel.apps.sayitfromthesky.fragments.dialogs.EditDrawingDialog;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ViewHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;


public class DrawingViewerActivity extends FragmentActivity implements SayItMapFragment.Callback, EditDrawingDialog.Callback {

    public static final String EXTRA_KEY_DRAWING = "DrawingViewerActivity.Extra.Key.Drawing";
    private static final String FRAGMENT_TAG_MAP = "DrawingViewerActivity.Fragment.Tag.Map";
    private static final String TAG = DrawingViewerActivity.class.getSimpleName();

    private SayItMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private PolylineOptions mPathOptions;
    private Drawing mDrawing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing_viewer);
        ButterKnife.inject(this);

        // Create a PolylineOptions to draw the paths
        mPathOptions = new PolylineOptions();
        mPathOptions.color(getResources().getColor(R.color.primary_color));

        mDrawing = getDrawing();
        createMapFragment();
        initActionBar(mDrawing);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (R.id.action_share == id) {
            return handleShareAction();
        }
        if (R.id.action_edit == id) {
            return handleEditAction();
        }
        return super.onOptionsItemSelected(item);
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
            mMapFragment = new SayItMapFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.activity_drawing_viewer_map_container, mMapFragment,
                    FRAGMENT_TAG_MAP).commit();
        } else {
            // Re-use the old map fragment.
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
                    @Override
                    public void onGlobalLayout() {
                        ViewHelper.removeOnGlobalLayoutListener(mapView, this);
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
                    }
                });
            }
        }
    }

    /**
     * Handle the share action.
     *
     * @return true to consume the action, false otherwise.
     */
    private boolean handleShareAction() {
        // TODO
        if (mGoogleMap != null) {
            mGoogleMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                @Override
                public void onSnapshotReady(Bitmap bitmap) {
                    final Uri snapshotUri = saveSnapshotToTempFile(bitmap);
                    if (snapshotUri != null) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_STREAM, snapshotUri);
                        intent.setType("image/jpeg");

                        // Grant permissions to all apps that can handle this intent
                        // thanks to this answer http://stackoverflow.com/a/18332000
                        List<ResolveInfo> resInfoList = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                        for (ResolveInfo resolveInfo : resInfoList) {
                            final String packageName = resolveInfo.activityInfo.packageName;
                            grantUriPermission(packageName, snapshotUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }

                        // And start
                        startActivity(Intent.createChooser(intent, getString(R.string.activity_drawing_viewer_chooser_title_snapshots)));
                    }
                }
            });
        }
        return true;
    }

    /**
     * Handle the edit action.
     *
     * @return true to consume the action, false otherwise.
     */
    private boolean handleEditAction() {
        EditDrawingDialog.newInstance(mDrawing).show(getSupportFragmentManager(), null);
        return true;
    }

    /**
     * Init the action bar with a {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}
     *
     * @param drawing the {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing} used to init the action bar.
     */
    private void initActionBar(Drawing drawing) {
        final ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_color)));
            setActionBarTitle(actionBar, drawing.getTitle());
            final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
            setActionBarSubtitle(actionBar, dateFormat.format(drawing.getCreationTimeInMillis()));
        }
    }

    /**
     * Set the title of an action bar.
     *
     * @param actionBar the {@link android.app.ActionBar} the action bar whose title will be set.
     * @param title     the title to be set.
     */
    private void setActionBarTitle(ActionBar actionBar, String title) {
        final ForegroundColorSpan colorSpanMaterialGrey300 = new ForegroundColorSpan(getResources().getColor(R.color.material_grey_300));
        final SpannableString spannableStringTitle = new SpannableString(title);
        spannableStringTitle.setSpan(colorSpanMaterialGrey300, 0, spannableStringTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionBar.setTitle(spannableStringTitle);
    }

    /**
     * Set the subtitle of an action bar.
     *
     * @param actionbar the {@link android.app.ActionBar} the action bar whose subtitle will be set.
     * @param subtitle  the subtitle to be set.
     */
    private void setActionBarSubtitle(ActionBar actionbar, String subtitle) {
        final ForegroundColorSpan colorSpanMaterialGrey500 = new ForegroundColorSpan(getResources().getColor(R.color.material_grey_500));
        final SpannableString spannableStringDate = new SpannableString(subtitle);
        spannableStringDate.setSpan(colorSpanMaterialGrey500, 0, spannableStringDate.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        actionbar.setSubtitle(spannableStringDate);
    }

    /**
     * Save a snapshot to temporary file in internal storage.
     *
     * @param bitmap the {@link android.graphics.Bitmap} that will be saved.
     * @return a Content URI that can be served to another app {@see http://developer.android.com/reference/android/support/v4/content/FileProvider.html}
     */
    private Uri saveSnapshotToTempFile(Bitmap bitmap) {
        final ByteArrayOutputStream bytesStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytesStream);
        try {
            final File snapshotPath = new File(getFilesDir(), "snapshots");
            if (!snapshotPath.isDirectory()) snapshotPath.mkdirs();
            final File file = new File(snapshotPath, "temp_snapshot.jpg");
            final FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytesStream.toByteArray());
            outputStream.close();
            return FileProvider.getUriForFile(this, "fr.tvbarthel.apps.sayitfromthesky.fileprovider", file);
        } catch (IOException e) {
            Log.e(TAG, "saveSnapshotToTempFile error", e);
            return null;
        }
    }

    @Override
    public void onDrawingEdited(Drawing drawing) {
        // At the moment, only the title can be edited.
        // So we only change the title in the action bar.
        mDrawing = drawing;
        setActionBarTitle(getActionBar(), mDrawing.getTitle());
    }
}
