package fr.tvbarthel.apps.sayitfromthesky.ui;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {

    private float mPaddingTop;

    public MyScrollView(Context context) {
        super(context);
        mPaddingTop = getPaddingTop();
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaddingTop = getPaddingTop();
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mPaddingTop = getPaddingTop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getY() > (mPaddingTop - getScrollY()) || getScrollY() >= mPaddingTop) {
            return super.onTouchEvent(ev);
        } else {
            return false;
        }
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        mPaddingTop = top;
    }
}
