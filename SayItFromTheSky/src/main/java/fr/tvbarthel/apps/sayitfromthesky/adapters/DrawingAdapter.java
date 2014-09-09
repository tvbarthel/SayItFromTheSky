package fr.tvbarthel.apps.sayitfromthesky.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import fr.tvbarthel.apps.sayitfromthesky.R;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

/**
 * A simple {@link android.widget.ArrayAdapter} for {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}.
 */
public class DrawingAdapter extends ArrayAdapter<Drawing> {

    private LayoutInflater mInflater;

    public DrawingAdapter(Context context, List<Drawing> objects) {
        super(context, R.layout.drawing_entry, objects);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Drawing drawing = getItem(position);
        ViewHolder viewHolder;

        if (convertView != null) {
            viewHolder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.drawing_entry, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        viewHolder.mTitle.setText(drawing.getTitle());

        return convertView;
    }

    static class ViewHolder {
        @InjectView(R.id.drawing_entry_title)
        TextView mTitle;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
