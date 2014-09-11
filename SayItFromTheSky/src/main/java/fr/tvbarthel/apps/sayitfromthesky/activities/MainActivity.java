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
import fr.tvbarthel.apps.sayitfromthesky.helpers.ActionBarHelper;

public class MainActivity extends Activity {

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
                mListView.setPadding(0, headerHeight - mActionBarSize, 0, 0);
                ViewTreeObserver obs = mRootView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

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

        mListView.setEmptyView(mEmptyView);
    }


}
