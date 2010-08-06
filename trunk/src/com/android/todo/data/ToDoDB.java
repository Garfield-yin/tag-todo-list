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

import com.android.todo.AlarmReceiver;
import com.android.todo.BootReceiver;
import com.android.todo.R;
import com.android.todo.TagToDoList;
import com.android.todo.Utils;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;

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
  public static final String KEY_SUPERTASK = "supertask";
  public static final String KEY_SUBTASKS = "subtasks";
  // some flags to see if there are certain types of notes
  public static final String KEY_NOTE_IS_WRITTEN = "iswrittennote";
  public static final String KEY_NOTE_IS_GRAPHICAL = "isgraphicalnote";
  public static final String KEY_NOTE_IS_AUDIO = "isaudionote";
  public static final String KEY_NOTE_IS_PHOTO = "isphotonote";
  // if 1 it means the task is collapsed
  public static final String KEY_COLLAPSED = "iscollapsed";
  private static final String KEY_CHECKED_TASKS_LIMIT_AWARE = "checkedTasksLimitAware";

  private static final String DB_TAG_TABLE = "tags";
  private DatabaseHelper mDbHelper;
  private static String PRIORITY_ORDER_TOKEN = "";
  private static String ALPHABET_ORDER_TOKEN = "";
  private static String DUEDATE_ORDER_TOKEN = "";
  private static boolean FLAG_UPDATED = false;

  private class DatabaseHelper extends SQLiteOpenHelper {

    DatabaseHelper(Context context) {
      super(context, DB_NAME, null, DATABASE_VERSION);
      mCtx = context;
      res = context.getResources();
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
          + " text not null, " + KEY_STATUS + " integer, " + KEY_PARENT
          + " text not null);");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_PARENT + ") VALUES ('"
          + r.getString(R.string.default_content_entry4) + "',0,'"
          + r.getString(R.string.default_content_tag2) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_PARENT + ") VALUES ('"
          + r.getString(R.string.default_content_entry3) + "',0,'"
          + r.getString(R.string.default_content_tag1) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_PARENT + ") VALUES ('"
          + r.getString(R.string.default_content_entry2) + "',0,'"
          + r.getString(R.string.default_content_tag1) + "')");
      db.execSQL("INSERT INTO " + DB_TASK_TABLE + " (" + KEY_NAME + ", "
          + KEY_STATUS + ", " + KEY_PARENT + ") VALUES ('"
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
    public void upgrade(SQLiteDatabase db) {
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
              mCtx.openFileInput(Utils.getImageName(taskName));
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
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_COLLAPSED
              + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
      }

      // upgrade to db v101 (corresponding to app v3.5.0) or bigger;
      // this is for photo notes
      if (oldVersion < 101 && newVersion >= 101) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_NOTE_IS_PHOTO
              + " INTEGER DEFAULT 0");
        } catch (Exception e) {
        }
      }
      
   // upgrade to db v102 (corresponding to app v3.5.1) or bigger;
      // this is for photo notes
      if (oldVersion < 102 && newVersion >= 102) {
        try {
          db.execSQL("ALTER TABLE " + DB_TASK_TABLE + " ADD " + KEY_PHOTO_NOTE_URI
              + " TEXT");
        } catch (Exception e) {
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
    mDbHelper = new DatabaseHelper(mCtx);
    mDb = mDbHelper.getWritableDatabase();
    if (FLAG_UPDATED) {
      BootReceiver.setOldAlarms(mCtx, this);
    }
    return this;
  }

  public void close() {
    mDbHelper.close();
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
  public boolean createTag(final String tagName) {
    final Cursor c = mDb.query(DB_TAG_TABLE, new String[] { KEY_NAME },
        KEY_NAME + " = '" + tagName + "'", null, null, null, null);
    if (c.getCount() > 0) {
      c.close();
      return false;
    }

    // inserting the actual tag
    ContentValues args = new ContentValues();
    args.put(KEY_NAME, tagName);
    mDb.insert(DB_TAG_TABLE, null, args);
    c.close();
    return true;
  }

  /**
   * Creates a database backup on the SD card
   */
  public static final void createBackup() {
    try {
      // checking if the Tag-ToDo folder exists on the sdcard
      final File f = new File("/sdcard/Tag-ToDo_data/");
      if (f.exists() == false) {
        try {
          f.mkdirs();
        } catch (Exception e) {

        }
      }

      Utils.copy(new File("/data/data/com.android.todo/databases"), new File(
          "/sdcard/Tag-ToDo_data/database_backup"));
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
    // checking for duplicate entries
    final Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
        KEY_PARENT }, KEY_NAME + " = '" + task + "'", null, null, null, null);
    if (c.getCount() > 0) {
      c.moveToFirst();
      String s = c.getString(c.getColumnIndexOrThrow(KEY_PARENT));
      c.close();
      return s;
    }

    // inserting the actual task
    ContentValues args = new ContentValues();
    args.put(KEY_NAME, task);
    args.put(KEY_STATUS, 0);
    args.put(KEY_PARENT, tagName);
    mDb.insert(DB_TASK_TABLE, null, args);
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
    mDb.delete(DB_TAG_TABLE, KEY_NAME + "='" + tag + "'", null);
    final Cursor tasks = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
        KEY_PARENT }, KEY_PARENT + " = '" + tag + "'", null, null, null, null);
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
  public final Cursor getAllTags() {
    return mDb.query(DB_TAG_TABLE, new String[] { KEY_ROWID, KEY_NAME }, null,
        null, null, null,
        ALPHABET_ORDER_TOKEN != "" ? ALPHABET_ORDER_TOKEN.substring(2) : "");
  }

  /**
   * Returns the existing instance of ToDoDB. If it doesn't exist, it's created
   * (and opened).
   * 
   * @param c
   *          The caller context (used if we need to create from scratch)
   * @return A DbHelper instance, useful for DB stuff
   */
  public static final ToDoDB getInstance(Context c) {
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

    return mDb
        .query(
            DB_TASK_TABLE,
            new String[] { KEY_ROWID, KEY_NAME, KEY_STATUS, KEY_PARENT,
                KEY_SUBTASKS },
            ((tag != null ? KEY_PARENT + " = '" + tag + "' " : "1=1 ")
                + (depth != -1 ? "AND " + KEY_DEPTH + " = " + depth + " " : "") + (superTask != null ? "AND "
                + KEY_SUPERTASK + " = '" + superTask + "' "
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
    final Cursor tasks = mDb.query(DB_TASK_TABLE,
        new String[] { KEY_NAME, key }, KEY_NAME + "='" + task + "'", null,
        null, null, null);
    tasks.moveToFirst();
    final int value = tasks.getInt(1);
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
    final Cursor tasks = mDb.query(DB_TASK_TABLE,
        new String[] { KEY_NAME, key }, KEY_NAME + "='" + task + "'", null,
        null, null, null);
    tasks.moveToFirst();
    final String value = tasks.getString(1);
    tasks.close();
    return value;
  }

  /**
   * Return the priority of the given task
   * 
   * @param task
   * @return priority
   */
  public int getPriority(final String task) {
    final Cursor taskC = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_PRIORITY }, KEY_NAME + " = '" + task + "'", null, null,
        null, null);
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
   * Return a Cursor over the list of unchecked entries with due dates which
   * expire 'today' or earlier
   * 
   * @return Cursor
   */
  public final Cursor getDueEntries() {
    Chronos.refresh();
    final int year = Chronos.getYear();
    final int month = Chronos.getMonth();
    return mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        KEY_STATUS }, KEY_EXTRA_OPTIONS + " = 1 AND " + KEY_STATUS
        + " = 0 AND (" + KEY_DUE_YEAR + " < " + year + " OR (" + KEY_DUE_YEAR
        + " = " + year + " AND (" + KEY_DUE_MONTH + " < " + month + " OR ("
        + KEY_DUE_MONTH + " = " + month + " AND " + KEY_DUE_DATE + " <= "
        + Chronos.getDate() + "))))", null, null, null, null);
  }

  /**
   * Return a Cursor over the list of unchecked entries with due dates, from the
   * past or the future
   * 
   * @return Cursor
   */
  public final Cursor getAllDueEntries() {
    return mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID, KEY_NAME,
        KEY_STATUS, KEY_DUE_YEAR, KEY_DUE_MONTH, KEY_DUE_DATE },
        KEY_EXTRA_OPTIONS + " = 1 AND " + KEY_STATUS + " = 0", null, null,
        null, null);
  }

  /**
   * Returns the written note for the given task.
   * 
   * @param task
   * @return written note
   */
  public final String getWrittenNote(final String task) {
    final Cursor taskCursor = mDb.query(DB_TASK_TABLE, new String[] {
        KEY_ROWID, KEY_NAME, KEY_WRITTEN_NOTE },
        KEY_NAME + " = '" + task + "'", null, null, null, null);
    // for now, assuming we have a task named like this :)
    taskCursor.moveToFirst();
    String note = taskCursor.getString(taskCursor
        .getColumnIndexOrThrow(KEY_WRITTEN_NOTE));
    taskCursor.close();
    return note;
  }

  /**
   * Return the count of unchecked entries in a tag
   * 
   * @param tag
   *          for which to fetch all the to-do list entries
   * @return number of unchecked entries (tasks left to do)
   */
  public int countUncheckedEntries(String tag) {
    final Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_STATUS, KEY_PARENT }, KEY_PARENT + " = '" + tag + "'",
        null, null, null, null);
    final int value = c.getColumnIndexOrThrow(KEY_STATUS);
    int unchecked = 0;
    if (c.getCount() > 0) {
      c.moveToFirst();
      do {
        if (c.getInt(value) == 0) {
          unchecked += 1;
        }
      } while (c.moveToNext());
    }
    c.close();
    return unchecked;
  }

  /**
   * Return the count of unchecked entries in ALL tags
   * 
   * @return number of unchecked entries (tasks left to do)
   */
  public final int countUncheckedEntries() {
    final Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_ROWID,
        KEY_NAME, KEY_STATUS, KEY_PARENT }, KEY_STATUS + " = 0", null, null,
        null, null);
    final int count = c.getCount();
    c.close();
    return count;
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
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
  }
  
  /**
   * Sets the given flag on the given task with the given value.
   * 
   * @param task
   * @param key
   * @param value
   */
  public final void setFlag(final String task, final String key, final String value) {
    final ContentValues args = new ContentValues();
    args.put(key, value);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
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
    args1.put(KEY_PARENT, newName);
    mDb.update(DB_TASK_TABLE, args1, KEY_PARENT + " = '" + tagName + "'", null);
    final ContentValues args2 = new ContentValues();
    args2.put(KEY_NAME, newName);
    return mDb.update(DB_TAG_TABLE, args2, KEY_NAME + " = '" + tagName + "'",
        null) > 0;
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
      final Cursor checkedC = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
          KEY_STATUS }, KEY_STATUS + " = 1", null, null, null, null);
      final SharedPreferences settings = mCtx.getSharedPreferences(
          TagToDoList.PREFS_NAME, 50);
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
      mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
    } else if (goDown.equals(Boolean.TRUE)) {
      // applying the same to subtasks
      Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
          KEY_SUPERTASK }, KEY_SUPERTASK + "='" + task + "'", null, null, null,
          null);
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
      Cursor c = mDb
          .query(DB_TASK_TABLE, new String[] { KEY_NAME, KEY_SUPERTASK },
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
   * Updates the specified entry with the specified new name.
   * 
   * @param taskName
   *          The name of the entry to be modified
   * @param newName
   *          The new name
   * @return true if successfully updated
   */
  public final void updateTask(final String taskName, final String newName) {
    ContentValues args = new ContentValues();
    args.put(KEY_NAME, newName);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + taskName + "'", null);
    final Cursor subtasks = getTasks(null, 1, taskName);
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
   * of moving the subtasks.
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
      subtasks.moveToFirst();
      do {
        updateTaskParent(subtasks.getString(name), newParent, depth + 1);
      } while (subtasks.moveToNext());
    }
    subtasks.close();
    final ContentValues args = new ContentValues();
    args.put(KEY_PARENT, newParent);
    args.put(KEY_DEPTH, depth);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
  }

  /**
   * Updates the specified task with the specified due date
   * 
   * @param task
   * @param year
   * @param month
   * @param date
   * @return true if successfully updated
   */
  public final boolean updateTask(final String task, final int year,
      final int month, final int date) {
    final ContentValues args = new ContentValues();
    args.put(KEY_DUE_YEAR, year);
    args.put(KEY_DUE_MONTH, month);
    args.put(KEY_DUE_DATE, date);
    // args.put(KEY_EXTRA_OPTIONS, 1);
    return mDb
        .update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null) > 0;
  }

  /**
   * Updates the specified task with the specified due time
   * 
   * @param task
   * @param year
   * @param month
   * @param date
   * @return true if successfully updated
   */
  public final boolean updateTask(final String task, final int hour,
      final int minute) {
    final ContentValues args = new ContentValues();
    args.put(KEY_DUE_HOUR, hour);
    args.put(KEY_DUE_MINUTE, minute);
    return mDb
        .update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null) > 0;
  }

  /**
   * Attaches a day of the week to a periodic task
   * 
   * @param task
   * @param dayOfWeek
   */
  public final void updateTask(final String task, final int dayOfWeek) {
    final ContentValues args = new ContentValues();
    args.put(KEY_DUE_DAY_OF_WEEK, dayOfWeek);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
  }

  /**
   * Sets the necessity of extra options (due date) for a task. If set to false,
   * the actual date isn't deleted, so it's reusable.
   * 
   * @param task
   * @param b
   * @return
   */
  public final boolean setDueDate(final String task, final boolean b) {
    final Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
        KEY_EXTRA_OPTIONS }, KEY_NAME + " = '" + task + "'", null, null, null,
        null);
    c.moveToFirst();
    final ContentValues args = new ContentValues();
    // the due date is given by the last bit of KEY_EXTRA_OPTIONS
    args.put(
        KEY_EXTRA_OPTIONS,
        b ? (c.getInt(c.getColumnIndex(KEY_EXTRA_OPTIONS)) | 1) : (c.getInt(c
            .getColumnIndex(KEY_EXTRA_OPTIONS)) & 2));
    c.close();
    return mDb
        .update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null) > 0;
  }

  /**
   * Sets the necessity of extra options (due time) for a task. If set to false,
   * the actual time isn't deleted, so it's reusable.
   * 
   * @param task
   * @param b
   * @return
   */
  public final boolean setDueTime(final String task, final boolean b) {
    final Cursor c = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME,
        KEY_EXTRA_OPTIONS }, KEY_NAME + " = '" + task + "'", null, null, null,
        null);
    c.moveToFirst();
    final ContentValues args = new ContentValues();
    // the due date is given by the last bit of KEY_EXTRA_OPTIONS
    args.put(
        KEY_EXTRA_OPTIONS,
        b ? (c.getInt(c.getColumnIndex(KEY_EXTRA_OPTIONS)) | 2) : (c.getInt(c
            .getColumnIndex(KEY_EXTRA_OPTIONS)) & 5));
    c.close();
    return mDb
        .update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null) > 0;
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
    return mDb
        .update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null) > 0;
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
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
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
    while ((c = mDb.query(DB_TASK_TABLE,
        new String[] { KEY_NAME, KEY_SUPERTASK }, KEY_NAME + " = '" + curTask
            + "'", null, null, null, null)).getCount() > 0) {
      c.moveToFirst();
      if (task.equals(curTask = c.getString(1))) {
        throw new Exception();
      }
    }

    c = mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME, KEY_DEPTH,
        KEY_SUBTASKS, KEY_PARENT }, KEY_NAME + " = '" + superTask + "'", null,
        null, null, null);
    c.moveToFirst();
    ContentValues args = new ContentValues();
    args.put(KEY_SUPERTASK, superTask);
    args.put(KEY_DEPTH, c.getInt(1) + 1);
    updateTaskParent(task, c.getString(3), c.getInt(1) + 1);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + task + "'", null);
    args = new ContentValues();
    args.put(KEY_SUBTASKS, c.getInt(2) + 1);
    mDb.update(DB_TASK_TABLE, args, KEY_NAME + " = '" + superTask + "'", null);
    c.close();
    updateTask(superTask, false, null);
    updateTask(superTask, false, Boolean.FALSE);
  }

  /**
   * Deletes all the entries in the given tag.
   * 
   * @param name
   *          name of the tag to clear of tasks
   */
  public void deleteEntries(String tag) {
    Cursor c = getTasks(tag, -1, null);
    if (c.getCount() > 0) {
      int name = c.getColumnIndexOrThrow(KEY_NAME);
      c.moveToFirst();
      do {
        deleteTask(c.getString(name));
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
    return mDb.query(DB_TASK_TABLE, new String[] { KEY_NAME, KEY_STATUS,
        KEY_PARENT }, (tag != null ? KEY_PARENT + " = '" + tag + "' AND " : "")
        + KEY_STATUS + " = 0", null, null, null, null);
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
    final Cursor subC = mDb.query(true, DB_TASK_TABLE, new String[] { KEY_NAME,
        KEY_SUPERTASK }, KEY_NAME + "='" + task + "'", null, null, null, null,
        null);
    if (subC.getCount() > 0) {
      subC.moveToFirst();
      final Cursor supC = mDb.query(true, DB_TASK_TABLE, new String[] {
          KEY_NAME, KEY_SUBTASKS },
          KEY_NAME + "='" + subC.getString(subC.getColumnIndex(KEY_SUPERTASK))
              + "'", null, null, null, null, null);
      if (supC.getCount() > 0) {
        supC.moveToFirst();
        final ContentValues args = new ContentValues();
        args.put(KEY_SUBTASKS,
            supC.getInt(supC.getColumnIndex(KEY_SUBTASKS)) - 1);
        mDb.update(
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
    final Cursor subtasks = mDb.query(true, DB_TASK_TABLE,
        new String[] { KEY_NAME }, KEY_SUPERTASK + " = '" + task + "'", null,
        null, null, null, null);
    if (subtasks.getCount() > 0) {
      subtasks.moveToFirst();
      do {
        deleteTask(subtasks.getString(0));
      } while (subtasks.moveToNext());
    }
    subtasks.close();

    // now we actually delete it:
    mDb.delete(DB_TASK_TABLE, KEY_NAME + "='" + task + "'", null);
    mCtx.deleteFile(Utils.getImageName(task));
    new File(Utils.getAudioName(task)).delete();
    deleteAlarm(task);
  }

  /**
   * Attempts to repair the database in case it has an older version than the
   * app or something...
   */
  public final void repair() {
    mDbHelper.upgrade(mDb);
  }

}
