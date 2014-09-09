package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.tvbarthel.apps.sayitfromthesky.R;

public class MainActivity extends Activity {

    @InjectView(R.id.activity_main_root)
    View mRootView;

    @InjectView(R.id.activity_main_header_container)
    View mHeaderContainer;

    @InjectView(R.id.activity_main_list_view)
    ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        // Set the height of the header container to 1/3.5 of the root height.
        ViewTreeObserver vto = mRootView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final int rootHeight = mRootView.getHeight();
                mHeaderContainer.getLayoutParams().height = (int) (rootHeight / 3.5);
                ViewTreeObserver obs = mRootView.getViewTreeObserver();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    obs.removeOnGlobalLayoutListener(this);
                } else {
                    obs.removeGlobalOnLayoutListener(this);
                }
            }

        });

        fakeListViewData();
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

    /**
     * TODO remove
     * test purpose only.
     */
    private void fakeListViewData() {
        mListView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                java.util.Arrays.asList("string 1",
                        "string 2",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 3",
                        "string 4")));
    }


}
