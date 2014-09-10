package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.adapters.DrawingAdapter;
import fr.tvbarthel.apps.sayitfromthesky.helpers.ActionBarHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

public class MainActivity extends Activity {

    /**
     * Injected Views
     */
    @InjectView(R.id.activity_main_root)
    View mRootView;

    @InjectView(R.id.activity_main_header_container)
    View mHeaderContainer;

    @InjectView(R.id.activity_main_list_view)
    ListView mObservableListView;

    /**
     * Private attributes
     */
    private int mActionBarSize;
    private int mMaxHeaderTranslationY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // Compute the action bar size
        mActionBarSize = ActionBarHelper.getActionBarSize(this);

        // Set the height of the header container to 1/3.5 of the root height.
        ViewTreeObserver vto = mRootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int headerHeight = (int) (mRootView.getHeight() / 3.5);
                mHeaderContainer.getLayoutParams().height = headerHeight;
                mObservableListView.setPadding(0, headerHeight - mActionBarSize, 0, 0);
                ViewTreeObserver obs = mRootView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        fakeListViewData();
        initListView();
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
     */
    private void initListView() {
        mObservableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount > 0 && firstVisibleItem == 0) {
                    final int visibleTop = mObservableListView.getPaddingTop() - mObservableListView.getChildAt(0).getTop();
                    final int translationY = Math.max(-visibleTop / 2, -mActionBarSize);
                    mHeaderContainer.setTranslationY(translationY);
                }
            }
        });

    }

    /**
     * TODO remove
     * test purpose only.
     */
    private void fakeListViewData() {
        mObservableListView.setAdapter(new DrawingAdapter(this,
                java.util.Arrays.asList(new Drawing("drawing 1"),
                        new Drawing("drawing 2"),
                        new Drawing("drawing 3"),
                        new Drawing("drawing 4"),
                        new Drawing("drawing 5"),
                        new Drawing("drawing 6"),
                        new Drawing("drawing 7"),
                        new Drawing("drawing 8"),
                        new Drawing("drawing 9"),
                        new Drawing("drawing 10"),
                        new Drawing("drawing 11"),
                        new Drawing("drawing 12"),
                        new Drawing("drawing 13"),
                        new Drawing("drawing 14"),
                        new Drawing("drawing 15"))));
    }


}
