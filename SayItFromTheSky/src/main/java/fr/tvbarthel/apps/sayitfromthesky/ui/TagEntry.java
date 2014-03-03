package fr.tvbarthel.apps.sayitfromthesky.ui;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.tvbarthel.apps.sayitfromthesky.R;

public class TagEntry extends RelativeLayout {

    private TextView mTag;

    public TagEntry(Context context) {
        super(context);
        init(context);
    }

    public TagEntry(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TagEntry(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        setGravity(CENTER_VERTICAL);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.tag_entry, this, true);
        mTag = (TextView) findViewById(R.id.tag_entry_tag);
    }

    public void setTag(String tag) {
        mTag.setText(tag.replace(" ", ""));
    }
}
