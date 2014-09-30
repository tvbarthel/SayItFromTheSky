package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.fragments.SayItMapFragment;
import fr.tvbarthel.apps.sayitfromthesky.fragments.dialogs.EditDrawingDialog;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ViewHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;


public class DrawingViewerActivity extends FragmentActivity implements SayItMapFragment.Callback, EditDrawingDialog.Callback {

    public static final String EXTRA_KEY_DRAWING = "DrawingViewerActivity.Extra.Key.Drawing";
    private static final String FRAGMENT_TAG_MAP = "DrawingViewerActivity.Fragment.Tag.Map";
    private static final String TAG = DrawingViewerActivity.class.getSimpleName();


    @InjectView(R.id.activity_drawing_viewer_drawing_length)
    TextView mDrawingLength;

    @InjectView(R.id.activity_drawing_viewer_drawing_title)
    TextView mDrawingTitle;

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
        mDrawingTitle.setText(mDrawing.getTitle());
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
                final UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setCompassEnabled(false);
                uiSettings.setZoomControlsEnabled(false);

                // Draw the paths
                if (!mDrawing.getEncodedPolylines().isEmpty()) {
                    final LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    double drawingLengthInMeter = 0f;
                    for (String encodedPath : mDrawing.getEncodedPolylines()) {
                        final List<LatLng> pathPoints = PolyUtil.decode(encodedPath);
                        drawingLengthInMeter += SphericalUtil.computeLength(pathPoints);
                        mGoogleMap.addPolyline(mPathOptions).setPoints(pathPoints);
                        for (LatLng point : pathPoints) {
                            boundsBuilder.include(point);
                        }
                    }
                    animateCameraToBounds(boundsBuilder.build());
                    mDrawingLength.setText(getString(R.string.activity_drawing_viewer_drawing_length, drawingLengthInMeter));
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
    @OnClick(R.id.activity_drawing_viewer_share_action)
    void handleShareAction() {
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
    }

    /**
     * Handle the edit action.
     *
     * @return true to consume the action, false otherwise.
     */
    @OnClick(R.id.activity_drawing_viewer_edit_action)
    void handleEditAction() {
        EditDrawingDialog.newInstance(mDrawing).show(getSupportFragmentManager(), null);
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
        mDrawingTitle.setText(mDrawing.getTitle());
    }
}
