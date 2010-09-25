//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.data;

import java.io.File;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.android.todo.R;
import com.android.todo.TagToDoList;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;
import com.android.todo.receivers.AlarmReceiver;
import com.android.todo.receivers.BootReceiver;
import com.android.todo.utils.Utils;

/**
 * This class handles all the interactions with the database. The database
 * contains 2 tables. One of them is called "tags", the other one is called
 * "entries". An entry is what the user visually perceives as a task. Btw this
 * class is a singleton.
 */
public final class ToDoDB extends ADB {
  // Singleton stuff
  private static ToDoDB sInstance = null;

  // key for the written note (if any)
  public static final String KEY_WRITTEN_NOTE = "writtennote";

  // key for the uri of the photo note
  public static final String KEY_PHOTO_NOTE_URI = "photonoteuri";

  // keys useful for the subtasks feature
  public static final String KEY_DEPTH = "depth";
  public static final String KEY_SUBTASKS = "subtasks"; // no. of subtasks
  // some flags to see if there are certain types of notes (1 or 0)
  public static final String KEY_NOTE_IS_WRITTEN = "iswrittennote";
  public static final String KEY_NOTE_IS_GRAPHICAL = "isgraphicalnote";
  public static final String KEY_NOTE_IS_AUDIO = "isaudionote";
  public static final String KEY_NOTE_IS_PHOTO = "isphotonote";

  // key for the secondary tags string (they are concatenated into one big
  // string) (chose String on purpose)
  public static final String KEY_SECONDARY_TAGS = "secondaryTags";

  public static final String KEY_IS_COLLAPSED = "iscollapsed";
  private static final String KEY_CHECKED_TASKS_LIMIT_AWARE = "checkedTasksLimitAware";

  private static final String DB_TAG_TABLE = "tags";
  private static String PRIORITY_ORDER_TOKEN = "";
  private static String ALPHABET_ORDER_TOKEN = "";
  private static String DUEDATE_ORDER_TOKEN = "";
  private static boolean FLAG_UPDATED = false;
  private static boolean FLAG_REPAIRED = false;

  private class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context c) {
      super(c, DB_NAME, null, DATABASE_VERSION);
      mCtx = c;
      res = c.getResources();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      Resources r = res;
      // creating the tag and task tables and inserting a default tag
      db.execSQL("CREATE TABLE " + DB_TAG_TABLE + " (" + KEY_ROWID
          + " integer primary key autoincrement, " + KEY_NAME
          + " text not null);");
      db.execSQL("INSERT INTO " + DB_TAG_TABLE + " (" + KEY_NAME
          + ") VALUES ('" + r.getString(R.string.default_content_tag1) + "')");
      db.execSQL("INSERT INTO " + DB_TAG_TABLE + " (" + KEY_NAME
          + ") VALUES ('" + r.getString(R.string.default_content_tag2) + "')");

      db.execSQL("CREATE TABLE " + DB_TASK_TABLE + " (" + KEY_ROWID
          + " integer primary key autoincrement, " + KEY_NAME
          + " text not null, " + KEY_STATUS + " integer, " + KEY_TAG
          + " text not null);");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_TAG + ") VALUES ('"
          + r.getString(R.string.default_content_entry4) + "',0,'"
          + r.getString(R.string.default_content_tag2) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_TAG + ") VALUES ('"
          + r.getString(R.string.default_content_entry3) + "',0,'"
          + r.getString(R.string.default_content_tag1) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_TAG + ") VALUES ('"
          + r.getString(R.string.default_content_entry2) + "',0,'"
          + r.getString(R.string.default_content_tag1) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_TAG + ") VALUES ('"
          + r.getString(R.string.default_content_entry1) + "',0,'"
          + r.getString(R.string.default_content_tag1) + "')");

      // performing subsequent upgrades to this model
      upgrade(db);
    }

    /**
     * Ensures all upgrade scenarios, by going from each version to the next.
     * 
     * @param db
     *          as an SQLiteDatabase
     */
    public final void upgrade(final SQLiteDatabase db) {
      onUpgrade(db, 73, 74);
      onUpgrade(db, 74, 75);
      onUpgrade(db, 75, 76);
      onUpgrade(db, 76, 78);
      onUpgrade(db, 78, 79);
      onUpgrade(db, 81, 82);
      onUpgrade(db, 85, 86);
      onUpgrade(db, 91, 92);
      onUpgrade(db, 100, 101);
      onUpgrade(db, 101, 102);
      onUpgrade(db, 120, 121);
      // onUpgrade(db, 122, 123); not to be called, this is a one time
      // correction
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // upgrade to db v74 (corresponding to app v1.2.0) or bigger;
      // 4 columns need to be added for task dates (and other possible
      // future extra options).
      if (oldVersion < 74 && newVersion >= 74) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_EXTRA_OPTIONS + " INTEGER");
        } catch (Exception e) {
          // if we are here, it means there has been a downgrade and
          // then an upgrade, we don't need to delete the columns, but
          // we need to prevent an actual exception
        }

        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DUE_YEAR
              + " INTEGER");
        } catch (Exception e) {
        }

        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DUE_MONTH
              + " INTEGER");
        } catch (Exception e) {
        }

        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DUE_DATE
              + " INTEGER");
        } catch (Exception e) {
        }
      }

      // upgrade to db v75 (corresponding to app v1.3.0) or bigger;
      // a column needs to be added for written notes
      if (oldVersion < 75 && newVersion >= 75) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_WRITTEN_NOTE + " TEXT");
        } catch (Exception e) {
        }
      }

      // upgrade to db v76 (corresponding to app v1.5.0) or bigger;
      // one column needs to be added for task priority
      if (oldVersion < 76 && newVersion >= 76) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_PRIORITY
              + " INTEGER DEFAULT 50");
        } catch (Exception e) {
        }
      }

      // upgrade to db v78 (corresponding to app v1.6.2) or bigger;
      // 2 columns need to be added for task times
      if (oldVersion < 78 && newVersion >= 78) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DUE_HOUR
              + " INTEGER");
        } catch (Exception e) {
        }

        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DUE_MINUTE
              + " INTEGER");
        } catch (Exception e) {
        }
      }

      // upgrade to db v79 (corresponding to app v1.6.5) or bigger;
      // 1 column needs to be added for the due day of the week
      if (oldVersion < 79 && newVersion >= 79) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_DUE_DAY_OF_WEEK + " INTEGER DEFAULT -1");
        } catch (Exception e) {
        }
      }

      // upgrade to db v82 (corresponding to app v1.7.0) or bigger;
      // 1 column needs to be added for the depth of the task (part of the
      // subtask feature)
      // 1 column needs to be added for the parent of the eventual subtask
      // (not
      // the tag, but the supertask)
      // 1 column needs to be added for the number of subtasks of a
      // certain task
      // (just primary level)
      if (oldVersion < 82 && newVersion >= 82) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_DEPTH
              + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_SUPERTASK
              + " TEXT");
        } catch (Exception e) {
        }
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_SUBTASKS
              + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
      }

      // upgrade to db v86 (corresponding to app v1.8.0) or bigger;
      // 3 columns have been added, as boolean flags (but still int), to
      // see
      // whether there are certain types of notes
      if (oldVersion < 86 && newVersion >= 86) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_NOTE_IS_WRITTEN + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_NOTE_IS_GRAPHICAL + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_NOTE_IS_AUDIO + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
        final Cursor c = db.query(DB_TASK_TABLE, new String[] { KEY_NAME,
            KEY_WRITTEN_NOTE }, null, null, null, null, null);
        if (c.getCount() > 0) {
          c.moveToFirst();
          do {
            final String taskName = c.getString(0);

            // checking for a written note
            final String writtenNote = c.getString(1);
            if (!"".equals(writtenNote) && writtenNote != null) {
              final ContentValues args = new ContentValues();
              args.put(KEY_NOTE_IS_WRITTEN, 1);
              db.update(DB_TASK_TABLE, args,
                  KEY_NAME + " = '" + taskName + "'", null);
            }

            // checking for a graphical note
            boolean found = true;
            try {
              mCtx.openFileInput(Utils.getImageName(taskName, false));
            } catch (Exception e) {
              found = false;
            }
            if (found) {
              final ContentValues args = new ContentValues();
              args.put(KEY_NOTE_IS_GRAPHICAL, 1);
              db.update(DB_TASK_TABLE, args,
                  KEY_NAME + " = '" + taskName + "'", null);
            }

            // checking for an audio note
            found = true;
            try {
              mCtx.openFileInput(Utils.getAudioName(taskName));
            } catch (Exception e) {
              found = false;
            }
            if (found) {
              final ContentValues args = new ContentValues();
              args.put(KEY_NOTE_IS_AUDIO, 1);
              db.update(DB_TASK_TABLE, args,
                  KEY_NAME + " = '" + taskName + "'", null);
            }
          } while (c.moveToNext());
        }
        c.close();
      }

      // upgrade to db v92 (corresponding to app v1.9.0) or bigger;
      // fixing a legacy bug, some people might not have the KEY_SUBTASKS
      // column
      if (oldVersion < 92 && newVersion >= 92) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_IS_COLLAPSED + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
      }

      // upgrade to db v101 (corresponding to app v3.5.0) or bigger;
      // this is for photo notes
      if (oldVersion < 101 && newVersion >= 101) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_NOTE_IS_PHOTO + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
      }

      // upgrade to db v102 (corresponding to app v3.5.1) or bigger;
      // this is for photo notes
      if (oldVersion < 102 && newVersion >= 102) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_PHOTO_NOTE_URI + " TEXT");
        } catch (Exception e) {
        }
      }

      // upgrade to db v121 (corresponding to app v4.5) or bigger;
      // this is for secondary tags
      if (oldVersion < 121 && newVersion >= 121) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD "
              + KEY_SECONDARY_TAGS + " TEXT");
        } catch (Exception e) {
        }
      }

      // upgrade to db v123 (corresponding to app v4.5) or bigger;
      // changing the way the date is encoded; from (year*12+month)*31+date to
      // (year*12+month)*32+date
      if (oldVersion < 123 && newVersion >= 123) {
        try {
          final Cursor c = db.query(DB_TASK_TABLE, new String[] { KEY_NAME,
              KEY_DUE_DATE }, null, null, null, null, null);
          if (c.getCount() > 0) {
            c.moveToFirst();
            final ContentValues args = new ContentValues();
            do {
              try {
                final int encodedDate = c.getInt(1);
                if (encodedDate > 31 && encodedDate % 31 == 0) {
                  args.put(KEY_DUE_DATE, (encodedDate / 31 - 1) * 32 + 31);
                  db.update(DB_TASK_TABLE, args,
                      KEY_NAME + "='" + c.getString(0) + "'", null);
                }
              } catch (Exception e) {
                if (Analytics.sTracker != null) {
                  Analytics.sTracker.trackPageView(Analytics.EXCEPTION
                      + "/db/v123/inner/" + e.getMessage());
                }
              }
            } while (c.moveToNext());
          }
          c.close();
        } catch (Exception e) {
          Analytics.sTracker.trackPageView(Analytics.EXCEPTION
              + "/db/v123/outer/" + e.getMessage());
        }
      }

      // must be last:
      FLAG_UPDATED = true;
    }
  }

  /**
   * Constructor - takes the context to allow the database to be opened/created.
   * It's private because ToDoDB is a singleton.
   * 
   * @param ctx
   *          the Context within which to work
   */
  private ToDoDB(Context ctx) {
    // storing this context is not normal! Should somehow merge this method
    // with open()
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
  public ToDoDB open() throws SQLException {
    sDbHelper = new DatabaseHelper(mCtx);
    // sDb = sDbHelper.getWritableDatabase();
    if (FLAG_UPDATED && !FLAG_REPAIRED) {
      BootReceiver.setOldAlarms(mCtx, this);
    }
    FLAG_REPAIRED = false;
    return this;
  }

  public void close() {
    sDbHelper.close();
    sInstance = null;
  }

  /**
   * Create a new tag using the provided text. If a tag with the given name
   * exists it will not be created.
   * 
   * @param title
   *          the name of the ToDo List tag, as it will appear visually
   * @return true if insertion went ok, false if such a tag already exists
   */
  public final boolean createTag(final String tagName) {
    final Cursor c = sDbHelper.getWritableDatabase().query(DB_TAG_TABLE,
        new String[] { KEY_NAME }, KEY_NAME + " = '" + tagName + "'", null,
        null, null, null);
    if (c.getCount() > 0) {
      c.close();
      return false;
    }

    // inserting the actual tag
    ContentValues args = new ContentValues();
    args.put(KEY_NAME, tagName);
    sDbHelper.getWritableDatabase().insert(DB_TAG_TABLE, null, args);
    c.close();
    return true;
  }

  /**
   * Creates a database backup on the SD card
   */
  public static final void createBackup() {
    try {
      // checking if the Tag-ToDo folder exists on the sdcard
      final File f = new File(Environment.getExternalStorageDirectory(),
          "/Tag-ToDo_data/");
      if (!f.exists()) {
        try {
          f.mkdirs();
        } catch (Exception e) {

        }
      }

      Utils.copy(new File("/data/data/com.android.todo/databases"), new File(
          Environment.getExternalStorageDirectory(),
          "/Tag-ToDo_data/database_backup"));
    } catch (Exception e) {
      Utils.showDialog(R.string.notification, R.string.backup_fail, mCtx);
    }
  }

  /**
   * Create a new task with the provided name in the provided tag
   * 
   * @param tagName
   *          the parent tag
   * @param task
   *          the newly created task
   * @return null if such a task is inexistent in the to-do list or a String
   *         representing the tag where such a task was found
   */
  public final String createTask(final String tagName, final String task) {
    // checking for duplicate tasks
    final Cursor c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_TAG }, KEY_NAME + "='" + task + "'", null,
        null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      final String s = c.getString(c.getColumnIndexOrThrow(KEY_TAG));
      c.close();
      return s;
    }

    // inserting the actual task
    ContentValues args = new ContentValues();
    args.put(KEY_NAME, task);
    args.put(KEY_STATUS, 0);
    args.put(KEY_TAG, tagName);
    sDbHelper.getWritableDatabase().insert(DB_TASK_TABLE, null, args);
    c.close();
    return null;
  }

  /**
   * Deletes the attached alarm, if any
   * 
   * @param task
   */
  public final void deleteAlarm(final String task) {
    if (isDueTimeSet(task)) {
      ((AlarmManager) mCtx.getSystemService(Context.ALARM_SERVICE))
          .cancel(PendingIntent.getBroadcast(
              mCtx,
              task.hashCode(),
              Utils.getAlarmIntent(new Intent(mCtx, AlarmReceiver.class), task),
              0));
    }
  }

  /**
   * Delete the tag with the given name and all the tasks in it
   * 
   * @param tag
   *          name of the tag to be deleted
   */
  public final void deleteTag(final String tag) {
    sDbHelper.getWritableDatabase().delete(DB_TAG_TABLE,
        KEY_NAME + "='" + tag + "'", null);
    final Cursor tasks = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_TAG }, KEY_TAG + " = '" + tag + "'", null,
        null, null, null);
    if (tasks.getCount() > 0) {
      tasks.moveToFirst();
      final int taskName = tasks.getColumnIndexOrThrow(KEY_NAME);
      do {
        deleteTask(tasks.getString(taskName));
      } while (tasks.moveToNext());
    }
    tasks.close();
  }

  /**
   * Return a Cursor over the list of all tags in the database
   * 
   * @return Cursor over all tags
   */
  public final Cursor getTags() {
    return sDbHelper.getWritableDatabase().query(
        DB_TAG_TABLE,
        new String[] { KEY_ROWID, KEY_NAME },
        null,
        null,
        null,
        null,
        (!"".equals(ALPHABET_ORDER_TOKEN)) ? ALPHABET_ORDER_TOKEN.substring(2)
            : "");
  }

  /**
   * Returns the existing instance of ToDoDB. If it doesn't exist, it's created
   * (and opened).
   * 
   * @param c
   *          The caller context (used if we need to create from scratch)
   * @return A DbHelper instance, useful for DB stuff
   */
  public static final ToDoDB getInstance(final Context c) {
    return sInstance != null ? sInstance : (sInstance = new ToDoDB(c).open());
  }

  /**
   * Return a Cursor over the list of tasks in a certain tag
   * 
   * @param tag
   *          for which to get all the tasks. If null, all the tasks from all
   *          the tags will be returned.
   * @param depth
   *          of tasks (depth 0 is a task, >0 is a subtask). If -1, all tasks,
   *          no matter the depth, will be returned.
   * @param superTask
   *          filters to subtasks of a certain supertask. If null, this filter
   *          won't exist.
   * @return Cursor over all tasks in a tag
   */
  public final Cursor getTasks(final String tag, final int depth,
      final String superTask) {

    return sDbHelper
        .getWritableDatabase()
        .query(
            DB_TASK_TABLE,
            new String[] { KEY_ROWID, KEY_NAME, KEY_STATUS, KEY_TAG,
                KEY_SUBTASKS, KEY_SECONDARY_TAGS },
            ((tag != null ? '(' + KEY_TAG + "='" + tag + "' OR "
                + KEY_SECONDARY_TAGS + " LIKE '%" + tag + "%') " : "1=1 ")
                + (depth != -1 ? "AND " + KEY_DEPTH + "=" + depth + " " : "") + (superTask != null ? "AND "
                + KEY_SUPERTASK + "='" + superTask + "' "
                : ""))
                + "ORDER BY "
                + KEY_STATUS
                + " DESC"
                + PRIORITY_ORDER_TOKEN
                + DUEDATE_ORDER_TOKEN + ALPHABET_ORDER_TOKEN, null, null, null,
            null);
  }

  /**
   * Returns the value of the given key on the given task
   * 
   * @param task
   * @param key
   * @return
   */
  public final int getIntFlag(final String task, final String key) {
    // a try-catch clause should go here
    final Cursor tasks = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, key }, KEY_NAME + "='" + task + "'", null,
        null, null, null);
    tasks.moveToFirst();
    final int value = tasks.getInt(tasks.getColumnIndex(key));
    tasks.close();
    return value;
  }

  /**
   * Returns the value of the given key on the given task
   * 
   * @param task
   * @param key
   * @return
   */
  public final String getStringFlag(final String task, final String key) {
    // a try-catch clause should go here
    final Cursor tasks = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, key }, KEY_NAME + "='" + task + "'", null,
        null, null, null);
    tasks.moveToFirst();
    final String value = tasks.getString(tasks.getColumnIndex(key));
    tasks.close();
    return value != null ? value : "";
  }

  /**
   * Return the priority of the given task
   * 
   * @param task
   * @return priority
   */
  public final int getPriority(final String task) {
    final Cursor taskC = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_ROWID, KEY_NAME, KEY_PRIORITY },
        KEY_NAME + "='" + task + "'", null, null, null, null);
    // for now, assuming we have a task named like this :)
    taskC.moveToFirst();
    try {
      int p = taskC.getInt(taskC.getColumnIndexOrThrow(KEY_PRIORITY));
      taskC.close();
      return p;
    } catch (Exception e) {
      taskC.close();
      return 50;
    }
  }

  /**
   * Return a Cursor over the list of unchecked tasks with due dates which
   * expire 'today' or earlier
   * 
   * @return Cursor
   */
  public final Cursor getDueTasks() {
    Chronos.refresh();
    final int year = Chronos.getYear();
    final int month = Chronos.getMonth();
    return sDbHelper.getWritableDatabase().query(
        DB_TASK_TABLE,
        new String[] { KEY_ROWID, KEY_NAME, KEY_STATUS },
        KEY_EXTRA_OPTIONS + "=1 AND " + KEY_STATUS + "=0 AND (" + KEY_DUE_YEAR
            + "<" + year + " OR (" + KEY_DUE_YEAR + "=" + year + " AND ("
            + KEY_DUE_MONTH + "<" + month + " OR (" + KEY_DUE_MONTH + "="
            + month + " AND " + KEY_DUE_DATE + "<=" + Chronos.getDate()
            + "))))", null, null, null, null);
  }

  /**
   * Return a Cursor over the list of unchecked entries with due dates, from the
   * past or the future
   * 
   * @return Cursor
   */
  public final Cursor getAllDueEntries() {
    return sDbHelper.getWritableDatabase().query(
        DB_TASK_TABLE,
        new String[] { KEY_ROWID, KEY_NAME, KEY_STATUS, KEY_DUE_YEAR,
            KEY_DUE_MONTH, KEY_DUE_DATE },
        KEY_EXTRA_OPTIONS + "= 1 AND " + KEY_STATUS + "=0", null, null, null,
        null);
  }

  /**
   * Return the count of unchecked tasks
   * 
   * @param tag
   *          for which to fetch all the to-do list tasks. If null, it will
   *          return the number of unchecked tasks in all tags
   * @return number of unchecked tasks
   */
  public final int getUncheckedCount(final String tag) {
    final Cursor c = sDbHelper.getWritableDatabase().query(
        DB_TASK_TABLE,
        new String[] {},
        (tag != null ? '(' + KEY_TAG + "='" + tag + "' OR "
            + KEY_SECONDARY_TAGS + " LIKE '%" + tag + "%') AND " : "")
            + KEY_STATUS + "=0", null, null, null, null);
    final int unchecked = c.getCount();
    c.close();
    return unchecked;
  }

  /**
   * Sets the given flag on the given task with the given value.
   * 
   * @param task
   * @param key
   * @param value
   */
  public final void setFlag(final String task, final String key, final int value) {
    final ContentValues args = new ContentValues();
    args.put(key, value);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null);
  }

  /**
   * Sets the given flag on the given task with the given value.
   * 
   * @param task
   * @param key
   * @param value
   */
  public final void setFlag(final String task, final String key,
      final String value) {
    final ContentValues args = new ContentValues();
    args.put(key, value);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null);
  }

  /**
   * Update the tag using the details provided.
   * 
   * @param tagName
   *          name of the tag to update
   * @param newName
   * @return true if the tag was successfully updated, false otherwise
   */
  public boolean updateTag(final String tagName, final String newName) {
    final ContentValues args1 = new ContentValues();
    args1.put(KEY_TAG, newName);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args1,
        KEY_TAG + "='" + tagName + "'", null);
    final ContentValues args2 = new ContentValues();
    args2.put(KEY_NAME, newName);
    return sDbHelper.getWritableDatabase().update(DB_TAG_TABLE, args2,
        KEY_NAME + "='" + tagName + "'", null) > 0;
  }

  /**
   * Wrapper to the second definition of the updateTask method
   * 
   * @param task
   * @param checked
   * @return true if checking this task led to passing the checked tasks limit
   */
  public final boolean updateTask(final String task, final boolean checked) {
    boolean returnValue = false;

    updateTask(task, checked, null); // actual task
    updateTask(task, checked, Boolean.TRUE); // subtasks
    updateTask(task, checked, Boolean.FALSE); // supertasks too

    if (checked) {
      // removing tasks if their number surpasses a certain limit;
      // the removed tasks can only be checked ones (finished)
      final Cursor checkedC = sDbHelper.getWritableDatabase().query(
          DB_TASK_TABLE, new String[] { KEY_NAME, KEY_STATUS },
          KEY_STATUS + " = 1", null, null, null, null);
      final SharedPreferences settings = mCtx.getSharedPreferences(
          TagToDoList.PREFS_NAME, 0);
      int limit = settings.getInt("listSizeLimit", 50);
      if (checkedC.getCount() >= limit) {
        int counter = checkedC.getCount();
        int name = checkedC.getColumnIndexOrThrow(KEY_NAME);
        checkedC.moveToFirst();
        do {
          deleteTask(checkedC.getString(name));
          checkedC.moveToNext();
          counter -= 1;
        } while (counter > limit);
      } else if (checkedC.getCount() == limit - 1
          && !(settings.getBoolean(KEY_CHECKED_TASKS_LIMIT_AWARE, false))) {
        settings.edit().putBoolean(KEY_CHECKED_TASKS_LIMIT_AWARE, true)
            .commit();
        returnValue = true;
      }
      checkedC.close();

      // also need to remove attached alarms, if any
      deleteAlarm(task);
    } else {
      if (isDueTimeSet(task)) {
        // if it is unchecked and has an alarm, it needs to be remade
        final PendingIntent pi = PendingIntent.getBroadcast(mCtx,
            task.hashCode(),
            Utils.getAlarmIntent(new Intent(mCtx, AlarmReceiver.class), task),
            0);
        final AlarmManager am = (AlarmManager) mCtx
            .getSystemService(Context.ALARM_SERVICE);
        final Time t = new Time(getDueTime(task), getDueDayOfWeek(task));
        final Date d = new Date(getDueDate(task));
        if (isDueDateSet(task)) {// single occurence
          Chronos.setSingularAlarm(am, pi, t, d);
        } else {// daily or weekly
          Chronos.setRepeatingAlarm(am, pi, t, d);
        }
      }
    }
    return returnValue;
  }

  /**
   * Updates the specified task with the specified checked status. Can be
   * applied for subtasks and supertasks as well.
   * 
   * @param task
   *          the name of the task to be modified
   * @param checked
   *          the new checked/unchecked status of that task
   * @param goDown
   *          applies the same status to subtasks, if true. If false, it goes
   *          up, to supertasks. If null, only refers to the present task.
   * @return true if successfully updated
   */
  private final void updateTask(final String task, final boolean checked,
      final Boolean goDown) {
    if (goDown == null) {
      ContentValues args = new ContentValues();
      args.put(KEY_STATUS, checked ? 1 : 0);
      sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
          KEY_NAME + "='" + task + "'", null);
    } else if (goDown.equals(Boolean.TRUE)) {
      // applying the same to subtasks
      Cursor c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
          new String[] { KEY_NAME, KEY_SUPERTASK },
          KEY_SUPERTASK + "='" + task + "'", null, null, null, null);
      if (c.getCount() > 0) {
        final int name = c.getColumnIndex(KEY_NAME);
        c.moveToFirst();
        do {
          updateTask(c.getString(name), checked, null);
          updateTask(c.getString(name), checked, Boolean.TRUE);
        } while (c.moveToNext());
      }
      c.close();
    } else {
      // must do the same with supertasks
      Cursor c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
          new String[] { KEY_NAME, KEY_SUPERTASK },
          KEY_NAME + "='" + task + "'", null, null, null, null);
      if (c.getCount() > 0) {
        c.moveToFirst();
        if (!checked) {
          updateTask(c.getString(c.getColumnIndex(KEY_SUPERTASK)), false, null);
          updateTask(c.getString(c.getColumnIndex(KEY_SUPERTASK)), false,
              Boolean.FALSE);
        } else {
          // must only check the supertask if all its subtasks are
          // checked
          /*
           * Cursor subTasksOfSuperTaskC = mDb.query(DB_ENTRY_TABLE, new
           * String[] { KEY_NAME, KEY_SUPERTASK, KEY_STATUS }, KEY_SUPERTASK +
           * "='" + c.getString(c.getColumnIndex(KEY_SUPERTASK)) + "'", null,
           * null, null, null); if (subTasksOfSuperTaskC.getCount() > 0) { final
           * int status = subTasksOfSuperTaskC.getColumnIndex(KEY_STATUS);
           * boolean allChecked = true; subTasksOfSuperTaskC.moveToFirst(); do {
           * if (subTasksOfSuperTaskC.getInt(status) == 0) { allChecked = false;
           * } } while (subTasksOfSuperTaskC.moveToNext()); if (allChecked) {
           * updateEntry(c.getString(c.getColumnIndex(KEY_SUPERTASK)), true,
           * null); updateEntry(c.getString(c.getColumnIndex(KEY_SUPERTASK)),
           * true, Boolean.FALSE); } } subTasksOfSuperTaskC.close();
           */
        }
      }
      c.close();
    }
  }

  /**
   * Updates the specified task with the specified new name.
   * 
   * @param task
   *          The name of the task to be modified
   * @param newName
   *          The new name
   * @return true if successfully updated
   */
  public final void updateTask(final String task, final String newName) {
    // chaging things dependent to the name. The name should be replaced with
    // the ROWID soon...
    new File(Utils.getImageName(task, false)).renameTo(new File(Utils
        .getImageName(newName, false)));
    new File(Utils.getImageName(task, true)).renameTo(new File(Utils
        .getImageName(newName, true)));
    new File(Utils.getAudioName(task)).renameTo(new File(Utils
        .getAudioName(newName)));

    final ContentValues args = new ContentValues();
    args.put(KEY_NAME, newName);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + " = '" + task + "'", null);
    final Cursor subtasks = getTasks(null, -1, task);
    if (subtasks.getCount() > 0) {
      final int name = subtasks.getColumnIndex(KEY_NAME);
      subtasks.moveToFirst();
      do {
        try {
          setSuperTask(subtasks.getString(name), newName);
        } catch (Exception e) {
          // an exception won't be thrown here
        }
      } while (subtasks.moveToNext());
    }
    subtasks.close();
  }

  /**
   * Updates (Moves) the specified task with a new parent (tag). Also takes care
   * of moving the subtasks and of removing the new parent tag from the
   * secondary tags.
   * 
   * @param task
   * @param newParent
   *          the new parent name
   * @param depth
   *          the intended depth of the task
   * @return true if successfully updated
   */
  public final void updateTaskParent(final String task, final String newParent,
      final int depth) {
    final Cursor subtasks = getTasks(null, -1, task);
    if (subtasks.getCount() > 0) {
      final int name = subtasks.getColumnIndex(KEY_NAME);
      // a
      subtasks.moveToFirst();
      do {
        updateTaskParent(subtasks.getString(name), newParent, depth + 1);
      } while (subtasks.moveToNext());
    }
    subtasks.close();
    final ContentValues args = new ContentValues();
    args.put(KEY_TAG, newParent);
    args.put(KEY_DEPTH, depth);
    String secondaryTags = getStringFlag(task, KEY_SECONDARY_TAGS);
    secondaryTags = secondaryTags.replace(newParent, "");
    if (secondaryTags.length() > 0 && secondaryTags.charAt(0) == '\'') {
      secondaryTags = secondaryTags.substring(1);
    }
    if (secondaryTags.endsWith("'")) {
      secondaryTags = secondaryTags.substring(0, secondaryTags.length() - 1);
    }
    secondaryTags = secondaryTags.replace("\'\'", "\'");
    args.put(KEY_SECONDARY_TAGS, secondaryTags);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null);
  }

  /**
   * Updates the specified task with the specified due date
   * 
   * @param task
   * @param date
   * @return true if successfully updated
   */
  public final boolean updateTask(final String task, final Date d) {
    final ContentValues args = new ContentValues();
    args.put(KEY_DUE_YEAR, d.getYear());
    args.put(KEY_DUE_MONTH, d.getMonth());
    args.put(KEY_DUE_DATE, d.getDay());
    return sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + '\'', null) > 0;
  }

  /**
   * Updates the specified task with the specified due time
   * 
   * @param task
   * @param time
   * @return true if successfully updated
   */
  public final boolean updateTask(final String task, final Time t) {
    final ContentValues args = new ContentValues();
    args.put(KEY_DUE_HOUR, t.getHour());
    args.put(KEY_DUE_MINUTE, t.getMinute());
    args.put(KEY_DUE_DAY_OF_WEEK, t.getDayOfWeek());
    return sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + '\'', null) > 0;
  }

  /**
   * Sets the necessity of extra options (due date) for a task. If set to false,
   * the actual date isn't deleted, so it's reusable.
   * 
   * @param task
   * @param b
   * @return
   */
  public final boolean setIsDueDate(final String task, final boolean b) {
    final Cursor c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_EXTRA_OPTIONS },
        KEY_NAME + " = '" + task + "'", null, null, null, null);
    c.moveToFirst();
    final ContentValues args = new ContentValues();
    // the due date is given by the last bit of KEY_EXTRA_OPTIONS
    args.put(
        KEY_EXTRA_OPTIONS,
        b ? (c.getInt(c.getColumnIndex(KEY_EXTRA_OPTIONS)) | 1) : (c.getInt(c
            .getColumnIndex(KEY_EXTRA_OPTIONS)) & 2));
    c.close();
    return sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + " = '" + task + "'", null) > 0;
  }

  /**
   * Sets the necessity of extra options (due time) for a task. If set to false,
   * the actual time isn't deleted, so it's reusable.
   * 
   * @param task
   * @param b
   * @return
   */
  public final boolean setIsDueTime(final String task, final boolean b) {
    final Cursor c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_EXTRA_OPTIONS },
        KEY_NAME + " = '" + task + "'", null, null, null, null);
    c.moveToFirst();
    final ContentValues args = new ContentValues();
    // the due date is given by the last bit of KEY_EXTRA_OPTIONS
    args.put(
        KEY_EXTRA_OPTIONS,
        b ? (c.getInt(c.getColumnIndex(KEY_EXTRA_OPTIONS)) | 2) : (c.getInt(c
            .getColumnIndex(KEY_EXTRA_OPTIONS)) & 5));
    c.close();
    return sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null) > 0;
  }

  /**
   * Sets the priority for a task
   * 
   * @param task
   * @param priority
   * @return
   */
  public final boolean setPriority(final String task, final int priority) {
    final ContentValues args = new ContentValues();
    args.put(KEY_PRIORITY, priority);
    return sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null) > 0;
  }

  /**
   * Sets a written (text) note for the given task
   * 
   * @param task
   * @param note
   * @return
   */
  public final void setWrittenNote(final String task, final String note) {
    final ContentValues args = new ContentValues();
    args.put(KEY_WRITTEN_NOTE, note);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null);
    setFlag(task, KEY_NOTE_IS_WRITTEN, !"".equals(note) && note != null ? 1 : 0);
  }

  /**
   * Sets whether the functions of this class which return Cursors with tasks
   * should also order them by priority
   * 
   * @param order
   * @param ascending
   *          (if false it will be descending)
   */
  public final static void setPriorityOrder(final boolean order,
      final boolean ascending) {
    PRIORITY_ORDER_TOKEN = order ? ", " + KEY_PRIORITY
        + (ascending ? " DESC" : "") : "";
  }

  /**
   * Sets whether the functions of this class which return Cursors with tasks
   * should also order them alphabetically
   * 
   * @param order
   * @param ascending
   *          (if false it will be descending)
   */
  public final static void setAlphabeticalOrder(final boolean order,
      final boolean ascending) {
    ALPHABET_ORDER_TOKEN = order ? ", " + KEY_NAME + (ascending ? " DESC" : "")
        : "";
  }

  /**
   * Sets whether the functions of this class which return Cursors with tasks
   * should also order them by due date
   * 
   * @param order
   * @param ascending
   *          (if false it will be descending)
   */
  public final static void setDueDateOrder(final boolean order,
      final boolean ascending) {
    String direction = ascending ? " DESC" : "";
    DUEDATE_ORDER_TOKEN = order ? ", " + KEY_DUE_YEAR + direction + ", "
        + KEY_DUE_MONTH + direction + ", " + KEY_DUE_DATE + direction : "";
  }

  /**
   * The given task will be subordinate to the new supertask. The supertask will
   * also be made false. ASSUMPTION: this function is called when the task isn't
   * checked
   * 
   * @param task
   * @param superTask
   * @throws Exception
   *           if a task eventually is moved 'under itself'
   */
  public final void setSuperTask(final String task, final String superTask)
      throws Exception {
    // checking subtasks first, we can't move a subtask
    if (task.equals(superTask)) {
      throw new Exception();
    }
    String curTask = superTask;
    Cursor c;
    while ((c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_SUPERTASK },
        KEY_NAME + "='" + curTask + "'", null, null, null, null)).getCount() > 0) {
      c.moveToFirst();
      if (task.equals(curTask = c.getString(c.getColumnIndex(KEY_SUPERTASK)))) {
        throw new Exception();
      }
      c.close();
    }

    c = sDbHelper.getWritableDatabase().query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_DEPTH, KEY_SUBTASKS, KEY_TAG },
        KEY_NAME + "='" + superTask + "'", null, null, null, null);
    c.moveToFirst();
    ContentValues args = new ContentValues();
    args.put(KEY_SUPERTASK, superTask);
    args.put(KEY_DEPTH, c.getInt(c.getColumnIndex(KEY_DEPTH)) + 1);
    updateTaskParent(task, c.getString(c.getColumnIndex(KEY_TAG)),
        c.getInt(c.getColumnIndex(KEY_DEPTH)) + 1);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + task + "'", null);
    args = new ContentValues();
    args.put(KEY_SUBTASKS, c.getInt(c.getColumnIndex(KEY_SUBTASKS)) + 1);
    sDbHelper.getWritableDatabase().update(DB_TASK_TABLE, args,
        KEY_NAME + "='" + superTask + "'", null);
    c.close();
    updateTask(superTask, false, null);
    updateTask(superTask, false, Boolean.FALSE);
  }

  /**
   * Deletes all the entries in the given tag.
   * 
   * @param name
   *          name of the tag to clear of tasks
   * @param checked
   *          if true, will only delete checked tasks
   */
  public final void deleteEntries(final String tag, final boolean checked) {
    final Cursor c = getTasks(tag, -1, null);
    if (c.getCount() > 0) {
      final int name = c.getColumnIndexOrThrow(KEY_NAME);
      final int status = c.getColumnIndex(ToDoDB.KEY_STATUS);
      c.moveToFirst();
      do {
        if (checked) {
          if (c.getInt(status) > 0) {
            deleteTask(c.getString(name));
          }
        } else {
          deleteTask(c.getString(name));
        }
      } while (c.moveToNext());
    }
    c.close();
  }

  /**
   * Returns a Cursor with unchecked tasks.
   * 
   * @param parent
   *          tag
   * @return tasks which are not checked from a certain tag
   */
  public final Cursor getUncheckedTasks(final String tag) {
    return sDbHelper.getWritableDatabase().query(
        DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_STATUS, KEY_TAG },
        (tag != null ? KEY_TAG + " = '" + tag + "' AND " : "") + KEY_STATUS
            + " = 0", null, null, null, null);
  }

  /**
   * Deletes the task with the given name. Also deletes the associated graphical
   * or audio notes and alarms if there are any.
   * 
   * @param task
   *          name of the task to delete
   */
  public final void deleteTask(final String task) {
    // decrementing the subtask count of the supertask
    final Cursor subC = sDbHelper.getWritableDatabase().query(true,
        DB_TASK_TABLE, new String[] { KEY_NAME, KEY_SUPERTASK },
        KEY_NAME + "='" + task + "'", null, null, null, null, null);
    if (subC.getCount() > 0) {
      subC.moveToFirst();
      final Cursor supC = sDbHelper.getWritableDatabase().query(
          true,
          DB_TASK_TABLE,
          new String[] { KEY_NAME, KEY_SUBTASKS },
          KEY_NAME + "='" + subC.getString(subC.getColumnIndex(KEY_SUPERTASK))
              + "'", null, null, null, null, null);
      if (supC.getCount() > 0) {
        supC.moveToFirst();
        final ContentValues args = new ContentValues();
        args.put(KEY_SUBTASKS,
            supC.getInt(supC.getColumnIndex(KEY_SUBTASKS)) - 1);
        sDbHelper.getWritableDatabase().update(
            DB_TASK_TABLE,
            args,
            KEY_NAME + "='"
                + subC.getString(subC.getColumnIndex(KEY_SUPERTASK)) + "'",
            null);
      }
      supC.close();
    }
    subC.close();

    // recursively deleting subtasks, if any:
    final Cursor subtasks = sDbHelper.getWritableDatabase().query(true,
        DB_TASK_TABLE, new String[] { KEY_NAME },
        KEY_SUPERTASK + " = '" + task + "'", null, null, null, null, null);
    if (subtasks.getCount() > 0) {
      subtasks.moveToFirst();
      do {
        deleteTask(subtasks.getString(subtasks.getColumnIndex(KEY_NAME)));
      } while (subtasks.moveToNext());
    }
    subtasks.close();

    // now we actually delete it:
    sDbHelper.getWritableDatabase().delete(DB_TASK_TABLE,
        KEY_NAME + "='" + task + "'", null);

    // the next line is to be removed when Android 2.2 is no longer supported:
    mCtx.deleteFile(Utils.getImageName(task, false));
    new File(Utils.getImageName(task, true));
    new File(Utils.getAudioName(task)).delete();
    deleteAlarm(task);
  }

  /**
   * Attempts to repair the database in case it has an older version than the
   * app or something...
   */
  public final void repair() {
    if (sDbHelper instanceof DatabaseHelper) {
      ((DatabaseHelper) sDbHelper).upgrade(sDbHelper.getWritableDatabase());
      FLAG_REPAIRED = true;
    }
  }

  /**
   * Sanitizes for SQL and what not.
   * 
   * @param s
   * @return
   */
  public final static String sanitize(final String s) {
    return s.replaceAll("'", "`");
  }

}
