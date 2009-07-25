//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This is an activity used to process status bar notification activations
 */
public final class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		BootDB dbHelper = new BootDB(context);
		dbHelper.open();
		BootReceiver.setOldAlarms(context, dbHelper);
		dbHelper.close();
	}

	public final static void setOldAlarms(Context context, DB dbHelper) {
		Cursor c = dbHelper.getUncheckedEntries();
		if (c.getCount() <= 0) {
			return;
		}
		int name = c.getColumnIndex(BootDB.KEY_NAME);
		c.moveToFirst();
		do {
			String task = c.getString(name);
			if (dbHelper.isDueTimeSet(task)) {
				PendingIntent pi = PendingIntent.getBroadcast(context, task
						.hashCode(), Utils.getAlarmIntent(new Intent(context,
						AlarmReceiver.class), task), 0);
				AlarmManager alarmManager = (AlarmManager) context
						.getSystemService(Context.ALARM_SERVICE);
				if (dbHelper.isDueDateSet(task)) {// single occurence
					long millis = Utils.getTimeMillis(
							dbHelper.getDueTime(task), dbHelper
									.getDueDate(task), dbHelper
									.getDueDayOfWeek(task));
					if (millis > 0) {
						alarmManager.set(AlarmManager.RTC_WAKEUP, millis, pi);
					}
				} else {// daily or weekly
					int dayOfWeek = dbHelper.getDueDayOfWeek(task);
					alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Utils
							.getTimeMillis(dbHelper.getDueTime(task), -1,
									dayOfWeek), 86400000 * (dayOfWeek > -1 ? 7
							: 1), pi);
				}
			}
		} while (c.moveToNext());
	}
}

interface DB {
	boolean isDueDateSet(String task);

	boolean isDueTimeSet(String task);

	int getDueDate(String task);

	int getDueTime(String task);

	int getDueDayOfWeek(String task);

	Cursor getUncheckedEntries();
}

/**
 * Same DB as ToDoListDB.java
 */
final class BootDB implements DB {

	// name for tags and entries
	public static final String KEY_NAME = "name";
	// checked/unchecked
	public static final String KEY_STATUS = "status";
	// parent tag for an entry
	public static final String KEY_PARENT = "parent";
	// bit 1 of this integer shows if a due date is set
	public static final String KEY_EXTRA_OPTIONS = "extraoptions";
	public static final String KEY_DUE_DATE = "duedate";
	public static final String KEY_DUE_MONTH = "duemonth";
	public static final String KEY_DUE_YEAR = "dueyear";
	public static final String KEY_DUE_HOUR = "duehour";
	public static final String KEY_DUE_MINUTE = "dueminute";
	public static final String KEY_DUE_DAY_OF_WEEK = "dueday";
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
	 * Constructor - takes the context to allow the database to be
	 * opened/created
	 * 
	 * @param ctx
	 *            the Context within which to work
	 */
	public BootDB(Context ctx) {
		mCtx = ctx;
	}

	/**
	 * Open the database. If it cannot be opened, try to create a new instance
	 * of the database. If it cannot be created, throw an exception to signal
	 * the failure
	 * 
	 * @return this (self reference, allowing this to be chained in an
	 *         initialization call)
	 * @throws SQLException
	 *             if the database could be neither opened or created
	 */
	public BootDB open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	/**
	 * Return a Cursor over the list of unchecked entries in a certain tag
	 * 
	 * @return
	 */
	public Cursor getUncheckedEntries() {
		return mDb.query(DATABASE_ENTRY_TABLE, new String[] { KEY_ROWID,
				KEY_NAME, KEY_STATUS }, KEY_STATUS + " = 0", null, null, null,
				null);
	}

	/**
	 * Returns the attached day of the week (0 is monday)
	 * 
	 * @param entryName
	 * @return
	 */
	public int getDueDayOfWeek(String entryName) {
		Cursor entry = mDb.query(DATABASE_ENTRY_TABLE, new String[] {
				KEY_ROWID, KEY_NAME, KEY_DUE_DAY_OF_WEEK }, KEY_NAME + " = '"
				+ entryName + "'", null, null, null, null);
		// for now, assuming we have a task named like this :)
		entry.moveToFirst();
		return entry.getInt(entry.getColumnIndex(KEY_DUE_DAY_OF_WEEK));
	}

	/**
	 * Returns an int which contains all the necessary information. It is
	 * encoded like this: (year*12+month)*31+day
	 * 
	 * @param entryName
	 * @return encoded date
	 */
	public int getDueDate(String entryName) {
		Cursor entry = mDb.query(DATABASE_ENTRY_TABLE,
				new String[] { KEY_ROWID, KEY_NAME, KEY_DUE_YEAR,
						KEY_DUE_MONTH, KEY_DUE_DATE }, KEY_NAME + " = '"
						+ entryName + "'", null, null, null, null);
		// for now, assuming we have a task named like this :)
		entry.moveToFirst();
		return 372 * entry.getInt(entry.getColumnIndex(KEY_DUE_YEAR)) + 31
				* entry.getInt(entry.getColumnIndex(KEY_DUE_MONTH))
				+ entry.getInt(entry.getColumnIndex(KEY_DUE_DATE));
	}

	/**
	 * Returns an int which contains all the necessary information. It is
	 * encoded like this: hour*60+minute
	 * 
	 * @param entryName
	 * @return encoded date
	 */
	public int getDueTime(String entryName) {
		Cursor entry = mDb.query(DATABASE_ENTRY_TABLE, new String[] {
				KEY_ROWID, KEY_NAME, KEY_DUE_HOUR, KEY_DUE_MINUTE }, KEY_NAME
				+ " = '" + entryName + "'", null, null, null, null);
		// for now, assuming we have a task named like this :)
		entry.moveToFirst();
		return 60 * entry.getInt(entry.getColumnIndex(KEY_DUE_HOUR))
				+ entry.getInt(entry.getColumnIndex(KEY_DUE_MINUTE));
	}

	/**
	 * Verifies if a due date is actually set for an entry
	 * 
	 * @param task
	 * @return true, if a due date has been set
	 */
	public boolean isDueDateSet(String task) {
		Cursor entry = mDb.query(DATABASE_ENTRY_TABLE, new String[] {
				KEY_ROWID, KEY_NAME, KEY_EXTRA_OPTIONS }, KEY_NAME + " = '"
				+ task + "'", null, null, null, null);
		if (entry.getCount() == 0) {
			return false;
		}
		entry.moveToFirst();
		// the due date is given by the last bit of KEY_EXTRA_OPTIONS
		return entry.getInt(entry.getColumnIndex(KEY_EXTRA_OPTIONS)) % 2 == 1;
	}

	/**
	 * Verifies if a due time is actually set for an entry
	 * 
	 * @param task
	 * @return true, if a due time has been set
	 */
	public boolean isDueTimeSet(String task) {
		Cursor entry = mDb.query(DATABASE_ENTRY_TABLE, new String[] {
				KEY_ROWID, KEY_NAME, KEY_EXTRA_OPTIONS }, KEY_NAME + " = '"
				+ task + "'", null, null, null, null);
		if (entry.getCount() == 0) {
			return false;
		}
		entry.moveToFirst();
		// the due time is given by the second last bit of KEY_EXTRA_OPTIONS
		return (entry.getInt(entry.getColumnIndex(KEY_EXTRA_OPTIONS)) >> 1) % 2 == 1;
	}

}
