//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.todo.data.ADB;
import com.android.todo.olympus.Chronos;

/**
 * This is an activity used to process status bar notification activations
 */
public final class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      final Context c=context.getApplicationContext();
      final BootDB dbHelper = new BootDB(c);
      dbHelper.open();
      BootReceiver.setOldAlarms(c, dbHelper);
      dbHelper.close();
    } catch (Exception e) {
    }
  }

  public final static synchronized void setOldAlarms(final Context context,
      final ADB dbHelper) {
    Cursor c = dbHelper.getUncheckedEntries();
    if (c.getCount() <= 0) {
      c.close();
      return;
    }
    final int name = c.getColumnIndex(BootDB.KEY_NAME);
    c.moveToFirst();
    do {
      String task = c.getString(name);
      if (dbHelper.isDueTimeSet(task)) {
        PendingIntent pi = PendingIntent
            .getBroadcast(context, task.hashCode(), Utils.getAlarmIntent(
                new Intent(context, AlarmReceiver.class), task), 0);
        AlarmManager alarmManager = (AlarmManager) context
            .getSystemService(Context.ALARM_SERVICE);
        if (dbHelper.isDueDateSet(task)) {// single occurence
          long millis = Chronos.getTimeMillis(dbHelper.getDueTime(task), dbHelper
              .getDueDate(task), dbHelper.getDueDayOfWeek(task));
          if (millis > 0) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pi);
          }
        } else {// daily or weekly
          int dayOfWeek = dbHelper.getDueDayOfWeek(task);
          alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Chronos
              .getTimeMillis(dbHelper.getDueTime(task), -1, dayOfWeek),
              86400000L * (dayOfWeek > -1 ? 7 : 1), pi);
        }
      }
    } while (c.moveToNext());
    c.close();
  }
}

/**
 * Same database as ToDoDB, but lighter
 */
final class BootDB extends ADB {

  private DatabaseHelper mDbHelper;

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
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    return this;
  }

  public void close() {
    mDbHelper.close();
  }

}
