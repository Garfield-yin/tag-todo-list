//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Apollo;
import com.android.todo.receivers.AlarmReceiver;
import com.android.todo.utils.Utils;

/**
 * This is an Alarm screen (of the Snooze/Dismiss type).
 */
public final class Alarm extends Activity {
  public final static String ALARM_PHASE = "alarmPhase";
  public final static int SNOOZE_TIME = 5; // 5 minutes
  private String mTask;
  private int mPhase;

  private static SharedPreferences sPref;
  private Button mSnoozeButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    wakeScreen();
    unlockScreen();
    if (savedInstanceState != null) {
      mTask = savedInstanceState.getString(ToDoDB.KEY_NAME);
      mPhase = savedInstanceState.getInt(Alarm.ALARM_PHASE);
    } else {
      mTask = null;
    }
    if (mTask == null) {
      final Bundle extras = getIntent().getExtras();
      if (extras != null) {
        mTask = extras.getString(ToDoDB.KEY_NAME);
        mPhase = extras.getInt(Alarm.ALARM_PHASE);
      } else {
        mTask = null;
      }
    }
    final LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);
    final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    ll.setLayoutParams(lp);

    final ImageView iv = new ImageView(this);
    iv.setImageResource(R.drawable.icon);
    ll.addView(iv);

    TextView tv = new TextView(this);
    tv.setText(mTask);
    tv.setPadding(0, 10, 0, 10);
    tv.setTextSize(20);
    tv.setGravity(Gravity.CENTER_HORIZONTAL);
    ll.addView(tv);

    final Button b = new Button(this);
    b.setText(R.string.alarm_dismiss);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Apollo.shutUp();
        if (mPhase < 2) {
          // removing subsequent internal alarm
          ((AlarmManager) Alarm.this.getSystemService(Context.ALARM_SERVICE))
              .cancel(PendingIntent.getBroadcast(Alarm.this, mTask.hashCode()
                  - 1 - mPhase, Utils.getAlarmIntent(new Intent(Alarm.this,
                  AlarmReceiver.class).putExtra(Alarm.ALARM_PHASE, mPhase + 1),
                  mTask), 0));
        }
        finish();
      }
    });
    ll.addView(b);
    mSnoozeButton = new Button(this);
    mSnoozeButton.setText(R.string.alarm_snooze);
    switch (mPhase) {
      case 0:
        lp.weight = 1;
        mSnoozeButton.setLayoutParams(lp);
        break;
      case 1:
        mSnoozeButton.setLayoutParams(new LayoutParams(
            LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        break;
      case 2:
        mSnoozeButton.setVisibility(View.GONE);
        break;
    }
    mSnoozeButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Apollo.shutUp();
        finish();
      }
    });
    ll.addView(mSnoozeButton);

    if (mPhase == 2) {
      tv = new TextView(this);
      tv.setText(R.string.alarm_last_message);
      tv.setLayoutParams(lp);
      tv.setGravity(Gravity.CENTER);
      ll.addView(tv);
    } else {
      new Handler().postDelayed(new Runnable() {
        public void run() {
          finish();
        }
      }, 120000);
    }

    setContentView(ll);

    if (sPref == null) {
      sPref = getSharedPreferences(TagToDoList.PREFS_NAME, Context.MODE_PRIVATE);
    }

    Alarm.soundAlarm(this, sPref,
        Uri.parse("android.resource://com.android.todo/" + R.raw.beep));
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
    final KeyguardManager km = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE));
    if (km.inKeyguardRestrictedInputMode()) {
      final KeyguardLock kl = km.newKeyguardLock(KEYGUARD_SERVICE);
      kl.disableKeyguard();
    }
  }

  /**
   * Makes the screen bright
   */
  private final void wakeScreen() {
    final PowerManager.WakeLock wl = ((PowerManager) getSystemService(Context.POWER_SERVICE))
        .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
            | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TagToDoWake");
    wl.acquire();
    new Handler().postDelayed(new Runnable() {
      public void run() {
        wl.release();
      }
    }, 20000);
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
