//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Apollo;

/**
 * This is an Alarm screen (of the Snooze/Dismiss type).
 */
public final class Alarm extends Activity {
  private static long SNOOZE_TIME = 300000; // 5 minutes
  private String mTask;
  private Handler mHandler;
  private Runnable mRunnable;
  private static SharedPreferences sPref;
  private Button mSnoozeButton;
  private int mSnoozeCount;
  private int mAlarmCount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSnoozeCount = 0;
    mAlarmCount = 0;
    if (savedInstanceState != null) {
      mTask = savedInstanceState.getString(ToDoDB.KEY_NAME);
    } else {
      mTask = null;
    }
    if (mTask == null) {
      final Bundle extras = getIntent().getExtras();
      if (extras != null) {
        mTask = extras.getString(ToDoDB.KEY_NAME);
      } else {
        mTask = null;
      }
    }
    final LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    ll.setLayoutParams(lp);

    final TextView tv = new TextView(this);
    tv.setText(mTask);
    tv.setPadding(0, 10, 0, 10);
    tv.setTextSize(20);
    tv.setGravity(Gravity.CENTER_HORIZONTAL);
    ll.addView(tv);

    final Button b = new Button(this);
    b.setText(R.string.alarm_dismiss);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (mHandler != null && mRunnable != null) {
          mHandler.removeCallbacks(mRunnable);
        }
        Apollo.shutUp();
        finish();
      }
    });
    ll.addView(b);
    mSnoozeButton = new Button(this);
    mSnoozeButton.setText(R.string.alarm_snooze);
    lp.weight = 1;
    mSnoozeButton.setLayoutParams(lp);
    mSnoozeButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Apollo.shutUp();
        snoozeMorph(++mSnoozeCount);
      }
    });
    ll.addView(mSnoozeButton);
    setContentView(ll);
    unlockScreen();

    mRunnable = new Runnable() {
      public void run() {
        unlockScreen();
        switch (++mAlarmCount) {
          case 1:
            if (sPref == null) {
              sPref = Alarm.this.getSharedPreferences(TagToDoList.PREFS_NAME,
                  Context.MODE_PRIVATE);
            }
            mHandler.postDelayed(this, SNOOZE_TIME);
            break;
          case 2:
            final TextView tv = new TextView(Alarm.this);
            tv.setText(R.string.alarm_last_message);
            tv.setLayoutParams(lp);
            tv.setGravity(Gravity.CENTER);
            ll.addView(tv);
            break;
        }

        snoozeMorph(mAlarmCount);

        Alarm.soundAlarm(Alarm.this, sPref,
            Uri.parse("android.resource://com.android.todo/" + R.raw.beep));
      }
    };
    (mHandler = new Handler()).postDelayed(mRunnable, SNOOZE_TIME);

  }

  /**
   * Gradually waking up the user by shrinking the Snooze button :)
   * 
   * @param times
   */
  private final void snoozeMorph(final int times) {
    switch (times) {
      case 1:
        mSnoozeButton.setLayoutParams(new LayoutParams(
            LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        break;
      case 2:
        mSnoozeButton.setVisibility(View.GONE);
        break;
    }
  }

  /**
   * Sounds the alarm. (Which one? Depends on user preferences)
   * 
   * @param c
   * @param settings
   * @param ringtone
   *          Uri of the default ring
   */
  public final static void soundAlarm(final Context c,
      final SharedPreferences settings, final Uri ringtone) {
    if (settings.getBoolean(Config.CUSTOM_ALARM, false)) {
      final String uriString = settings.getString(Config.ALARM_URI, null);
      if (uriString != null) {
        Apollo.play(c, Uri.parse(uriString),
            settings.getInt(Config.ALARM_DURATION, 20));
      } else {
        Apollo.play(28, c, ringtone);
      }
    } else {
      Apollo.play(28, c, ringtone);
    }
  }

  /**
   * Unlocks the screen
   */
  private final void unlockScreen() {
    ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE))
        .newKeyguardLock(KEYGUARD_SERVICE).disableKeyguard();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_NAME, mTask);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }
}
