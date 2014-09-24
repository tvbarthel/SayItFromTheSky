package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.adapters.DrawingAdapter;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ActionBarHelper;
import fr.tvbarthel.apps.sayitfromthesky.helpers.CursorHelper;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ViewHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;
import fr.tvbarthel.apps.sayitfromthesky.providers.contracts.DrawingContract;

public class MainActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID_DRAWINGS = 10;

    /**
     * Injected Views
     */
    @InjectView(R.id.activity_main_root)
    View mRootView;

    @InjectView(R.id.activity_main_header_container)
    View mHeaderContainer;

    @InjectView(R.id.activity_main_list_view)
    ListView mListView;

    @InjectView(R.id.activity_main_empty_view)
    View mEmptyView;

    /**
     * Private attributes
     */
    private int mActionBarSize;
    private DrawingAdapter mDrawingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // Compute the action bar size
        mActionBarSize = ActionBarHelper.getActionBarSize(this);

        // Set the height of the header container to 1/3.5 of the root height.
        ViewTreeObserver vto = mRootView.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    final int headerHeight = (int) (mRootView.getHeight() / 3.5);
                    mHeaderContainer.getLayoutParams().height = headerHeight;
                    mListView.setPadding(0, headerHeight - mActionBarSize, 0, 0);
                    ViewHelper.removeOnGlobalLayoutListener(mRootView, this);
                }

            });
        }


        mDrawingAdapter = new DrawingAdapter(this);
        initListView();
        getLoaderManager().initLoader(LOADER_ID_DRAWINGS, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.global, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.activity_main_btn_new_drawing})
    public void startNewDrawing() {
        startActivity(new Intent(this, DrawingActivity.class));
    }

    /**
     * Initialize the ListView.
     * <p/>
     * Set the onScrollListener.
     * Set the Adapter.
     * Set the empty view.
     */
    private void initListView() {
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0 && firstVisibleItem == 0) {
                    final int realTop = Math.max(mListView.getChildAt(0).getTop(), 0);
                    final int translationY = (realTop - mListView.getPaddingTop()) / 2;
                    mHeaderContainer.setTranslationY(translationY);
                }
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor cursor = (Cursor) mDrawingAdapter.getItem(position);
                if (cursor != null) {
                    final Drawing drawingClicked = CursorHelper.cursorToDrawing(cursor);
                    final Intent intent = new Intent(MainActivity.this, DrawingViewerActivity.class);
                    intent.putExtra(DrawingViewerActivity.EXTRA_KEY_DRAWING, drawingClicked);
                    startActivity(intent);
                }

            }
        });
        mListView.setAdapter(mDrawingAdapter);
        mListView.setEmptyView(mEmptyView);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
        if (loaderId == LOADER_ID_DRAWINGS) return createDrawingLoader();
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        final int loaderId = cursorLoader.getId();
        if (loaderId == LOADER_ID_DRAWINGS) finishDrawingLoading(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        final int loaderId = cursorLoader.getId();
        if (loaderId == LOADER_ID_DRAWINGS) resetDrawingLoader();
    }

    /**
     * Create a {@link android.support.v4.content.Loader} for the drawings.
     *
     * @return a Loader<Cursor>
     */
    private Loader<Cursor> createDrawingLoader() {
        return new CursorLoader(this, DrawingContract.CONTENT_URI, null, null, null, DrawingContract.Columns.COLUMN_CREATION_TIME + " DESC");
    }

    /**
     * Finish to load the drawings.
     *
     * @param cursor a {@link android.database.Cursor} that represents the drawings to be loaded.
     */
    private void finishDrawingLoading(Cursor cursor) {
        mDrawingAdapter.swapCursor(cursor);
    }

    /**
     * Reset the loader of the drawings.
     */
    private void resetDrawingLoader() {
        mDrawingAdapter.swapCursor(null);
    }
}
