package fr.tvbarthel.apps.sayitfromthesky.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import com.google.android.gms.maps.SupportMapFragment;

/**
 * A Simple {@link SupportMapFragment} that can notify its parent fragment and its activity when its map is ready.
 * <p/>
 * Note that the parent fragment and the activity have to implement {@link fr.tvbarthel.apps.sayitfromthesky.fragments.SayItMapFragment.Callback} to get the notification.
 */
public class SayItMapFragment extends SupportMapFragment {

    /**
     * Default Constructor.
     * <p/>
     * lint [ValidFragment]
     * http://developer.android.com/reference/android/app/Fragment.html#Fragment()
     * Every fragment must have an empty constructor, so it can be instantiated when restoring its activity's state.
     */
    public SayItMapFragment() {
        super();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Fragment parentFragment = getParentFragment();
        final Activity activity = getActivity();

        if (parentFragment instanceof Callback) {
            ((Callback) parentFragment).onMapReady();
        }

        if (activity instanceof Callback) {
            ((Callback) activity).onMapReady();
        }
    }

    /**
     * Interface definition for a callback.
     */
    public interface Callback {
        /**
         * Called when the map is ready to be manipulated.
         */
        public void onMapReady();
    }
}
