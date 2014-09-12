package fr.tvbarthel.apps.sayitfromthesky.providers.contracts;

import android.net.Uri;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.HashSet;

/**
 * The contract between the drawing provider and applications.
 */
public final class DrawingContract {

    private static final Gson GSON = new Gson();

    public static final String AUTHORITY = "fr.tvbarthel.apps.sayitfromthesky.contentprovider";
    public static final String PATH = "drawing";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

    // Non-instantiability
    private DrawingContract() {
    }

    /**
     * The Columns used for the drawing data model.
     */
    public static final class Columns {

        // Non-instantiability
        private Columns() {
        }

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
    }

}
