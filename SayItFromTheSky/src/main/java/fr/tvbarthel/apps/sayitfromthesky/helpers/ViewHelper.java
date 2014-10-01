package fr.tvbarthel.apps.sayitfromthesky.helpers;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;

/**
 * Static methods for dealing with views.
 */
public final class ViewHelper {

    public static final int DEFAULT_ANIMATION_DURATION = 400;
    public static final int DEFAULT_ANIMATION_DELAY = 300;

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


    /**
     * Do a slide animation of the view from out of the screen to its position.
     *
     * @param view     The {@link android.view.View} to animate.
     * @param duration The length of the duration, in milliseconds.
     * @param delay    The amount of time, in milliseconds, to delay the animation after the call of this methods.
     */
    public static void slideFromBottom(final View view, final int duration, final int delay) {
        // Get the parent view or the root view.
        final View parentView = getParentView(view);

        ViewTreeObserver vto = view.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewHelper.removeOnGlobalLayoutListener(view, this);
                    // Compute and initialize the translation
                    final int translationY = parentView.getHeight() - view.getTop();
                    view.setTranslationY(translationY);

                    // Animate the translation
                    final ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", translationY, 0);
                    animator.setDuration(duration);
                    animator.setInterpolator(new DecelerateInterpolator());
                    animator.setStartDelay(delay);
                    animator.start();
                }
            });
            view.requestLayout();
        }
    }

    /**
     * Do a slide animation of the view from out of the screen to its position.
     * Call {@link #slideFromBottom(android.view.View, int, int)} with default values.
     *
     * @param view The {@link android.view.View} to animate.
     */
    public static void slideFromBottom(View view) {
        slideFromBottom(view, DEFAULT_ANIMATION_DURATION, DEFAULT_ANIMATION_DELAY);
    }

    private static View getParentView(View view) {
        if (view.getParent() instanceof View) {
            return (View) view.getParent();
        } else {
            return view.getRootView();
        }
    }
}
