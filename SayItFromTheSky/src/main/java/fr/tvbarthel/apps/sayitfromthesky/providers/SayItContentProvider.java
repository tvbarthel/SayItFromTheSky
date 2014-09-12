package fr.tvbarthel.apps.sayitfromthesky.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import fr.tvbarthel.apps.sayitfromthesky.database.DatabaseHelper;
import fr.tvbarthel.apps.sayitfromthesky.database.DrawingTable;
import fr.tvbarthel.apps.sayitfromthesky.providers.contracts.DrawingContract;

/**
 * A simple {@link android.content.ContentProvider} used to access data of this application.
 * <p/>
 * Inspiration from : http://www.vogella.com/tutorials/AndroidSQLite/article.html
 */
public class SayItContentProvider extends ContentProvider {

    // Used for the UriMatcher
    private static final int DRAWINGS = 10;
    private static final int DRAWING_ID = 20;

    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(DrawingContract.AUTHORITY, DrawingContract.PATH, DRAWINGS);
        URI_MATCHER.addURI(DrawingContract.AUTHORITY, DrawingContract.PATH + "/#", DRAWING_ID);
    }

    // database
    protected DatabaseHelper mDatabaseHelper;

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() methods.
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        int uriType = URI_MATCHER.match(uri);

        switch (uriType) {

            case DRAWING_ID:
                queryBuilder.appendWhere(DrawingContract.Columns.COLUMN_ID + " = " + uri.getLastPathSegment());
            case DRAWINGS:
                queryBuilder.setTables(DrawingTable.TABLE_NAME);
                DrawingContract.Columns.checkColumns(projection);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase readableDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor cursorResult = queryBuilder.query(readableDatabase, projection, selection, selectionArgs, null, null, sortOrder);
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
        Uri result = null;

        switch (uriType) {
            case DRAWINGS:
                id = writableDatabase.insert(DrawingTable.TABLE_NAME, null, values);
                result = Uri.parse(DrawingContract.PATH + "/" + id);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = URI_MATCHER.match(uri);
        SQLiteDatabase writableDatabase = mDatabaseHelper.getWritableDatabase();
        int rowsDeleted;

        switch (uriType) {
            case DRAWINGS:
                rowsDeleted = writableDatabase.delete(DrawingTable.TABLE_NAME, selection, selectionArgs);
                break;

            case DRAWING_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = writableDatabase.delete(DrawingTable.TABLE_NAME, DrawingContract.Columns.COLUMN_ID + "=" + id, null);
                } else {
                    rowsDeleted = writableDatabase.delete(DrawingTable.TABLE_NAME, DrawingContract.Columns.COLUMN_ID + "=" + id + " and "
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
            case DRAWINGS:
                rowsUpdated = writableDatabase.update(DrawingTable.TABLE_NAME, values, selection, selectionArgs);
                break;

            case DRAWING_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = writableDatabase.update(DrawingTable.TABLE_NAME, values, DrawingContract.Columns.COLUMN_ID + "="
                            + id, null);
                } else {
                    rowsUpdated = writableDatabase.update(DrawingTable.TABLE_NAME, values, DrawingContract.Columns.COLUMN_ID + "="
                            + id + " and " + selection, selectionArgs);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}
