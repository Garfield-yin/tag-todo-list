//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
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

import com.android.todo.sync.GoogleCalendar;

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

  public static final String PREFS_NAME = "TagToDoListPrefs";
  private static final String PRIORITY_SORT = "prioritySorting";
  private static final String ALPHABET_SORT = "alphabeticalSorting";
  private static final String DUEDATE_SORT = "dueDateSorting";

  // Flags
  public static boolean SYNC_GCAL;
  public static boolean SHINY_PRIORITY;

  private static ToDoListDB mDbHelper;
  private static SharedPreferences mSettings;
  private static Context mContext = null;
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
          public boolean onTouch(View arg0, MotionEvent arg1) {
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
        String s = mDbHelper.countUncheckedEntries(mTagsArrayAdapter.getItem(
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
        s = mDbHelper.countUncheckedEntries() + "";
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

    mDbHelper = new ToDoListDB(this);
    mDbHelper.open();

    showDueTasks(true);
  }

  /**
   * Visually applies the selection in the spinner to the main LinearLayout.
   * 
   * @param selectedTab
   *          index of the selected tab, as it will come from the spinner
   */
  private void selectTag(int selectedTag) {
    // fetching the data from the database:
    Cursor c = mDbHelper.getEntries(selectedTag != -1 ? mTagsArrayAdapter
        .getItem(selectedTag).toString() : null);
    startManagingCursor(c);

    LinearLayout el = mEntryLayout;
    el.removeAllViews();
    CheckBox cb;
    int name;
    int value;
    try {
      name = c.getColumnIndexOrThrow(ToDoListDB.KEY_NAME);
      value = c.getColumnIndexOrThrow(ToDoListDB.KEY_STATUS);
    } catch (Exception e) {
      return;
    }

    Boolean checked;
    int numberOfUnchecked = 0;
    CompoundButton.OnCheckedChangeListener ccl = new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mDbHelper.updateEntry(buttonView.getText().toString(), isChecked);
        selectTag(mTagSpinner.getSelectedItemPosition());
      }
    };
    final int maxPriority = mMaxPriority;
    final ToDoListDB dbHelper = mDbHelper;
    if (c.getCount() > 0) {
      c.moveToLast();
      do {
        cb = new CheckBox(this);
        if (SHINY_PRIORITY) {
          int color = dbHelper.getPriority(c.getString(name)) * 191
              / maxPriority + 64;
          cb.setTextColor(Color.rgb(color, color, color));
        }
        cb.setText(c.getString(name));
        if (c.getInt(value) == 1) { // 1 means checked, 0 means
          // unchecked
          checked = true;
        } else {
          checked = false;
          numberOfUnchecked += 1;
        }
        cb.setChecked(checked);
        cb.setOnCheckedChangeListener(ccl);
        registerForContextMenu(cb);
        el.addView(cb);
      } while (c.moveToPrevious());
    }

    mStatButton.setText(numberOfUnchecked + ""); // updating stats
  }

  /**
   * Selection of menu button option
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    return applyMenuChoice(item) || super.onOptionsItemSelected(item);
  }

  /**
   * Chooses the right action depending on what menu item has been clicked
   * 
   * @param item
   *          the menu item which has been clicked
   * @return
   */
  private boolean applyMenuChoice(MenuItem item) {
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
    i.putExtra(ToDoListDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_DELETE_TAG + "");
    startActivity(i);
  }

  /**
   * Deletes all the tasks in the active tag
   */
  private void removeTags() {
    if (mTagSpinner.getCount() == 1) {
      Utils.showDialog(-1, R.string.impossible_tag_deletion, TagToDoList.this);
      return;
    }
    Intent i = new Intent(this, ConfirmationScreen.class);
    i.putExtra(ToDoListDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_CLEAR_ENTRIES + "");
    startActivity(i);
  }

  /**
   * Triggers an activity which asks for the active tag's new name
   */
  private void editTag() {
    Intent i = new Intent(this, EditScreen.class);
    i.putExtra(ToDoListDB.KEY_NAME, mTagsArrayAdapter.getItem(
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
    i.putExtra(ToDoListDB.KEY_NAME, mTagsArrayAdapter.getItem(
        mTagSpinner.getSelectedItemPosition()).toString());
    i.setAction(ACTIVITY_CREATE_ENTRY + "");
    startActivity(i);
  }

  /**
   * Populates the menu with actions like tag creation, renaming, deletion
   * 
   * @param menu
   * @return
   */
  private boolean populateMenu(Menu menu) {
    MenuItem item1 = menu.add(0, TAG_INSERT_ID, 0, R.string.menu_create_tag);
    item1.setIcon(R.drawable.add);
    item1 = menu.add(0, TAG_REMOVE_ID, 0, R.string.menu_delete_tag);
    item1.setIcon(R.drawable.delete);
    item1 = menu.add(0, TAG_EDIT_ID, 0, R.string.menu_edit_tag);
    item1.setIcon(R.drawable.rename);
    item1 = menu.add(0, TAG_HELP_ID, 0, R.string.menu_instructions);
    item1.setIcon(R.drawable.help);
    return true;
  }

  /**
   * Hook into menu button for activity
   */
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    populateMenu(menu);
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

    Cursor c = mDbHelper.getAllTags();
    ArrayAdapter<CharSequence> taa = mTagsArrayAdapter;
    startManagingCursor(c);

    c.moveToFirst();
    do {
      taa.add(c.getString(1));
    } while (c.moveToNext());

    mTagSpinner.setAdapter(taa);
  }

  /**
   * Copies a database copy from the SD card over the existing database
   */
  public static final void importBackupSD() {
    mDbHelper.close();
    try {
      Utils.copy(new File("/sdcard/Tag-ToDo_data/database_backup"), new File(
          "/data/data/com.android.todo/databases"));
    } catch (Exception e) {
      Utils.showDialog(R.string.notification, R.string.backup_import_fail,
          mContext);
    }
    mDbHelper = new ToDoListDB(mContext);
    mDbHelper.open();
    mContext = null;
  }

  @Override
  protected void onDestroy() {
    if (mSettings.getBoolean(ConfigScreen.BACKUP_SDCARD, false)) {
      ToDoListDB.createBackup();
    }
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mSettings = getSharedPreferences(PREFS_NAME, 0);
    populateFields();
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
   * Initiates the interface population
   */
  private void populateFields() {
    fillTagData();

    setPrioritySort(mSettings.getInt(PRIORITY_SORT, 0));
    setAlphabeticalSort(mSettings.getInt(ALPHABET_SORT, 0));
    setDueDateSort(mSettings.getInt(DUEDATE_SORT, 0));

    // Is a Google Calendar sync enabled?
    if (SYNC_GCAL = mSettings.getBoolean(ConfigScreen.GOOGLE_CALENDAR, false)) {
      GoogleCalendar.setLogin(mSettings.getString(ConfigScreen.GOOGLE_USERNAME,
          ""), mSettings.getString(ConfigScreen.GOOGLE_PASSWORD, ""));
    }

    // do we visually distinguish tasks by priority?
    if (SHINY_PRIORITY = mSettings.getBoolean(ConfigScreen.VISUAL_PRIORITY,
        false)) {
      mMaxPriority = mSettings.getInt(ConfigScreen.PRIORITY_MAX, 100);
      // selectTag(mTagSpinner.getSelectedItemPosition());
    }

    // Restore the last selected tag
    int lastSelectedTag = mSettings.getInt("lastSelectedTag", 0);
    if (lastSelectedTag >= mTagSpinner.getCount()) {
      lastSelectedTag = 0;
    }
    mTagSpinner.setSelection(lastSelectedTag, true);
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
    Action a = new Action();
    String possibleAction = a.setAndExtractAction(mContextEntry);
    if (!("".equals(possibleAction))) {
      mContextAction = a;
      menu.add(0, ENTRY_INSTANTACTION_ID, 0, possibleAction);
    }
    menu.add(0, ENTRY_EDIT_ID, 0, R.string.entry_edit);
    menu.add(0, ENTRY_REMOVE_ID, 0, R.string.entry_delete);
    SubMenu submenu = menu.addSubMenu(R.string.entry_group_notes);
    submenu.add(0, ENTRY_AUDIO_ID, 0, R.string.entry_audio_note);
    submenu.add(0, ENTRY_GRAPHICAL_ID, 0, R.string.entry_graphical_note);
    submenu.add(0, ENTRY_WRITTEN_ID, 0, R.string.entry_written_note);
    submenu = menu.addSubMenu(R.string.entry_group_move);
    submenu.add(0, ENTRY_MOVE_ID, 0, R.string.entry_move);
    submenu.add(0, ENTRY_DOWN_ID, 0, R.string.entry_down);
    menu.setHeaderTitle(R.string.entry_menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int id = item.getItemId();
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
    switch (selectedItem) {
    case ENTRY_EDIT_ID:
      Intent i1 = new Intent(this, EditScreen.class);
      i1.putExtra(ToDoListDB.KEY_NAME, mContextEntry);
      i1.setAction(ACTIVITY_EDIT_ENTRY + "");
      startActivity(i1);
      break;
    case ENTRY_REMOVE_ID:
      mDbHelper.deleteEntry(mContextEntry);
      selectTag(mTagSpinner.getSelectedItemPosition());
      break;
    case ENTRY_GRAPHICAL_ID:
      Intent i2 = new Intent(this, PaintScreen.class);
      i2.putExtra(ToDoListDB.KEY_NAME, mContextEntry);
      startActivity(i2);
      break;
    case ENTRY_AUDIO_ID:
      Intent i3 = new Intent(this, AudioScreen.class);
      i3.putExtra(ToDoListDB.KEY_NAME, mContextEntry);
      i3.putExtra(ToDoListDB.KEY_STATUS, true);
      startActivity(i3);
      break;
    case ENTRY_DOWN_ID:
      mDbHelper.pushEntryDown(mContextEntry);
      selectTag(mTagSpinner.getSelectedItemPosition());
      break;
    case ENTRY_MOVE_ID:
      final AdapterView.OnItemSelectedListener l = mTagSpinner
          .getOnItemSelectedListener();
      final int p = mTagSpinner.getSelectedItemPosition();
      mTagSpinner
          .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                int arg2, long arg3) {
              mDbHelper.updateEntryParent(mContextEntry, mTagsArrayAdapter
                  .getItem(arg2).toString());
              arg0.setOnItemSelectedListener(l);
              arg0.setSelection(p);
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
          });
      mTagSpinner.performClick();
      break;
    case ENTRY_WRITTEN_ID:
      Intent i4 = new Intent(this, EditScreen.class);
      i4.putExtra(ToDoListDB.KEY_NAME, mContextEntry);
      i4.setAction(ACTIVITY_WRITE_NOTE + "");
      startActivity(i4);
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
    int keyCode = msg.getKeyCode();
    switch (keyCode) {
    case KeyEvent.KEYCODE_T:
      if (msg.isShiftPressed()) {
        mTagSpinner.performLongClick();
      } else {
        mTagSpinner.performClick();
      }
      break;
    case (KeyEvent.KEYCODE_N):
      if (msg.isAltPressed()) {
        mTagSpinner.setSelection(Utils.iterate(mTagSpinner
            .getSelectedItemPosition(), mTagSpinner.getCount(), 1));
      } else {
        selectAnotherEntry(1);
      }
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
        ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry))).performClick();
      }
      break;
    case (KeyEvent.KEYCODE_SPACE):
      if (mActiveEntry > -1) {
        ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry))).performLongClick();
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
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
              .getText().toString();
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
          mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
              .getText().toString();
          changeTask(ENTRY_EDIT_ID);
        }
      }
      break;
    case (KeyEvent.KEYCODE_G):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ENTRY_GRAPHICAL_ID);
      }
      break;
    case (KeyEvent.KEYCODE_F):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ENTRY_AUDIO_ID);
      }
      break;
    case (KeyEvent.KEYCODE_0):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ENTRY_DOWN_ID);
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
      removeTags();
      break;
    case (KeyEvent.KEYCODE_U):
      startActivity(new Intent(this, NotificationActivity.class));
      break;
    case (KeyEvent.KEYCODE_M):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ENTRY_MOVE_ID);
        mActiveEntry = Utils.iterate(mActiveEntry,
            mEntryLayout.getChildCount(), -1);
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
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ENTRY_WRITTEN_ID);
      }
      break;
    case (KeyEvent.KEYCODE_B):
      mContext = this;
      Intent i = new Intent(this, ConfirmationScreen.class);
      i.setAction(ACTIVITY_BACKUP_IMPORT + "");
      startActivity(i);
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
  private void selectAnotherEntry(int increment) {
    if (mActiveEntry > -1) {
      ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
          .setBackgroundColor(Color.TRANSPARENT);
    }
    if (mEntryLayout.getChildCount() == 0) {
      return;
    }
    mActiveEntry = Utils.iterate(mActiveEntry, mEntryLayout.getChildCount(),
        increment);
    CheckBox cb = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)));
    cb.setBackgroundColor(Color.DKGRAY);
    if (cb.getTop() < mScrollView.getScrollY()
        || cb.getTop() > mScrollView.getScrollY() + mScrollView.getHeight()) {
      mScrollView.smoothScrollTo(0, cb.getTop());
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
    Cursor dueEntries = mDbHelper.getDueEntries();
    if (dueEntries.getCount() > 0) {
      if (showDialog) {
        dueEntries.moveToFirst();
        int name;
        try {
          name = dueEntries.getColumnIndexOrThrow(ToDoListDB.KEY_NAME);
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
    final SharedPreferences.Editor editor = mSettings.edit();
    int currentLimit = mSettings.getInt(ConfigScreen.CHECKED_LIMIT, 50);
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
   * Necessary in the notification activity, if it ever gets launched
   * 
   * @return a handle to the DbHelper
   */
  public static ToDoListDB getDbHelper() {
    return mDbHelper;
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
    switch (mSettings.getInt(PRIORITY_SORT, 0)) {
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
    switch (mSettings.getInt(DUEDATE_SORT, 0)) {
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
    switch (mSettings.getInt(ALPHABET_SORT, 0)) {
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

    Button b = new Button(this);
    b.setText(R.string.confirm);
    b.setOnClickListener(new View.OnClickListener() {
      public void onClick(View arg0) {
        d.dismiss();
        SharedPreferences.Editor editor = mSettings.edit();

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

        editor.commit();
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
      ToDoListDB.setPriorityOrder(true, false);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 2:
      ToDoListDB.setPriorityOrder(true, true);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 3:
      ToDoListDB.setPriorityOrder(false, false);
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
      ToDoListDB.setAlphabeticalOrder(true, false);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 2:
      ToDoListDB.setAlphabeticalOrder(true, true);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 3:
      ToDoListDB.setAlphabeticalOrder(false, false);
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
      ToDoListDB.setDueDateOrder(true, false);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 2:
      ToDoListDB.setDueDateOrder(true, true);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    case 3:
      ToDoListDB.setDueDateOrder(false, false);
      selectTag(mTagSpinner.getSelectedItemPosition());
      return;
    }
  }
}