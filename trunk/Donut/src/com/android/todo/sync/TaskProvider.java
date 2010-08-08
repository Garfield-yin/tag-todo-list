package com.android.todo.sync;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.android.todo.data.ToDoDB;

public class TaskProvider extends ContentProvider {
  public static final String AUTHORITY = "com.android.teo.sync.provider";

  private static final String[] COLUMN_NAMES = new String[] { ToDoDB.KEY_ROWID,
      SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2,
      SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
  private static final int URI_TAGS = 1;
  private static final int URI_TASKS = 2;
  private static final int URI_SPECIFIC_TAG = 3;
  private static final int URI_TASKS_DUE = 4;
  private static final int URI_TASKS_DUE_TODAY = 5;

  private static UriMatcher sUriMatcher;
  
  static {
    sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    sUriMatcher.addURI(AUTHORITY, "tags", URI_TAGS);
    sUriMatcher.addURI(AUTHORITY, "tasks", URI_TASKS);
    sUriMatcher.addURI(AUTHORITY, "tags/*", URI_SPECIFIC_TAG);
    sUriMatcher.addURI(AUTHORITY, "tasks/due", URI_TASKS_DUE);
    sUriMatcher.addURI(AUTHORITY, "tasks/due/present", URI_TASKS_DUE_TODAY);
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    final int match = sUriMatcher.match(uri);
    final ToDoDB dbHelper = ToDoDB.getInstance(this.getContext()
        .getApplicationContext());
    switch (match) {
      case URI_TASKS:
        return dbHelper.getTasks(null, -1, null);
      case URI_TAGS:
        return dbHelper.getAllTags();
      case URI_SPECIFIC_TAG:
        return dbHelper.getTasks(uri.getLastPathSegment(), -1, null);
      case URI_TASKS_DUE:
        return dbHelper.getAllDueEntries();
      case URI_TASKS_DUE_TODAY:
        return dbHelper.getDueEntries();
      default:
        // QSB search?
        final String searchString = uri.getLastPathSegment().toLowerCase();
        final MatrixCursor cursor = new MatrixCursor(COLUMN_NAMES);
        final Cursor c = dbHelper.getTasks(null, -1, null);
        if (c.getCount() > 0) {
          final int idIndex = c.getColumnIndex(ToDoDB.KEY_ROWID);
          final int nameIndex = c.getColumnIndex(ToDoDB.KEY_NAME);
          final int tagIndex = c.getColumnIndex(ToDoDB.KEY_PARENT);
          c.moveToFirst();
          do {
            final String task = c.getString(nameIndex);
            if (task.toLowerCase().contains(searchString)) {
              final Object[] rowObject = new Object[] { c.getInt(idIndex),
                  task, c.getString(tagIndex), task };
              cursor.addRow(rowObject);
            }
          } while (c.moveToNext());
        }
        return cursor;
    }
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public boolean onCreate() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    // TODO Auto-generated method stub
    return 0;
  }

}
