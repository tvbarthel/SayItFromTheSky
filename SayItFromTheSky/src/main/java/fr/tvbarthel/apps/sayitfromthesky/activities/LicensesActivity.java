package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.tvbarthel.apps.sayitfromthesky.R;

public class LicensesActivity extends Activity {

    private static final String URL_LICENSES = "file:///android_asset/licenses.html";

    @InjectView(R.id.activity_licenses_webview)
    WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licenses);
        ButterKnife.inject(this);

        getActionBar().setDisplayHomeAsUpEnabled(true);

        mWebView.loadUrl(URL_LICENSES);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
