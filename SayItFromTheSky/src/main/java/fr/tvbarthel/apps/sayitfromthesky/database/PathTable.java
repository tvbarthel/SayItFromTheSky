package fr.tvbarthel.apps.sayitfromthesky.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class PathTable {

    // Database table
    public static final String TABLE_PATH = "path_table";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ENCODED_PATHS = "encoded_paths";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DESCRIPTION = "description";

    // Database creation SQL statement
    private static final String SQL_DATABASE_CREATE = "create table "
            + TABLE_PATH
            + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_ENCODED_PATHS + " text not null, "
            + COLUMN_TITLE + " text, "
            + COLUMN_DESCRIPTION + "text "
            + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_DATABASE_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(PathTable.class.getCanonicalName(), "Upgrading database from version" + oldVersion + " to version "
                + newVersion + ", which will destroy all old data");
        database.execSQL("drop table is exists " + TABLE_PATH);
        onCreate(database);
    }
}
