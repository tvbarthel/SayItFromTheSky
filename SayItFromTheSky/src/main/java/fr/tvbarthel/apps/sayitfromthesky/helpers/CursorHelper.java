package fr.tvbarthel.apps.sayitfromthesky.helpers;

import android.database.Cursor;

/**
 * Static utility methods for dealing with cursor.
 */
public final class CursorHelper {

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

}
