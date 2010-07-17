//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;

import com.android.todo.data.Analytics;
import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Chronos;
import com.android.todo.speech.TTS;
import com.android.todo.sync.GoogleCalendar;
import com.android.todo.widget.TagToDoWidget;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * This is the main activity. It shows the main UI elements, including the ToDo
 * list entries.
 */
public class TagToDoList extends Activity {
  // Activities:
  public static final int ACTIVITY_CREATE_TAG = 1;
  public static final int ACTIVITY_EDIT_TAG = 2;
  public static final int ACTIVITY_DELETE_TAG = 3;
  public static final int ACTIVITY_INSTRUCTIONS = 4;
  public static final int ACTIVITY_CREATE_ENTRY = 5;
  public static final int ACTIVITY_EDIT_ENTRY = 6;
  public static final int ACTIVITY_DRAW_NOTE = 7;
  public static final int ACTIVITY_CLEAR_ENTRIES = 8;
  public static final int ACTIVITY_WRITE_NOTE = 9;
  public static final int ACTIVITY_BACKUP_IMPORT = 10;
  // Menu IDs:
  public static final int TAG_INSERT_ID = 1;
  public static final int TAG_REMOVE_ID = 2;
  public static final int TAG_EDIT_ID = 3;
  public static final int TAG_HELP_ID = 4;
  public static final int TAG_CLEAR_ID = 5;
  public static final int TAG_UNINDENT_ID = 6;
  public static final int TAG_IMPORTBACKUP_ID = 7;
  // Task menu IDs:
  public static final int ENTRY_EDIT_ID = 1;
  public static final int ENTRY_REMOVE_ID = 2;
  public static final int ENTRY_GRAPHICAL_ID = 3;
  public static final int ENTRY_AUDIO_ID = 4;
  public static final int ENTRY_DOWN_ID = 5;
  // public static final int ENTRY_CLOSE_ID = 6;
  public static final int ENTRY_MOVE_ID = 7;
  public static final int ENTRY_WRITTEN_ID = 8;
  public static final int ENTRY_INSTANTACTION_ID = 9;
  public static final int ENTRY_SUBTASK_ID = 10;
  public static final int ENTRY_MOVE_UNDER_TASK_ID = 11;
  public static final int ENTRY_EMAIL_ID = 12;
  public static final int ENTRY_SMS_ID = 13;

  public static final String PREFS_NAME = "TagToDoListPrefs";
  private static final String PRIORITY_SORT = "prioritySorting";
  private static final String ALPHABET_SORT = "alphabeticalSorting";
  private static final String DUEDATE_SORT = "dueDateSorting";
  private static final String HIDE_CHECKED_SORT = "hideChecked";

  // Flags (ideally, should be eliminated sometime in the future)
  public static boolean SYNC_GCAL;
  public static boolean SHINY_PRIORITY;
  public static boolean BLIND_MODE = false;
  public static boolean HIDE_CHECKED;
  public static boolean SHOW_NOTES;
  public static boolean SHOW_COLLAPSE;
  public static boolean SHOW_DUE_TIME;

  /**
   * A local flag - if true, clicking a task won't check it. Useful, for
   * example, when the user wants to move a task under another task.
   */
  private static boolean CHOICE_MODE = false;

  public static TTS sTts = null; // text to speech
  private static GestureDetector sGestureDetector;
  private static OnTouchListener sGestureListener;
  private static OnClickListener sDescriptionClickListener;
  private static ToDoDB sDbHelper;
  private static SharedPreferences sSettings;
  private static GoogleAnalyticsTracker sTracker = null;
  private Spinner mTagSpinner;
  private LinearLayout mEntryLayout;
  private ArrayAdapter<CharSequence> mTagsArrayAdapter;
  private Button mStatButton;
  private Button mAddEntryButton;
  private ScrollView mScrollView;
  private String mContextEntry;
  private Action mContextAction;
  private int mActiveEntry; // useful only in keyboard mode
  private int mMaxPriority; // useful only with shiny priority :)

  @Override
  public void onCreate(Bundle icicle) {
    sSettings = getSharedPreferences(PREFS_NAME, 0);
    setTheme(sSettings.getInt(ConfigScreen.THEME, android.R.style.Theme));
    super.onCreate(icicle);
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .cancel(2);
    setContentView(R.layout.main);
    mTagSpinner = (Spinner) findViewById(R.id.tagSpinner);
    mTagSpinner
        .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> parent, View v,
              int position, long id) {
            mActiveEntry = -1;
            selectTag(position);
            if (BLIND_MODE) {
              if (sTts != null) {
                sTts.speak(mTagsArrayAdapter.getItem(
                    mTagSpinner.getSelectedItemPosition()).toString());
              }
            }
          }

          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
    mTagSpinner.setOnLongClickListener(new View.OnLongClickListener() {
      public boolean onLongClick(View arg0) {
        ((Spinner) arg0).setVisibility(View.GONE);
        final ImageButton configButton = ((ImageButton) findViewById(R.id.configButton));
        configButton.setVisibility(View.GONE);
        mAddEntryButton.setText(R.string.go_back);
        mAddEntryButton.setOnClickListener(null);
        mAddEntryButton.setOnTouchListener(new View.OnTouchListener() {
          public boolean onTouch(View arg0, MotionEvent me) {
            mTagSpinner.setVisibility(View.VISIBLE);
            configButton.setVisibility(View.VISIBLE);
            selectTag(mTagSpinner.getSelectedItemPosition());
            mAddEntryButton.setText(R.string.menu_new_entry);
            mAddEntryButton.setOnTouchListener(null);
            mAddEntryButton.setOnClickListener(new View.OnClickListener() {
              public void onClick(View view) {
                createEntry();
              }
            });
            return true;
          }
        });
        selectTag(-1);
        return true;
      }
    });
    mScrollView = (ScrollView) findViewById(R.id.entryScrollView);
    mScrollView.setSmoothScrollingEnabled(true);
    mEntryLayout = (LinearLayout) findViewById(R.id.entryLayout);
    mStatButton = (Button) findViewById(R.id.statButton);
    mStatButton.setBackgroundColor(Color.TRANSPARENT);
    mStatButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        final Dialog d = new Dialog(TagToDoList.this);

        TableLayout tl = new TableLayout(TagToDoList.this);
        TableRow tr1 = new TableRow(TagToDoList.this);
        TableRow tr2 = new TableRow(TagToDoList.this);
        TableRow tr3 = new TableRow(TagToDoList.this);
        TextView tvCount1 = new TextView(TagToDoList.this);
        TextView tvExplanation1 = new TextView(TagToDoList.this);
        TextView tvCount2 = new TextView(TagToDoList.this);
        TextView tvExplanation2 = new TextView(TagToDoList.this);

        /* First row */
        tvCount1.setTextColor(Color.WHITE);
        tvCount1.setTextSize(44);
        tvCount1.setTextColor(Color.YELLOW);
        tvCount1.setPadding(1, 20, 0, 0);
        tvCount1.setGravity(1);
        String s = sDbHelper.countUncheckedEntries(mTagsArrayAdapter.getItem(
            mTagSpinner.getSelectedItemPosition()).toString())
            + "";
        tvCount1.setText(s);
        tr1.addView(tvCount1);

        tvExplanation1.setText(R.string.message_tasks_left_tag);
        tvExplanation1.setTextSize(19);
        tvExplanation1.setPadding(0, 37, 0, 0);
        tr1.addView(tvExplanation1);

        tr1.setBaselineAligned(false);
        tl.addView(tr1);
        /* First row */

        /* Second row */
        tvCount2.setTextColor(Color.WHITE);
        tvCount2.setTextSize(44);
        tvCount2.setTextColor(Color.YELLOW);
        tvCount2.setPadding(1, 20, 0, 20);
        tvCount2.setGravity(1);
        s = sDbHelper.countUncheckedEntries() + "";
        tvCount2.setText(s);
        tr2.addView(tvCount2);

        tvExplanation2.setText(R.string.message_tasks_left_total);
        tvExplanation2.setTextSize(19);
        tvExplanation2.setPadding(0, 37, 0, 0);
        tr2.addView(tvExplanation2);

        tr2.setBaselineAligned(false);
        tl.addView(tr2);
        /* Second row */

        final Button b = new Button(TagToDoList.this);
        b.setText(R.string.go_back);
        b.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
            d.dismiss();
          }
        });
        tr3.addView(b);
        tl.addView(tr3);

        d.setTitle(R.string.message_stats);
        d.setContentView(tl);
        d.setOnKeyListener(new Dialog.OnKeyListener() {
          public boolean onKey(DialogInterface di, int keyCode, KeyEvent msg) {
            if (keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_DEL
                || keyCode == KeyEvent.KEYCODE_SPACE) {
              b.performClick();
            }
            return false;
          }
        });
        d.show();
      }
    });
    Button toggleButton = (Button) findViewById(R.id.toggleButton);
    toggleButton.setText(R.string.sort);
    toggleButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View arg0) {
        showSortOptions();
      }
    });
    mAddEntryButton = (Button) findViewById(R.id.addEntryButton);
    mAddEntryButton.setText(R.string.menu_new_entry);
    mAddEntryButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        createEntry();
        if (sTracker != null) {
          // we log the action, pressed button, action trigger and the
          // existing
          // number of tasks #EventLogSyntax
          sTracker.trackEvent(Analytics.PRESS, Analytics.ADD_TASK_BUTTON,
              Analytics.INTERFACE, mEntryLayout.getChildCount());
        }
      }
    });

    final ImageButton configButton = (ImageButton) findViewById(R.id.configButton);
    configButton.setBackgroundColor(Color.TRANSPARENT);
    configButton.setImageResource(android.R.drawable.ic_menu_preferences);
    configButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(v.getContext(), ConfigScreen.class));
      }
    });

    sDbHelper = ToDoDB.getInstance(getApplicationContext());
    showDueTasks(true);

    sGestureDetector = new GestureDetector(new MyGestureDetector());
    sGestureListener = new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (sGestureDetector.onTouchEvent(event)) {
          mTagSpinner.setSelection(Utils.iterate(mTagSpinner
              .getSelectedItemPosition(), mTagSpinner.getCount(),
              MyGestureDetector.getDirection()));
          return true;
        }
        return false;
      }
    };
  }

  /**
   * Visually applies the selection in the spinner to the main LinearLayout.
   * 
   * @param selectedTab
   *          index of the selected tab, as it will come from the spinner
   */
  private void selectTag(final int selectedTag) {
    final LinearLayout el = mEntryLayout;
    el.removeAllViews();
    final OnCheckedChangeListener ccl = CHOICE_MODE ? new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
        try {
          cb.setChecked(!isChecked);
          sDbHelper.setSuperTask(mContextEntry, cb.getText().toString());
          selectTag(mTagSpinner.getSelectedItemPosition());
          CHOICE_MODE = false;
          ((LinearLayout) findViewById(R.id.lowerLayout))
              .setVisibility(View.VISIBLE);
        } catch (Exception e) {
          Utils.showDialog(R.string.notification, R.string.move_fail, cb
              .getContext());
        }
      }
    }
        : new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
            sDbHelper.updateEntry(cb.getText().toString(), isChecked);
            selectTag(mTagSpinner.getSelectedItemPosition());
          }
        };

    final ToDoDB dbHelper = sDbHelper;
    final LayoutInflater inflater = getLayoutInflater();
    mStatButton.setText(processDepth(dbHelper, inflater, el, ccl, selectedTag,
        0, null)
        + "");
  }

  /**
   * Populates the given LinearLayout with tasks.
   * 
   * @param dbHelper
   *          A local reference to sDbHelper
   * @param inflater
   *          Needed to inflate every task LinearLayout with the task.xml layout
   * @param el
   *          The LinearLayout to be populated
   * @param notesLayout
   *          A local reference to mNotesLayout
   * @param collapseLayout
   *          A local reference to mCollapseLayout
   * @param ccl
   *          Things to happen when a task is checked
   * @param selectedTag
   *          Index of the selected tag
   * @param depth
   *          Depth of tasks to start processing with
   * @param superTask
   *          Populates with the subtasks of this superTask
   * @return The number of unchecked tasks
   */
  public final int processDepth(final ToDoDB dbHelper,
      final LayoutInflater inflater, final LinearLayout el,
      final OnCheckedChangeListener ccl, final int selectedTag,
      final int depth, final String superTask) {
    final Cursor c = sDbHelper.getTasks(selectedTag != -1 ? mTagsArrayAdapter
        .getItem(selectedTag).toString() : null, depth, superTask);

    final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
    final int value = c.getColumnIndex(ToDoDB.KEY_STATUS);
    final int subtasks = c.getColumnIndex(ToDoDB.KEY_SUBTASKS);

    int numberOfUnchecked = 0;
    final int maxPriority = mMaxPriority;
    final boolean hideChecked = HIDE_CHECKED;
    int auxInt;
    boolean auxBool;

    if (c.getCount() > 0) {
      c.moveToLast();
      do {
        final LinearLayout ll = new LinearLayout(this);
        inflater.inflate(R.layout.task, ll);
        final CheckBox cb = (CheckBox) ll.findViewById(R.id.taskCheckBox);
        final String taskName = c.getString(name);
        cb.setText(taskName);
        boolean checked;
        if (c.getInt(value) == 1) { // 1 = checked, 0 = unchecked
          checked = true;
          if (hideChecked) {
            continue;
          }
        } else {
          checked = false;
          numberOfUnchecked += 1;
        }
        if (SHINY_PRIORITY && maxPriority != 0) {
          final int color = dbHelper.getPriority(c.getString(name)) * 191
              / maxPriority + 64;
          cb.setTextColor(Color.rgb(color, color, color));
        } else {
          if (checked) {
            cb.setTextColor(Color.GRAY);
            cb.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
          }
        }
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener(ccl);
        cb.setOnTouchListener(sGestureListener);
        registerForContextMenu(cb);
        if (depth > 0) {
          LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
              LayoutParams.WRAP_CONTENT);
          lp.leftMargin = depth * 30;
          lp.weight = 1;
          cb.setLayoutParams(lp);
        }

        if (SHOW_NOTES) {
          final LinearLayout taskNoteLayout = (LinearLayout) ll
              .findViewById(R.id.taskNotesLayout);
          taskNoteLayout.setOrientation(0);

          int isWrittenNote;
          try {
            isWrittenNote = sDbHelper.getFlag(taskName,
                ToDoDB.KEY_NOTE_IS_WRITTEN);
          } catch (Exception e) {
            sDbHelper.repair();
            isWrittenNote = sDbHelper.getFlag(taskName,
                ToDoDB.KEY_NOTE_IS_WRITTEN);
          }
          if (isWrittenNote > 0) {
            final ImageButton ib = new ImageButton(this);
            ib.setBackgroundColor(Color.TRANSPARENT);
            ib.setPadding(0, 0, 0, 0);
            ib.setImageResource(R.drawable.written);
            ib.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                mContextEntry = taskName;
                changeTask(ENTRY_WRITTEN_ID);
              }
            });
            taskNoteLayout.addView(ib);
          }

          if (sDbHelper.getFlag(taskName, ToDoDB.KEY_NOTE_IS_GRAPHICAL) > 0) {
            final ImageButton ib = new ImageButton(this);
            ib.setBackgroundColor(Color.TRANSPARENT);
            ib.setPadding(0, 0, 0, 0);
            ib.setImageResource(android.R.drawable.ic_menu_edit);
            ib.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                mContextEntry = taskName;
                changeTask(ENTRY_GRAPHICAL_ID);
              }
            });
            taskNoteLayout.addView(ib);
          }

          if (sDbHelper.getFlag(taskName, ToDoDB.KEY_NOTE_IS_AUDIO) > 0) {
            final ImageButton ib = new ImageButton(this);
            ib.setBackgroundColor(Color.TRANSPARENT);
            ib.setPadding(0, 0, 0, 0);
            ib.setImageResource(R.drawable.audio);
            ib.setOnClickListener(new View.OnClickListener() {
              public void onClick(View v) {
                mContextEntry = taskName;
                changeTask(ENTRY_AUDIO_ID);
              }
            });
            taskNoteLayout.addView(ib);
          }
        }

        boolean collapsed = false;
        if (SHOW_COLLAPSE) {
          final ImageButton ib = (ImageButton) ll
              .findViewById(R.id.taskCollapseButton);
          if (sDbHelper.getFlag(taskName, ToDoDB.KEY_SUBTASKS) > 0) {
            ib.setTag(Boolean.valueOf(collapsed = sDbHelper.getFlag(taskName,
                ToDoDB.KEY_COLLAPSED) != 0));
            ib.setImageResource(collapsed ? android.R.drawable.ic_menu_add
                : android.R.drawable.ic_menu_close_clear_cancel);
            ib.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                sDbHelper.setFlag(taskName, ToDoDB.KEY_COLLAPSED, (Boolean) ib
                    .getTag() ? 0 : 1);
                selectTag(mTagSpinner.getSelectedItemPosition());
              }
            });
          } else {
            ib.setVisibility(View.GONE);
          }
        }

        // should we show the due time?
        if (SHOW_DUE_TIME
            && (auxInt = sDbHelper.getFlag(taskName, ToDoDB.KEY_EXTRA_OPTIONS)) > 0) {
          Chronos.refresh();
          final LinearLayout descLayout = (LinearLayout) ll
              .findViewById(R.id.descriptionLayout);
          descLayout.setPadding(26, -10, 0, -5);
          final Button b = new Button(this);
          b.setBackgroundColor(Color.TRANSPARENT);
          b.setTextColor(Color.GRAY);
          StringBuilder sb = new StringBuilder();

          if (auxBool = (auxInt % 2 == 1)) { // the LSB tells us whether there's
            // a due date
            sb.append(Chronos.decodeDate(sDbHelper.getDueDate(taskName),
                getString(R.string.months)));
            sb.append(' ');
          }

          if (auxInt >> 1 % 2 == 1) { // the 2nd LSB is about a due time
            sb.append(Chronos.decodeTime(sDbHelper.getDueTime(taskName)));
            sb.append(' ');

            if ((auxInt = sDbHelper.getDueDayOfWeek(taskName)) > -1) {
              // showing the due date of the week if any
              sb.append(getString(R.string.every));
              sb.append(' ');
              sb.append(getString(Chronos.DAYS[auxInt]));
            } else if (!auxBool) {
              sb.append(getString(R.string.every));
              sb.append(' ');
              sb.append(getString(R.string.day));
            }
          }

          b.setText(sb.toString());
          b.setOnClickListener(sDescriptionClickListener);
          descLayout.addView(b);
        }

        el.addView(ll);

        if (c.getInt(subtasks) > 0 && !collapsed) {
          numberOfUnchecked += processDepth(dbHelper, inflater, el, ccl,
              selectedTag, depth + 1, c.getString(name));
        }
      } while (c.moveToPrevious());
    }
    c.close();
    return numberOfUnchecked;
  }

  /**
   * Selection of menu button option
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return applyMenuChoice(item) || super.onOptionsItemSelected(item);
  }

  /**
   * Chooses the right action depending on what menu item has been clicked.
   * 
   * @param item
   *          The menu item which has been clicked
   * @return
   */
  private final boolean applyMenuChoice(MenuItem item) {
    switch (item.getItemId()) {
      case TAG_INSERT_ID:
        createTag();
        return true;
      case TAG_REMOVE_ID:
        removeTag();
        return true;
      case TAG_EDIT_ID:
        editTag();
        return true;
      case TAG_HELP_ID:
        showHelpScreen();
        return true;
      case TAG_CLEAR_ID:
        removeAllTasks();
        return true;
      case TAG_UNINDENT_ID:
        final Cursor c = sDbHelper.getTasks(mTagsArrayAdapter.getItem(
            mTagSpinner.getSelectedItemPosition()).toString(), -1, null);
        final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
        if (c.getCount() > 0) {
          final ContentValues args = new ContentValues();
          args.put(ToDoDB.KEY_DEPTH, 0);
          args.put(ToDoDB.KEY_SUPERTASK, "");
          c.moveToFirst();
          do {
            sDbHelper.mDb.update(ToDoDB.DB_ENTRY_TABLE, args, ToDoDB.KEY_NAME
                + " = '" + c.getString(name) + "'", null);
          } while (c.moveToNext());
        }
        selectTag(mTagSpinner.getSelectedItemPosition());
        c.close();
        return true;
      case TAG_IMPORTBACKUP_ID:
        final Intent i = new Intent(this, ConfirmationScreen.class);
        i.setAction(ACTIVITY_BACKUP_IMPORT + "");
        startActivity(i);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Triggers the activity which asks for the tag's name, and when the response
   * reaches that activity, the tag is created with the given name
   */
  private void createTag() {
    Intent i = new Intent(this, EditScreen.class);
    i.setAction(ACTIVITY_CREATE_TAG + "");
    startActivity(i);
  }

  /**
   * Deletes the active tag
   */
  private void removeTag() {
    if (mTagSpinner.getCount() == 1) {
      Utils.showDialog(-1, R.string.impossible_tag_deletion, TagToDoList.this);
      return;
    }
    Intent i = new Intent(this, ConfirmationScreen.class);
    i.putExtra(ToDoDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_DELETE_TAG + "");
    startActivity(i);
  }

  /**
   * Deletes all the tasks in the active tag
   */
  private final void removeAllTasks() {
    Intent i = new Intent(this, ConfirmationScreen.class);
    i.putExtra(ToDoDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_CLEAR_ENTRIES + "");
    startActivity(i);
  }

  /**
   * Triggers an activity which asks for the active tag's new name
   */
  private void editTag() {
    Intent i = new Intent(this, EditScreen.class);
    i.putExtra(ToDoDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_EDIT_TAG + "");
    startActivity(i);
  }

  /**
   * Triggers an activity which shows a help screen
   */
  private void showHelpScreen() {
    Intent i = new Intent(this, ConfirmationScreen.class);
    i.setAction(ACTIVITY_INSTRUCTIONS + "");
    startActivity(i);
  }

  /**
   * Triggers the activity which asks for the new entry name, and when the
   * response reaches that activity, the tag is created with the given name
   */
  private void createEntry() {
    Intent i = new Intent(this, EditScreen.class);
    i.putExtra(ToDoDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.putExtra(ToDoDB.KEY_SUPERTASK, "");
    i.setAction(ACTIVITY_CREATE_ENTRY + "");
    startActivity(i);
  }

  /**
   * Hook into menu button for activity
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    final SubMenu sb = menu.addSubMenu(R.string.more);
    sb.add(0, TAG_CLEAR_ID, 0, R.string.menu_clear);
    sb.add(0, TAG_IMPORTBACKUP_ID, 0, R.string.backup_import);
    sb.add(0, TAG_EDIT_ID, 0, R.string.menu_edit_tag);
    sb.add(0, TAG_UNINDENT_ID, 0, R.string.menu_unindent);
    MenuItem item = menu.add(0, TAG_HELP_ID, 0, R.string.menu_instructions);
    item.setIcon(R.drawable.help);
    item = menu.add(0, TAG_INSERT_ID, 0, R.string.menu_create_tag);
    item.setIcon(R.drawable.add);
    item = menu.add(0, TAG_REMOVE_ID, 0, R.string.menu_delete_tag);
    item.setIcon(R.drawable.delete);
    return true;
  }

  /**
   * Populates the interface with tags
   */
  private void fillTagData() {
    mTagsArrayAdapter = new ArrayAdapter<CharSequence>(this,
        android.R.layout.simple_spinner_item);
    mTagsArrayAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    Cursor c = sDbHelper.getAllTags();
    ArrayAdapter<CharSequence> taa = mTagsArrayAdapter;

    c.moveToFirst();
    do {
      taa.add(c.getString(1));
    } while (c.moveToNext());

    mTagSpinner.setAdapter(taa);
    c.close();
  }

  /**
   * Copies a database copy from the SD card over the existing database
   * 
   * @param An
   *          application context (preferably through getApplicationContext())
   */
  public static final void importBackupSD(final Context c) {
    sDbHelper.close();
    try {
      Utils.copy(new File("/sdcard/Tag-ToDo_data/database_backup"), new File(
          "/data/data/com.android.todo/databases"));
    } catch (Exception e) {
      Utils.showDialog(R.string.notification, R.string.backup_import_fail, c);
    }
    sDbHelper = ToDoDB.getInstance(c);
  }

  @Override
  protected void onDestroy() {
    if (sSettings.getBoolean(ConfigScreen.BACKUP_SDCARD, false)) {
      ToDoDB.createBackup();
    }

    if (BLIND_MODE) {
      sTts.shutdown();
      sTts = null;
    }

    if (sTracker != null) {
      final int month = Calendar.getInstance().get(Calendar.MONTH);
      if (month != sSettings.getInt(Analytics.LAST_SYNCHRONIZED_MONTH, -1)) {
        sTracker.dispatch();
        sSettings.edit().putInt(Analytics.LAST_SYNCHRONIZED_MONTH, month)
            .commit();
      }
      sTracker.stop();
    }
    // should we really close this, ToDoDB being a singleton? Figure this
    // out! ???
    // sDbHelper.close();

    TagToDoWidget.onUpdate(getApplicationContext(), AppWidgetManager
        .getInstance(getApplicationContext()));
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onTouchEvent(MotionEvent me) {
    return sGestureListener.onTouch(null, me);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // populating the fields:
    fillTagData();

    setPrioritySort(sSettings.getInt(PRIORITY_SORT, 0));
    setAlphabeticalSort(sSettings.getInt(ALPHABET_SORT, 0));
    setDueDateSort(sSettings.getInt(DUEDATE_SORT, 0));

    // Is the notes preview feature enabled?
    SHOW_NOTES = sSettings.getBoolean(ConfigScreen.NOTE_PREVIEW, false)
        || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

    // Should the collapse buttons be shown?
    SHOW_COLLAPSE = sSettings.getBoolean(ConfigScreen.SHOW_COLLAPSE, false);

    // Should due time be shown for the tasks that have it?
    if (SHOW_DUE_TIME = sSettings.getBoolean(ConfigScreen.SHOW_DUE_TIME, false)) {
      sDescriptionClickListener = new OnClickListener() {
        public void onClick(View v) {
          mContextEntry = ((CheckBox) ((LinearLayout) ((LinearLayout) v
              .getParent().getParent()).getChildAt(0)).getChildAt(0)).getText()
              .toString();
          changeTask(ENTRY_EDIT_ID);
        }
      };
    }

    // Should checked tasks be hidden?
    HIDE_CHECKED = sSettings.getBoolean(HIDE_CHECKED_SORT, false);

    // Is a Google Calendar sync enabled?
    if (SYNC_GCAL = sSettings.getBoolean(ConfigScreen.GOOGLE_CALENDAR, false)) {
      GoogleCalendar.setLogin(sSettings.getString(ConfigScreen.GOOGLE_USERNAME,
          ""), sSettings.getString(ConfigScreen.GOOGLE_PASSWORD, ""));
    }

    // do we visually distinguish tasks by priority?
    if (SHINY_PRIORITY = sSettings.getBoolean(ConfigScreen.VISUAL_PRIORITY,
        false)) {
      mMaxPriority = sSettings.getInt(ConfigScreen.PRIORITY_MAX, 100);
      // selectTag(mTagSpinner.getSelectedItemPosition());
    }

    // Restore the last selected tag
    int lastSelectedTag = sSettings.getInt("lastSelectedTag", 0);
    if (lastSelectedTag >= mTagSpinner.getCount()) {
      lastSelectedTag = 0;
    }
    mTagSpinner.setSelection(lastSelectedTag, true);

    if (BLIND_MODE = sSettings.getBoolean(ConfigScreen.BLIND_MODE, false)) {
      if (sTts == null) {
        sTts = new TTS(getApplicationContext(), null);
      }
    } else {
      if (sTts != null) {
        sTts.shutdown();
        sTts = null;
      }
    }

    // instantiate usage stats tracker (if it's the case)
    if (sTracker == null
        && sSettings.getBoolean(ConfigScreen.USAGE_STATS, false)) {
      sTracker = GoogleAnalyticsTracker.getInstance();
      sTracker.start(Analytics.UA_CODE, this);
      sTracker.trackPageView(Analytics.VIEW_MAIN);
    } else {
      sTracker = null;
    }

  }

  /**
   * Saves the state on pause
   */
  private void saveState() {
    // Saving the selected tag
    SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0)
        .edit();
    editor.putInt("lastSelectedTag", mTagSpinner.getSelectedItemPosition());
    editor.commit();
  }

  /**
   * Creates the context menu for a to-do list entry (task editing, deletion,
   * etc.)
   */
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo) {
    mContextEntry = ((CheckBox) v).getText().toString();
    // menu.add(0, ENTRY_CLOSE_ID, 0, R.string.entry_exit);
    final Action a = new Action();
    final String possibleAction = a.setAndExtractAction(mContextEntry);
    if (!("".equals(possibleAction))) {
      mContextAction = a;
      menu.add(0, ENTRY_INSTANTACTION_ID, 0, possibleAction);
    }
    menu.add(0, ENTRY_SUBTASK_ID, 0, R.string.entry_subtask_add);
    menu.add(0, ENTRY_EDIT_ID, 0, R.string.entry_edit);
    menu.add(0, ENTRY_REMOVE_ID, 0, R.string.entry_delete);
    // menu.add(0, ENTRY_EMAIL_ID, 0, R.string.entry_email);
    SubMenu submenu = menu.addSubMenu(R.string.entry_group_move);
    submenu.add(0, ENTRY_MOVE_ID, 0, R.string.entry_move);
    submenu.add(0, ENTRY_MOVE_UNDER_TASK_ID, 0, R.string.entry_move_under_task);
    //submenu.add(0, ENTRY_DOWN_ID, 0, R.string.entry_down);
    submenu = menu.addSubMenu(R.string.entry_group_notes);
    submenu.add(0, ENTRY_AUDIO_ID, 0, R.string.entry_audio_note);
    submenu.add(0, ENTRY_GRAPHICAL_ID, 0, R.string.entry_graphical_note);
    submenu.add(0, ENTRY_WRITTEN_ID, 0, R.string.entry_written_note);
    submenu = menu.addSubMenu(R.string.entry_group_share);
    submenu.add(0, ENTRY_SMS_ID, 0, R.string.SMS);
    menu.setHeaderTitle(R.string.entry_menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    final int id = item.getItemId();
    return (id > 0) ? changeTask(id) : true;
  }

  /**
   * The following function is used by the context menu, as well as the keyboard
   * shortcuts. Needs mContextEntry to be set before being called. mContextEntry
   * is a string representation of the selected task.
   * 
   * @param selectedItem
   *          basically is the function that needs to be done
   * @return
   */
  private boolean changeTask(int selectedItem) {
    Intent i;
    switch (selectedItem) {
      case ENTRY_SUBTASK_ID:
        i = new Intent(this, EditScreen.class);
        i.putExtra(ToDoDB.KEY_NAME, mTagsArrayAdapter.getItem(
            mTagSpinner.getSelectedItemPosition()).toString());
        i.putExtra(ToDoDB.KEY_SUPERTASK, mContextEntry);
        i.setAction(ACTIVITY_CREATE_ENTRY + "");
        startActivity(i);
        break;
      case ENTRY_EDIT_ID:
        i = new Intent(this, EditScreen.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.setAction(ACTIVITY_EDIT_ENTRY + "");
        startActivity(i);
        break;
      case ENTRY_REMOVE_ID:
        sDbHelper.deleteEntry(mContextEntry);
        selectTag(mTagSpinner.getSelectedItemPosition());
        break;
      case ENTRY_GRAPHICAL_ID:
        i = new Intent(this, PaintScreen.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        startActivity(i);
        break;
      case ENTRY_AUDIO_ID:
        i = new Intent(this, AudioScreen.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.putExtra(ToDoDB.KEY_STATUS, true);
        startActivity(i);
        break;
      case ENTRY_DOWN_ID:
        sDbHelper.pushEntryDown(mContextEntry);
        selectTag(mTagSpinner.getSelectedItemPosition());
        break;
      case ENTRY_MOVE_ID:
        final AdapterView.OnItemSelectedListener l = mTagSpinner
            .getOnItemSelectedListener();
        final int p = mTagSpinner.getSelectedItemPosition();
        mTagSpinner
            .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
              public void onItemSelected(AdapterView<?> av, View v, int index,
                  long arg3) {
                sDbHelper.updateEntryParent(mContextEntry, mTagsArrayAdapter
                    .getItem(index).toString(), 0);
                av.setOnItemSelectedListener(l);
                av.setSelection(p);
              }

              public void onNothingSelected(AdapterView<?> arg0) {
              }
            });
        mTagSpinner.performClick();
        break;
      case ENTRY_MOVE_UNDER_TASK_ID:
        CHOICE_MODE = true;
        ((LinearLayout) findViewById(R.id.lowerLayout))
            .setVisibility(View.GONE);
        selectTag(mTagSpinner.getSelectedItemPosition());
        break;
      case ENTRY_WRITTEN_ID:
        i = new Intent(this, EditScreen.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.setAction(ACTIVITY_WRITE_NOTE + "");
        startActivity(i);
        break;
      case ENTRY_SMS_ID:
        i = new Intent(Intent.ACTION_VIEW);
        i.putExtra("sms_body", getString(R.string.todo) + ": " + mContextEntry);
        i.setType("vnd.android-dir/mms-sms");
        startActivity(i);
        break;
      case ENTRY_EMAIL_ID:
        String[] mailto = { "" };
        // Create a new Intent to send messages
        i = new Intent(Intent.ACTION_SEND);
        // Add attributes to the intent
        i.putExtra(Intent.EXTRA_EMAIL, mailto);
        i.putExtra(Intent.EXTRA_SUBJECT, "subiect");
        i.putExtra(Intent.EXTRA_TEXT, "corp");
        i.setType("text/plain");
        startActivity(Intent.createChooser(i, "MySendMail"));
        break;
      case ENTRY_INSTANTACTION_ID:
        mContextAction.perform(this);
        break;
    }
    return true;
  }

  /**
   * This intercepts a keyboard action. The desired shortcut system is described
   * in the keyboard_help_text string. This system will also be applied in
   * implementations of this method in other classes(Activities) as well, where
   * not conflicting with input methods.
   */
  public boolean dispatchKeyEvent(KeyEvent msg) {
    if (msg.getAction() != KeyEvent.ACTION_DOWN) {
      return false;
    }
    final int keyCode = msg.getKeyCode();
    switch (keyCode) {
      case KeyEvent.KEYCODE_T:
        if (msg.isShiftPressed()) {
          mTagSpinner.performLongClick();
        } else {
          mTagSpinner.performClick();
        }
        break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
        mTagSpinner.setSelection(Utils.iterate(mTagSpinner
            .getSelectedItemPosition(), mTagSpinner.getCount(), 1));
        // ???replace this with a method, it's called too many times
        break;
      case (KeyEvent.KEYCODE_N):
        if (msg.isAltPressed()) {
          mTagSpinner.setSelection(Utils.iterate(mTagSpinner
              .getSelectedItemPosition(), mTagSpinner.getCount(), 1));
        } else {
          selectAnotherEntry(1);
        }
        break;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        mTagSpinner.setSelection(Utils.iterate(mTagSpinner
            .getSelectedItemPosition(), mTagSpinner.getCount(), -1));
        break;
      case (KeyEvent.KEYCODE_P):
        if (msg.isAltPressed()) {
          mTagSpinner.setSelection(Utils.iterate(mTagSpinner
              .getSelectedItemPosition(), mTagSpinner.getCount(), -1));
        } else {
          selectAnotherEntry(-1);
        }
        break;
      case (KeyEvent.KEYCODE_ENTER):
        if (mActiveEntry > -1) {
          ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).performClick();
        }
        break;
      case (KeyEvent.KEYCODE_SPACE):
        if (mActiveEntry > -1) {
          ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).performLongClick();
        }
        break;
      case (KeyEvent.KEYCODE_A):
        if (msg.isAltPressed()) {
          createTag();
        } else {
          mAddEntryButton.performClick();
        }
        break;
      case (KeyEvent.KEYCODE_S):
        mStatButton.performClick();
        break;
      case (KeyEvent.KEYCODE_D):
        if (msg.isAltPressed()) {
          removeTag();
        } else {
          if (mActiveEntry > -1) {
            mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
                .findViewById(R.id.taskCheckBox))).getText().toString();
            changeTask(ENTRY_REMOVE_ID);
            mActiveEntry = Utils.iterate(mActiveEntry, mEntryLayout
                .getChildCount(), -1);
          }
        }
        break;
      case (KeyEvent.KEYCODE_E):
      case (KeyEvent.KEYCODE_R):
        if (msg.isAltPressed()) {
          editTag();
        } else {
          if (mActiveEntry > -1) {
            mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
                .findViewById(R.id.taskCheckBox))).getText().toString();
            changeTask(ENTRY_EDIT_ID);
          }
        }
        break;
      case (KeyEvent.KEYCODE_G):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_GRAPHICAL_ID);
        }
        break;
      case (KeyEvent.KEYCODE_F):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_AUDIO_ID);
        }
        break;
      case (KeyEvent.KEYCODE_0):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_DOWN_ID);
        }
        break;
      case (KeyEvent.KEYCODE_PERIOD):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_SUBTASK_ID);
        }
        break;
      case (KeyEvent.KEYCODE_H):
        showHelpScreen();
        break;
      case (KeyEvent.KEYCODE_O):
        changeSizeLimit(1);
        break;
      case (KeyEvent.KEYCODE_I):
        changeSizeLimit(-1);
        break;
      case (KeyEvent.KEYCODE_X):
        removeAllTasks();
        break;
      case (KeyEvent.KEYCODE_U):
        startActivity(new Intent(this, NotificationActivity.class));
        break;
      case (KeyEvent.KEYCODE_M):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_MOVE_ID);
          mActiveEntry = Utils.iterate(mActiveEntry, mEntryLayout
              .getChildCount(), -1);
        }
        break;
      case (KeyEvent.KEYCODE_C):
        startActivity(new Intent(this, ConfigScreen.class));
        break;
      case KeyEvent.KEYCODE_BACK:
        finish();
        break;
      case (KeyEvent.KEYCODE_W):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(ENTRY_WRITTEN_ID);
        }
        break;
    }

    if (keyCode > KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
      int newIndex = keyCode - 0x00000007;
      if (newIndex <= mTagSpinner.getCount()) {
        mTagSpinner.setSelection(newIndex - 1);
      }
    }

    return false;
  }

  /**
   * Selects another task based on the passed increment. It also deselects the
   * previously selected task.
   * 
   * @param increment
   *          1 for the next task, or -1 for the previous one
   */
  private final void selectAnotherEntry(final int increment) {
    if (mActiveEntry > -1) {
      ((LinearLayout) (mEntryLayout.getChildAt(mActiveEntry)))
          .setBackgroundColor(Color.TRANSPARENT);
    }
    if (mEntryLayout.getChildCount() == 0) {
      return;
    }
    mActiveEntry = Utils.iterate(mActiveEntry, mEntryLayout.getChildCount(),
        increment);
    final LinearLayout ll = ((LinearLayout) mEntryLayout
        .getChildAt(mActiveEntry));
    ll.setBackgroundColor(Color.DKGRAY);
    if (BLIND_MODE) {
      final CheckBox cb = (CheckBox) ll.findViewById(R.id.taskCheckBox);
      sTts.speak(cb.getText().toString()
          + (cb.isChecked() ? ')' + getString(R.string.checked) : ""));
    }
    if (ll.getTop() < mScrollView.getScrollY()
        || ll.getTop() > mScrollView.getScrollY() + mScrollView.getHeight()) {
      mScrollView.smoothScrollTo(0, ll.getTop());
    }
  }

  /**
   * Shows a notification dialog with tasks due today or earlier (if any)
   * 
   * @param showDialog
   *          determines whether the notification will actually be shown (used
   *          only when the return value is of interest)
   * @return false, if there are no due tasks
   */
  private boolean showDueTasks(boolean showDialog) {
    Cursor dueEntries = sDbHelper.getDueEntries();
    if (dueEntries.getCount() > 0) {
      if (showDialog) {
        dueEntries.moveToFirst();
        int name;
        try {
          name = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_NAME);
        } catch (Exception e) {
          return false;
        }
        StringBuilder sb = new StringBuilder(this
            .getString(R.string.due_date_notification)
            + "\n");
        do {
          sb.append(dueEntries.getString(name));
          sb.append("\n");
        } while (dueEntries.moveToNext());
        Utils.showDueTasksNotification(sb.toString(), TagToDoList.this);
      }
      return true;
    }
    return false;
  }

  /**
   * Increases or decreases the to-do list size by 50 tasks. This refers to the
   * number of checked items you still want to have stored.
   * 
   * @param direction
   *          is positive (+1) if we want an increase
   */
  private void changeSizeLimit(int direction) {
    final SharedPreferences.Editor editor = sSettings.edit();
    int currentLimit = sSettings.getInt(ConfigScreen.CHECKED_LIMIT, 50);
    int newLimit = direction * 10 + currentLimit;
    if (newLimit < 0) {
      newLimit = 0;
    } else if (newLimit > 5000) {
      newLimit = 5000;
    }
    editor.putInt(ConfigScreen.CHECKED_LIMIT, newLimit);
    editor.commit();

    final Dialog d = new Dialog(TagToDoList.this);
    LinearLayout h = new LinearLayout(TagToDoList.this);
    h.setPadding(0, 10, 0, 10);
    h.setGravity(Gravity.CENTER_HORIZONTAL);
    h.setOrientation(1);

    final TextView tv = new TextView(TagToDoList.this);
    tv.setGravity(Gravity.CENTER_HORIZONTAL);
    tv.setBackgroundColor(Color.DKGRAY);
    tv.setTextColor(Color.YELLOW);
    tv.setTextSize(60);
    tv.setText(newLimit + "");
    h.addView(tv);

    d.setContentView(h);

    d.setTitle(R.string.size_change);
    d.show();

    final Handler handler = new Handler();
    final Runnable task = new Runnable() {
      public void run() {
        d.dismiss();
      }
    };
    handler.postDelayed(task, 1500);
    d.setOnKeyListener(new Dialog.OnKeyListener() {
      public boolean onKey(DialogInterface di, int keyCode, KeyEvent msg) {
        if (msg.getAction() != KeyEvent.ACTION_DOWN) {
          return true;
        }
        if (keyCode == KeyEvent.KEYCODE_O) {
          handler.removeCallbacks(task);
          int newValue = Integer.valueOf(tv.getText().toString()) + 10;
          if (newValue > 5000) {
            newValue = 5000;
          }
          tv.setText(newValue + "");
          editor.putInt(ConfigScreen.CHECKED_LIMIT, newValue);
          editor.commit();
          handler.postDelayed(task, 1500);
        } else if (keyCode == KeyEvent.KEYCODE_I) {
          handler.removeCallbacks(task);
          int newValue = Integer.valueOf(tv.getText().toString()) - 10;
          if (newValue < 0) {
            newValue = 0;
          }
          tv.setText(newValue + "");
          editor.putInt(ConfigScreen.CHECKED_LIMIT, newValue);
          editor.commit();
          handler.postDelayed(task, 1500);
        }
        return true;
      }
    });
  }

  /**
   * Shows a dialog which allows the user to choose which sorting to apply
   */
  public void showSortOptions() {
    final Dialog d = new Dialog(this);
    d.setTitle(R.string.sort);
    ScrollView sv = new ScrollView(this);
    LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    ll.setPadding(10, 0, 10, 0);
    LinearLayout row = new LinearLayout(this);
    row.setOrientation(LinearLayout.HORIZONTAL);

    // next comes priority
    TextView tv = new TextView(this);
    tv.setText(R.string.priority);
    ll.addView(tv);

    final CheckBox priorityDown = new CheckBox(this);
    final CheckBox priorityUp = new CheckBox(this);
    switch (sSettings.getInt(PRIORITY_SORT, 0)) {
      case 1:
        priorityDown.setChecked(true);
        break;
      case 2:
        priorityUp.setChecked(true);
        break;
    }
    priorityDown.setText(R.string.descending);
    priorityDown
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              priorityUp.setChecked(false);
            }
          }
        });
    row.addView(priorityDown);
    priorityUp.setText(R.string.ascending);
    priorityUp
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              priorityDown.setChecked(false);
            }
          }
        });
    row.addView(priorityUp);
    ll.addView(row);

    // next comes due date
    tv = new TextView(this);
    tv.setText(R.string.edit_task_date_set);
    ll.addView(tv);

    final CheckBox dateDown = new CheckBox(this);
    final CheckBox dateUp = new CheckBox(this);
    row = new LinearLayout(this);
    switch (sSettings.getInt(DUEDATE_SORT, 0)) {
      case 1:
        dateDown.setChecked(true);
        break;
      case 2:
        dateUp.setChecked(true);
        break;
    }
    dateDown.setText(R.string.descending);
    dateDown
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              dateUp.setChecked(false);
            }
          }
        });
    row.addView(dateDown);
    dateUp.setText(R.string.ascending);
    dateUp
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              dateDown.setChecked(false);
            }
          }
        });
    row.addView(dateUp);
    ll.addView(row);

    // next comes alphabetical
    tv = new TextView(this);
    tv.setText(R.string.alphabetical);
    ll.addView(tv);

    final CheckBox alphabetDown = new CheckBox(this);
    final CheckBox alphabetUp = new CheckBox(this);
    row = new LinearLayout(this);
    switch (sSettings.getInt(ALPHABET_SORT, 0)) {
      case 1:
        alphabetDown.setChecked(true);
        break;
      case 2:
        alphabetUp.setChecked(true);
        break;
    }
    alphabetDown.setText(R.string.descending);
    alphabetDown
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              alphabetUp.setChecked(false);
            }
          }
        });
    row.addView(alphabetDown);
    alphabetUp.setText(R.string.ascending);
    alphabetUp
        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
              alphabetDown.setChecked(false);
            }
          }
        });
    row.addView(alphabetUp);
    ll.addView(row);

    final CheckBox hideChecked = new CheckBox(this);
    hideChecked.setText(R.string.hide_checked);
    hideChecked.setChecked(HIDE_CHECKED);
    ll.addView(hideChecked);

    Button b = new Button(this);
    b.setText(R.string.confirm);
    b.setOnClickListener(new View.OnClickListener() {
      public void onClick(View arg0) {
        d.dismiss();
        SharedPreferences.Editor editor = sSettings.edit();

        if (priorityDown.isChecked()) {
          setPrioritySort(1);
          editor.putInt(PRIORITY_SORT, 1);
        } else if (priorityUp.isChecked()) {
          setPrioritySort(2);
          editor.putInt(PRIORITY_SORT, 2);
        } else {
          setPrioritySort(3);
          editor.putInt(PRIORITY_SORT, 0);
        }

        if (alphabetDown.isChecked()) {
          setAlphabeticalSort(1);
          editor.putInt(ALPHABET_SORT, 1);
        } else if (alphabetUp.isChecked()) {
          setAlphabeticalSort(2);
          editor.putInt(ALPHABET_SORT, 2);
        } else {
          setAlphabeticalSort(3);
          editor.putInt(ALPHABET_SORT, 0);
        }

        if (dateDown.isChecked()) {
          setDueDateSort(1);
          editor.putInt(DUEDATE_SORT, 1);
        } else if (dateUp.isChecked()) {
          setDueDateSort(2);
          editor.putInt(DUEDATE_SORT, 2);
        } else {
          setDueDateSort(3);
          editor.putInt(DUEDATE_SORT, 0);
        }

        editor.putBoolean(HIDE_CHECKED_SORT, HIDE_CHECKED = hideChecked
            .isChecked());

        editor.commit();
        selectTag(mTagSpinner.getSelectedItemPosition());
      }
    });

    ll.addView(b);

    sv.addView(ll);
    d.setContentView(sv);
    d.show();
  }

  /**
   * Sets the priority sorting - 0 does nothing, but is sometimes called
   * automatically; 1 is descending; 2 is ascending; 3 enforces no priority
   * sorting
   * 
   * @param x
   */
  public void setPrioritySort(int x) {
    switch (x) {
      case 1:
        ToDoDB.setPriorityOrder(true, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 2:
        ToDoDB.setPriorityOrder(true, true);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 3:
        ToDoDB.setPriorityOrder(false, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
    }
  }

  /**
   * @see setPrioritySort Sets the alphabetical sorting
   * 
   * @param x
   */
  public void setAlphabeticalSort(int x) {
    switch (x) {
      case 1:
        ToDoDB.setAlphabeticalOrder(true, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 2:
        ToDoDB.setAlphabeticalOrder(true, true);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 3:
        ToDoDB.setAlphabeticalOrder(false, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
    }
  }

  /**
   * @see setPrioritySort Sets sorting by due date
   * 
   * @param x
   */
  public void setDueDateSort(int x) {
    switch (x) {
      case 1:
        ToDoDB.setDueDateOrder(true, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 2:
        ToDoDB.setDueDateOrder(true, true);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
      case 3:
        ToDoDB.setDueDateOrder(false, false);
        selectTag(mTagSpinner.getSelectedItemPosition());
        return;
    }
  }
}