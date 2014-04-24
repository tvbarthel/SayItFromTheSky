package fr.tvbarthel.apps.sayitfromthesky.utils;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * A util class used for the action bar.
 */
public class ActionBarUtils {

    /**
     * Get the action bar size in pixel.
     *
     * @param context the {@link android.content.Context} used to retrieve the theme.
     * @return
     */
    public static int getActionBarSize(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.actionBarSize});
        return (int) styledAttributes.getDimension(0, 0);
    }
}
