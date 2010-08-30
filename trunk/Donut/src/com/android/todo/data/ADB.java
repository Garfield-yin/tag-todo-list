package com.android.todo.data;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * This is an abstraction of the actual database we'll use throughout the app.
 * This class is inherited by the big (main) database class, ToDoDB, and by
 * BootDB, which is a smaller, lighter version for resetting alarms on boot.
 * 
 * @author Teo
 * 
 */
public abstract class ADB {

  /**
   * Needs to be increased with every push of a live version, to prevent
   * deletion of alarms. The name of the attribute (DATABASE_VERSION) IS NOT TO
   * BE CHANGED!
   */
  public static final int DATABASE_VERSION = 120;

  protected static Context mCtx;
  public SQLiteDatabase mDb;
  public static Resources res;

  /**
   * Name of the DB
   */
  protected static final String DB_NAME = "data";

  /**
   * Name of the table containing tasks
   */
  public static final String DB_TASK_TABLE = "entries";

  /**
   * Name key for tags and tasks
   */
  public static final String KEY_NAME = "name";

  /**
   * The checked/unchecked status of a task
   */
  public static final String KEY_STATUS = "status";

  /**
   * The parent of a task (tag)
   */
  public static final String KEY_TAG = "parent";

  /**
   * The supertask of a task
   */
  public static final String KEY_SUPERTASK = "supertask";

  /**
   * Bit 1 (lsb) of this integer shows if a due date is set. Bit 2 of this
   * integer shows if a due time is set.
   */
  public static final String KEY_EXTRA_OPTIONS = "extraoptions";

  public static final String KEY_DUE_DATE = "duedate";
  public static final String KEY_DUE_MONTH = "duemonth";
  public static final String KEY_DUE_YEAR = "dueyear";
  public static final String KEY_DUE_HOUR = "duehour";
  public static final String KEY_DUE_MINUTE = "dueminute";
  public static final String KEY_DUE_DAY_OF_WEEK = "dueday";

  /**
   * Priority of the task
   */
  public static final String KEY_PRIORITY = "priority";

  /**
   * A helpful ID
   */
  public static final String KEY_ROWID = "_id";

  /**
   * Checks whether a certain task exists.
   * 
   * @param task
   * @return
   */
  public final boolean isTask(final String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] {}, KEY_NAME
        + "='" + task + "'", null, null, null, null);
    final boolean b = entry.getCount() > 0;
    entry.close();
    return b;
  }

  /**
   * Returns an int which contains all the necessary information. It is encoded
   * like this: (year*12+month)*31+day
   * 
   * @param task
   * @return encoded date
   */
  public final int getDueDate(final String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_DUE_YEAR, KEY_DUE_MONTH, KEY_DUE_DATE }, KEY_NAME
        + " = '" + task + "'", null, null, null, null);
    // for now, assuming we have a task named like this :)
    entry.moveToFirst();
    final int e = 372 * entry.getInt(entry.getColumnIndex(KEY_DUE_YEAR)) + 31
        * entry.getInt(entry.getColumnIndex(KEY_DUE_MONTH))
        + entry.getInt(entry.getColumnIndex(KEY_DUE_DATE));
    entry.close();
    return e;
  }

  /**
   * Returns the attached day of the week
   * 
   * @param task
   * @return 0 if Monday, ..., 6 is Sunday and -1 if there isn't a due day of
   *         the week set
   */
  public int getDueDayOfWeek(String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_DUE_DAY_OF_WEEK }, KEY_NAME + " = '" + task + "'", null,
        null, null, null);
    // for now, assuming we have a task named like this :)
    entry.moveToFirst();
    final int d = entry.getInt(entry.getColumnIndex(KEY_DUE_DAY_OF_WEEK));
    entry.close();
    return d;
  }

  /**
   * Returns an int which contains all the necessary information. It is encoded
   * like this: hour*60+minute
   * 
   * @param task
   * @return encoded date
   */
  public int getDueTime(String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_DUE_HOUR, KEY_DUE_MINUTE }, KEY_NAME + " = '" + task
        + "'", null, null, null, null);
    // for now, assuming we have a task named like this :)
    entry.moveToFirst();
    final int e = 60 * entry.getInt(entry.getColumnIndex(KEY_DUE_HOUR))
        + entry.getInt(entry.getColumnIndex(KEY_DUE_MINUTE));
    entry.close();
    return e;
  }

  /**
   * Gets the tasks which are not checked.
   * 
   * @return a Cursor over the tasks which are not checked
   */
  public Cursor getUncheckedEntries() {
    return mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        KEY_STATUS }, KEY_STATUS + "=0", null, null, null, null);
  }

  /**
   * Verifies if a due date is actually set for an entry.
   * 
   * @param task
   * @return true, if a due date has been set
   */
  public final boolean isDueDateSet(final String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_EXTRA_OPTIONS }, KEY_NAME + " = '" + task + "'", null,
        null, null, null);
    if (entry.getCount() == 0) {
      entry.close();
      return false;
    }
    entry.moveToFirst();
    // the due date is given by the last bit of KEY_EXTRA_OPTIONS
    final boolean b = entry.getInt(entry.getColumnIndex(KEY_EXTRA_OPTIONS)) % 2 == 1;
    entry.close();
    return b;
  }

  /**
   * Verifies if a due time is actually set for an entry.
   * 
   * @param task
   * @return true, if a due time has been set
   */
  public final boolean isDueTimeSet(final String task) {
    final Cursor entry = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_EXTRA_OPTIONS }, KEY_NAME + " = '" + task + "'", null,
        null, null, null);
    if (entry.getCount() == 0) {
      entry.close();
      return false;
    }
    entry.moveToFirst();
    // the due time is given by the second last bit of KEY_EXTRA_OPTIONS
    final boolean b = (entry.getInt(entry.getColumnIndex(KEY_EXTRA_OPTIONS)) >> 1) % 2 == 1;
    entry.close();
    return b;
  }
}
