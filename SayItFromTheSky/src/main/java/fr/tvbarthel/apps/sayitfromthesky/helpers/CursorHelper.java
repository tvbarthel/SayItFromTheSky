package fr.tvbarthel.apps.sayitfromthesky.helpers;

import android.database.Cursor;

import com.google.gson.Gson;

import java.util.Arrays;

import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;
import fr.tvbarthel.apps.sayitfromthesky.providers.contracts.DrawingContract;

/**
 * Static utility methods for dealing with cursor.
 */
public final class CursorHelper {

    private static final Gson GSON = new Gson();

    // Non-instantiability
    private CursorHelper() {
    }

    public static String getString(Cursor cursor, String columnName, String defaultValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) return defaultValue;
        return cursor.getString(columnIndex);
    }

    public static long getLong(Cursor cursor, String columnName, long defaultValue) {
        final int columnIndex = cursor.getColumnIndex(columnName);
        if (columnIndex == -1) return defaultValue;
        return cursor.getLong(columnIndex);
    }

    public static Drawing cursorToDrawing(Cursor cursor) {
        final String title = CursorHelper.getString(cursor, DrawingContract.Columns.COLUMN_TITLE, "");
        final long creationTime = CursorHelper.getLong(cursor, DrawingContract.Columns.COLUMN_CREATION_TIME, 0l);
        final String encodedPolylines = CursorHelper.getString(cursor, DrawingContract.Columns.COLUMN_ENCODED_POLYLINES, "");
        final String[] polylines = GSON.fromJson(encodedPolylines, String[].class);
        return new Drawing(title, creationTime, Arrays.asList(polylines));
    }

}
