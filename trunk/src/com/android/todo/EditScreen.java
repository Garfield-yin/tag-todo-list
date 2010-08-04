//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.ToggleButton;

import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;
import com.android.todo.speech.OneTimeTTS;
import com.android.todo.sync.GoogleCalendar;
import com.android.todo.widget.TagToDoWidget;

/**
 * This is another activity (basically an editing screen). It will allow the
 * user to do the following: - input the name of a new tag - edit the name of an
 * existing tag - input the text in a new to-do list entry - edit the text in an
 * existing to-do list entry
 */
public final class EditScreen extends Activity {
  public final static String EXTERNAL_INVOKER="widgetInitiated";

  private TextView mTaskText;
  private EditText mBodyText;
  private Button mConfirmButton;
  private Button mCancelButton;
  private SeekBar mPrioritySb;
  private ToggleButton mDateTb;
  private ToggleButton mTimeTb;
  private ToggleButton mWhenButton;
  private LinearLayout mDateTimeLayout;

  private static String sParameter;
  private static String sSuperTask;
  private static String mPriorityText;
  private ToDoDB mDbHelper;
  private static String mMonthsString;
  private static String aux = "";
  private static int keyCount = 0;
  private static int sYear;
  private static int sMonth;
  private static int sDate;
  private static int sHour = -1;
  private static int sMinute = -1;
  private static int sDayOfWeek = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mDbHelper = ToDoDB.getInstance(getApplicationContext());
    setContentView(R.layout.edit);
    final boolean noPhysicalKeyboard = (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS);
    mTaskText = (TextView) findViewById(R.id.task);
    mBodyText = (EditText) findViewById(R.id.body);
    mBodyText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_ENTER:
            if (event.isShiftPressed()) {
              return false;
            }
            if (noPhysicalKeyboard) {
              InputMethodManager inputManager = (InputMethodManager) v
                  .getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
              inputManager.hideSoftInputFromWindow(v.getWindowToken(),
                  InputMethodManager.HIDE_IMPLICIT_ONLY);
              return true;
            }
            String currentText = ((EditText) v).getText().toString();
            if (!(currentText.equals(aux))) {
              mConfirmButton.performClick();
            }
            aux = currentText;
            return true;
          case KeyEvent.KEYCODE_DEL:
            String currentText2 = ((EditText) v).getText().toString();
            if (keyCount == 0 && currentText2.length() == 0) {
              mCancelButton.performClick();
              return true;
            }
        }
        keyCount += 1;
        return false;
      }
    });

    // setting a maximum height for the edit box;
    // we can't have it overlapping other stuff (it will usually be in
    // landscape mode)
    final DisplayMetrics dm = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(dm);
    mBodyText.setMaxHeight(dm.heightPixels - 270);

    mConfirmButton = (Button) findViewById(R.id.confirmButton);
    mCancelButton = (Button) findViewById(R.id.cancelButton);

    if (savedInstanceState != null) {
      sParameter = savedInstanceState.getString(ToDoDB.KEY_NAME);
      sSuperTask = savedInstanceState.getString(ToDoDB.KEY_SUPERTASK);
    } else {
      sParameter = null;
      sSuperTask = null;
    }

    if (sParameter == null) {
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        sParameter = extras.getString(ToDoDB.KEY_NAME);
        sSuperTask = extras.getString(ToDoDB.KEY_SUPERTASK);
      } else {
        sParameter = null;
        sSuperTask = null;
      }
    }

    final String action = getIntent().getAction();
    final boolean creating = action.equals(TagToDoList.ACTIVITY_CREATE_ENTRY
        + "");

    if (action.equals(TagToDoList.ACTIVITY_EDIT_ENTRY + "") || creating) {
      LinearLayout ll = (LinearLayout) findViewById(R.id.editLinearLayout);

      // now comes priority stuff
      mPriorityText = creating ? this.getString(R.string.priority_default)
          : this.getString(R.string.priority);
      final TextView priorityTv = new TextView(this);
      priorityTv.setGravity(Gravity.CENTER);
      mPrioritySb = new SeekBar(this);
      mPrioritySb.setMax(getSharedPreferences(TagToDoList.PREFS_NAME,
          Context.MODE_PRIVATE).getInt(ConfigScreen.PRIORITY_MAX, 100) + 1);
      int priority = creating ? mPrioritySb.getMax() / 2 : mDbHelper
          .getPriority(EditScreen.sParameter);
      priorityTv.setText(mPriorityText + priority);
      mPrioritySb.setPadding(0, 0, 0, 10);
      mPrioritySb.setProgress(priority);
      mPrioritySb
          .setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
              priorityTv.setText(mPriorityText + arg1);
            }

            public void onStartTrackingTouch(SeekBar arg0) {
              mPriorityText = EditScreen.this.getString(R.string.priority);
              priorityTv.setTextSize(20);
            }

            public void onStopTrackingTouch(SeekBar arg0) {
              priorityTv.setTextSize(14);
            }

          });
      ll.addView(priorityTv);
      ll.addView(mPrioritySb);

      mDateTimeLayout = new LinearLayout(this);
      mDateTimeLayout.setOrientation(LinearLayout.HORIZONTAL);

      // now comes due dates stuff
      mDateTb = new ToggleButton(this);
      mDateTb.setTextOff(this.getString(R.string.edit_task_no_date));
      if (mDbHelper.isDueDateSet(EditScreen.sParameter)) {
        mDateTb.setTextOn(this.getString(R.string.edit_task_date_set)
            + " "
            + Chronos.decodeDate(mDbHelper.getDueDate(EditScreen.sParameter),
                getString(R.string.months)));
        mDateTb.setChecked(true);
      } else {
        mDateTb.setChecked(false);
      }
      mDateTb.setTag(Boolean.valueOf(false));
      mDateTb.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          if (((ToggleButton) v).isChecked()) {
            // showing the DatePickerDialog
            OnDateSetListener odsl = new OnDateSetListener() {
              public void onDateSet(DatePicker view, int year, int monthOfYear,
                  int dayOfMonth) {
                mDateTb.setTag(Boolean.valueOf(true));
                if (!(creating)) {
                  mDbHelper.setDueDate(EditScreen.sParameter, true);
                  mDbHelper.updateTask(EditScreen.sParameter, year,
                      monthOfYear, dayOfMonth);
                  if (mTimeTb.isChecked()) {
                    mDbHelper.deleteAlarm(EditScreen.sParameter);
                    setAlarm(EditScreen.sParameter, sHour, sMinute);
                  }
                } else {
                  sYear = year;
                  sMonth = monthOfYear;
                  sDate = dayOfMonth;
                }
                if (mMonthsString == null) {
                  mMonthsString = view.getContext().getString(R.string.months);
                }
                mDateTb.setText(view.getContext().getString(
                    R.string.edit_task_date_set)
                    + " "
                    + mMonthsString.split(" ")[monthOfYear]
                    + " "
                    + dayOfMonth + ", " + year);
                mDateTb.setTextOn(mDateTb.getText());
              }
            };
            GregorianCalendar gc;
            int encodedDate;
            if (!(creating)
                && (encodedDate = mDbHelper.getDueDate(EditScreen.sParameter)) > 0) {
              gc = new GregorianCalendar(encodedDate / 372,
                  encodedDate / 31 % 12, encodedDate % 31);
            } else {
              gc = new GregorianCalendar();
            }
            DatePickerDialog dpd = new DatePickerDialog(EditScreen.this, odsl,
                gc.get(GregorianCalendar.YEAR),
                gc.get(GregorianCalendar.MONTH), gc.get(GregorianCalendar.DATE));
            dpd.setCancelable(true);
            dpd.show();

            dpd.setOnDismissListener(new OnDismissListener() {
              public void onDismiss(DialogInterface dialog) {
                if ((Boolean) (mDateTb.getTag()) == false) {
                  mDateTb.setChecked(false);
                }
                mDateTb.setTag(Boolean.valueOf(false));
              }
            });
            setWhenButton(false);
          } else {
            if (!(creating)) {
              mDbHelper.setDueDate(EditScreen.sParameter, false);
              if (mTimeTb.isChecked()) {
                mDbHelper.deleteAlarm(EditScreen.sParameter);
                setAlarm(EditScreen.sParameter, sHour, sMinute);
              }
            }
            if (mTimeTb.isChecked()) {
              setWhenButton(true);
            }
          }
        }
      });
      mDateTimeLayout.addView(mDateTb);
      mTimeTb = new ToggleButton(this);
      mTimeTb.setTextOff(this.getString(R.string.edit_task_no_time));
      mDateTimeLayout.addView(mTimeTb);
      if (mDbHelper.isDueTimeSet(EditScreen.sParameter)) {
        mTimeTb.setTextOn(this.getString(R.string.edit_task_time_set) + " "
            + Chronos.decodeTime(mDbHelper.getDueTime(EditScreen.sParameter)));
        mTimeTb.setChecked(true);
        if (!(mDateTb.isChecked())) {
          sDayOfWeek = mDbHelper.getDueDayOfWeek(EditScreen.sParameter);
          setWhenButton(true);
          mWhenButton.setChecked(sDayOfWeek > -1);
        }
      } else {
        mTimeTb.setChecked(false);
      }
      mTimeTb.setTag(Boolean.valueOf(false));
      mTimeTb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton cb, boolean checked) {
          if (checked) {
            final Calendar c = Calendar.getInstance();
            Dialog d = new TimePickerDialog(cb.getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                  public void onTimeSet(TimePicker view, int hour, int minute) {
                    mTimeTb.setTag(Boolean.valueOf(true));
                    mTimeTb.setText(view.getContext().getString(
                        R.string.edit_task_time_set)
                        + " "
                        + (hour < 10 ? "0" : "")
                        + hour
                        + ":"
                        + (minute < 10 ? "0" : "") + minute);
                    if (!(creating)) {
                      setAlarm(EditScreen.sParameter, hour, minute);
                    }
                    mTimeTb.setTextOn(mTimeTb.getText());
                    sHour = hour;
                    sMinute = minute;
                  }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);
            d.show();
            d.setOnDismissListener(new OnDismissListener() {
              public void onDismiss(DialogInterface dialog) {
                if ((Boolean) (mTimeTb.getTag()) == false) {
                  mTimeTb.setChecked(false);
                }
                mTimeTb.setTag(Boolean.valueOf(false));
              }
            });
            if (!(mDateTb.isChecked())) {
              setWhenButton(true);
            }
          } else {
            if (!(creating)) {
              mDbHelper.setDueTime(EditScreen.sParameter, false);
              mDbHelper.deleteAlarm(EditScreen.sParameter);
            }
            sHour = -1;
            sMinute = -1;
            setWhenButton(false);
          }
        }
      });

      ll.addView(mDateTimeLayout);
    }

    mConfirmButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        String name = mBodyText.getText().toString().replaceAll("'", "`");
        if (action.equals(TagToDoList.ACTIVITY_CREATE_TAG + "")) {
          if (!(mDbHelper.createTag(name))) {
            showMessage(view.getContext().getString(R.string.tag_existent));
            return;
          }
        } else if (action.equals(TagToDoList.ACTIVITY_EDIT_TAG + "")) {
          mDbHelper.updateTag(EditScreen.sParameter, name);
        } else if (action.equals(TagToDoList.ACTIVITY_CREATE_ENTRY + "")) {
          String result = mDbHelper.createTask(EditScreen.sParameter, name);
          if (sSuperTask != null && sSuperTask.length() > 0) {
            try {
              mDbHelper.setSuperTask(name, sSuperTask);
            } catch (Exception e) {
              // can't have an exception here
            }
          }
          if (result != null) {
            showMessage(view.getContext().getString(R.string.entry_existent)
                + " '" + result + "'");
            return;
          }
          // we remember a constant for the date state because we also
          // need it to see which kind of alarm we are going to set:
          // daily or single occurence
          final boolean dateSet = mDateTb.isChecked();
          if (dateSet) {
            mDbHelper.updateTask(name, sYear, sMonth, sDate);
            mDbHelper.setDueDate(name, true);
          }
          if (mTimeTb.isChecked()) {
            setAlarm(name, sHour, sMinute);
            if (!(dateSet)) {
              mDbHelper.updateTask(name, sDayOfWeek);
            }
          }
          mDbHelper.setPriority(name, mPrioritySb.getProgress());
          if (dateSet) {
            syncToWeb(name);
          }
          if (getIntent().getExtras().getBoolean(EXTERNAL_INVOKER,
              false)) {
            TagToDoWidget.onUpdate(getApplicationContext(),
                AppWidgetManager.getInstance(getApplicationContext()));
          }
        } else if (action.equals(TagToDoList.ACTIVITY_EDIT_ENTRY + "")) {
          mDbHelper.updateTask(EditScreen.sParameter, name);
          mDbHelper.setPriority(EditScreen.sParameter,
              mPrioritySb.getProgress());
          if (mDateTb.isChecked()) {
            syncToWeb(name);
          }
        } else if (action.equals(TagToDoList.ACTIVITY_WRITE_NOTE + "")) {
          mDbHelper.setWrittenNote(EditScreen.sParameter, name);
        }
        finish();
      }
    });

    mCancelButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        finish();
      }
    });
  }

  /**
   * Makes the When button appear or disappear. That button reveals some
   * settings about periodic alarms.
   * 
   * @visible
   */
  private void setWhenButton(boolean visible) {
    if (visible) {
      mDateTimeLayout.addView(mWhenButton = new ToggleButton(this));
      mWhenButton.setTextOff(getString(R.string.daily));
      mWhenButton.setChecked(false);
      if (sDayOfWeek > -1) {
        mWhenButton.setTextOn(getString(Chronos.DAYS[sDayOfWeek]));
      }
      mWhenButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          if (!((ToggleButton) v).isChecked()) {
            sDayOfWeek = -1;
            return;
          }
          final Dialog d = new Dialog(EditScreen.this);
          d.setTitle(R.string.weekly);
          LinearLayout ll = new LinearLayout(EditScreen.this);
          ll.setOrientation(LinearLayout.VERTICAL);
          Button b = new Button(EditScreen.this);
          b.setMinimumWidth(150);
          b.setText(R.string.go_back);
          b.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
              mWhenButton.setTextOn(getString(Chronos.DAYS[sDayOfWeek]));
              mWhenButton.setChecked(true);
              if (getIntent().getAction().equals(
                  TagToDoList.ACTIVITY_EDIT_ENTRY + "")) {
                mDbHelper.deleteAlarm(sParameter);
                mDbHelper.updateTask(sParameter, sDayOfWeek);
                setAlarm(sParameter, sHour, sMinute);
              }
              d.dismiss();
            }
          });
          ll.addView(b);
          final TextView tv = new TextView(EditScreen.this);
          if (sDayOfWeek == -1) {
            sDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2;
            if (sDayOfWeek < 0) {
              sDayOfWeek += 7;
            }
          }
          tv.setText(Chronos.DAYS[sDayOfWeek]);
          tv.setGravity(Gravity.CENTER_HORIZONTAL);
          tv.setTextSize(18);
          b = new Button(EditScreen.this);
          b.setText("<");
          b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              sDayOfWeek = Utils.iterate(sDayOfWeek, 7, -1);
              tv.setText(Chronos.DAYS[sDayOfWeek]);
            }
          });
          ll.addView(b);
          ll.addView(tv);
          b = new Button(EditScreen.this);
          b.setText(">");
          b.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
              sDayOfWeek = Utils.iterate(sDayOfWeek, 7, 1);
              tv.setText(Chronos.DAYS[sDayOfWeek]);
            }
          });
          ll.addView(b);
          d.setContentView(ll);
          d.show();
        }
      });
    } else {
      mDateTimeLayout.removeView(mWhenButton);
    }
  }

  /**
   * Sets the alarm up in Android's AlarmManager system
   * 
   * @param task
   *          of task
   * @param hour
   * @param minute
   */
  private final void setAlarm(final String task, final int hour,
      final int minute) {
    mDbHelper.updateTask(task, hour, minute);
    mDbHelper.setDueTime(task, true);
    final PendingIntent pi = PendingIntent.getBroadcast(this, task.hashCode(),
        Utils.getAlarmIntent(new Intent(this, AlarmReceiver.class), task), 0);
    final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    if (mDateTb.isChecked()) {// single occurence
      Chronos.setSingularAlarm(am, pi, new Time(mDbHelper.getDueTime(task),
          sDayOfWeek), new Date(mDbHelper.getDueDate(task)));
    } else {// daily or weekly
      Chronos.setRepeatingAlarm(am, pi, new Time(hour, minute, sDayOfWeek),
          null);
    }
  }

  /**
   * Sets the general message of this edit screen, depending on the purpose and
   * also sets a default value if something is edited (the present value)
   */
  private void populateFields() {
    String action = getIntent().getAction();
    if (sParameter != null) {
      if (action.equals(TagToDoList.ACTIVITY_EDIT_TAG + "")) {
        mTaskText.setText(R.string.edit_tag);
        mBodyText.setText(sParameter);
        mBodyText.setSelection(sParameter.length(), sParameter.length());
      } else if (action.equals(TagToDoList.ACTIVITY_CREATE_ENTRY + "")) {
        mTaskText.setText(R.string.entry_create);
      } else if (action.equals(TagToDoList.ACTIVITY_EDIT_ENTRY + "")) {
        mTaskText.setText(R.string.edit_entry);
        mBodyText.setText(sParameter);
        mBodyText.setSelection(sParameter.length(), sParameter.length());
      } else if (action.equals(TagToDoList.ACTIVITY_WRITE_NOTE + "")) {
        mTaskText.setText(R.string.edit_written_note);
        mBodyText.setText(mDbHelper.getWrittenNote(sParameter));
      }
    } else {
      if (action.equals(TagToDoList.ACTIVITY_CREATE_TAG + "")) {
        mTaskText.setText(R.string.create_tag);
      }
    }
    if (TagToDoList.BLIND_MODE) {
      new OneTimeTTS(this, mTaskText.getText().toString());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  // What's this about? Where do i read it?
  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_ROWID, sParameter);
  }

  @Override
  protected void onPause() {
    super.onPause();
    saveState();
  }

  @Override
  protected void onResume() {
    super.onResume();
    populateFields();
  }

  /**
   * Saves the state on pause
   */
  private void saveState() {
  }

  /**
   * Shows a dialog box with the given message
   * 
   * @param message
   */
  private void showMessage(String message) {
    final Dialog d = new Dialog(EditScreen.this);
    final Button b = new Button(EditScreen.this);
    b.setText(R.string.go_back);
    b.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        d.dismiss();
      }
    });
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
    d.setContentView(b);
    d.setTitle(message);
    d.show();
  }

  /**
   * Sync changes to the web, if necessary
   * 
   * @param name
   *          The name of the task
   */
  private void syncToWeb(String name) {
    if (TagToDoList.SYNC_GCAL) {
      SharedPreferences settings = getSharedPreferences(TagToDoList.PREFS_NAME,
          Context.MODE_PRIVATE);
      GoogleCalendar.setLogin(
          settings.getString(ConfigScreen.GOOGLE_USERNAME, ""),
          settings.getString(ConfigScreen.GOOGLE_PASSWORD, ""));
      try {
        GoogleCalendar.createEvent(name, sYear, sMonth, sDate, sHour, sMinute);
      } catch (Exception e) {
        // TODO Show error message somehow
      }
    }
  }
}
