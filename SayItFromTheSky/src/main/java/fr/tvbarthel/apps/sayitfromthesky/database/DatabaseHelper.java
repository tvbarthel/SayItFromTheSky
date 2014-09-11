package fr.tvbarthel.apps.sayitfromthesky.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * A simple {@link android.database.sqlite.SQLiteOpenHelper} used to manage the database of this application.
 * <p/>
 * Inspiration from : http://www.vogella.com/tutorials/AndroidSQLite/article.html.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * The name of the database
     */
    private static final String DATABASE_NAME = "say_it_from_the_sky.db";

    /**
     * The current version of the database
     */
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        DrawingTable.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        DrawingTable.upgrade(db, oldVersion, newVersion);
    }
}
