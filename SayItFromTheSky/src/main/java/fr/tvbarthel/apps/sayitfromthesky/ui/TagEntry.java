package fr.tvbarthel.apps.sayitfromthesky.ui;

import android.app.Service;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import fr.tvbarthel.apps.sayitfromthesky.R;

public class TagEntry extends RelativeLayout implements View.OnClickListener {

    private TextView mTag;
    private Button mDeleteButton;
    private Callback mCallback;

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
        mDeleteButton = (Button) findViewById(R.id.tag_entry_delete);
        mDeleteButton.setOnClickListener(this);
        mCallback = sDummyCallback;
    }

    public void setTag(String tag) {
        mTag.setText(tag.replace(" ", ""));
    }

    public String getTag() {
        return mTag.getText().toString();
    }

    public void setCallback(Callback callback) {
        mCallback = callback != null ? callback : sDummyCallback;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tag_entry_delete) {
            mCallback.onTagDeletion(this);
        }
    }

    public interface Callback {
        void onTagDeletion(TagEntry tag);
    }

    private static Callback sDummyCallback = new Callback() {
        @Override
        public void onTagDeletion(TagEntry tag) {

        }
    };
}
