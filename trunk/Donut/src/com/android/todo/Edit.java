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
import android.app.TimePickerDialog.OnTimeSetListener;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.admob.android.ads.AdView;
import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;
import com.android.todo.receivers.AlarmReceiver;
import com.android.todo.speech.OneTimeTTS;
import com.android.todo.sync.GoogleCalendar;
import com.android.todo.widget.TagToDoWidget;

/**
 * This is another activity (basically an editing screen). It will allow the
 * user to do the following: - input the name of a new tag - edit the name of an
 * existing tag - input the text in a new to-do list entry - edit the text in an
 * existing to-do list entry
 */
public final class Edit extends Activity {
  public final static String EXTERNAL_INVOKER = "widgetInitiated";

  private TextView mTaskText;
  private EditText mBodyText;
  private Button mConfirmButton;
  private Button mCancelButton;
  private SeekBar mPrioritySb;
  private ToggleButton mDateTb;
  private ToggleButton mTimeTb;
  private ToggleButton mWhenButton;
  private LinearLayout mDateTimeLayout;

  private static String sTask;
  private static String sSuperTask;
  private static String mPriorityText;
  private ToDoDB mDbHelper;
  private static String mMonthsString;
  private static String aux = "";
  private static int keyCount = 0;
  private static Date sDate;
  private static Time sTime;
  private static SharedPreferences sPref;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    sPref = getSharedPreferences(ToDo.PREFS_NAME, Context.MODE_PRIVATE);
    ToDo.setTheme(this, sPref);
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
      sTask = savedInstanceState.getString(ToDoDB.KEY_NAME);
      sSuperTask = savedInstanceState.getString(ToDoDB.KEY_SUPERTASK);
    } else {
      sTask = null;
      sSuperTask = null;
    }

    if (sTask == null) {
      Bundle extras = getIntent().getExtras();
      if (extras != null) {
        sTask = extras.getString(ToDoDB.KEY_NAME);
        sSuperTask = extras.getString(ToDoDB.KEY_SUPERTASK);
      } else {
        sTask = null;
        sSuperTask = null;
      }
    }

    final String action = getIntent().getAction();
    final boolean creating = action.equals(Integer
        .toString(ToDo.ACTIVITY_CREATE_ENTRY));

    if (creating || action.equals(Integer.toString(ToDo.ACTIVITY_EDIT_ENTRY))) {
      final LinearLayout ll = (LinearLayout) findViewById(R.id.editLinearLayout);

      // now comes priority stuff

      mPriorityText = creating ? this.getString(R.string.priority_default)
          : this.getString(R.string.priority);
      mPrioritySb = new SeekBar(this);
      mPrioritySb.setVerticalScrollBarEnabled(true);
      mPrioritySb.setMax(getSharedPreferences(ToDo.PREFS_NAME,
          Context.MODE_PRIVATE).getInt(Config.PRIORITY_MAX, 100) + 1);
      int priority = creating ? mPrioritySb.getMax() / 2 : mDbHelper
          .getPriority(Edit.sTask);
      mPrioritySb.setPadding(0, 0, 0, 10);
      mPrioritySb.setProgress(priority);
      final Toast t = Toast.makeText(this, null, Toast.LENGTH_LONG);
      mPrioritySb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
          t.setText(mPriorityText + arg1);
        }

        public void onStartTrackingTouch(SeekBar arg0) {
          t.setGravity(Gravity.TOP, 0, mBodyText.getTop()
              + (int) (32 * dm.ydpi / 240));
          mPriorityText = Edit.this.getString(R.string.priority);
          t.show();
        }

        public void onStopTrackingTouch(SeekBar arg0) {
          t.show();
        }

      });
      if (!sPref.getBoolean(Config.PRIORITY_DISABLE, false)) {
        ll.addView(mPrioritySb);
      }

      mDateTimeLayout = new LinearLayout(this);
      mDateTimeLayout.setOrientation(LinearLayout.HORIZONTAL);

      // now comes due dates stuff
      if (creating) {
        sTime = new Time(-1, -1, -1);
        sDate = new Date(0);
      } else {
        sTime = new Time(mDbHelper.getDueTime(sTask),
            mDbHelper.getDueDayOfWeek(sTask));
        sDate = new Date(mDbHelper.getDueDate(sTask));
      }

      mDateTb = new ToggleButton(this);
      mDateTb.setTextOff(getString(R.string.edit_task_no_date));
      if (mDbHelper.isDueDateSet(sTask)) {
        mDateTb.setTextOn(getString(R.string.edit_task_date_set)
            + ' '
            + Chronos.decodeDate(mDbHelper.getDueDate(Edit.sTask),
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
            final OnDateSetListener odsl = new OnDateSetListener() {
              public void onDateSet(DatePicker view, int year, int month,
                  int dayOfMonth) {
                mDateTb.setTag(Boolean.valueOf(true));
                sDate = new Date(year, month, dayOfMonth);
                mMonthsString = view.getContext().getString(R.string.months);
                mDateTb.setText(view.getContext().getString(
                    R.string.edit_task_date_set)
                    + ' '
                    + mMonthsString.split(" ")[month]
                    + ' '
                    + dayOfMonth
                    + ", " + year);
                mDateTb.setTextOn(mDateTb.getText());
              }
            };
            GregorianCalendar gc;
            if (!creating && !sDate.isNull()) {
              gc = new GregorianCalendar(sDate.getYear(), sDate.getMonth(),
                  sDate.getDay());
            } else {
              gc = new GregorianCalendar();
            }
            final DatePickerDialog dpd = new DatePickerDialog(Edit.this, odsl,
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
            if (mTimeTb.isChecked()) {
              setWhenButton(true);
            }
          }
        }
      });
      mDateTimeLayout.addView(mDateTb);

      mTimeTb = new ToggleButton(this);
      mTimeTb.setTextOff(getString(R.string.edit_task_no_time));
      mDateTimeLayout.addView(mTimeTb);
      if (mDbHelper.isDueTimeSet(sTask)) {
        mTimeTb.setTextOn(getString(R.string.edit_task_time_set) + ' '
            + Chronos.decodeTime(sTime.getEncodedTime()));
        mTimeTb.setChecked(true);
        if (!mDateTb.isChecked()) {
          setWhenButton(true);
          mWhenButton.setChecked(sTime.isWeekly() || sDate.isMonthly());
        }
      } else {
        mTimeTb.setChecked(false);
      }
      mTimeTb.setTag(Boolean.valueOf(false));
      mTimeTb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton cb, boolean checked) {
          if (checked) {
            final Calendar c = Calendar.getInstance();
            final Dialog d = new TimePickerDialog(cb.getContext(),
                new OnTimeSetListener() {
                  public void onTimeSet(TimePicker view, int hour, int minute) {
                    mTimeTb.setTag(Boolean.valueOf(true));
                    mTimeTb.setText(view.getContext().getString(
                        R.string.edit_task_time_set)
                        + ' '
                        + (hour < 10 ? '0' : "")
                        + hour
                        + ':'
                        + (minute < 10 ? '0' : "") + minute);
                    mTimeTb.setTextOn(mTimeTb.getText());
                    sTime = new Time(hour, minute, sTime.getDayOfWeek());
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
            sTime = new Time(-1, -1, sTime.getDayOfWeek());
            setWhenButton(false);
          }
        }
      });

      ll.addView(mDateTimeLayout);

      if (!sPref.getBoolean(Config.AD_DISABLED, false)) {
        final AdView ad = new AdView(this);
        ad.setBackgroundColor(Color.BLACK);
        ad.setPrimaryTextColor(Color.WHITE);
        ad.setSecondaryTextColor(Color.DKGRAY);
        ad.setKeywords(getString(R.string.ad_keywords));
        ad.setRequestInterval(0);
        ll.addView(ad);
      }
    }

    mConfirmButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        final String newName = ToDoDB.sanitize(mBodyText.getText().toString());
        if (action.equals(Integer.toString(ToDo.TAG_CREATE_ID))) {
          if (!(mDbHelper.createTag(newName))) {
            showMessage(v.getContext().getString(R.string.tag_existent));
            return;
          } else {
            Edit.this.setResult(RESULT_OK,
                new Intent().putExtra(ToDoDB.KEY_NAME, newName));
          }
        } else if (action.equals(Integer.toString(ToDo.TAG_EDIT_ID))) {
          mDbHelper.updateTag(sTask, newName);
        } else if (action.equals(Integer.toString(ToDo.ACTIVITY_CREATE_ENTRY))) {
          final String result = mDbHelper.createTask(sTask, newName);
          if (sSuperTask != null && sSuperTask.length() > 0) {
            try {
              mDbHelper.setSuperTask(newName, sSuperTask);
            } catch (Exception e) {
              // can't have an exception here
            }
          }
          if (result != null) {
            showMessage(v.getContext().getString(R.string.entry_existent)
                + " '" + result + '\'');
            return;
          }
          updateTask(newName);
        } else if (action.equals(Integer.toString(ToDo.ACTIVITY_EDIT_ENTRY))) {
          mDbHelper.deleteAlarm(sTask);
          mDbHelper.updateTask(sTask, newName);
          updateTask(newName);
        } else if (action.equals(Integer.toString(ToDo.TASK_WRITTEN_ID))) {
          mDbHelper.setWrittenNote(sTask, newName);
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
   * Applies the actual edits to the task in the database. Also performs web
   * sync if necessary.
   * 
   * @param task
   */
  private final void updateTask(final String task) {
    mDbHelper.updateTask(task, sDate); // setting this anyway, might be monthly
    final boolean dateSet = mDateTb.isChecked();
    mDbHelper.setIsDueDate(task, dateSet);
    if (mTimeTb.isChecked()) {
      mDbHelper.updateTask(task, sTime);
      mDbHelper.setIsDueTime(task, true);
      setAlarm(task);
    } else {
      mDbHelper.setIsDueTime(task, false);
    }
    mDbHelper.setPriority(task, mPrioritySb.getProgress());
    if (dateSet) {
      syncToWeb(task);
    }
    if (getIntent().getExtras().getBoolean(EXTERNAL_INVOKER, false)) {
      TagToDoWidget.onUpdate(getApplicationContext(),
          AppWidgetManager.getInstance(getApplicationContext()));
    }
  }

  /**
   * Makes the When button appear or disappear. That button reveals some
   * settings about periodic alarms.
   * 
   * @visible
   */
  private final void setWhenButton(boolean visible) {
    if (visible) {
      mDateTimeLayout.addView(mWhenButton = new ToggleButton(this));
      mWhenButton.setTextOff(getString(R.string.daily));
      mWhenButton.setChecked(false);
      if (sTime.isWeekly()) {
        mWhenButton.setTextOn(getString(Chronos.DAYS[sTime.getDayOfWeek()]));
      } else if (sDate.isMonthly()) {
        mWhenButton.setTextOn(getString(R.string.monthly) + ", "
            + sDate.getDay());
      }
      mWhenButton.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          if (!((ToggleButton) v).isChecked()) {
            sTime = new Time(sTime.getEncodedTime(), -1);
            sDate = new Date(0);
            return;
          }
          final Calendar cal = Calendar.getInstance();
          final Dialog d = new Dialog(Edit.this);
          d.setTitle(getString(R.string.weekly) + ' ' + getString(R.string.or)
              + ' ' + getString(R.string.monthly));
          final LinearLayout ll = new LinearLayout(Edit.this);
          ll.setOrientation(LinearLayout.VERTICAL);
          final TextView tv = new TextView(Edit.this);
          tv.setGravity(Gravity.CENTER_HORIZONTAL);
          tv.setTextSize(18);

          final OnClickListener weeklyMinus = new OnClickListener() {
            public void onClick(View v) {
              sTime = new Time(sTime.getEncodedTime(), Utils.iterate(
                  sTime.getDayOfWeek(), 7, -1));
              tv.setText(Chronos.DAYS[sTime.getDayOfWeek()]);
            }
          };

          final OnClickListener weeklyPlus = new OnClickListener() {
            public void onClick(View v) {
              sTime = new Time(sTime.getEncodedTime(), Utils.iterate(
                  sTime.getDayOfWeek(), 7, 1));
              tv.setText(Chronos.DAYS[sTime.getDayOfWeek()]);
            }
          };

          final OnClickListener monthlyMinus = new OnClickListener() {
            public void onClick(View v) {
              sDate = new Date(0, 0, Utils.iterate(sDate.getDay(),
                  cal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1, -1));
              if (sDate.getDay() == 0) {
                sDate = new Date(0, 0, cal
                    .getActualMaximum(Calendar.DAY_OF_MONTH));
              }
              tv.setText(Integer.toString(sDate.getDay()));
            }
          };

          final OnClickListener monthlyPlus = new OnClickListener() {
            public void onClick(View v) {
              sDate = new Date(0, 0, Utils.iterate(sDate.getDay(),
                  cal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1, 1));
              if (sDate.getDay() == 0) {
                sDate = new Date(0, 0, 1);
              }
              tv.setText(Integer.toString(sDate.getDay()));
            }
          };

          final Button minus = new Button(Edit.this);
          minus.setText("<");
          final Button plus = new Button(Edit.this);
          plus.setText(">");

          final RadioGroup rg = new RadioGroup(Edit.this);
          final RadioButton weeklyRadio = new RadioButton(Edit.this);
          weeklyRadio.setText(R.string.weekly);
          weeklyRadio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton v, boolean isChecked) {
              if (!isChecked) {
                return;
              }
              if (!sTime.isWeekly()) {
                sTime = new Time(sTime.getEncodedTime(), Calendar.getInstance()
                    .get(Calendar.DAY_OF_WEEK) - 2);
                if (sTime.getDayOfWeek() < 0) {
                  sTime = new Time(sTime.getEncodedTime(),
                      sTime.getDayOfWeek() + 7);
                }
              }
              tv.setText(Chronos.DAYS[sTime.getDayOfWeek()]);
              minus.setOnClickListener(weeklyMinus);
              plus.setOnClickListener(weeklyPlus);
            }
          });
          final RadioButton monthlyRadio = new RadioButton(Edit.this);
          monthlyRadio.setText(R.string.monthly);
          monthlyRadio
              .setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton v, boolean isChecked) {
                  if (!isChecked) {
                    return;
                  }
                  if (sDate.isNull()) {
                    sDate = new Date(0, 0, cal.get(Calendar.DAY_OF_MONTH));
                  }
                  tv.setText(Integer.toString(sDate.getDay()));
                  minus.setOnClickListener(monthlyMinus);
                  plus.setOnClickListener(monthlyPlus);
                }
              });
          rg.addView(weeklyRadio);
          rg.addView(monthlyRadio);
          weeklyRadio.setChecked(sTime.isWeekly());
          monthlyRadio.setChecked(sDate.isMonthly());
          if (!(weeklyRadio.isChecked() || monthlyRadio.isChecked())) {
            weeklyRadio.setChecked(true);
          }
          ll.addView(rg);

          ll.addView(minus);
          ll.addView(tv);
          ll.addView(plus);

          final Button b = new Button(Edit.this);
          b.setMinimumWidth(150);
          b.setText(R.string.go_back);
          b.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
              if (weeklyRadio.isChecked()) {
                mWhenButton.setTextOn(getString(Chronos.DAYS[sTime
                    .getDayOfWeek()]));
                sDate = new Date(0);
              } else {
                mWhenButton.setTextOn(getString(R.string.monthly) + ", "
                    + sDate.getDay());
                sTime = new Time(sTime.getEncodedTime(), -1);
                // sDate = new Date(0, 0, dayOfMonth);
              }
              mWhenButton.setChecked(true);
              d.dismiss();
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
  private final void setAlarm(final String task) {
    final PendingIntent pi = PendingIntent.getBroadcast(this, task.hashCode(),
        Utils.getAlarmIntent(new Intent(this, AlarmReceiver.class), task), 0);
    final AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
    if (mDateTb.isChecked()) {// single occurence
      Chronos.setSingularAlarm(am, pi, sTime, sDate);
    } else {// daily or weekly or monthly
      Chronos.setRepeatingAlarm(am, pi, sTime, sDate);
    }
  }

  /**
   * Sets the general message of this edit screen, depending on the purpose and
   * also sets a default value if something is edited (the present value)
   */
  private void populateFields() {
    final String action = getIntent().getAction();
    if (sTask != null) {
      if (action.equals(Integer.toString(ToDo.TAG_EDIT_ID))) {
        mTaskText.setText(R.string.edit_tag);
        mBodyText.setText(sTask);
        mBodyText.setSelection(sTask.length(), sTask.length());
      } else if (action.equals(Integer.toString(ToDo.ACTIVITY_CREATE_ENTRY))) {
        mTaskText.setText(R.string.entry_create);
      } else if (action.equals(Integer.toString(ToDo.ACTIVITY_EDIT_ENTRY))) {
        mTaskText.setText(R.string.edit_entry);
        mBodyText.setText(sTask);
        mBodyText.setSelection(sTask.length(), sTask.length());
      } else if (action.equals(Integer.toString(ToDo.TASK_WRITTEN_ID))) {
        mTaskText.setText(R.string.edit_written_note);
        mBodyText.setText(mDbHelper.getWrittenNote(sTask));
      }
    } else {
      if (action.equals(Integer.toString(ToDo.TAG_CREATE_ID))) {
        mTaskText.setText(R.string.create_tag);
      }
    }
    if (ToDo.sTts != null) {
      new OneTimeTTS(this, mTaskText.getText().toString());
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_ROWID, sTask);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
    populateFields();
  }

  /**
   * Shows a dialog box with the given message
   * 
   * @param message
   */
  private void showMessage(String message) {
    final Dialog d = new Dialog(Edit.this);
    final Button b = new Button(Edit.this);
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
  private final void syncToWeb(final String name) {
    if (ToDo.SYNC_GCAL) {
      final SharedPreferences pref = getSharedPreferences(ToDo.PREFS_NAME,
          Context.MODE_PRIVATE);
      GoogleCalendar.setLogin(pref.getString(Config.GOOGLE_USERNAME, ""),
          pref.getString(Config.GOOGLE_PASSWORD, ""));
      try {
        GoogleCalendar.createEvent(name, sDate.getYear(), sDate.getMonth(),
            sDate.getDay(), sTime.getHour(), sTime.getMinute());
      } catch (Exception e) {
        // TODO Show error message somehow
      }
    }
  }
}
