//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.todo.data.ToDoDB;
import com.android.todo.sync.CSV;
import com.android.todo.utils.Utils;

/**
 * This is a multi-purpose class used to request confirmations for tag
 * deletions, to show help screens, etc.
 */
public final class Confirmation extends Activity {
  private TextView mMessage;
  private Button mFirstButton, mSecondButton;
  private String mTagName, mAction;
  private static ToDoDB sDbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    TagToDoList.setTheme(this, TagToDoList.sPref);
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

    mFirstButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (mAction.equals(Integer.toString(TagToDoList.TAG_DELETE_ID))) {
          sDbHelper.deleteTag(mTagName);
          setResult(RESULT_OK);
          finish();
        } else if (mAction.equals(Integer.toString(TagToDoList.TAG_HELP_ID))) {
          startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(v
              .getContext().getString(R.string.url_help))));
        } else if (mAction.equals(Integer.toString(TagToDoList.TAG_CLEAR_ID))) {
          sDbHelper.deleteEntries(mTagName, false);
          setResult(RESULT_OK);
          finish();
        } else if (mAction.equals(Integer
            .toString(TagToDoList.TAG_IMPORT_BACKUP_ID))) {
          TagToDoList.importBackupSD(getApplicationContext());
          finish();
        } else if (mAction.equals(Integer
            .toString(TagToDoList.TAG_IMPORT_CSV_ID))) {
          final File[] files = Utils.listFilesAsArray(
              Environment.getExternalStorageDirectory(), new FilenameFilter() {
                public boolean accept(File dir, String name) {
                  return name.endsWith(".csv");
                }
              }, true);
          if (files.length < 1) {
            Utils.showDialog(R.string.notification,
                R.string.import_CSV_impossible, v.getContext());
          } else if (files.length == 1) {
            mMessage.setText(v.getContext().getString(
                R.string.import_CSV_one_file_found)
                + ":\n"
                + files[0].getName()
                + '\n'
                + v.getContext().getString(R.string.import_CSV_file_confirm));
            mFirstButton.setOnClickListener(new OnClickListener() {
              public void onClick(View arg0) {
                mMessage.setText(CSV.importCSV(files[0], sDbHelper));
                mFirstButton.setVisibility(View.GONE);
                mSecondButton.setText(R.string.go_back);
              }
            });
          } else {
            mMessage.setText(files[0].getName() + '\n'
                + v.getContext().getString(R.string.import_CSV_file_confirm));
            final Button b = new Button(v.getContext());
            b.setTag(new Integer(0));
            b.setText(R.string.import_CSV_keep_looking);
            b.setOnClickListener(new OnClickListener() {
              public void onClick(View arg0) {
                try {
                  arg0.setTag((Integer) arg0.getTag() + 1);
                  mMessage.setText(files[(Integer) arg0.getTag()].getName()
                      + '\n'
                      + arg0.getContext().getString(
                          R.string.import_CSV_file_confirm));
                } catch (Exception e) {
                  b.setTag(new Integer(0));
                  mMessage.setText(files[0].getName()
                      + '\n'
                      + arg0.getContext().getString(
                          R.string.import_CSV_file_confirm));
                }
              }
            });
            mFirstButton.setOnClickListener(new OnClickListener() {
              public void onClick(View arg0) {
                mMessage.setText(CSV.importCSV(files[(Integer) b.getTag()],
                    sDbHelper));
                mFirstButton.setVisibility(View.GONE);
                mSecondButton.setText(R.string.go_back);
                b.setVisibility(View.GONE);
              }
            });
            ((LinearLayout) mMessage.getParent()).addView(b);
          }
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
      if (mAction.equals(Integer.toString(TagToDoList.TAG_DELETE_ID))) {
        mMessage.setText(R.string.confirm_tag_deletion);
      } else if (mAction.equals(Integer.toString(TagToDoList.TAG_CLEAR_ID))) {
        mMessage.setText(R.string.confirm_entry_clearing);
      }
      mFirstButton.setText(android.R.string.yes);
      mSecondButton.setText(android.R.string.no);
    } else {
      if (mAction.equals(Integer.toString(TagToDoList.TAG_HELP_ID))) {
        mMessage.setText(R.string.help_text);
        mFirstButton.setText(R.string.help_site);
        mSecondButton.setText(R.string.go_back);
      } else if (mAction.equals(Integer
          .toString(TagToDoList.TAG_IMPORT_BACKUP_ID))) {
        mMessage.setText(R.string.confirm_backup_import);
        mFirstButton.setText(android.R.string.yes);
        mSecondButton.setText(android.R.string.no);
      } else if (mAction
          .equals(Integer.toString(TagToDoList.TAG_IMPORT_CSV_ID))) {
        mMessage.setText(R.string.import_CSV_confirm);
        mFirstButton.setText(android.R.string.yes);
        mSecondButton.setText(android.R.string.no);
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
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAction = getIntent().getAction();
    sDbHelper = ToDoDB.getInstance(getApplicationContext());
    if (mAction.equals(Integer.toString(TagToDoList.TAG_HELP_ID))) {
      final LinearLayout ll = (LinearLayout) findViewById(R.id.standardButtonLayout);
      if (ll.getChildCount() < 3) {
        if (TagToDoList.sPref.getBoolean(Config.AD_DISABLED, false)) {
          final ImageButton ib = new ImageButton(this);
          ib.setImageResource(R.drawable.paypal);
          ib.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              final Intent i = new Intent(Intent.ACTION_VIEW);
              i.setData(Uri
                  .parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TTVTAWLMS6AWG&lc=GB&item_name=Teo%27s%20free%20projects&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG_global%2egif%3aNonHosted"));
              startActivity(i);
            }
          });
          ll.addView(ib, 0);
        }
      }
    }
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
            Confirmation.this);
        break;
    }
    return false;
  }
}
