package fr.tvbarthel.apps.sayitfromthesky.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.utils.ActionBarUtils;

/**
 * A simple {@link android.support.v4.app.Fragment} that shows the paths saved by the user.
 */
public class MyPathFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_my_paths, container, false);
        int actionBarSize = ActionBarUtils.getActionBarSize(getActivity());
        fragmentView.setPadding(0, actionBarSize, 0, 0);
        return fragmentView;
    }
}
