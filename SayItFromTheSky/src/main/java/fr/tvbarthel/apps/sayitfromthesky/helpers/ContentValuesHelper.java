package fr.tvbarthel.apps.sayitfromthesky.helpers;

import android.content.ContentValues;

import com.google.gson.Gson;

import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;
import fr.tvbarthel.apps.sayitfromthesky.providers.contracts.DrawingContract;

/**
 * Static utility methods for dealing with {@link android.content.ContentValues}.
 */
public final class ContentValuesHelper {

    private static final Gson GSON = new Gson();

    // Non-instantiability
    private ContentValuesHelper() {
    }

    /**
     * Convert a {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing} to {@link android.content.ContentValues}.
     *
     * @param drawing the {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing} to be converted.
     * @return a new {@link android.content.ContentValues} representing the {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}.
     */
    public static ContentValues drawingToContentValues(Drawing drawing) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(DrawingContract.Columns.COLUMN_TITLE, drawing.getTitle());
        contentValues.put(DrawingContract.Columns.COLUMN_CREATION_TIME, drawing.getCreationTimeInMillis());
        contentValues.put(DrawingContract.Columns.COLUMN_ENCODED_POLYLINES, GSON.toJson(drawing.getEncodedPolylines().toArray()));
        return contentValues;
    }

}
