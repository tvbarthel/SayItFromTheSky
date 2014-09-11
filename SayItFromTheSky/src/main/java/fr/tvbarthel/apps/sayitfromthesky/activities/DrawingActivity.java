package fr.tvbarthel.apps.sayitfromthesky.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;

import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.fragments.DrawingFragment;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

public class DrawingActivity extends FragmentActivity implements DrawingFragment.Callback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DrawingFragment())
                    .commit();
        }
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.primary_color)));
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

    @Override
    public void saveDrawing(Drawing drawingToSave) {
        // TODO save the drawing into a the database.
        final Intent intent = new Intent(this, DrawingViewerActivity.class);
        intent.putExtra(DrawingViewerActivity.EXTRA_KEY_DRAWING, drawingToSave);
        startActivity(intent);
        finish();
    }
}
