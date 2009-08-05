// class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.RemoteViews;

/**
 * Class that takes care of the Widget behavior
 */
public class TagToDoWidget extends AppWidgetProvider {
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
    rv.setOnClickPendingIntent(R.id.widgetLogo, PendingIntent.getActivity(
        context, 0, new Intent(context, TagToDoList.class), 0));
    WidgetDB dbHelper = new WidgetDB(context);
    dbHelper.open();
    rv.setTextViewText(R.id.widgetItem, dbHelper.getAutomaticTasks(3));
    appWidgetManager.updateAppWidget(new ComponentName(context,
        TagToDoWidget.class), rv);
    dbHelper.close();
  }
}

/**
 * Same DB as ToDoListDB.java
 */
final class WidgetDB {

  // name for tags and entries
  public static final String KEY_NAME = "name";
  // checked/unchecked
  public static final String KEY_STATUS = "status";
  // parent tag for an entry
  public static final String KEY_PARENT = "parent";
  // key for the written note (if any)
  public static final String KEY_WRITTEN_NOTE = "writtennote";
  // bit 1 of this integer shows if a due date is set
  public static final String KEY_EXTRA_OPTIONS = "extraoptions";
  public static final String KEY_DUE_DATE = "duedate";
  public static final String KEY_DUE_MONTH = "duemonth";
  public static final String KEY_DUE_YEAR = "dueyear";
  // priority key name
  public static final String KEY_PRIORITY = "priority";
  // a helpful id
  public static final String KEY_ROWID = "_id";

  private static final String DATABASE_NAME = "data";
  private static final String DATABASE_ENTRY_TABLE = "entries";
  private static final int DATABASE_VERSION = 77;
  private static Context mCtx;
  private DatabaseHelper mDbHelper;
  private SQLiteDatabase mDb;
  public static Resources res;
  private static String PRIORITY_ORDER_TOKEN = "";
  private static String ALPHABET_ORDER_TOKEN = "";
  private static String DUEDATE_ORDER_TOKEN = "";

  private static class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
  public WidgetDB(Context ctx) {
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
  public WidgetDB open() throws SQLException {
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    return this;
  }

  public void close() {
    mDbHelper.close();
  }

  /**
   * Calculates which are the most likely tasks to interest the user at a
   * certain point
   * 
   * @param howMany
   *          how many tasks to return
   * @return a String, with the tasks separated by empty lines
   */
  public String getAutomaticTasks(int howMany) {
    StringBuilder sb = new StringBuilder();
    Cursor dueDates = getDueEntries();
    int i = 0;
    if (dueDates.getCount() > 0) {
      int taskText = dueDates.getColumnIndex(KEY_NAME);
      dueDates.moveToFirst();
      for (; i < howMany; i++) {
        sb.append(dueDates.getString(taskText));
        sb.append("\n\n");
        if (!(dueDates.moveToNext())) {
          break;
        }
      }
    }
    dueDates.close();
    if (i < howMany) {
      Cursor entries = getEntries(null);
      if (entries.getCount() > 0) {
        boolean notDone = true;
        entries.moveToFirst();
        int taskText = entries.getColumnIndex(KEY_NAME);
        int status = entries.getColumnIndex(KEY_STATUS);
        for (; i < howMany && notDone; i++, entries.moveToNext()) {
          while (entries.getInt(status) != 0) {
            if (!(entries.moveToNext())) {
              notDone = false;
              break;
            }
          }
          if (!(notDone)) {
            break;
          }
          String s = entries.getString(taskText);
          if (sb.indexOf(s) < 0) {
            sb.append(s);
            sb.append("\n\n");
          }
          if (entries.isLast()) {
            break;
          }
        }
      }
      entries.close();
    }
    return sb.toString();
  }

  /**
   * Return a Cursor over the list of entries in a certain tag
   * 
   * @param tag
   *          for which to get all the to-do list entries. If null, all the
   *          entries from all the tags will be returned.
   * @return Cursor over all entries in a tag
   */
  public Cursor getEntries(String tag) {

    return mDb.query(DATABASE_ENTRY_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        KEY_STATUS, KEY_PARENT }, (tag != null ? KEY_PARENT + " = '" + tag
        + "' " : "1=1 ")
        + "ORDER BY "
        + KEY_STATUS
        + " DESC"
        + PRIORITY_ORDER_TOKEN
        + DUEDATE_ORDER_TOKEN + ALPHABET_ORDER_TOKEN, null, null, null, null);
  }

  /**
   * Return a Cursor over the list of unchecked entries with due dates which
   * expire 'today' or earlier
   * 
   * @return Cursor
   */
  public Cursor getDueEntries() {
    GregorianCalendar gc = new GregorianCalendar();
    int year = gc.get(GregorianCalendar.YEAR);
    int month = gc.get(GregorianCalendar.MONTH);
    return mDb.query(DATABASE_ENTRY_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        KEY_STATUS }, KEY_EXTRA_OPTIONS + " = 1 AND " + KEY_STATUS
        + " = 0 AND (" + KEY_DUE_YEAR + " < " + year + " OR (" + KEY_DUE_YEAR
        + " = " + year + " AND (" + KEY_DUE_MONTH + " < " + month + " OR ("
        + KEY_DUE_MONTH + " = " + month + " AND " + KEY_DUE_DATE + " <= "
        + gc.get(GregorianCalendar.DAY_OF_MONTH) + "))))", null, null, null,
        null);
  }
}

/**
 * This is how a specific data (cursor) provider will look.
 */
abstract interface CursorProvider {
  public Cursor getCursor();

  public int getNameId();
}

/**
 * Provides data about all the tasks.
 */
class GeneralProvider implements CursorProvider {

  public Cursor getCursor() {
    return TagToDoList.getDbHelper().getEntries(null, -1, null);
  }

  public int getNameId() {
    return R.string.all_tasks;
  }
}

/**
 * Provides data about dates due today or earlier.
 */
class DueDateProvider implements CursorProvider {

  public Cursor getCursor() {
    return TagToDoList.getDbHelper().getDueEntries();
  }

  public int getNameId() {
    return R.string.due_dates;
  }
}