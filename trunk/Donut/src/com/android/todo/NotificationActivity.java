//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import com.android.todo.data.ToDoDB;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

/**
 * This is an activity used to process status bar notification activations
 */
public final class NotificationActivity extends Activity {
  private LinearLayout mEntryLayout;
  private ScrollView mScrollView;
  private ToDoDB mDbHelper;
  private String mContextEntry;
  private int mActiveEntry; // useful only in keyboard mode

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ToDo.setTheme(this, getSharedPreferences(ToDo.PREFS_NAME,
        Context.MODE_PRIVATE));
    super.onCreate(savedInstanceState);
    setTitle(R.string.due_date_notification);

    // cancelling the notification;
    // For now we leave the notification ID in plain
    // since we only have one notification purpose at this point.
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
        .cancel(1);

    // reusing the main layout
    setContentView(R.layout.main);
    mEntryLayout = (LinearLayout) findViewById(R.id.entryLayout);
    mScrollView = (ScrollView) findViewById(R.id.entryScrollView);
    mScrollView.setSmoothScrollingEnabled(true);
    // deleting the top spinner and other buttons:
    LinearLayout ll = (LinearLayout) findViewById(R.id.upperLayout);
    ll.setVisibility(View.GONE);
    // the add entry button is now the go back button :)
    Button addEntryButton = (Button) findViewById(R.id.addEntryButton);
    addEntryButton.setText(R.string.go_back);
    addEntryButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        finish();
      }
    });
    // the stat button is now a help button
    Button statButton = (Button) findViewById(R.id.statButton);
    statButton.setText("?");
    statButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Utils.showDialog(R.string.notification_view_info_short,
            R.string.notification_view_info_long, NotificationActivity.this);
      }
    });
    ((Button) findViewById(R.id.toggleButton)).setVisibility(View.GONE);

    mDbHelper = ToDoDB.getInstance(getApplicationContext());
  }

  @Override
  protected void onResume() {
    super.onResume();
    populateFields();
  }

  private void populateFields() {
    populateEntries();
  }

  /**
   * Populates the interface
   */
  private void populateEntries() {
    mActiveEntry = -1;

    // fetching the due entries from the database
    Cursor c = mDbHelper.getDueEntries();

    LinearLayout el = mEntryLayout;
    el.removeAllViews();
    CheckBox cb;
    int name = c.getColumnIndexOrThrow(ToDoDB.KEY_NAME);

    CompoundButton.OnCheckedChangeListener ccl = new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mDbHelper.updateTask(buttonView.getText().toString(), isChecked);
        populateEntries();
      }
    };

    if (c.getCount() > 0) {
      c.moveToLast();
      do {
        cb = new CheckBox(this);
        cb.setText(c.getString(name));
        cb.setChecked(false);
        cb.setOnCheckedChangeListener(ccl);
        registerForContextMenu(cb);
        el.addView(cb);
      } while (c.moveToPrevious());
    }
    c.close();
  }

  /**
   * @see same function in the TagToDoList class
   */
  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenu.ContextMenuInfo menuInfo) {
    mContextEntry = ((CheckBox) v).getText().toString();
    // menu.add(0, TagToDoList.ENTRY_CLOSE_ID, 0, R.string.entry_exit);
    menu.add(0, ToDo.TASK_EDIT_ID, 0, R.string.entry_edit);
    menu.add(0, ToDo.TASK_REMOVE_ID, 0, R.string.entry_delete);
    SubMenu submenu = menu.addSubMenu(R.string.entry_group_notes);
    submenu.add(0, ToDo.TASK_AUDIO_ID, 0, R.string.entry_audio_note);
    submenu.add(0, ToDo.TASK_GRAPHICAL_ID, 0,
        R.string.entry_graphical_note);
    submenu
        .add(0, ToDo.TASK_WRITTEN_ID, 0, R.string.entry_written_note);
    submenu = menu.addSubMenu(R.string.entry_group_move);
    submenu.add(0, ToDo.TASK_MOVE_ID, 0, R.string.entry_move);
    menu.setHeaderTitle(R.string.entry_menu);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    return changeTask(item.getItemId());
  }

  /**
   * @see same function in the TagToDoList class
   */
  private boolean changeTask(int selectedItem) {
    switch (selectedItem) {
    case ToDo.TASK_EDIT_ID:
      Intent i1 = new Intent(this, EditScreen.class);
      i1.putExtra(ToDoDB.KEY_NAME, mContextEntry);
      i1.setAction(Integer.toString(ToDo.ACTIVITY_EDIT_ENTRY));
      startActivity(i1);
      break;
    case ToDo.TASK_REMOVE_ID:
      mDbHelper.deleteTask(mContextEntry);
      populateEntries();
      break;
    case ToDo.TASK_GRAPHICAL_ID:
      Intent i2 = new Intent(this, PaintScreen.class);
      i2.putExtra(ToDoDB.KEY_NAME, mContextEntry);
      startActivity(i2);
      break;
    case ToDo.TASK_AUDIO_ID:
      Intent i3 = new Intent(this, AudioScreen.class);
      i3.putExtra(ToDoDB.KEY_NAME, mContextEntry);
      i3.putExtra(ToDoDB.KEY_STATUS, true);
      startActivity(i3);
      break;
    case ToDo.TASK_WRITTEN_ID:
      Intent i4 = new Intent(this, EditScreen.class);
      i4.putExtra(ToDoDB.KEY_NAME, mContextEntry);
      i4.setAction(Integer.toString(ToDo.TASK_WRITTEN_ID));
      startActivity(i4);
      break;
    }
    mContextEntry = null;
    return true;
  }

  /**
   * @see same function in the TagToDoList class
   */
  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_BACK:
      finish();
      break;
    case (KeyEvent.KEYCODE_N):
      selectAnotherEntry(1);
      break;
    case (KeyEvent.KEYCODE_P):
      selectAnotherEntry(-1);
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
    case (KeyEvent.KEYCODE_D):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ToDo.TASK_REMOVE_ID);
        mActiveEntry = Utils.iterate(mActiveEntry,
            mEntryLayout.getChildCount(), -1);
      }
      break;
    case (KeyEvent.KEYCODE_E):
    case (KeyEvent.KEYCODE_R):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ToDo.TASK_EDIT_ID);
      }
      break;
    case (KeyEvent.KEYCODE_G):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ToDo.TASK_GRAPHICAL_ID);
      }
      break;
    case (KeyEvent.KEYCODE_F):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ToDo.TASK_AUDIO_ID);
      }
      break;
    case (KeyEvent.KEYCODE_DEL):
      finish();
      break;
    case (KeyEvent.KEYCODE_W):
      if (mActiveEntry > -1) {
        mContextEntry = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
            .getText().toString();
        changeTask(ToDo.TASK_WRITTEN_ID);
      }
      break;
    }

    return false;
  }

  /**
   * @see same function in the TagToDoList class
   */
  private void selectAnotherEntry(int increment) {
    if (mActiveEntry > -1) {
      ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)))
          .setTextColor(Color.WHITE);
    }
    if (mEntryLayout.getChildCount() == 0) {
      return;
    }
    mActiveEntry = Utils.iterate(mActiveEntry, mEntryLayout.getChildCount(),
        increment);
    CheckBox cb = ((CheckBox) (mEntryLayout.getChildAt(mActiveEntry)));
    cb.setTextColor(Color.GRAY);
    if (cb.getTop() < mScrollView.getScrollY()
        || cb.getTop() > mScrollView.getScrollY() + mScrollView.getHeight()) {
      mScrollView.smoothScrollTo(0, cb.getTop());
    }
  }
}
