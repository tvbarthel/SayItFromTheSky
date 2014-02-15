package fr.tvbarthel.apps.sayitfromthesky.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class PathDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pathtable.db";
    private static final int DATABASE_VERSION = 1;

    public PathDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        PathTable.onCreate(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        PathTable.onUpgrade(database, oldVersion, newVersion);
    }
}
