package fr.tvbarthel.apps.sayitfromthesky.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class MyPathContentProvider extends ContentProvider {

    // Used for the UriMatcher
    private static final int PATHS = 10;
    private static final int PATH_ID = 20;
    private static final String AUTHORITY = "fr.vbarthel.apps.sayitfromthesky.database";
    private static final String BASE_PATH = "paths";

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/paths";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/path";

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH, PATHS);
        URI_MATCHER.addURI(AUTHORITY, BASE_PATH + "/#", PATH_ID);
    }

    // database
    private PathDatabaseHelper mDatabaseHelper;


    @Override
    public boolean onCreate() {
        mDatabaseHelper = new PathDatabaseHelper(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Using SQLiteQueryBuilder instead of query() methods.
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exist.
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(PathTable.TABLE_PATH);

        int uriType = URI_MATCHER.match(uri);
        switch (uriType) {
            case PATHS:
                // Nothing to add to the query
                break;

            case PATH_ID:
                // Add the id to the where request
                queryBuilder.appendWhere(PathTable.COLUMN_ID + " = " + uri.getLastPathSegment());
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase writableDatabase = mDatabaseHelper.getWritableDatabase();
        Cursor cursorResult = queryBuilder.query(writableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
        cursorResult.setNotificationUri(getContext().getContentResolver(), uri);

        return cursorResult;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = mDatabaseHelper.getWritableDatabase();
        long id = 0;

        switch (uriType) {
            case PATHS:
                id = writableDatabase.insert(PathTable.TABLE_PATH, null, values);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted = 0;

        switch (uriType) {
            case PATHS:
                rowsDeleted = writableDatabase.delete(PathTable.TABLE_PATH, selection, selectionArgs);
                break;

            case PATH_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = writableDatabase.delete(PathTable.TABLE_PATH, PathTable.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = writableDatabase.delete(PathTable.TABLE_PATH, PathTable.COLUMN_ID + "=" + id + " and "
                            + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = mDatabaseHelper.getWritableDatabase();
        int rowsUpdated = 0;

        switch (uriType) {
            case PATHS:
                rowsUpdated = writableDatabase.update(PathTable.TABLE_PATH, values, selection, selectionArgs);
                break;

            case PATH_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = writableDatabase.update(PathTable.TABLE_PATH, values, PathTable.COLUMN_ID + "="
                            + id, null);
                } else {
                    rowsUpdated = writableDatabase.update(PathTable.TABLE_PATH, values, PathTable.COLUMN_ID + "="
                            + id + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(final String[] projection) {
        String[] available = {PathTable.COLUMN_DESCRIPTION, PathTable.COLUMN_ENCODED_PATHS,
                PathTable.COLUMN_TITLE, PathTable.COLUMN_ID};
        if (projection != null && projection.length > 0) {
            final HashSet<String> requestColumns = new HashSet<String>(Arrays.asList(projection));
            final HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            if (!availableColumns.containsAll(requestColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection.");
            }
        }
    }
}
