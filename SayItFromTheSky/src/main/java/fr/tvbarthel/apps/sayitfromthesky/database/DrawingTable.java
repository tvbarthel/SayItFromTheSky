package fr.tvbarthel.apps.sayitfromthesky.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;

import fr.tvbarthel.apps.sayitfromthesky.helpers.CursorHelper;
import fr.tvbarthel.apps.sayitfromthesky.models.Drawing;

/**
 * A simple class that represents a SQL table of {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}
 * <p/>
 * Inspiration from : http://www.vogella.com/tutorials/AndroidSQLite/article.html.
 */
public final class DrawingTable {

    /**
     * The name of the table
     */
    public static final String TABLE_NAME = "drawing_table";
    /**
     * The name of the id column
     */
    public static final String COLUMN_ID = "_id";
    /**
     * The name of the title column
     */
    public static final String COLUMN_TITLE = "title";
    /**
     * The name of the creation time column
     */
    public static final String COLUMN_CREATION_TIME = "creation_time";
    /**
     * The name of the encoded polylines column
     */
    public static final String COLUMN_ENCODED_POLYLINES = "encoded_polylines";
    /**
     * An array of all the columns available.
     */
    public static final String[] COLUMNS_AVAILABLE = {COLUMN_ID, COLUMN_TITLE, COLUMN_CREATION_TIME, COLUMN_ENCODED_POLYLINES};

    private static final Gson GSON = new Gson();

    /**
     * Database creation SQL statement
     */
    private static final String SQL_DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CREATION_TIME + " integer not null,"
            + COLUMN_ENCODED_POLYLINES + " text not null"
            + ");";

    // Non-instantiability
    private DrawingTable() {
    }

    /**
     * Create a DrawingTable.
     * Execute the SQL_DATABASE_CREATE statement.
     *
     * @param database the {@link android.database.sqlite.SQLiteDatabase} where the table will be created.
     */
    public static void create(SQLiteDatabase database) {
        database.execSQL(SQL_DATABASE_CREATE);
    }

    /**
     * Update a DrawingTable.
     * Destroy all old data and create a new empty table.
     *
     * @param database   the {@link android.database.sqlite.SQLiteDatabase} from where the table will be updated.
     * @param oldVersion an int representing the old version of the table.
     * @param newVersion an int representing the new version of the table.
     */
    public static void upgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(DrawingTable.class.getCanonicalName(), "Upgrading database from version" + oldVersion + " to version "
                + newVersion + ", which will destroy all old data");
        database.execSQL("drop table if exists " + TABLE_NAME);
        create(database);
    }

    /**
     * Check if the columns in the projection are actual columns.
     *
     * @param projection the columns to check
     */
    public static void checkColumns(final String[] projection) {
        if (projection != null && projection.length > 0) {
            final HashSet<String> requestColumns = new HashSet<String>(Arrays.asList(projection));
            final HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(COLUMNS_AVAILABLE));
            if (!availableColumns.containsAll(requestColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection.");
            }
        }
    }

    public static Drawing convertCursorToDrawing(Cursor cursor) {
        final String title = CursorHelper.getString(cursor, COLUMN_TITLE, "");
        final long creationTime = CursorHelper.getLong(cursor, COLUMN_CREATION_TIME, 0l);
        final String encodedPolylines = CursorHelper.getString(cursor, COLUMN_ENCODED_POLYLINES, "");
        final String[] polylines = GSON.fromJson(encodedPolylines, String[].class);
        return new Drawing(title, creationTime, Arrays.asList(polylines));
    }

    public static ContentValues drawingToContentValue(Drawing drawing) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, drawing.getTitle());
        contentValues.put(COLUMN_CREATION_TIME, drawing.getCreationTimeInMillis());
        contentValues.put(COLUMN_ENCODED_POLYLINES, GSON.toJson(drawing.getEncodedPolylines().toArray()));
        return contentValues;
    }
}
