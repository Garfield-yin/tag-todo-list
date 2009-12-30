//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import com.android.todo.data.ToDoDB;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This is a multi-purpose class used to request confirmations for tag
 * deletions, to show help screens, etc.
 */
public final class ConfirmationScreen extends Activity {
  private TextView mMessage;
  private Button mFirstButton, mSecondButton;
  private String mTagName, mAction;
  private ToDoDB mDbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.message);
    mMessage = (TextView) findViewById(R.id.messageText);
    mFirstButton = (Button) findViewById(R.id.firstButton);
    mSecondButton = (Button) findViewById(R.id.secondButton);

    mTagName = savedInstanceState != null ? savedInstanceState
        .getString(ToDoDB.KEY_NAME) : null;

    if (mTagName == null) {
      Bundle extras = getIntent().getExtras();
      mTagName = extras != null ? extras.getString(ToDoDB.KEY_NAME) : null;
    }

    mFirstButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        if (mAction.equals(TagToDoList.ACTIVITY_DELETE_TAG + "")) {
          mDbHelper.deleteTag(mTagName);
          setResult(RESULT_OK);
          finish();
        } else if (mAction.equals(TagToDoList.ACTIVITY_INSTRUCTIONS + "")) {
          startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(view
              .getContext().getString(R.string.url_help))));
        } else if (mAction.equals(TagToDoList.ACTIVITY_CLEAR_ENTRIES + "")) {
          mDbHelper.deleteEntries(mTagName);
          setResult(RESULT_OK);
          finish();
        } else { // ACTIVITY_BACKUP_IMPORT
          TagToDoList.importBackupSD();
          finish();
        }
      }
    });

    mSecondButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        setResult(RESULT_OK);
        finish();
      }
    });
  }

  /**
   * Sets the general message of the screen, depending on the action
   */
  private void populateFields() {
    if (mTagName != null) {
      if (mAction.equals(TagToDoList.ACTIVITY_DELETE_TAG + "")) {
        mMessage.setText(R.string.confirm_tag_deletion);
      } else if (mAction.equals(TagToDoList.ACTIVITY_CLEAR_ENTRIES + "")) {
        mMessage.setText(R.string.confirm_entry_clearing);
      }
      mFirstButton.setText(R.string.ok);
      mSecondButton.setText(R.string.no);
    } else {
      if (mAction.equals(TagToDoList.ACTIVITY_INSTRUCTIONS + "")) {
        mMessage.setText(R.string.help_text);
        mFirstButton.setText(R.string.help_site);
        mSecondButton.setText(R.string.go_back);
      } else if (mAction.equals(TagToDoList.ACTIVITY_BACKUP_IMPORT + "")) {
        mMessage.setText(R.string.confirm_backup_import);
        mFirstButton.setText(R.string.ok);
        mSecondButton.setText(R.string.no);
      }
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_NAME, mTagName);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
    mAction = null;
    mDbHelper.close();
    mDbHelper = null;
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAction = getIntent().getAction();
    mDbHelper = new ToDoDB(this);
    mDbHelper.open();
    populateFields();
  }

  /**
   * Saves the state on pause
   */
  private void saveState() {
  }

  /**
   * Intercepts a key press. A positive outcome (a confirmation) = RETURN or Y A
   * negative outcome (a cancellation) = DEL or N
   */
  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_ENTER:
      mFirstButton.performClick();
      break;
    case KeyEvent.KEYCODE_DEL:
      mSecondButton.performClick();
      break;
    case KeyEvent.KEYCODE_BACK:
      finish();
      break;
    case KeyEvent.KEYCODE_K:
      Utils.showDialog(R.string.keyboard_title, R.string.keyboard_help_text,
          ConfirmationScreen.this);
      break;
    }
    return false;
  }
}
