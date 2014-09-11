package fr.tvbarthel.apps.sayitfromthesky.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * A simple class that represents a SQL table of {@link fr.tvbarthel.apps.sayitfromthesky.models.Drawing}
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
     * Database creation SQL statement
     */
    private static final String SQL_DATABASE_CREATE = "create table "
            + TABLE_NAME
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITLE + " text not null, "
            + COLUMN_CREATION_TIME + " integer not null,"
            + COLUMN_ENCODED_POLYLINES + "text not null"
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
        Log.w(PathTable.class.getCanonicalName(), "Upgrading database from version" + oldVersion + " to version "
                + newVersion + ", which will destroy all old data");
        database.execSQL("drop table if exists " + TABLE_NAME);
        create(database);
    }


}
