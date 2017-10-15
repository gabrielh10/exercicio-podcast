package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PodcastProvider extends ContentProvider {
    private PodcastDBHelper dbhelper;

    public PodcastProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String selection = PodcastProviderContract.EPISODE_LINK + " = ?";
        String[] selectionArgs = new String[]{values.getAsString(PodcastDBHelper.EPISODE_LINK)};
        long id = dbhelper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
        if (id == 0) {
            values.put(PodcastDBHelper.EPISODE_FILE_URI, "Nulo");
            id = dbhelper.getWritableDatabase().insert(PodcastDBHelper.DATABASE_TABLE, null, values);
        }
        return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, String.valueOf(id));
    }

    @Override
    public boolean onCreate() {
        dbhelper = PodcastDBHelper.getInstance(getContext());
        if(dbhelper != null) return true;
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        cursor = dbhelper.getReadableDatabase().query(PodcastDBHelper.DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return dbhelper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE, values, selection, selectionArgs);
    }
}
