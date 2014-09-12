package fr.tvbarthel.apps.sayitfromthesky.helpers;

import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * Static methods for dealing with views.
 */
public final class ViewHelper {

    // Non-instantiability
    private ViewHelper() {
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static void removeOnGlobalLayoutListener(View view, ViewTreeObserver.OnGlobalLayoutListener listener) {
        if (view == null || listener == null) return;
        final ViewTreeObserver viewTreeObserver = view.getViewTreeObserver();
        if (!viewTreeObserver.isAlive()) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeGlobalOnLayoutListener(listener);
        } else {
            viewTreeObserver.removeOnGlobalLayoutListener(listener);
        }
    }
}
