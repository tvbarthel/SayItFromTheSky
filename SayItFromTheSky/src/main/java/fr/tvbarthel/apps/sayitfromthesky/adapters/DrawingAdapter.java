package fr.tvbarthel.apps.sayitfromthesky.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.helpers.CursorHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

/**
 * A simple {@link android.widget.ArrayAdapter} for {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}.
 */
public class DrawingAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private DateFormat mDateFormat;

    public DrawingAdapter(Context context) {
        super(context, null, false);
        mInflater = LayoutInflater.from(context);
        mDateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.drawing_entry, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        bind(holder, cursor);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        bind((ViewHolder) view.getTag(), cursor);
    }

    private void bind(ViewHolder holder, Cursor cursor) {
        final Drawing drawing = CursorHelper.cursorToDrawing(cursor);
        holder.title.setText(drawing.getTitle());
        holder.creationTime.setText(mDateFormat.format(drawing.getCreationTimeInMillis()));
    }

    static class ViewHolder {
        @InjectView(R.id.drawing_entry_title)
        TextView title;
        @InjectView(R.id.drawing_entry_creation_time)
        TextView creationTime;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
