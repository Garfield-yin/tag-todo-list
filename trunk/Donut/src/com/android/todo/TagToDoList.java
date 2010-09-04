//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
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
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.todo.action.Action;
import com.android.todo.data.Analytics;
import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.speech.TTS;
import com.android.todo.sync.CSV;
import com.android.todo.sync.GoogleCalendar;
import com.android.todo.widget.TagToDoWidget;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * This is the main activity. It shows the main UI elements, including the ToDo
 * list entries.
 */
public class TagToDoList extends Activity {
  // Activities: (keeping these as activities so as not to confuse with some of
  // the ones below)
  public static final int ACTIVITY_CREATE_ENTRY = 5;
  public static final int ACTIVITY_EDIT_ENTRY = 6;

  // Task menu IDs:
  public static final int TASK_EDIT_ID = 1;
  public static final int TASK_REMOVE_ID = 2;
  public static final int TASK_GRAPHICAL_ID = 3;
  public static final int TASK_AUDIO_ID = 4;
  public static final int TASK_MOVE_ID = 7;
  public static final int TASK_WRITTEN_ID = 8;
  public static final int TASK_INSTANTACTION_ID = 9;
  public static final int TASK_SUBTASK_ID = 10;
  public static final int TASK_MOVE_UNDER_TASK_ID = 11;
  public static final int TASK_EMAIL_ID = 12;
  public static final int TASK_SMS_ID = 13;
  public static final int TASK_PHOTO_ID = 14;
  public static final int TASK_TAGS_ID = 25;

  // Menu IDs:
  public static final int TAG_CREATE_ID = 15;
  public static final int TAG_DELETE_ID = 16;
  public static final int TAG_EDIT_ID = 17;
  public static final int TAG_HELP_ID = 18;
  public static final int TAG_CLEAR_ID = 19;
  public static final int TAG_UNINDENT_ID = 20;
  public static final int TAG_IMPORT_BACKUP_ID = 21;
  public static final int TAG_IMPORT_CSV_ID = 22;
  public static final int TAG_CLEAR_CHECKED_ID = 23;
  public static final int TAG_EXPORT_CSV_ID = 24;

  public static final String PREFS_NAME = "TagToDoListPrefs";
  private static final String PRIORITY_SORT = "prioritySorting";
  private static final String ALPHABET_SORT = "alphabeticalSorting";
  private static final String DUEDATE_SORT = "dueDateSorting";
  private static final String HIDE_CHECKED_SORT = "hideChecked";
  public static final String LAST_TAB = "lastSelectedTab";

  // Flags (ideally, should be eliminated sometime in the future)
  public static boolean SYNC_GCAL;
  public static boolean SHINY_PRIORITY;
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
  private static DisplayMetrics sDisplayMetrics;
  private static int sDescriptionAlignment;
  private static GestureDetector sGestureDetector;
  private static OnTouchListener sGestureListener;
  private static OnClickListener sDescriptionClickListener;
  private static ToDoDB sDbHelper;
  public static SharedPreferences sPref;
  public static Editor sEditor;
  private Spinner mTagSpinner;
  private LinearLayout mEntryLayout;
  private ArrayAdapter<CharSequence> mTagsAdapter;
  private Button mStatButton;
  private Button mAddEntryButton;
  private ScrollView mScrollView;
  private LinearLayout mTabletColumn = null;
  private String mContextEntry;
  private Action mContextAction;
  private int mActiveEntry; // useful only in keyboard mode
  private int mMaxPriority; // useful only with shiny priority :)
  private static int sCurrentTag;

  /**
   * Sets the theme of the given context, based on the give preferences.
   * 
   * @param c
   * @param sp
   */
  public final static void setTheme(final Context c, final SharedPreferences sp) {
    if (sp.getInt(Config.THEME, android.R.style.Theme) == android.R.style.Theme) {
      if (sp.getBoolean(Config.FULLSCREEN, false)) {
        c.setTheme(android.R.style.Theme_Black_NoTitleBar_Fullscreen);
      } else {
        c.setTheme(android.R.style.Theme_Black_NoTitleBar);
      }
    } else {
      if (sp.getBoolean(Config.FULLSCREEN, false)) {
        c.setTheme(android.R.style.Theme_Light_NoTitleBar_Fullscreen);
      } else {
        c.setTheme(android.R.style.Theme_Light_NoTitleBar);
      }
    }
  }

  @Override
  public void onCreate(Bundle icicle) {
    sPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    sEditor = sPref.edit();
    TagToDoList.setTheme(this, sPref);
    super.onCreate(icicle);
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .cancel(2);
    setContentView(R.layout.main);

    mTagSpinner = (Spinner) findViewById(R.id.tagSpinner);
    mTagSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      public void onItemSelected(AdapterView<?> parent, View v, int position,
          long id) {
        mActiveEntry = -1;
        selectTag(false, position);
        if (sTts != null) {
          sTts.speak(mTagsAdapter
              .getItem(sCurrentTag).toString());
        }
      }
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });
    mTagSpinner.setOnLongClickListener(new OnLongClickListener() {
      public boolean onLongClick(View arg0) {
        ((Spinner) arg0).setVisibility(View.GONE);
        final ImageButton configButton = ((ImageButton) findViewById(R.id.configButton));
        configButton.setVisibility(View.GONE);
        mAddEntryButton.setText(R.string.go_back);
        mAddEntryButton.setOnClickListener(null);
        mAddEntryButton.setOnTouchListener(new OnTouchListener() {
          public boolean onTouch(View arg0, MotionEvent me) {
            mTagSpinner.setVisibility(View.VISIBLE);
            configButton.setVisibility(View.VISIBLE);
            selectTag(false, -2);
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
        selectTag(false, -1);
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
        final boolean detailed = sPref.getBoolean(Config.DETAILED_STATS, false);
        final Button b = new Button(TagToDoList.this);
        b.setText(R.string.go_back);
        b.setOnClickListener(new View.OnClickListener() {
          public void onClick(View view) {
            d.dismiss();
          }
        });

        final CheckBox cb = new CheckBox(TagToDoList.this);
        cb.setChecked(detailed);
        cb.setText(R.string.stats_detailed);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            sEditor.putBoolean(Config.DETAILED_STATS, isChecked).commit();
            d.dismiss();
            mStatButton.performClick();
          }
        });
        final TableLayout tl = getStats(d, detailed);
        final TableRow tr = new TableRow(TagToDoList.this);
        tr.addView(b);
        tr.addView(cb);
        tl.addView(tr);
        final ScrollView sv = new ScrollView(TagToDoList.this);
        sv.addView(tl);

        d.setContentView(sv);
        d.setTitle(R.string.message_stats);
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
        if (Analytics.sTracker != null) {
          Analytics.sTracker.trackEvent(Analytics.ACTION_PRESS,
              "ADD_TASK_BUTTON", Analytics.SPACE_INTERFACE,
              mEntryLayout.getChildCount());
        }
      }
    });

    final ImageButton configButton = (ImageButton) findViewById(R.id.configButton);
    configButton.setBackgroundColor(Color.TRANSPARENT);
    configButton.setImageResource(android.R.drawable.ic_menu_preferences);
    configButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        startActivity(new Intent(v.getContext(), Config.class));
      }
    });

    sDbHelper = ToDoDB.getInstance(getApplicationContext());
    showDueTasks(true);
    showTabletMode();

    sGestureDetector = new GestureDetector(new MyGestureDetector());
    sGestureListener = new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (sGestureDetector.onTouchEvent(event)) {
          mTagSpinner.setSelection(Utils.iterate(
              sCurrentTag, mTagSpinner.getCount(),
              MyGestureDetector.getDirection()));
          return true;
        }
        return false;
      }
    };
  }

  /**
   * Shows the tablet mode (specifically an extra column), if it's the case
   */
  private final void showTabletMode() {
    sDisplayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(sDisplayMetrics);
    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE
        || sDisplayMetrics.widthPixels < 800) {
      return;
    }
    // tablet/landscape mode
    final LinearLayout bigLayout = (LinearLayout) findViewById(R.id.TagToDoLayout);
    mTabletColumn = new LinearLayout(this);
    final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    lp.width = 200;
    mTabletColumn.setLayoutParams(lp);
    getLayoutInflater().inflate(R.layout.taglist, mTabletColumn);
    bigLayout.addView(mTabletColumn, 0);
    ((LinearLayout) findViewById(R.id.upperLayout)).setVisibility(View.GONE);
    final ListView lv = (ListView) bigLayout.findViewById(R.id.tagList);
    lv.setOnTouchListener(new OnTouchListener(){
      public boolean onTouch(View arg0, MotionEvent arg1) {
        selectTag(true, lv.getSelectedItemPosition());
        return false;
      }
    });
  }

  /**
   * Gets the stats view.
   * 
   * @param detailed
   * @return
   */
  private final TableLayout getStats(final Dialog d, final boolean detailed) {
    final TableLayout tl = new TableLayout(this);
    if (detailed) {
      tl.setPadding(5, 0, 5, 0);
      final TableRow tr1 = new TableRow(TagToDoList.this);
      TextView tv1 = new TextView(TagToDoList.this);
      tv1.setText(R.string.stats_tasks_unchecked);
      tr1.addView(tv1);
      tv1 = new TextView(TagToDoList.this);
      tv1.setGravity(Gravity.CENTER_HORIZONTAL);
      tv1.setText(R.string.tag);
      tr1.addView(tv1);
      tl.addView(tr1);

      final Cursor c = sDbHelper.getTags();
      final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
      c.moveToFirst();
      do {
        final String tagName = c.getString(name);
        final TableRow tr = new TableRow(TagToDoList.this);
        final TextView tvCount = new TextView(TagToDoList.this);
        tvCount.setTextSize(30);
        tvCount.setTextColor(Color.YELLOW);
        tvCount.setGravity(Gravity.CENTER);
        tvCount.setText(Integer.toString(sDbHelper.getUncheckedCount(tagName)));
        tr.addView(tvCount);
        final Button b = new Button(TagToDoList.this);
        b.setText(tagName);
        b.setTag(c.getPosition());
        b.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            d.dismiss();
            mTagSpinner.setSelection((Integer) v.getTag());
          }
        });
        tr.addView(b);
        tl.addView(tr);
      } while (c.moveToNext());
      c.close();
      return tl;
    }
    final TableRow tr1 = new TableRow(this);
    final TableRow tr2 = new TableRow(this);
    final TableRow tr3 = new TableRow(this);
    final TextView tvCount1 = new TextView(this);
    final TextView tvExplanation1 = new TextView(this);
    final TextView tvCount2 = new TextView(this);
    final TextView tvExplanation2 = new TextView(this);

    tvCount1.setTextSize(44);
    tvCount1.setTextColor(Color.YELLOW);
    tvCount1.setPadding(1, 20, 0, 0);
    tvCount1.setGravity(LinearLayout.VERTICAL);
    tvCount1.setText(Integer.toString(sDbHelper.getUncheckedCount(mTagsAdapter
        .getItem(sCurrentTag).toString())));
    tr1.addView(tvCount1);
    tvExplanation1.setText(R.string.message_tasks_left_tag);
    tvExplanation1.setTextSize(19);
    tvExplanation1.setPadding(0, 37, 0, 0);
    tr1.addView(tvExplanation1);
    tr1.setBaselineAligned(false);
    tl.addView(tr1);
    tvCount2.setTextSize(44);
    tvCount2.setTextColor(Color.YELLOW);
    tvCount2.setPadding(1, 20, 0, 20);
    tvCount2.setGravity(LinearLayout.VERTICAL);
    tvCount2.setText(Integer.toString(sDbHelper.getUncheckedCount(null)));
    tr2.addView(tvCount2);
    tvExplanation2.setText(R.string.message_tasks_left_total);
    tvExplanation2.setTextSize(19);
    tvExplanation2.setPadding(0, 37, 0, 0);
    tr2.addView(tvExplanation2);
    tr2.setBaselineAligned(false);
    tl.addView(tr2);
    tl.addView(tr3);
    return tl;
  }

  /**
   * Visually applies the selection in the spinner to the main LinearLayout.
   * 
   * @param forceUI
   *          Make this method act as cause, not effect. If true, it will change
   *          the tag spinner itself. Equivalent to calling setSelection on the
   *          spinner.
   * @param selectedTag
   *          Index of the selected tag, as it will come from the spinner. If
   *          -1, it will show all tags. If -2, it will refresh the current tag.
   * 
   */
  private final void selectTag(final boolean forceUI, int selectedTag) {
    if (forceUI) {
      mTagSpinner.setSelection(selectedTag);
      return;
    }
    final LinearLayout el = mEntryLayout;
    el.removeAllViews();
    final OnCheckedChangeListener ccl = CHOICE_MODE ? new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
        try {
          cb.setChecked(!isChecked);
          sDbHelper.setSuperTask(mContextEntry, cb.getText().toString());
          selectTag(false, -2);
          CHOICE_MODE = false;
          ((LinearLayout) findViewById(R.id.lowerLayout))
              .setVisibility(View.VISIBLE);
        } catch (Exception e) {
          Utils.showDialog(R.string.notification, R.string.move_fail,
              cb.getContext());
        }
      }
    } : new OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
        if (sDbHelper.updateTask(cb.getText().toString(), isChecked)) {
          Utils.showDialog(R.string.notification,
              R.string.notification_checked_tasks_limit, cb.getContext());
        }
        selectTag(false, -2);
      }
    };

    if (selectedTag == -2) {
      selectedTag = sCurrentTag;
    }
    sCurrentTag=selectedTag;

    final ToDoDB dbHelper = sDbHelper;
    final LayoutInflater inflater = getLayoutInflater();
    mStatButton.setText(Integer.toString(processDepth(dbHelper, inflater, el,
        ccl, sPref.getInt(Config.TEXT_SIZE, 16),
        sPref.getInt(Config.TASK_PADDING, 12) - 12, selectedTag, 0, null)));
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
   * @param textSize
   *          Font size
   * @param taskPadding
   *          Task padding
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
      final OnCheckedChangeListener ccl, final int textSize,
      final int taskPadding, final int selectedTag, final int depth,
      final String superTask) {
    final Cursor c = sDbHelper.getTasks(selectedTag != -1 ? mTagsAdapter
        .getItem(selectedTag).toString() : null, depth, superTask);

    final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
    final int value = c.getColumnIndex(ToDoDB.KEY_STATUS);
    final int subtasks = c.getColumnIndex(ToDoDB.KEY_SUBTASKS);

    // these LayoutParams will be used for subtask indentation
    final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.WRAP_CONTENT);
    lp.weight = 1;

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
        ll.setPadding(0, 0, 0, taskPadding);
        final CheckBox cb = (CheckBox) ll.findViewById(R.id.taskCheckBox);
        final String taskName = c.getString(name);
        cb.setText(taskName);
        cb.setTextSize(textSize);

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
          lp.leftMargin = depth * 30;
          cb.setLayoutParams(lp);
        } else {
          lp.leftMargin = 0;
        }

        if (SHOW_NOTES) {
          final LinearLayout taskNoteLayout = (LinearLayout) ll
              .findViewById(R.id.taskNotesLayout);
          taskNoteLayout.setOrientation(0);

          try {
            auxInt = sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_WRITTEN);
          } catch (Exception e) {
            sDbHelper.repair();
            auxInt = sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_WRITTEN);
          }

          if (auxInt > 0) {
            taskNoteLayout.addView(getNoteButton(taskName, R.drawable.written,
                TASK_WRITTEN_ID));
          }

          if (sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_GRAPHICAL) > 0) {
            taskNoteLayout.addView(getNoteButton(taskName,
                android.R.drawable.ic_menu_edit, TASK_GRAPHICAL_ID));
          }

          if (sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_AUDIO) > 0) {
            taskNoteLayout.addView(getNoteButton(taskName, R.drawable.audio,
                TASK_AUDIO_ID));
          }

          if (sDbHelper.getStringFlag(taskName, ToDoDB.KEY_SECONDARY_TAGS)
              .length() > 0) {
            taskNoteLayout.addView(getNoteButton(taskName, R.drawable.star,
                TASK_TAGS_ID));
          }

          try { // placed in different try-catch clauses because these fields
                // appeared in different versions
            auxInt = sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_PHOTO);
          } catch (Exception e) {
            sDbHelper.repair();
            auxInt = sDbHelper.getIntFlag(taskName, ToDoDB.KEY_NOTE_IS_PHOTO);
          }

          if (auxInt > 0) {
            taskNoteLayout.addView(getNoteButton(taskName,
                android.R.drawable.ic_menu_camera, TASK_PHOTO_ID));
          }
        }

        boolean collapsed = false;
        if (SHOW_COLLAPSE) {
          final ImageButton ib = (ImageButton) ll
              .findViewById(R.id.taskCollapseButton);
          if (sDbHelper.getIntFlag(taskName, ToDoDB.KEY_SUBTASKS) > 0) {
            ib.setTag(Boolean.valueOf(collapsed = sDbHelper.getIntFlag(
                taskName, ToDoDB.KEY_IS_COLLAPSED) != 0));
            ib.setImageResource(collapsed ? android.R.drawable.ic_menu_add
                : android.R.drawable.ic_menu_close_clear_cancel);
            ib.setOnClickListener(new OnClickListener() {
              public void onClick(View v) {
                sDbHelper.setFlag(taskName, ToDoDB.KEY_IS_COLLAPSED,
                    (Boolean) ib.getTag() ? 0 : 1);
                selectTag(false, -2);
              }
            });
          } else {
            ib.setVisibility(View.GONE);
          }
        }

        // should we show the due time?
        if (SHOW_DUE_TIME
            && (auxInt = sDbHelper.getIntFlag(taskName,
                ToDoDB.KEY_EXTRA_OPTIONS)) > 0) {
          Chronos.refresh();
          final LinearLayout descLayout = (LinearLayout) ll
              .findViewById(R.id.descriptionLayout);
          descLayout.setPadding(sDescriptionAlignment + lp.leftMargin, -10, 0,
              -5);
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
              final Date d = new Date(sDbHelper.getDueDate(taskName));
              if (d.isMonthly()) {
                sb.append(getString(R.string.every));
                sb.append(' ');
                sb.append(getString(R.string.month));
                sb.append(", ");
                sb.append(getString(R.string.day));
                sb.append(' ');
                sb.append(d.getDay());
              } else {
                sb.append(getString(R.string.every));
                sb.append(' ');
                sb.append(getString(R.string.day));
              }
            }
          }

          b.setText(sb.toString());
          b.setOnClickListener(sDescriptionClickListener);
          descLayout.addView(b);
        }

        el.addView(ll);

        if (c.getInt(subtasks) > 0 && !collapsed) {
          numberOfUnchecked += processDepth(dbHelper, inflater, el, ccl,
              textSize, taskPadding, selectedTag, depth + 1, c.getString(name));
        }
      } while (c.moveToPrevious());
    }
    c.close();
    return numberOfUnchecked;
  }

  /**
   * Returns the image button which is actually the small thumbnail that shows
   * up next to the task, if there are notes.
   * 
   * @param task
   * @param resId
   *          The resource id for the image
   * @param changeId
   * @see {@link #changeTask(int)}
   * @return
   */
  public final ImageButton getNoteButton(final String task, final int resId,
      final int changeId) {
    final ImageButton ib = new ImageButton(this);
    ib.setBackgroundColor(Color.TRANSPARENT);
    ib.setPadding(0, 0, 0, 0);
    ib.setImageResource(resId);
    ib.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        mContextEntry = task;
        changeTask(changeId);
      }
    });
    return ib;
  }

  /**
   * Selection of menu button option
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent i;
    if (Analytics.sTracker != null) {
      Analytics.sTracker.trackPageView(Analytics.ACTION_PRESS + "/menu/"
          + item.getItemId());
    }
    switch (item.getItemId()) {
      case TAG_CREATE_ID:
        createTag();
        return true;
      case TAG_DELETE_ID:
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
      case TAG_CLEAR_CHECKED_ID:
        sDbHelper.deleteEntries(
            mTagsAdapter.getItem(sCurrentTag)
                .toString(), true);
        selectTag(false, -2);
        return true;
      case TAG_UNINDENT_ID:
        final Cursor c = sDbHelper.getTasks(
            mTagsAdapter.getItem(sCurrentTag)
                .toString(), -1, null);
        final int name = c.getColumnIndex(ToDoDB.KEY_NAME);
        if (c.getCount() > 0) {
          final ContentValues args = new ContentValues();
          args.put(ToDoDB.KEY_DEPTH, 0);
          args.put(ToDoDB.KEY_SUPERTASK, "");
          c.moveToFirst();
          do {
            sDbHelper.mDb.update(ToDoDB.DB_TASK_TABLE, args, ToDoDB.KEY_NAME
                + " = '" + c.getString(name) + "'", null);
          } while (c.moveToNext());
        }
        selectTag(false, -2);
        c.close();
        return true;
      case TAG_IMPORT_CSV_ID:
        i = new Intent(this, Confirmation.class);
        i.setAction(Integer.toString(TAG_IMPORT_CSV_ID));
        startActivity(i);
        break;
      case TAG_EXPORT_CSV_ID:
        final Calendar cal = Calendar.getInstance();
        final StringBuilder sb = new StringBuilder();
        sb.append("tag-todo-");
        sb.append(cal.get(Calendar.YEAR));
        sb.append('-');
        sb.append(cal.get(Calendar.MONTH) + 1);
        sb.append('-');
        sb.append(cal.get(Calendar.DAY_OF_MONTH));
        sb.append(".csv");
        Toast.makeText(
            this,
            getString(
                CSV.exportCSV(
                    new File(Environment.getExternalStorageDirectory(),
                        CSV.PATH + sb.toString()), sDbHelper)).concat(
                " ".concat(sb.toString())), Toast.LENGTH_LONG).show();
        break;
      case TAG_IMPORT_BACKUP_ID:
        i = new Intent(this, Confirmation.class);
        i.setAction(Integer.toString(TAG_IMPORT_BACKUP_ID));
        startActivity(i);
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Triggers the activity which asks for the tag's name, and when the response
   * reaches that activity, the tag is created with the given name
   */
  private final void createTag() {
    startActivityForResult(
        new Intent(this, Edit.class).setAction(Integer.toString(TAG_CREATE_ID)),
        TAG_CREATE_ID);
  }

  /**
   * Deletes the active tag
   */
  private void removeTag() {
    if (mTagSpinner.getCount() == 1) {
      Utils.showDialog(-1, R.string.impossible_tag_deletion, TagToDoList.this);
      return;
    }
    Intent i = new Intent(this, Confirmation.class);
    i.putExtra(ToDoDB.KEY_NAME,
        mTagsAdapter.getItem(sCurrentTag).toString());
    i.setAction(Integer.toString(TAG_DELETE_ID));
    startActivity(i);
  }

  /**
   * Deletes all the tasks in the active tag
   */
  private final void removeAllTasks() {
    final Intent i = new Intent(this, Confirmation.class);
    i.putExtra(ToDoDB.KEY_NAME,
        mTagsAdapter.getItem(sCurrentTag).toString());
    i.setAction(Integer.toString(TAG_CLEAR_ID));
    startActivity(i);
  }

  /**
   * Triggers an activity which asks for the active tag's new name
   */
  private void editTag() {
    Intent i = new Intent(this, Edit.class);
    i.putExtra(ToDoDB.KEY_NAME,
        mTagsAdapter.getItem(sCurrentTag).toString());
    i.setAction(Integer.toString(TAG_EDIT_ID));
    startActivity(i);
  }

  /**
   * Triggers an activity which shows a help screen
   */
  private void showHelpScreen() {
    startActivity(new Intent(this, Confirmation.class).setAction(Integer
        .toString(TAG_HELP_ID)));
  }

  /**
   * Triggers the activity which asks for the new entry name, and when the
   * response reaches that activity, the tag is created with the given name
   */
  private void createEntry() {
    Intent i = new Intent(this, Edit.class);
    i.putExtra(ToDoDB.KEY_NAME,
        mTagsAdapter.getItem(sCurrentTag).toString());
    i.putExtra(ToDoDB.KEY_SUPERTASK, "");
    i.setAction(Integer.toString(ACTIVITY_CREATE_ENTRY));
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
    sb.add(0, TAG_CLEAR_CHECKED_ID, 0, R.string.delete_checked);
    sb.add(0, TAG_IMPORT_CSV_ID, 0, R.string.import_CSV);
    sb.add(0, TAG_EXPORT_CSV_ID, 0, R.string.export_CSV);
    sb.add(0, TAG_IMPORT_BACKUP_ID, 0, R.string.backup_import);
    sb.add(0, TAG_EDIT_ID, 0, R.string.menu_edit_tag);
    sb.add(0, TAG_UNINDENT_ID, 0, R.string.menu_unindent);
    MenuItem item = menu.add(0, TAG_HELP_ID, 0, R.string.menu_instructions);
    item.setIcon(R.drawable.help);
    item = menu.add(0, TAG_CREATE_ID, 0, R.string.menu_create_tag);
    item.setIcon(R.drawable.add);
    item = menu.add(0, TAG_DELETE_ID, 0, R.string.menu_delete_tag);
    item.setIcon(R.drawable.delete);
    return true;
  }

  /**
   * Populates the interface with tags
   */
  private final void fillTagData() {
    ArrayAdapter<CharSequence> taa;
    if (mTabletColumn == null) {
      taa = new ArrayAdapter<CharSequence>(this,
          android.R.layout.simple_spinner_item);
      taa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    } else {
      taa = new ArrayAdapter<CharSequence>(this,
          android.R.layout.test_list_item);
    }

    final Cursor c = sDbHelper.getTags();

    c.moveToFirst();
    do {
      taa.add(c.getString(1));
    } while (c.moveToNext());
    c.close();

    mTagSpinner.setAdapter(taa);

    if (mTabletColumn != null) {
      ((ListView) findViewById(R.id.tagList)).setAdapter(taa);
    }
    mTagsAdapter = taa;
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
      Utils.copy(new File(Environment.getExternalStorageDirectory(),
          "/Tag-ToDo_data/database_backup"), new File(
          "/data/data/com.android.todo/databases"));
    } catch (Exception e) {
      Utils.showDialog(R.string.notification, R.string.backup_import_fail,
          c.getApplicationContext());
    }
    sDbHelper = ToDoDB.getInstance(c);
  }

  @Override
  protected void onDestroy() {
    if (sPref.getBoolean(Config.BACKUP_SDCARD, false)) {
      ToDoDB.createBackup();
    }

    if (sTts != null) {
      sTts.shutdown();
      sTts = null;
    }

    if (Analytics.sTracker != null) {
      final int month = Calendar.getInstance().get(Calendar.MONTH);
      if (month != sPref.getInt(Analytics.LAST_SYNCHRONIZED_MONTH, -1)) {
        Analytics.sTracker.trackPageView("version/".concat(Integer
            .toString(VERSION.SDK_INT)));
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY, "TAG_NUMBER",
            Analytics.SPACE_STATE, mTagSpinner.getCount());
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.AD_DISABLED, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.AD_DISABLED, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.ALARM_VIBRATION, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.AD_DISABLED, true) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.BACKUP_SDCARD, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.BACKUP_SDCARD, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.BLIND_MODE, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.BLIND_MODE, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.CHECKED_LIMIT, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.AD_DISABLED, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.CUSTOM_ALARM, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.CUSTOM_ALARM, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.FULLSCREEN, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.FULLSCREEN, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.GOOGLE_CALENDAR, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.GOOGLE_CALENDAR, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.NOTE_PREVIEW, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.NOTE_PREVIEW, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.PRIORITY_DISABLE, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.PRIORITY_DISABLE, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.PRIORITY_MAX, Analytics.SPACE_STATE,
            sPref.getInt(Config.PRIORITY_MAX, 100));
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.SHOW_COLLAPSE, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.SHOW_COLLAPSE, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.SHOW_DUE_TIME, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.SHOW_DUE_TIME, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.TASK_PADDING, Analytics.SPACE_STATE,
            sPref.getInt(Config.TASK_PADDING, 12));
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.TEXT_SIZE, Analytics.SPACE_STATE,
            sPref.getInt(Config.TEXT_SIZE, 16));
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.VISUAL_PRIORITY, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.VISUAL_PRIORITY, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.ALARM_DURATION, Analytics.SPACE_STATE,
            sPref.getInt(Config.ALARM_DURATION, 20));
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.ALARM_SCREEN, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.ALARM_SCREEN, false) ? 1 : 0);
        Analytics.sTracker.trackEvent(Analytics.ACTION_NOTIFY,
            Config.DETAILED_STATS, Analytics.SPACE_STATE,
            sPref.getBoolean(Config.DETAILED_STATS, false) ? 1 : 0);
        Analytics.sTracker.dispatch();
        sEditor.putInt(Analytics.LAST_SYNCHRONIZED_MONTH, month).commit();
      }
      Analytics.sTracker.stop();
    }

    TagToDoWidget.onUpdate(getApplicationContext(),
        AppWidgetManager.getInstance(getApplicationContext()));
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    super.onPause();
    sEditor.putInt(LAST_TAB, sCurrentTag).commit();
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

    setPrioritySort(sPref.getInt(PRIORITY_SORT, 0));
    setAlphabeticalSort(sPref.getInt(ALPHABET_SORT, 0));
    setDueDateSort(sPref.getInt(DUEDATE_SORT, 0));

    // Is the notes preview feature enabled?
    SHOW_NOTES = sPref.getBoolean(Config.NOTE_PREVIEW, false)
        || mTabletColumn != null;

    // Should the collapse buttons be shown?
    SHOW_COLLAPSE = sPref.getBoolean(Config.SHOW_COLLAPSE, false);

    // Should due time be shown for the tasks that have it?
    if (SHOW_DUE_TIME = sPref.getBoolean(Config.SHOW_DUE_TIME, false)) {
      sDescriptionClickListener = new OnClickListener() {
        public void onClick(View v) {
          mContextEntry = ((CheckBox) ((LinearLayout) ((LinearLayout) v
              .getParent().getParent()).getChildAt(0)).getChildAt(0)).getText()
              .toString();
          changeTask(TASK_EDIT_ID);
        }
      };
      // this positions the description under the task, but to the right of the
      // checkbox icon
      sDescriptionAlignment = (int) (26 * sDisplayMetrics.xdpi / 150);
    }

    // Should checked tasks be hidden?
    HIDE_CHECKED = sPref.getBoolean(HIDE_CHECKED_SORT, false);

    // Is a Google Calendar sync enabled?
    if (SYNC_GCAL = sPref.getBoolean(Config.GOOGLE_CALENDAR, false)) {
      GoogleCalendar.setLogin(sPref.getString(Config.GOOGLE_USERNAME, ""),
          sPref.getString(Config.GOOGLE_PASSWORD, ""));
    }

    // do we visually distinguish tasks by priority?
    if (SHINY_PRIORITY = sPref.getBoolean(Config.VISUAL_PRIORITY, false)) {
      mMaxPriority = sPref.getInt(Config.PRIORITY_MAX, 100);
    }

    // Restore the last selected tag
    sCurrentTag = sPref.getInt(LAST_TAB, 0);
    if (sCurrentTag >= mTagSpinner.getCount()) {
      sCurrentTag = 0;
    }
    mTagSpinner.setSelection(sCurrentTag, true);

    if (sPref.getBoolean(Config.BLIND_MODE, false)) {
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
    if (Analytics.sTracker == null
        && sPref.getBoolean(Config.USAGE_STATS, false)) {
      Analytics.sTracker = GoogleAnalyticsTracker.getInstance();
      Analytics.sTracker.start(Analytics.UA_CODE, this);
      Analytics.sTracker.trackPageView(Analytics.VIEW_MAIN);
    } else {
      Analytics.sTracker = null;
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case TASK_PHOTO_ID:
        if (resultCode == RESULT_OK) {
          sDbHelper.setFlag(mContextEntry, ToDoDB.KEY_NOTE_IS_PHOTO, 1);
        } else if (resultCode == RESULT_CANCELED) {
          // sDbHelper.setFlag(mContextEntry, ToDoDB.KEY_NOTE_IS_PHOTO, 0);
        } else {
          Toast.makeText(this, getString(R.string.error_photo),
              Toast.LENGTH_LONG).show();
        }
        break;
      case TAG_CREATE_ID:
        if (resultCode == RESULT_OK) {
          fillTagData();
          sEditor.putInt(LAST_TAB,
              mTagsAdapter.getPosition(data.getStringExtra(ToDoDB.KEY_NAME)))
              .commit();
        }
        break;
    }
  }

  /**
   * Creates the context menu for a to-do list entry (task editing, deletion,
   * etc.)
   */
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo) {
    mContextEntry = ((CheckBox) v).getText().toString();
    final Action a = new Action();
    final String possibleAction = a.setAndExtractAction(mContextEntry);
    if (possibleAction != null) {
      mContextAction = a;
      menu.add(0, TASK_INSTANTACTION_ID, 0, possibleAction);
    }
    menu.add(0, TASK_SUBTASK_ID, 0, R.string.entry_subtask_add);
    menu.add(0, TASK_EDIT_ID, 0, R.string.entry_edit);
    menu.add(0, TASK_REMOVE_ID, 0, R.string.entry_delete);
    SubMenu submenu = menu.addSubMenu(R.string.entry_group_notes);
    submenu.add(0, TASK_AUDIO_ID, 0, R.string.entry_audio_note);
    submenu.add(0, TASK_GRAPHICAL_ID, 0, R.string.entry_graphical_note);
    submenu.add(0, TASK_PHOTO_ID, 0, R.string.entry_photo_note);
    submenu.add(0, TASK_WRITTEN_ID, 0, R.string.entry_written_note);
    submenu = menu.addSubMenu(R.string.entry_group_move);
    submenu.add(0, TASK_TAGS_ID, 0, R.string.entry_add_tags);
    submenu.add(0, TASK_MOVE_ID, 0, R.string.entry_move);
    submenu.add(0, TASK_MOVE_UNDER_TASK_ID, 0, R.string.entry_move_under_task);
    submenu = menu.addSubMenu(R.string.entry_group_share);
    submenu.add(0, TASK_EMAIL_ID, 0, R.string.email);
    submenu.add(0, TASK_SMS_ID, 0, R.string.SMS);
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
  private final boolean changeTask(final int selectedItem) {
    Intent i;
    if (Analytics.sTracker != null) {
      Analytics.sTracker.trackPageView(Analytics.ACTION_PRESS + "/taskMenu/"
          + selectedItem);
    }
    switch (selectedItem) {
      case TASK_EDIT_ID:
        i = new Intent(this, Edit.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.setAction(Integer.toString(ACTIVITY_EDIT_ENTRY));
        startActivity(i);
        break;
      case TASK_SUBTASK_ID:
        i = new Intent(this, Edit.class);
        i.putExtra(ToDoDB.KEY_NAME,
            mTagsAdapter.getItem(sCurrentTag)
                .toString());
        i.putExtra(ToDoDB.KEY_SUPERTASK, mContextEntry);
        i.setAction(Integer.toString(ACTIVITY_CREATE_ENTRY));
        startActivity(i);
        break;
      case TASK_REMOVE_ID:
        sDbHelper.deleteTask(mContextEntry);
        selectTag(false, -2);
        break;
      case TASK_GRAPHICAL_ID:
        i = new Intent(this, Graphics.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        startActivity(i);
        break;
      case TASK_AUDIO_ID:
        i = new Intent(this, Audio.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.putExtra(ToDoDB.KEY_STATUS, true);
        startActivity(i);
        break;
      case TASK_MOVE_ID:
        final OnItemSelectedListener l = mTagSpinner
            .getOnItemSelectedListener();
        final int p = sCurrentTag;
        mTagSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
          public void onItemSelected(AdapterView<?> av, View v, int index,
              long arg3) {
            sDbHelper.updateTaskParent(mContextEntry,
                mTagsAdapter.getItem(index).toString(), 0);
            av.setOnItemSelectedListener(l);
            av.setSelection(p);
          }

          public void onNothingSelected(AdapterView<?> arg0) {
          }
        });
        mTagSpinner.performClick();
        break;
      case TASK_MOVE_UNDER_TASK_ID:
        CHOICE_MODE = true;
        ((LinearLayout) findViewById(R.id.lowerLayout))
            .setVisibility(View.GONE);
        selectTag(false, -2);
        break;
      case TASK_PHOTO_ID:
        int isPhoto;
        try {
          isPhoto = sDbHelper.getIntFlag(mContextEntry,
              ToDoDB.KEY_NOTE_IS_PHOTO);
        } catch (Exception e) {
          sDbHelper.repair();
          isPhoto = sDbHelper.getIntFlag(mContextEntry,
              ToDoDB.KEY_NOTE_IS_PHOTO);
        }
        if (isPhoto == 0) {
          final ContentValues values = new ContentValues();
          values.put(MediaStore.Images.Media.TITLE, mContextEntry);
          values.put(MediaStore.Images.Media.DESCRIPTION,
              getString(R.string.entry_photo_note));
          try {
            final Uri imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            sDbHelper.setFlag(mContextEntry, ToDoDB.KEY_NOTE_IS_PHOTO, 1);
            sDbHelper.setFlag(mContextEntry, ToDoDB.KEY_PHOTO_NOTE_URI,
                imageUri.toString());
            i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(i, TASK_PHOTO_ID);
          } catch (Exception e) {
            Utils.showDialog(R.string.notification,
                R.string.recording_impossible, this);
          }
        } else {
          i = new Intent(this, Photo.class);
          i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
          startActivity(i);
        }
        break;
      case TASK_WRITTEN_ID:
        i = new Intent(this, Edit.class);
        i.putExtra(ToDoDB.KEY_NAME, mContextEntry);
        i.setAction(Integer.toString(TASK_WRITTEN_ID));
        startActivity(i);
        break;
      case TASK_SMS_ID:
        i = new Intent(Intent.ACTION_VIEW);
        i.putExtra("sms_body", getString(R.string.todo) + ": " + mContextEntry);
        i.setType("vnd.android-dir/mms-sms");
        startActivity(i);
        break;
      case TASK_EMAIL_ID:
        // Create a new Intent to send messages
        i = new Intent(Intent.ACTION_SEND);
        // Add attributes to the intent
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.tasks) + " - "
            + mTagsAdapter.getItem(sCurrentTag));
        i.putExtra(Intent.EXTRA_TEXT, mContextEntry);
        i.setType("plain/text");
        startActivity(Intent.createChooser(i, getString(R.string.email)));
        break;
      case TASK_INSTANTACTION_ID:
        mContextAction.perform(this);
        break;
      case TASK_TAGS_ID:
        final ArrayList<String> al = new ArrayList<String>(
            Arrays.asList(sDbHelper.getStringFlag(mContextEntry,
                ToDoDB.KEY_SECONDARY_TAGS).split("'")));
        final Dialog d = new Dialog(this);
        d.setTitle(R.string.entry_add_tags);
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(0, 0, 10, 0);
        final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
            LayoutParams.FILL_PARENT);
        final Button closeButton = new Button(this);
        closeButton.setText(R.string.go_back);
        closeButton.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            final StringBuilder sb = new StringBuilder();
            for (String s : al) {
              sb.append('\'');
              sb.append(s);
            }
            String s = sb.toString();
            if (s.startsWith("'")) {
              s = s.substring(1);
            }
            sDbHelper.setFlag(mContextEntry, ToDoDB.KEY_SECONDARY_TAGS, s);
            selectTag(false, -2);
            d.dismiss();
          }
        });
        for (int tagIndex = 0; tagIndex < mTagsAdapter.getCount(); tagIndex++) {
          final LinearLayout row = new LinearLayout(this);
          row.setOrientation(LinearLayout.HORIZONTAL);
          final ImageButton ib = new ImageButton(this);
          ib.setBackgroundColor(Color.TRANSPARENT);
          ib.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              ImageButton ib = (ImageButton) v;
              TextView tv = (TextView) ((LinearLayout) v.getParent())
                  .getChildAt(1);
              if ((Integer) ib.getTag() == 0) {
                ib.setImageResource(android.R.drawable.btn_star_big_on);
                tv.setTextAppearance(v.getContext(),
                    android.R.style.TextAppearance_Medium);
                al.add(tv.getText().toString());
                ib.setTag(1);
              } else {
                ib.setImageResource(android.R.drawable.btn_star_big_off);
                tv.setTextAppearance(v.getContext(),
                    android.R.style.TextAppearance_Small);
                al.remove(tv.getText().toString());
                ib.setTag(0);
              }
            }
          });
          final TextView tv = new TextView(this);
          tv.setTag(tagIndex);
          tv.setLayoutParams(lp);
          tv.setGravity(Gravity.CENTER_VERTICAL);
          tv.setText(mTagsAdapter.getItem(tagIndex));
          if (al.contains(mTagsAdapter.getItem(tagIndex))) {
            ib.setImageResource(android.R.drawable.btn_star_big_on);
            tv.setTextAppearance(this, android.R.style.TextAppearance_Medium);
            ib.setTag(1);
          } else {
            ib.setImageResource(android.R.drawable.btn_star_big_off);
            tv.setTextAppearance(this, android.R.style.TextAppearance_Small);
            ib.setTag(0);
          }
          if (sDbHelper.getStringFlag(mContextEntry, ToDoDB.KEY_TAG).equals(
              mTagsAdapter.getItem(tagIndex))) {
            ib.setVisibility(View.INVISIBLE);
            tv.setTextAppearance(this, android.R.style.TextAppearance_Medium);
            tv.setTypeface(null, Typeface.ITALIC);
          }
          tv.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              closeButton.performClick();
              mTagSpinner.setSelection((Integer) v.getTag());
            }
          });
          row.addView(ib);
          row.addView(tv);
          ll.addView(row);
        }
        final ScrollView sv = new ScrollView(this);
        sv.addView(ll);
        final LinearLayout bigLayout = new LinearLayout(this);
        bigLayout.setOrientation(LinearLayout.VERTICAL);
        final TextView tv = new TextView(this);
        tv.setPadding(10, 0, 10, 5);
        tv.setText(R.string.entry_tags_explanation);
        bigLayout.addView(tv);
        bigLayout.addView(closeButton);
        bigLayout.addView(sv);
        d.setContentView(bigLayout);
        d.show();
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
  @Override
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
        return false;
      case KeyEvent.KEYCODE_DPAD_LEFT:
        mTagSpinner.setSelection(Utils.iterate(
            sCurrentTag, mTagSpinner.getCount(), 1));
        // ???replace this with a method, it's called too many times
        return false;
      case (KeyEvent.KEYCODE_N):
        if (msg.isAltPressed()) {
          mTagSpinner
              .setSelection(Utils.iterate(
                  sCurrentTag,
                  mTagSpinner.getCount(), 1));
        } else {
          selectAnotherEntry(1);
        }
        return false;
      case KeyEvent.KEYCODE_DPAD_DOWN:
        selectAnotherEntry(1);
        return true;
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        mTagSpinner.setSelection(Utils.iterate(
            sCurrentTag, mTagSpinner.getCount(), -1));
        return false;
      case (KeyEvent.KEYCODE_P):
        if (msg.isAltPressed()) {
          mTagSpinner
              .setSelection(Utils.iterate(
                  sCurrentTag,
                  mTagSpinner.getCount(), -1));
        } else {
          selectAnotherEntry(-1);
        }
        return false;
      case KeyEvent.KEYCODE_DPAD_UP:
        selectAnotherEntry(-1);
        return true;
      case (KeyEvent.KEYCODE_ENTER):
      case (KeyEvent.KEYCODE_DPAD_CENTER):
        if (mActiveEntry > -1) {
          ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).performClick();
        }
        return false;
      case (KeyEvent.KEYCODE_SPACE):
        if (mActiveEntry > -1) {
          ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).performLongClick();
        }
        return false;
      case (KeyEvent.KEYCODE_A):
        if (msg.isAltPressed()) {
          createTag();
        } else {
          mAddEntryButton.performClick();
        }
        return false;
      case (KeyEvent.KEYCODE_S):
        mStatButton.performClick();
        return false;
      case (KeyEvent.KEYCODE_D):
        if (msg.isAltPressed()) {
          removeTag();
        } else {
          if (mActiveEntry > -1) {
            mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
                .findViewById(R.id.taskCheckBox))).getText().toString();
            changeTask(TASK_REMOVE_ID);
            mActiveEntry = Utils.iterate(mActiveEntry,
                mEntryLayout.getChildCount(), -1);
          }
        }
        return false;
      case (KeyEvent.KEYCODE_E):
      case (KeyEvent.KEYCODE_R):
        if (msg.isAltPressed()) {
          editTag();
        } else {
          if (mActiveEntry > -1) {
            mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
                .findViewById(R.id.taskCheckBox))).getText().toString();
            changeTask(TASK_EDIT_ID);
          }
        }
        return false;
      case (KeyEvent.KEYCODE_G):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(TASK_GRAPHICAL_ID);
        }
        return false;
      case (KeyEvent.KEYCODE_F):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(TASK_AUDIO_ID);
        }
        return false;
      case (KeyEvent.KEYCODE_PERIOD):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(TASK_SUBTASK_ID);
        }
        return false;
      case (KeyEvent.KEYCODE_H):
        showHelpScreen();
        return false;
      case (KeyEvent.KEYCODE_X):
        removeAllTasks();
        return false;
      case (KeyEvent.KEYCODE_U):
        startActivity(new Intent(this, NotificationActivity.class));
        return false;
      case (KeyEvent.KEYCODE_M):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(TASK_MOVE_ID);
          mActiveEntry = Utils.iterate(mActiveEntry,
              mEntryLayout.getChildCount(), -1);
        }
        return false;
      case (KeyEvent.KEYCODE_C):
        startActivity(new Intent(this, Config.class));
        return false;
      case KeyEvent.KEYCODE_BACK:
        finish();
        return false;
      case (KeyEvent.KEYCODE_W):
        if (mActiveEntry > -1) {
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)
              .findViewById(R.id.taskCheckBox))).getText().toString();
          changeTask(TASK_WRITTEN_ID);
        }
        return false;
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
    if (sTts != null) {
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
  private final boolean showDueTasks(boolean showDialog) {
    final Cursor dueEntries = sDbHelper.getDueEntries();
    if (dueEntries.getCount() > 0) {
      if (showDialog) {
        dueEntries.moveToFirst();
        int name;
        try {
          name = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_NAME);
        } catch (Exception e) {
          dueEntries.close();
          return false;
        }
        StringBuilder sb = new StringBuilder(
            this.getString(R.string.due_date_notification) + "\n");
        do {
          sb.append(dueEntries.getString(name));
          sb.append("\n");
        } while (dueEntries.moveToNext());
        Utils.showDueTasksNotification(sb.toString(), TagToDoList.this);
      }
      dueEntries.close();
      return true;
    }
    dueEntries.close();
    return false;
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
    switch (sPref.getInt(PRIORITY_SORT, 0)) {
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
    switch (sPref.getInt(DUEDATE_SORT, 0)) {
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
    switch (sPref.getInt(ALPHABET_SORT, 0)) {
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

        if (priorityDown.isChecked()) {
          setPrioritySort(1);
          sEditor.putInt(PRIORITY_SORT, 1);
        } else if (priorityUp.isChecked()) {
          setPrioritySort(2);
          sEditor.putInt(PRIORITY_SORT, 2);
        } else {
          setPrioritySort(3);
          sEditor.putInt(PRIORITY_SORT, 0);
        }

        if (alphabetDown.isChecked()) {
          setAlphabeticalSort(1);
          sEditor.putInt(ALPHABET_SORT, 1);
        } else if (alphabetUp.isChecked()) {
          setAlphabeticalSort(2);
          sEditor.putInt(ALPHABET_SORT, 2);
        } else {
          setAlphabeticalSort(3);
          sEditor.putInt(ALPHABET_SORT, 0);
        }

        if (dateDown.isChecked()) {
          setDueDateSort(1);
          sEditor.putInt(DUEDATE_SORT, 1);
        } else if (dateUp.isChecked()) {
          setDueDateSort(2);
          sEditor.putInt(DUEDATE_SORT, 2);
        } else {
          setDueDateSort(3);
          sEditor.putInt(DUEDATE_SORT, 0);
        }

        sEditor.putBoolean(HIDE_CHECKED_SORT,
            HIDE_CHECKED = hideChecked.isChecked());

        sEditor.commit();
        selectTag(false, -2);
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
        break;
      case 2:
        ToDoDB.setPriorityOrder(true, true);
        break;
      case 3:
        ToDoDB.setPriorityOrder(false, false);
        break;
    }
    selectTag(false, -2);
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
        break;
      case 2:
        ToDoDB.setAlphabeticalOrder(true, true);
        break;
      case 3:
        ToDoDB.setAlphabeticalOrder(false, false);
        break;
    }
    selectTag(false, -2);
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
        break;
      case 2:
        ToDoDB.setDueDateOrder(true, true);
        break;
      case 3:
        ToDoDB.setDueDateOrder(false, false);
        break;
    }
    selectTag(false, -2);
  }
}