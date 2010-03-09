//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.todo.data.ToDoDB;
import com.android.todo.sync.GoogleCalendar;

/**
 * This activity represents a configuration screen
 */
public final class ConfigScreen extends Activity {
  public static final String GOOGLE_CALENDAR = "googleCalendar";
  public static final String GOOGLE_USERNAME = "googleUsername";
  public static final String GOOGLE_PASSWORD = "googlePassword";
  public static final String BACKUP_SDCARD = "backupSD";
  public static final String PRIORITY_MAX = "priorityMax";
  public static final String VISUAL_PRIORITY = "visualPriority";
  public static final String CHECKED_LIMIT = "listSizeLimit";
  public static final String BLIND_MODE = "blindMode";
  public static final String USAGE_STATS = "usageStats";
  public static final String NOTE_PREVIEW = "notePreview";
  public static final String THEME = "theme";
  public static final String SHOW_COLLAPSE = "showCollapse";

  private EditText mUserEdit, mPassEdit;
  private Button mConfirmButton, mCloseButton, mHelpButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    final SharedPreferences settings = getSharedPreferences(
        TagToDoList.PREFS_NAME, Context.MODE_PRIVATE);
    final SharedPreferences.Editor editor = settings.edit();
    setTheme(settings.getInt(ConfigScreen.THEME, android.R.style.Theme));

    super.onCreate(savedInstanceState);
    setTitle(R.string.configuration_screen_title);
    setContentView(R.layout.configuration);

    mCloseButton = (Button) findViewById(R.id.closeConfigurationButton);
    mCloseButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    mHelpButton = (Button) findViewById(R.id.websiteButton);
    mHelpButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        myIntent
            .setData(Uri.parse(v.getContext().getString(R.string.url_help)));
        startActivity(myIntent);
      }
    });

    LinearLayout ll = (LinearLayout) findViewById(R.id.optionsLayout);
    ll.setGravity(Gravity.FILL_HORIZONTAL);

    // usage stats
    CheckBox cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(USAGE_STATS, false));
    cb.setText(R.string.configuration_5_stats);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(USAGE_STATS, isChecked);
        editor.commit();
      }
    });
    ll.addView(cb);

    // show collapse buttons (for subtasks)
    cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(SHOW_COLLAPSE, false));
    cb.setText(R.string.configuration_8_collapse);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(SHOW_COLLAPSE, isChecked).commit();
      }
    });
    ll.addView(cb);

    // choose theme
    cb = new CheckBox(this);
    cb
        .setChecked(settings.getInt(THEME, android.R.style.Theme) != android.R.style.Theme);
    cb.setText(R.string.configuration_7_theme);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putInt(THEME,
            isChecked ? android.R.style.Theme_Light : android.R.style.Theme)
            .commit();
      }
    });
    ll.addView(cb);

    // sync TO Google Calendar
    cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(GOOGLE_CALENDAR, false));
    cb.setText(R.string.configuration_1_gcal);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(GOOGLE_CALENDAR, isChecked);
        if (!(isChecked)) {
          editor.putString(GOOGLE_USERNAME, "");
          editor.putString(GOOGLE_PASSWORD, "");
        }
        editor.commit();
        mUserEdit.setText(settings.getString(GOOGLE_USERNAME,
            getString(R.string.username)));
        mPassEdit.setText(settings.getString(GOOGLE_PASSWORD,
            getString(R.string.password)));
        showLogin(isChecked);
      }
    });
    ll.addView(cb);

    // backup on the SD card every time app closes
    cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(BACKUP_SDCARD, false));
    cb.setText(R.string.configuration_2_backup);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(BACKUP_SDCARD, isChecked);
        editor.commit();
      }
    });
    ll.addView(cb);

    // visually distinguish tasks by priority
    cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(VISUAL_PRIORITY, false));
    cb.setText(R.string.configuration_4_shine);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(VISUAL_PRIORITY, isChecked);
        editor.commit();
      }
    });
    ll.addView(cb);

    // visually distinguish tasks by priority
    cb = new CheckBox(this);
    cb.setChecked(settings.getBoolean(NOTE_PREVIEW, false));
    cb.setText(R.string.configuration_6_notes);
    cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean(NOTE_PREVIEW, isChecked);
        editor.commit();
      }
    });
    ll.addView(cb);

    // setting the checked tasks limit
    TextView tv = new TextView(this);
    tv.setTextSize(16);
    tv.setText(R.string.size_change);
    LinearLayout priorityLayout = new LinearLayout(this); // reusing
    // priorityLayout
    // object
    priorityLayout.setOrientation(LinearLayout.VERTICAL);
    LinearLayout textLayout = new LinearLayout(this); // also reusing textLayout
    textLayout.setOrientation(LinearLayout.HORIZONTAL);
    textLayout.addView(tv);
    final TextView limitTv = new TextView(this);
    limitTv.setMinimumWidth(50);
    priorityLayout.addView(textLayout);
    final SeekBar sLimit = new SeekBar(this);
    sLimit.setMax(5000);
    sLimit.setProgress(settings.getInt(CHECKED_LIMIT, 100));
    limitTv.setTextSize(17);
    limitTv.setPadding(5, 3, 0, 0);
    limitTv.setText(Integer.toString(sLimit.getProgress()));
    textLayout.addView(limitTv);
    sLimit.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        limitTv.setText(Integer.toString(progress));
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
        editor.putInt(CHECKED_LIMIT, seekBar.getProgress());
        editor.commit();
      }
    });
    priorityLayout.addView(sLimit);
    ll.addView(priorityLayout);

    // setting minimum and maximum priority for tasks
    tv = new TextView(this);
    tv.setPadding(0, 15, 0, 0);
    tv.setTextSize(16);
    tv.setText(R.string.configuration_3_priority);
    priorityLayout = new LinearLayout(this);
    priorityLayout.setOrientation(LinearLayout.VERTICAL);
    textLayout = new LinearLayout(this);
    textLayout.setOrientation(LinearLayout.HORIZONTAL);
    textLayout.addView(tv);
    final TextView maxTv = new TextView(this);
    maxTv.setMinimumWidth(50);
    priorityLayout.addView(textLayout);
    final SeekBar sMax = new SeekBar(this);
    sMax.setMax(101);
    sMax.setProgress(settings.getInt(PRIORITY_MAX, 100));
    maxTv.setTextSize(17);
    maxTv.setPadding(5, 3, 0, 0);
    maxTv.setText(Integer.toString(sMax.getProgress()));
    textLayout.addView(maxTv);
    sMax.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        maxTv.setText(Integer.toString(progress));
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
        int p = seekBar.getProgress();
        editor.putInt(PRIORITY_MAX, p > 0 ? p : 1);
        editor.commit();
      }
    });
    priorityLayout.addView(sMax);
    ll.addView(priorityLayout);

    mUserEdit = (EditText) findViewById(R.id.usernameEdit);
    mUserEdit.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_ENTER:
          mPassEdit.requestFocus();
          return true;
        }
        return false;
      }
    });
    mUserEdit.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        if (mUserEdit.getText().toString().equals(getString(R.string.username))) {
          mUserEdit.setText("");
        }
      }
    });
    mPassEdit = (EditText) findViewById(R.id.passwordEdit);
    mPassEdit
        .setTransformationMethod(new android.text.method.PasswordTransformationMethod());
    mPassEdit.setInputType(InputType.TYPE_CLASS_TEXT
        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    mPassEdit.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_ENTER:
          if (((EditText) v).getText().length() > 1) {
            mConfirmButton.requestFocus();
            return true;
          }
        }
        return false;
      }
    });
    mPassEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
      public void onFocusChange(View v, boolean hasFocus) {
        if (mPassEdit.getText().toString().equals(getString(R.string.password))) {
          mPassEdit.setText("");
        }
      }
    });
    mConfirmButton = (Button) findViewById(R.id.confirmLoginButton);
    mConfirmButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        editor.putString(GOOGLE_USERNAME, mUserEdit.getText().toString());
        editor.putString(GOOGLE_PASSWORD, mPassEdit.getText().toString());
        editor.commit();

        GoogleCalendar.setLogin(mUserEdit.getText().toString(), mPassEdit
            .getText().toString());
        boolean canAuthenticate = false;

        try {
          canAuthenticate = GoogleCalendar.authenticate(true);
        } catch (Exception e) {
          canAuthenticate = false;
        }

        if (canAuthenticate) {
          boolean allTasksSynced = true;
          Cursor dueEntries = new ToDoDB(ConfigScreen.this).open()
              .getAllDueEntries();
          if (dueEntries.getCount() > 0) {
            int title = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_NAME);
            int year = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_DUE_YEAR);
            int month = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_DUE_MONTH);
            int day = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_DUE_DATE);
            int hour = dueEntries.getColumnIndexOrThrow(ToDoDB.KEY_DUE_HOUR);
            int minute = dueEntries
                .getColumnIndexOrThrow(ToDoDB.KEY_DUE_MINUTE);
            dueEntries.moveToFirst();
            do {
              try {
                allTasksSynced &= GoogleCalendar.createEvent(dueEntries
                    .getString(title), dueEntries.getInt(year), dueEntries
                    .getInt(month), dueEntries.getInt(day), dueEntries
                    .getInt(hour), dueEntries.getInt(minute));
              } catch (Exception e) {
                allTasksSynced = false;
              }
            } while (dueEntries.moveToNext());
          }
          dueEntries.close();
          if (allTasksSynced) {
            Utils.showDialog(R.string.notification, R.string.login_sync_succes,
                ConfigScreen.this);
            showLogin(false);
          } else {
            Utils.showDialog(R.string.notification,
                R.string.login_succes_sync_fail, ConfigScreen.this);
          }
        } else {
          Utils.showDialog(R.string.notification, R.string.login_fail,
              ConfigScreen.this);
        }
      }
    });
  }

  /**
   * Shows the login UI elements (basically an EditText for the username and one
   * for the password)
   * 
   * @param b
   *          if true, they will be showed, if not, they will be hidden
   */
  public void showLogin(boolean b) {
    int visibility = b ? View.VISIBLE : View.GONE;
    mUserEdit.setVisibility(visibility);
    mPassEdit.setVisibility(visibility);
    mConfirmButton.setVisibility(visibility);
    mCloseButton.setVisibility(8 - visibility);
    mHelpButton.setVisibility(8 - visibility);
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  /**
   * @see same function in the TagToDoList class
   */
  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_BACK:
    case KeyEvent.KEYCODE_DEL:
    case KeyEvent.KEYCODE_DPAD_RIGHT:
      if (mCloseButton.getVisibility() == View.VISIBLE) {
        finish();
      }
      break;
    }
    return false;
  }
}
