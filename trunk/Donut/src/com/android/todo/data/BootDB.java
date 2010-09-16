package com.android.todo.data;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Same database as ToDoDB, but lighter
 */
public final class BootDB extends ADB {

  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DB_NAME, null, DATABASE_VERSION);
      mCtx = context;
      res = context.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
  }

  /**
   * Constructor - takes the context to allow the database to be opened/created
   * 
   * @param ctx
   *          the Context within which to work
   */
  public BootDB(Context ctx) {
    mCtx = ctx;
  }

  /**
   * Open the database. If it cannot be opened, try to create a new instance of
   * the database. If it cannot be created, throw an exception to signal the
   * failure
   * 
   * @return this (self reference, allowing this to be chained in an
   *         initialization call)
   * @throws SQLException
   *           if the database could be neither opened or created
   */
  public BootDB open() throws SQLException {
    sDbHelper = new DatabaseHelper(mCtx);
    sDb = sDbHelper.getWritableDatabase();
    return this;
  }

  public void close() {
    sDbHelper.close();
  }

}
