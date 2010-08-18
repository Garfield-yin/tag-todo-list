//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

import com.android.todo.data.Analytics;
import com.android.todo.data.ToDoDB;
import com.android.todo.speech.TTS;
import com.android.todo.sync.GoogleCalendar;

/**
 * This activity represents a configuration screen
 */
public final class Config extends Activity {
  public static final String SELECTED_TAB = "selectedTab";
  public static final String CUSTOM_ALARM = "customAlarm";
  public static final String ALARM_URI = "alarmUri";
  public static final String ALARM_VIBRATION = "alarmVibration";
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
  public static final String SHOW_DUE_TIME = "showDueTime";
  public static final String FULLSCREEN = "fullscreen";
  public static final String AD_DISABLED = "adDisabled";
  public static final String TEXT_SIZE = "textSize";
  public static final String PRIORITY_DISABLE = "priorityDisabled";

  private static EditText sUserEdit, sPassEdit;
  private static Button sSongPicker, sConfirmButton, sHelpButton, sCloseButton;
  private static ImageButton sDonateButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    ToDo.setTheme(this, ToDo.sPref);

    super.onCreate(savedInstanceState);
    setContentView(R.layout.config);

    final TabHost th = (TabHost) this.findViewById(R.id.configTabHost);
    th.setup();
    th.addTab(th.newTabSpec("")
        .setIndicator(getString(R.string.config_tab_todo))
        .setContent(R.id.configScrollView));
    th.addTab(th.newTabSpec("1")
        .setIndicator(getString(R.string.config_tab_ui))
        .setContent(R.id.configScrollView));
    th.addTab(th.newTabSpec("22")
        .setIndicator(getString(R.string.config_tab_sounds))
        .setContent(R.id.configScrollView));
    th.addTab(th.newTabSpec("333")
        .setIndicator(getString(R.string.config_tab_web))
        .setContent(R.id.configScrollView));
    ImageView iv = (ImageView) th.getTabWidget().getChildAt(0)
        .findViewById(android.R.id.icon);
    iv.setPadding(0, 0, 0, 10);
    iv.setImageDrawable(getResources().getDrawable(
        android.R.drawable.ic_menu_agenda));
    iv = (ImageView) th.getTabWidget().getChildAt(1)
        .findViewById(android.R.id.icon);
    iv.setPadding(0, 0, 0, 10);
    iv.setImageDrawable(getResources().getDrawable(
        android.R.drawable.ic_menu_view));
    iv = (ImageView) th.getTabWidget().getChildAt(2)
        .findViewById(android.R.id.icon);
    iv.setPadding(0, 0, 0, 10);
    iv.setMinimumHeight(48);
    iv.setMinimumWidth(48);
    iv.setImageDrawable(getResources().getDrawable(
        android.R.drawable.ic_lock_silent_mode_off));
    iv = (ImageView) th.getTabWidget().getChildAt(3)
        .findViewById(android.R.id.icon);
    iv.setPadding(0, 0, 0, 10);
    iv.setImageDrawable(getResources().getDrawable(
        android.R.drawable.ic_menu_send));

    final int selectedTab = ToDo.sPref.getInt(SELECTED_TAB, 1);
    th.setCurrentTab(Utils.iterate(selectedTab, 3, 1));

    th.setOnTabChangedListener(new OnTabChangeListener() {
      public void onTabChanged(String tab) {
        ToDo.sEditor.putInt(SELECTED_TAB, tab.length());
        ToDo.sEditor.commit();
        showTab(tab.length());
      }
    });

    th.setCurrentTab(selectedTab);

    sDonateButton = (ImageButton) findViewById(R.id.donateButton);
    sCloseButton = (Button) findViewById(R.id.closeButton);
    sConfirmButton = (Button) findViewById(R.id.confirmLoginButton);
    sHelpButton = (Button) findViewById(R.id.websiteButton);

    sCloseButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });

    sConfirmButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        ToDo.sEditor.putString(GOOGLE_USERNAME, sUserEdit.getText().toString());
        ToDo.sEditor.putString(GOOGLE_PASSWORD, sPassEdit.getText().toString());
        ToDo.sEditor.commit();

        GoogleCalendar.setLogin(sUserEdit.getText().toString(), sPassEdit
            .getText().toString());
        boolean canAuthenticate = false;

        try {
          canAuthenticate = GoogleCalendar.authenticate(true);
        } catch (Exception e) {
          canAuthenticate = false;
        }

        if (canAuthenticate) {
          boolean allTasksSynced = true;
          Cursor dueEntries = ToDoDB.getInstance(getApplicationContext())
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
                allTasksSynced &= GoogleCalendar.createEvent(
                    dueEntries.getString(title), dueEntries.getInt(year),
                    dueEntries.getInt(month), dueEntries.getInt(day),
                    dueEntries.getInt(hour), dueEntries.getInt(minute));
              } catch (Exception e) {
                allTasksSynced = false;
              }
            } while (dueEntries.moveToNext());
          }
          dueEntries.close();
          if (allTasksSynced) {
            Utils.showDialog(R.string.notification, R.string.login_sync_succes,
                Config.this);
            showLogin(false);
          } else {
            Utils.showDialog(R.string.notification,
                R.string.login_succes_sync_fail, Config.this);
          }
        } else {
          Utils.showDialog(R.string.notification, R.string.login_fail,
              Config.this);
        }
      }
    });

    if (!ToDo.sPref.getBoolean(AD_DISABLED, false)) {
      sDonateButton.setVisibility(View.GONE);
      sHelpButton.setVisibility(View.GONE);
      return;
    }

    sHelpButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        Intent myIntent = new Intent(Intent.ACTION_VIEW);
        myIntent.setData(Uri.parse(v.getContext().getString(R.string.url_help)));
        startActivity(myIntent);
      }
    });

    sDonateButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        final Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri
            .parse("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=TTVTAWLMS6AWG&lc=GB&item_name=Teo%27s%20free%20projects&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG_global%2egif%3aNonHosted"));
        startActivity(i);
        if (Analytics.sTracker != null) {
          Analytics.sTracker.trackEvent(Analytics.ACTION_PRESS,
              "CONFIG_SCREEN_DONATION_BUTTON", Analytics.SPACE_INTERFACE, 0);
        }
      }
    });
  }

  /**
   * Populates the layout with the needed tab content.
   * 
   * @param index
   *          of the tab.
   */
  private final void showTab(final int index) {
    CheckBox cb;
    final LinearLayout ll = (LinearLayout) findViewById(R.id.configLayout);
    ll.removeAllViews();
    switch (index) {
      case 0:
        // setting the checked tasks limit
        Utils.addSeekBar(ll, ToDo.sPref, CHECKED_LIMIT, 100, 1000,
            R.string.size_change, R.string.checked_tasks_limit_description);

        // setting minimum and maximum priority for tasks
        Utils.addSeekBar(ll, ToDo.sPref, PRIORITY_MAX, 100, 101,
            R.string.config_3_priority, R.string.max_priority_description);

        // backup on the SD card every time app closes
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(PRIORITY_DISABLE, false));
        cb.setText(R.string.config_15_priority_disable);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(PRIORITY_DISABLE, isChecked).commit();
          }
        });
        ll.addView(cb);

        // backup on the SD card every time app closes
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(BACKUP_SDCARD, false));
        cb.setText(R.string.config_2_backup);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(BACKUP_SDCARD, isChecked);
            ToDo.sEditor.commit();
          }
        });
        ll.addView(cb);

        break;
      case 1:
        // setting the checked tasks limit
        Utils.addSeekBar(ll, ToDo.sPref, TEXT_SIZE, 16, 30,
            R.string.config_14_text_size, R.string.text_size_description);

        // show collapse buttons (for subtasks)
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(SHOW_COLLAPSE, false));
        cb.setText(R.string.config_8_collapse);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(SHOW_COLLAPSE, isChecked).commit();
          }
        });
        ll.addView(cb);

        // show which tasks have notes in portrait mode as well
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(NOTE_PREVIEW, false));
        cb.setText(R.string.config_6_notes);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(NOTE_PREVIEW, isChecked);
            ToDo.sEditor.commit();
          }
        });
        ll.addView(cb);

        // show which tasks have due dates
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(SHOW_DUE_TIME, false));
        cb.setText(R.string.config_9_duedate);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(SHOW_DUE_TIME, isChecked).commit();
          }
        });
        ll.addView(cb);

        // visually distinguish tasks by priority
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(VISUAL_PRIORITY, false));
        cb.setText(R.string.config_4_shine);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(VISUAL_PRIORITY, isChecked);
            ToDo.sEditor.commit();
          }
        });
        ll.addView(cb);

        // ad
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(AD_DISABLED, false));
        cb.setText(R.string.config_13_ad_disable);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(AD_DISABLED, isChecked).commit();
          }
        });
        ll.addView(cb);

        // choose theme
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getInt(THEME, android.R.style.Theme) != android.R.style.Theme);
        cb.setText(R.string.config_7_theme);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor
                .putInt(
                    THEME,
                    isChecked ? android.R.style.Theme_Light
                        : android.R.style.Theme).commit();
          }
        });
        ll.addView(cb);

        // fullscreen
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(FULLSCREEN, false));
        cb.setText(R.string.config_12_fullscreen);
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(FULLSCREEN, isChecked).commit();
          }
        });
        ll.addView(cb);

        break;
      case 2:
        // show Visually Challenged mode
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(BLIND_MODE, false));
        cb.setText(R.string.visually_challenged_mode_enabled);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            if (!isChecked) {
              if (ToDo.sTts != null) {
                ToDo.sTts.shutdown();
                ToDo.sTts = null;
              }
            } else {
              ToDo.sTts = new TTS(getApplicationContext(), null);
            }
            ToDo.sEditor.putBoolean(BLIND_MODE, isChecked).commit();
          }
        });
        ll.addView(cb);

        // change default alarm sound?
        cb = new CheckBox(this);
        sSongPicker = new Button(this);
        final String UriString = ToDo.sPref.getString(ALARM_URI, null);
        if (UriString != null) {
          sSongPicker.setText(getAudioTitle(Uri.parse(UriString)));
        } else {
          sSongPicker.setText(R.string.config_10_alarm_pick);
        }
        sSongPicker.setOnClickListener(new OnClickListener() {
          public void onClick(View v) {
            // Intent intent = new Intent(Intent.ACTION_PICK);
            // intent.setType("vnd.android.cursor.dir/track");
            // ConfigScreen.this.startActivityForResult(intent,1);

            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("audio/*");
            Intent c = Intent.createChooser(i,
                v.getContext().getString(R.string.config_10_alarm_pick));
            startActivityForResult(c, 1);
          }
        });
        boolean b = ToDo.sPref.getBoolean(CUSTOM_ALARM, false);
        cb.setChecked(b);
        sSongPicker.setVisibility(b ? View.VISIBLE : View.GONE);
        cb.setText(R.string.config_10_alarm);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(CUSTOM_ALARM, isChecked).commit();
            sSongPicker.setVisibility(isChecked ? View.VISIBLE : View.GONE);
          }
        });
        ll.addView(cb);
        ll.addView(sSongPicker);

        // vibrate during the alarm?
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(ALARM_VIBRATION, true));
        cb.setText(R.string.config_11_alarm_vibrate);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(ALARM_VIBRATION, isChecked).commit();
          }
        });
        ll.addView(cb);
        break;
      case 3:
        // usage stats
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(USAGE_STATS, false));
        cb.setText(R.string.config_5_stats);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(USAGE_STATS, isChecked);
            ToDo.sEditor.commit();
          }
        });
        ll.addView(cb);

        // sync TO Google Calendar
        cb = new CheckBox(this);
        cb.setChecked(ToDo.sPref.getBoolean(GOOGLE_CALENDAR, false));
        cb.setText(R.string.config_1_gcal);
        cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton v, boolean isChecked) {
            ToDo.sEditor.putBoolean(GOOGLE_CALENDAR, isChecked);
            if (!(isChecked)) {
              ToDo.sEditor.putString(GOOGLE_USERNAME, "");
              ToDo.sEditor.putString(GOOGLE_PASSWORD, "");
            }
            ToDo.sEditor.commit();
            sUserEdit.setText(ToDo.sPref.getString(GOOGLE_USERNAME,
                getString(R.string.username)));
            sPassEdit.setText(ToDo.sPref.getString(GOOGLE_PASSWORD,
                getString(R.string.password)));
            showLogin(isChecked);
          }
        });
        ll.addView(cb);

        sUserEdit = (EditText) findViewById(R.id.usernameEdit);
        sUserEdit.setOnKeyListener(new View.OnKeyListener() {
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
              case KeyEvent.KEYCODE_ENTER:
                sPassEdit.requestFocus();
                return true;
            }
            return false;
          }
        });
        sUserEdit.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
            if (sUserEdit.getText().toString()
                .equals(getString(R.string.username))) {
              sUserEdit.setText("");
            }
          }
        });
        sPassEdit = (EditText) findViewById(R.id.passwordEdit);
        sPassEdit
            .setTransformationMethod(new android.text.method.PasswordTransformationMethod());
        sPassEdit.setInputType(InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        sPassEdit.setOnKeyListener(new View.OnKeyListener() {
          public boolean onKey(View v, int keyCode, KeyEvent event) {
            switch (keyCode) {
              case KeyEvent.KEYCODE_ENTER:
                if (((EditText) v).getText().length() > 1) {
                  sConfirmButton.requestFocus();
                  return true;
                }
            }
            return false;
          }
        });
        sPassEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
          public void onFocusChange(View v, boolean hasFocus) {
            if (sPassEdit.getText().toString()
                .equals(getString(R.string.password))) {
              sPassEdit.setText("");
            }
          }
        });
        break;
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1) {
      if (resultCode == RESULT_OK) {
        final Uri uri = data.getData();
        sSongPicker.setText(getAudioTitle(uri));
        ToDo.sEditor.putString(ALARM_URI, uri.toString()).commit();
      }
    }
  }

  private final String getAudioTitle(final Uri uri) {
    final ContentResolver cr = getContentResolver();
    final Cursor c = cr.query(uri,
        new String[] { MediaStore.Audio.AudioColumns.TITLE }, null, null, null);
    c.moveToFirst();
    return c.getString(c.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
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
    sUserEdit.setVisibility(visibility);
    sPassEdit.setVisibility(visibility);
    sConfirmButton.setVisibility(visibility);
    sDonateButton.setVisibility(8 - visibility);
    sHelpButton.setVisibility(8 - visibility);
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
        if (sDonateButton.getVisibility() == View.VISIBLE) {
          finish();
        }
        break;
    }
    return false;
  }
}
